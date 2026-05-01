package com.qhiot.survey.common.enums;

import lombok.Getter;

/**
 * 是否标识枚举
 */
@Getter
public enum YesNo {
    NO(0, "否"),
    YES(1, "是");

    private final int code;
    private final String description;

    YesNo(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public static YesNo fromCode(int code) {
        for (YesNo yesNo : values()) {
            if (yesNo.code == code) {
                return yesNo;
            }
        }
        throw new IllegalArgumentException("未知的标识: " + code);
    }
}