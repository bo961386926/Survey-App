package com.survey.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.survey.entity.Project;

import java.util.List;
import java.util.Map;

public interface ProjectService extends IService<Project> {
    List<Project> getProjectList();
    boolean createProject(Project project);
    boolean updateProject(Project project);
    boolean deleteProject(Long id);
}
