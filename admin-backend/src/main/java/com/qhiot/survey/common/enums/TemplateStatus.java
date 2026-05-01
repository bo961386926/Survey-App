package com.qhiot.survey.common.enums;

import lombok.Getter;

/**
 * 模板状态枚举
 */
@Getter
public enum TemplateStatus {
    DRAFT(0, "草稿"),
    PUBLISHED(1, "已发布"),
    DISABLED(2, "已停用");

    private final int code;
    private final String description;

    TemplateStatus(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public static TemplateStatus fromCode(int code) {
        for (TemplateStatus status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        throw new IllegalArgumentException("未知的模板状态: " + code);
    }
}