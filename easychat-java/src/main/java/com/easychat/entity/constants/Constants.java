package com.easychat.entity.constants;

public class Constants {

    public static final String REDIS_KEY_CHECK_CODE = "easychat:checkcode:";

    public static final String REDIS_KEY_WS_USER_HEART_BEAT = "easychat:ws:user:heartbeat:";

    public static final String REDIS_KEY_TOKEN = "easychat:ws:token:";

    public static final String REDIS_KEY_TOKEN_USERID = "easychat:ws:token:userid:";

    public static final Integer REDIS_TIME_1MIN = 60;

    public static final Integer REDIS_KEY_EXPIRES_DAY = REDIS_TIME_1MIN * 60 * 24;

    public static final Integer LENGTH_11 = 11;

    public static final Integer LENGTH_20 = 20;
}
