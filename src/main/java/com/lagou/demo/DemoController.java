package com.lagou.demo;

import com.lagou.edu.mvcframework.annotations.LagouAutowire;
import com.lagou.edu.mvcframework.annotations.LagouController;
import com.lagou.edu.mvcframework.annotations.LagouRequestMapping;
import com.lagou.service.IDemoService;
import org.springframework.ui.Model;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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

    @LagouRequestMapping("/handle01")
    public ModelAndView handle01(HttpServletRequest request, HttpServletResponse response, Model model) {
        Date date = new Date();
        //返回服务器时间到前端页面
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("date",date);
        modelAndView.setViewName("success");
        return modelAndView;
    }

}
