package com.chatapp.entity.enums;

public enum MediaFileTypeEnum {
    IMAGE(0, "图片"),
    VIDEO(1, "视频"),
    FILE(2, "文件");

    private final Integer type;
    private final String desc;

    MediaFileTypeEnum(Integer type, String desc) {
        this.type = type;
        this.desc = desc;
    }

    public Integer getType() {
        return type;
    }

    public String getDesc() {
        return desc;
    }

    public static MediaFileTypeEnum getByType(Integer type) {
        for (MediaFileTypeEnum value : values()) {
            if (value.type.equals(type)) {
                return value;
            }
        }
        return null;
    }
}
