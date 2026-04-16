package com.survey.service.impl;

import com.survey.entity.Project;
import com.survey.mapper.ProjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProjectServiceImplTest {

    @Mock
    private ProjectMapper projectMapper;

    @InjectMocks
    private ProjectServiceImpl projectService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getProjectList() {
        // Given
        Project project1 = new Project();
        project1.setId(1L);
        project1.setProjectName("测试项目1");

        Project project2 = new Project();
        project2.setId(2L);
        project2.setProjectName("测试项目2");

        List<Project> mockProjects = Arrays.asList(project1, project2);
        when(projectMapper.selectList(null)).thenReturn(mockProjects);

        // When
        List<Project> result = projectService.getProjectList();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("测试项目1", result.get(0).getProjectName());
        verify(projectMapper).selectList(null);
    }

    @Test
    void createProject() {
        // Given
        Project project = new Project();
        project.setProjectName("新建项目");
        when(projectMapper.insert(project)).thenReturn(1);

        // When
        boolean result = projectService.createProject(project);

        // Then
        assertTrue(result);
        verify(projectMapper).insert(project);
    }

    @Test
    void updateProject() {
        // Given
        Project project = new Project();
        project.setId(1L);
        project.setProjectName("更新项目");
        when(projectMapper.updateById(project)).thenReturn(1);

        // When
        boolean result = projectService.updateProject(project);

        // Then
        assertTrue(result);
        verify(projectMapper).updateById(project);
    }

    @Test
    void deleteProject() {
        // Given
        Long projectId = 1L;
        when(projectMapper.deleteById(projectId)).thenReturn(1);

        // When
        boolean result = projectService.deleteProject(projectId);

        // Then
        assertTrue(result);
        verify(projectMapper).deleteById(projectId);
    }
}
