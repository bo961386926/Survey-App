package com.qhiot.survey.common.util;

import com.qhiot.survey.common.constant.Permissions;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class PermissionRegistryTest {

    @Test
    void wildcardExpandsToDeclaredPermissions() {
        PermissionRegistry registry = new PermissionRegistry();
        registry.logSummary();

        Set<String> expanded = PermissionRegistry.expandWildcard(Set.of(PermissionRegistry.WILDCARD));

        assertThat(expanded)
                .contains(
                        Permissions.PROJECT_VIEW,
                        Permissions.PROJECT_EDIT,
                        Permissions.POINT_VIEW,
                        Permissions.POINT_EDIT,
                        Permissions.TEMPLATE_VIEW,
                        Permissions.TEMPLATE_EDIT,
                        Permissions.TEMPLATE_BIND,
                        Permissions.SURVEY_CREATE,
                        Permissions.SURVEY_EDIT,
                        Permissions.SURVEY_SUBMIT,
                        Permissions.TASK_VIEW,
                        Permissions.TASK_EDIT,
                        Permissions.TASK_ASSIGN,
                        Permissions.AUDIT_VIEW,
                        Permissions.AUDIT_PASS,
                        Permissions.AUDIT_REJECT,
                        Permissions.SYSTEM_USER,
                        Permissions.SYSTEM_ROLE,
                        Permissions.SYSTEM_DICT,
                        Permissions.SYSTEM_LOG,
                        Permissions.MESSAGE_PUSH
                )
                .doesNotContain(PermissionRegistry.WILDCARD);
    }

    @Test
    void legacyPermissionsAreExpandedToCurrentDomainPermissions() {
        Set<String> expanded = PermissionRegistry.expandWildcard(Set.of(
                "user:list",
                "audit:list",
                "task:assign",
                "project:update",
                "result:list",
                "ROLE_ADMIN"
        ));

        assertThat(expanded)
                .contains(
                        "user:list",
                        "ROLE_ADMIN",
                        Permissions.SYSTEM_USER,
                        Permissions.AUDIT_VIEW,
                        Permissions.TASK_ASSIGN,
                        Permissions.PROJECT_EDIT,
                        Permissions.SURVEY_EDIT
                );
    }
}
