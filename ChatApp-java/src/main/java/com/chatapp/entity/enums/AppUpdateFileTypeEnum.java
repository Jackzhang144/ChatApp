package com.chatapp.entity.enums;

public enum AppUpdateFileTypeEnum {
    LOCAL(0, "本地"), OUTER_LINK(1, "外链");

    private final Integer type;
    private final String description;

    AppUpdateFileTypeEnum(int type, String description) {
        this.type = type;
        this.description = description;
    }

    public Integer getType() {
        return type;
    }

    public String getDescription() {
        return description;
    }

    public static AppUpdateFileTypeEnum getByType(Integer type) {
        for (AppUpdateFileTypeEnum item : values()) {
            if (item.type.equals(type)) {
                return item;
            }
        }
        return null;
    }
}
