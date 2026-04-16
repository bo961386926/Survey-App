package com.survey.controller;

import com.survey.common.Result;
import com.survey.entity.Project;
import com.survey.service.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/project")
@CrossOrigin(origins = "*")
public class ProjectController {

    @Autowired
    private ProjectService projectService;

    @GetMapping("/list")
    public Result<List<Project>> getProjectList() {
        List<Project> projects = projectService.getProjectList();
        return Result.success(projects);
    }

    @PostMapping("/create")
    public Result<Boolean> createProject(@RequestBody Project project) {
        boolean success = projectService.createProject(project);
        return success ? Result.success(true) : Result.error("创建失败");
    }

    @PutMapping("/update")
    public Result<Boolean> updateProject(@RequestBody Project project) {
        boolean success = projectService.updateProject(project);
        return success ? Result.success(true) : Result.error("更新失败");
    }

    @DeleteMapping("/delete/{id}")
    public Result<Boolean> deleteProject(@PathVariable Long id) {
        boolean success = projectService.deleteProject(id);
        return success ? Result.success(true) : Result.error("删除失败");
    }
}
