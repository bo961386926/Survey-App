package com.qhiot.survey.service.impl;

import com.qhiot.survey.entity.Project;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ProjectService 简单单元测试
 * 测试实体类的基本功能
 */
class ProjectServiceImplTest {

    @Test
    void testProjectEntity() {
        // Given
        Project project = new Project();
        project.setId(1L);
        project.setProjectName("测试项目");
        project.setProjectCode("PRJ001");

        // When & Then
        assertNotNull(project);
        assertEquals(1L, project.getId());
        assertEquals("测试项目", project.getProjectName());
        assertEquals("PRJ001", project.getProjectCode());
    }

    @Test
    void testProjectCopy() {
        // Given
        Project original = new Project();
        original.setId(1L);
        original.setProjectName("原始项目");

        // When
        Project copy = new Project();
        copy.setProjectName(original.getProjectName());

        // Then
        assertEquals(original.getProjectName(), copy.getProjectName());
    }
}