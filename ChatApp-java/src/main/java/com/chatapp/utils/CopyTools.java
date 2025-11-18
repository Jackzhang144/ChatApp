package com.chatapp.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class CopyTools {
    public static <T, S> List<T> copyList(List<S> sList, Class<T> classz) {
        List<T> list = new ArrayList<>();
        for (S s : sList) {
            T t = newInstance(classz);
            BeanUtils.copyProperties(s, t);
            list.add(t);
        }
        return list;
    }

    public static <T, S> T copy(S s, Class<T> classz) {
        T t = newInstance(classz);
        BeanUtils.copyProperties(s, t);
        return t;
    }

    private static <T> T newInstance(Class<T> classz) {
        try {
            return classz.getDeclaredConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            log.error("实例化 {} 失败", classz.getName(), e);
            throw new IllegalStateException("实例化对象失败", e);
        }
    }
}
