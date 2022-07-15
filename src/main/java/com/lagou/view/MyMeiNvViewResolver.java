package com.lagou.view;

import org.springframework.core.Ordered;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.ViewResolver;

import java.util.Locale;

/**
 * @author hezifeng
 * @create 2022/7/15 10:20
 */
public class MyMeiNvViewResolver implements ViewResolver, Ordered {
    private Integer order;
    @Override
    public int getOrder() {
        return this.order;
    }

    public void setOrder(Integer i) {
        this.order=i;
    }

    @Override
    public View resolveViewName(String viewName, Locale locale) throws Exception {
        if (viewName.startsWith("meinv:")) {
            return new MyView();
        }
        return null;
    }
}
