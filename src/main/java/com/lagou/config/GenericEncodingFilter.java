package com.lagou.config;

import com.lagou.request.MyRequest;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author hezifeng
 * @create 2022/7/15 9:18
 */
public class GenericEncodingFilter implements Filter {
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletResponse myResponse = (HttpServletResponse) response;
        myResponse.setContentType("text/html;charset=UTF-8");

        //转换为协议相关对象
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        MyRequest myRequest = new MyRequest(httpServletRequest);
        chain.doFilter(myRequest, myResponse);
    }

    @Override
    public void destroy() {

    }
}
