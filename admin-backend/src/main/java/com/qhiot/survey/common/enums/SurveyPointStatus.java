package com.qhiot.survey.common.enums;

import lombok.Getter;

/**
 * 点位状态枚举
 */
@Getter
public enum SurveyPointStatus {
    PENDING(0, "待采集"),
    DRAFT(1, "草稿中"),
    PENDING_AUDIT(2, "待审核"),
    AUDIT_PASSED(3, "审核通过"),
    REJECTED(4, "驳回待修改"),
    ARCHIVED(5, "已归档"),
    INVALIDATED(6, "作废");

    private final int code;
    private final String description;

    SurveyPointStatus(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public static SurveyPointStatus fromCode(int code) {
        for (SurveyPointStatus status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        throw new IllegalArgumentException("未知的点位状态: " + code);
    }
}