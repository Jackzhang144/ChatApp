package com.chatapp.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface GlobalInterceptor {
    // 默认需要校验登录
    boolean checkLogin() default true;

    // 默认不为超级管理员
    boolean checkAdmin() default false;
}
