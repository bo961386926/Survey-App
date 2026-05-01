package com.qhiot.survey.common.enums;

import lombok.Getter;

/**
 * 审核状态枚举
 */
@Getter
public enum AuditStatus {
    PENDING(0, "待审"),
    PASSED(1, "通过"),
    REJECTED(2, "驳回");

    private final int code;
    private final String description;

    AuditStatus(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public static AuditStatus fromCode(int code) {
        for (AuditStatus status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        throw new IllegalArgumentException("未知的审核状态: " + code);
    }
}