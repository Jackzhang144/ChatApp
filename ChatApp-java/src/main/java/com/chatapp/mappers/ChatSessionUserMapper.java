package com.chatapp.mappers;

import org.apache.ibatis.annotations.Param;

public interface ChatSessionUserMapper<T, P> extends BaseMapper<T, P> {

    Integer updateByUserIdAndContactId(@Param("bean") T t, @Param("userId") String userId, @Param("contactId") String contactId);

    Integer deleteByUserIdAndContactId(@Param("userId") String userId, @Param("contactId") String contactId);

    T selectByUserIdAndContactId(@Param("userId") String userId, @Param("contactId") String contactId);
}
