package com.lagou.view;

import org.springframework.web.servlet.View;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * @author hezifeng
 * @create 2022/7/15 10:22
 */
public class MyView  implements View {
    @Override
    public String getContentType() {
        return "text/html";
    }

    @Override
    public void render(Map<String, ?> model, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws Exception {
        System.out.println("之前保存的数据:" + model);
        httpServletResponse.setContentType("text/html");
        httpServletResponse.getWriter().write("<h1> Hello world </h1>");
    }
}
