package com.lagou.service.impl;

import com.lagou.edu.mvcframework.annotations.LagouService;
import com.lagou.service.IDemoService;

@LagouService
public class DemoServiceImpl implements IDemoService {
    @Override
    public String get(String name) {
        System.out.println("service实现类中的name参数:" + name);
        return name;
    }
}
