package com.lagou.edu.mvcframework.servlet;

import com.lagou.edu.mvcframework.annotations.*;
import com.lagou.edu.mvcframework.pojo.Handler;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LgDispatcherServlet extends HttpServlet {
    private Properties properties = new Properties();
    private List<String> classNames = new ArrayList<>();
    //IOC容器
    private Map<String, Object> ioc = new HashMap<>();
    // handlerMapping
//    private Map<String, Method> handlerMapping = new HashMap<>(); //存储url和method之间的映射关系

    private List<Handler> handlerMapping = new ArrayList<>();


    @Override
    public void init(ServletConfig config) throws ServletException {
        //加载配置文件 springmvc.properties
        String contextConfigLocation = config.getInitParameter("contextConfigLocation");
        doLoadConfig(contextConfigLocation);
        //扫描相关类,扫描注解
        doScan(properties.getProperty("scanPackage"));
        //Bean初始化(实现IOC容器)
        doInstance();
        //维护对象之间的依赖关系，实现依赖注入
        doAutowire();
        //构造一个handlerMapping 处理器映射器，将配置好的url和method建立映射关系
        intHandlerMapping();

        System.out.println("lagou mvc 初始化完成");
        //等待请求

        super.init(config);
    }

    //构造映射器
    //目的 url 和controller建立关系
    private void intHandlerMapping() {
        if (ioc.isEmpty()) return;
        for (Map.Entry<String, Object> entry : ioc.entrySet()) {
            // 获取IOC当前遍历的class类型
            Class<?> aClass = entry.getValue().getClass();
            if (!aClass.isAnnotationPresent(LagouController.class)) continue;
            if (!aClass.isAnnotationPresent(LagouRequestMapping.class)) continue;
            String baseUrl = "";
            LagouRequestMapping annotation = aClass.getAnnotation(LagouRequestMapping.class);
            baseUrl = annotation.value(); // /demo

            //获取方法
            Method[] methods = aClass.getMethods();
            for (int i = 0; i < methods.length; i++) {
                Method method = methods[i];
                //方法上没有表示requestMapping就不处理
                if (!method.isAnnotationPresent(LagouRequestMapping.class)) continue;
                LagouRequestMapping methodAnnotation = method.getAnnotation(LagouRequestMapping.class);
                String methodUrl = methodAnnotation.value();
                String url = baseUrl + methodUrl; // /demo/query
                //把method所有信息及url封装成handler
                Handler handler = new Handler(entry.getValue(), method, Pattern.compile(url));

                //处理计算参数位置
                Parameter[] parameters = method.getParameters();
                for (int j = 0; j < parameters.length; j++) {
                    Parameter parameter = parameters[j];
                    if (parameter.getType() == HttpServletRequest.class || parameter.getType() == HttpServletResponse.class) {
                        //如果是request 或 response对象 参数名称存HttpServletRequest和 HttpServletResponse
                        handler.getParamIndexMapping().put(parameter.getType().getSimpleName(), j); //<HttpServletRequest,1>
                    } else {
                        handler.getParamIndexMapping().put(parameter.getName(), j); //<name, 2>
                    }
                }
                handlerMapping.add(handler);
                //建立url和method之间的映射关系
//                handlerMapping.put(url, method);
            }

        }
    }

    //实现依赖注入
    private void doAutowire() {
        if(ioc.isEmpty()) return;
        //存在bean,进行依赖注入
        try {
            //遍历IOC中所有字段，查看对象中的字段是否存在LagouAutowire注解，如果有需要维护依赖注入
            for (Map.Entry<String, Object> entry : ioc.entrySet()) {
                // 获取bean对象中的字段信息
                Field[] fields = entry.getValue().getClass().getDeclaredFields();
                for (int i = 0; i < fields.length; i++) {
                    Field field = fields[i]; //demoService
                    if (!field.isAnnotationPresent(LagouAutowire.class)) {
                        continue;
                    }
                    //有该注解
                    LagouAutowire annotation = field.getAnnotation(LagouAutowire.class);
                    String beanName = annotation.value(); //需要注入的bean的ID
                    if ("".equals(beanName)) {
                        //没有配置具体的beanId,需要根据当前的字段类型注入 （接口注入 IDemoService
                        beanName = field.getType().getName();
                    }
                    //开启赋值
                    field.setAccessible(true);//强制访问
                    field.set(entry.getValue(), ioc.get(beanName));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    //初始化IOC容器
    //基于classNames缓存的类的全限定类名以及反射技术，完成对象的创建和管理
    private void doInstance() {
        if (classNames.size() == 0) return;
        try {
            for (int i = 0; i < classNames.size(); i++) {
                String className = classNames.get(i);
                //反射创建对象
                Class<?> aClass = Class.forName(className);
                //区分controller,区分service 是否指定beanName
                if (aClass.isAnnotationPresent(LagouController.class)) {
                    // controller 用类的首字母小写保存到IOC容器
                    String simpleName = aClass.getSimpleName(); //DemoController
                    String beanName = lowerFirst(simpleName); //demoController
                    //实例化并注入IOC容器
                    Object obj = aClass.newInstance();
                    ioc.put(beanName, obj);
                } else if (aClass.isAnnotationPresent(LagouService.class)) {
                    LagouService annotation = aClass.getAnnotation(LagouService.class);
                    //获取注解value值
                    String beanName = annotation.value();
                    if (!"".equals(beanName.trim())) {
                        //指定bean id
                        ioc.put(beanName, aClass.newInstance());
                    } else {
                        beanName = lowerFirst(aClass.getSimpleName());
                        ioc.put(beanName, aClass.newInstance());
                    }
                    // service层往往存在接口，面向接口开发，放入一份到IOC容器中，便于后期根据接口注入
                    Class<?>[] interfaces = aClass.getInterfaces();
                    for (int j = 0; j < interfaces.length; j++) {
                        Class<?> anInterface = interfaces[j];
                        //以接口的全限定类名为id进行放入
                        ioc.put(anInterface.getName(),aClass.newInstance());
                    }
                } else continue;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    //扫描类 获取磁盘上的文件夹  com/lagou/demo
    private void doScan(String scanPackage) {
        String scanPackagePath = Thread.currentThread().getContextClassLoader().getResource("").getPath() + scanPackage.replaceAll("\\.", "/");
        File pack = new File(scanPackagePath);
        File[] files = pack.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                //子package
                doScan(scanPackage + "." + file.getName()); // com.lagou.demo.controller
            } else if (file.getName().endsWith(".class")) {
                String className = scanPackage + "." + file.getName().replaceAll(".class", "");// 全限定类名
                classNames.add(className);//缓存所有类名
            }
        }
    }

    //加载配置文件
    private void doLoadConfig(String contextConfigLocation) {
        try {
            InputStream resourceAsStream = this.getClass().getClassLoader().getResourceAsStream(contextConfigLocation);
            properties.load(resourceAsStream);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //接收处理请求
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        //处理请求:根据url，找到对应的method，进行处理
//        String requestURI = req.getRequestURI();
//        Method method = handlerMapping.get(requestURI);//获取反射方法
        //反射调用,需要传入参数
//        method.invoke()
        //根据url获取当前请求的handler
        Handler handler = getHandler(req);
        if (handler == null) {
            resp.getWriter().write("404 not found!");
            return;
        }
        String validateParam = null;
        List<String> allowValue = null;
        if (handler.getMethod().isAnnotationPresent(LagouSecurity.class)) {
            LagouSecurity annotation = handler.getMethod().getAnnotation(LagouSecurity.class);
            validateParam = annotation.param() != null ? annotation.param() : null;
            allowValue = annotation.value() != null ? Arrays.asList(annotation.value().split(",")) : new ArrayList<>();
        }

        //参数绑定
        //获取所有参数类型数组,这个数组的长度就是我们最后要传入的args的长度
        Class<?>[] parameterTypes = handler.getMethod().getParameterTypes();
        //根据上述数组长度创建新的数组,要传入反射调用
        Object[] paramValues = new Object[parameterTypes.length];
        // 以下向参数数组中传值，还要包状参数的顺序和方法中的形参顺序一致
        Map<String, String[]> parameterMap = req.getParameterMap();

        //进行参数校验
        if (validateParam != null) {
            if (!parameterMap.containsKey(validateParam)) {
                resp.getWriter().write("身份校验失败!");
                return;
            }
            String value = StringUtils.join(parameterMap.get(validateParam), ",");
            if (!allowValue.contains(value)) {
                resp.getWriter().write("身份校验失败!");
                return;
            }
        }
        //遍历request中所有参数，填充入args中
        for (Map.Entry<String, String[]> param : parameterMap.entrySet()) {
            // name=1&name=2 name [1,2]
            String value = StringUtils.join(param.getValue(), ","); // 如同1,2
            //如果参数和方法中的参数匹配上了，填充参数
            if (!handler.getParamIndexMapping().containsKey(param.getKey())) continue;

            // 与形参列表中参数对应，对应索引位置放入paramValues
            Integer index = handler.getParamIndexMapping().get(param.getKey());//name index = 2
            paramValues[index] = value;
        }




        // 单独传req, resp
        int requestIndex = handler.getParamIndexMapping().get(HttpServletRequest.class.getSimpleName());
        paramValues[requestIndex] = req;
        int responseIndex = handler.getParamIndexMapping().get(HttpServletResponse.class.getSimpleName());
        paramValues[responseIndex] = resp;
        //最终调用handler的method属性
        try {
            handler.getMethod().invoke(handler.getController(), paramValues);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        //最终调用handlerMethod
    }

    private Handler getHandler(HttpServletRequest req) {
        if (handlerMapping.isEmpty()) return null;
        String uri = req.getRequestURI();
        for (Handler handler : handlerMapping) {
            Matcher matcher = handler.getPattern().matcher(uri);
            if (!matcher.find()) continue;
            return handler;
        }
        return null;
    }

    //首字母小写
    private String lowerFirst(String str) {
        char[] chars = str.toCharArray();
        if ('A' <=  chars[0] && chars[0] <= 'Z') {
            chars[0] += 32;
        }
        return String.valueOf(chars);
    }


    @Test
    public void test() throws IOException {
        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("springmvc.properties");
        properties.load(inputStream);
    }
}
