package com.qhiot.survey.common.enums;

import lombok.Getter;

/**
 * 结果状态枚举
 */
@Getter
public enum ResultStatus {
    DRAFT(0, "草稿"),
    SUBMITTED(1, "已提交"),
    PENDING_AUDIT(2, "待审核"),
    PASSED(3, "已通过"),
    REJECTED(4, "已驳回"),
    ARCHIVED(5, "已归档");

    private final int code;
    private final String description;

    ResultStatus(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public static ResultStatus fromCode(int code) {
        for (ResultStatus status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        throw new IllegalArgumentException("未知的结果状态: " + code);
    }
}