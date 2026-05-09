package com.qhiot.survey.common.constant;

/**
 * 权限码常量定义
 *
 * 命名规范: MODULE_PERMISSION = "module:permission"
 * 所有使用 @PreAuthorize("hasAuthority('xxx')") 或 v-permission="'xxx'" 的权限码必须在此声明
 */
public final class Permissions {

    private Permissions() {}

    // ============================
    // 项目管理 (project)
    // ============================
    /** 查看项目 */
    public static final String PROJECT_VIEW = "project:view";
    /** 编辑项目 */
    public static final String PROJECT_EDIT = "project:edit";
    /** 模板绑定 */
    public static final String TEMPLATE_BIND = "template:bind";

    // ============================
    // 点位管理 (point)
    // ============================
    /** 查看点位 */
    public static final String POINT_VIEW = "point:view";
    /** 编辑点位 */
    public static final String POINT_EDIT = "point:edit";

    // ============================
    // 勘查管理 (survey)
    // ============================
    /** 创建勘查 */
    public static final String SURVEY_CREATE = "survey:create";
    /** 编辑勘查 */
    public static final String SURVEY_EDIT = "survey:edit";
    /** 提交勘查 */
    public static final String SURVEY_SUBMIT = "survey:submit";
    /** 协作勘查 */
    public static final String SURVEY_ASSIST = "survey:assist";

    // ============================
    // 审核管理 (audit)
    // ============================
    /** 查看审核 */
    public static final String AUDIT_VIEW = "audit:view";
    /** 审核通过 */
    public static final String AUDIT_PASS = "audit:pass";
    /** 审核驳回 */
    public static final String AUDIT_REJECT = "audit:reject";

    // ============================
    // 系统管理 (system)
    // ============================
    /** 系统操作日志 */
    public static final String SYSTEM_LOG = "system:log";

    // ============================
    // 导出 (export)
    // ============================
    /** 导出项目数据 */
    public static final String EXPORT_PROJECT = "export:project";
    /** 导出审核数据 */
    public static final String EXPORT_AUDIT = "export:audit";

    /**
     * 获取所有权限码
     */
    public static String[] getAll() {
        return new String[]{
                PROJECT_VIEW, PROJECT_EDIT, TEMPLATE_BIND,
                POINT_VIEW, POINT_EDIT,
                SURVEY_CREATE, SURVEY_EDIT, SURVEY_SUBMIT, SURVEY_ASSIST,
                AUDIT_VIEW, AUDIT_PASS, AUDIT_REJECT,
                SYSTEM_LOG,
                EXPORT_PROJECT, EXPORT_AUDIT
        };
    }
}
