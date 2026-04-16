package com.survey.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.survey.entity.Project;
import com.survey.mapper.ProjectMapper;
import com.survey.service.ProjectService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProjectServiceImpl extends ServiceImpl<ProjectMapper, Project> implements ProjectService {

    @Override
    public List<Project> getProjectList() {
        return list();
    }

    @Override
    @Transactional
    public boolean createProject(Project project) {
        return save(project);
    }

    @Override
    @Transactional
    public boolean updateProject(Project project) {
        return updateById(project);
    }

    @Override
    @Transactional
    public boolean deleteProject(Long id) {
        return removeById(id);
    }
}
