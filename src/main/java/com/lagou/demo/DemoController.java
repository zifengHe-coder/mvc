package com.lagou.demo;

import com.lagou.edu.mvcframework.annotations.LagouAutowire;
import com.lagou.edu.mvcframework.annotations.LagouController;
import com.lagou.edu.mvcframework.annotations.LagouRequestMapping;
import com.lagou.edu.mvcframework.annotations.LagouSecurity;
import com.lagou.service.IDemoService;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;

@LagouController
@LagouRequestMapping("/demo")
public class DemoController {

    @LagouAutowire
    private IDemoService demoService;

    /**
     *url: /demo/query
     * @param request
     * @param response
     * @param name
     * @return
     */
    @LagouRequestMapping("/query")
    public String query(HttpServletRequest request, HttpServletResponse response, String name) {
        return demoService.get(name);
    }

    //url://http://localhost:8080/demo/handle01
    @LagouRequestMapping("/handle01")
    @LagouSecurity(param = "username", value = "张三")
    public void handle01(HttpServletRequest request, HttpServletResponse response, String username) throws IOException {
        response.getWriter().write("请求成功!请求用户"+username);
    }

    @LagouRequestMapping("/handle02")
    @LagouSecurity(param = "username", value = "李四,王五")
    public void handle02(HttpServletRequest request, HttpServletResponse response, String username) throws IOException {
        response.getWriter().write("请求成功!请求用户"+username);
    }

}
