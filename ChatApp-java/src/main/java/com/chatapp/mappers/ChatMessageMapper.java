package com.chatapp.mappers;

import org.apache.ibatis.annotations.Param;

public interface ChatMessageMapper<T, P> extends BaseMapper<T, P> {

    Integer updateByMessageId(@Param("bean") T t, @Param("messageId") Long messageId);

    Integer deleteByMessageId(@Param("messageId") Long messageId);

    T selectByMessageId(@Param("messageId") Long messageId);
}
