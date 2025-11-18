package com.chatapp.entity.enums;

public enum AppUpdateSatusEnum {
    INIT(0, "未发布"),
    GRAYSCALE(1, "灰度发布"),
    ALL(2, "全网发布");

    private final Integer status;
    private final String description;

    AppUpdateSatusEnum(int status, String description) {
        this.status = status;
        this.description = description;
    }

    public Integer getStatus() {
        return status;
    }

    public String getDescription() {
        return description;
    }

    public static AppUpdateSatusEnum getByStatus(Integer status) {
        for (AppUpdateSatusEnum item : values()) {
            if (item.status.equals(status)) {
                return item;
            }
        }
        return null;
    }
}
