package com.chatapp.mappers;

import org.apache.ibatis.annotations.Param;

public interface AppUpdateMapper<T, P> extends BaseMapper<T, P> {

    Integer updateById(@Param("bean") T t, @Param("id") Integer id);

    Integer deleteById(@Param("id") Integer id);

    T selectById(@Param("id") Integer id);

    Integer updateByVersion(@Param("bean") T t, @Param("version") String version);

    Integer deleteByVersion(@Param("version") String version);

    T selectByVersion(@Param("version") String version);

    T selectLatestUpdate(@Param("version") String version, @Param("uid") String uid);
}
