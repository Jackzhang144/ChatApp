package com.chatapp.mappers;

import org.apache.ibatis.annotations.Param;

public interface ChatSessionMapper<T, P> extends BaseMapper<T, P> {

    Integer updateBySessionId(@Param("bean") T t, @Param("sessionId") String sessionId);

    Integer deleteBySessionId(@Param("sessionId") String sessionId);

    T selectBySessionId(@Param("sessionId") String sessionId);
}
