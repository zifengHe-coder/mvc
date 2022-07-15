package com.lagou.edu.mvcframework.annotations;

import java.lang.annotation.*;

/**
 * @author hezifeng
 * @create 2022/7/15 10:47
 */
@Documented
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface LagouSecurity {
    //校验属性放行值，用逗号分隔 如 张三,李四
    String value() default "";
    String param() default "";
}
