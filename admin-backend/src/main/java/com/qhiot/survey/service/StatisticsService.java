
package com.qhiot.survey.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class StatisticsService {

    @Autowired
    private SysUserService sysUserService;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private SurveyPointService surveyPointService;

    @Autowired
    private SurveyResultService surveyResultService;

    @Autowired
    private com.qhiot.survey.mapper.SysUserRoleMapper sysUserRoleMapper;

    /**
     * 获取系统概览统计数据
     */
    public Map<String, Object> getSystemOverview() {
        Map<String, Object> overview = new HashMap<>();

        // 用户统计
        long totalUsers = sysUserService.count();
        long activeUsers = sysUserService.lambdaQuery().eq(com.qhiot.survey.entity.SysUser::getStatus, 1).count();

        // 统计勘察人员数量（角色ID为4）
        long surveyors = sysUserRoleMapper.selectCount(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<com.qhiot.survey.entity.SysUserRole>()
                        .eq(com.qhiot.survey.entity.SysUserRole::getRoleId, 4)
        );

        // 统计审核人员数量（角色ID为3）
        long reviewers = sysUserRoleMapper.selectCount(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<com.qhiot.survey.entity.SysUserRole>()
                        .eq(com.qhiot.survey.entity.SysUserRole::getRoleId, 3)
        );

        // 项目统计
        long totalProjects = projectService.count();
        long activeProjects = projectService.lambdaQuery().eq(com.qhiot.survey.entity.Project::getStatus, 1).count();

        // 点位统计
        long totalPoints = surveyPointService.count();
        long surveyedPoints = surveyPointService.lambdaQuery().ne(com.qhiot.survey.entity.SurveyPoint::getStatus, 0).count();
        long pendingAudit = surveyResultService.lambdaQuery().eq(com.qhiot.survey.entity.SurveyResult::getAuditStatus, 0).count();
        long approvedResults = surveyResultService.lambdaQuery().eq(com.qhiot.survey.entity.SurveyResult::getAuditStatus, 1).count();

        overview.put("userStats", Map.of(
                "total", totalUsers,
                "active", activeUsers,
                "surveyors", surveyors,
                "reviewers", reviewers
        ));

        overview.put("projectStats", Map.of(
                "total", totalProjects,
                "active", activeProjects
        ));

        overview.put("pointStats", Map.of(
                "total", totalPoints,
                "surveyed", surveyedPoints,
                "completionRate", totalPoints > 0 ? (double) surveyedPoints / totalPoints * 100 : 0
        ));

        overview.put("auditStats", Map.of(
                "pending", pendingAudit,
                "approved", approvedResults,
                "total", surveyResultService.count()
        ));

        return overview;
    }

    /**
     * 获取用户统计数据
     */
    public Map<String, Object> getUserStatistics() {
        Map<String, Object> stats = new HashMap<>();

        long totalUsers = sysUserService.count();
        long activeUsers = sysUserService.lambdaQuery().eq(com.qhiot.survey.entity.SysUser::getStatus, 1).count();
        long newUsersThisMonth = sysUserService.lambdaQuery()
                .apply("DATE_FORMAT(create_time, '%Y-%m') = DATE_FORMAT(NOW(), '%Y-%m')")
                .count();

        stats.put("totalUsers", totalUsers);
        stats.put("activeUsers", activeUsers);
        stats.put("newUsersThisMonth", newUsersThisMonth);
        stats.put("activeRate", totalUsers > 0 ? (double) activeUsers / totalUsers * 100 : 0);

        return stats;
    }

    /**
     * 获取项目统计数据
     */
    public Map<String, Object> getProjectStatistics() {
        Map<String, Object> stats = new HashMap<>();

        long totalProjects = projectService.count();
        long activeProjects = projectService.lambdaQuery().eq(com.qhiot.survey.entity.Project::getStatus, 1).count();

        stats.put("totalProjects", totalProjects);
        stats.put("activeProjects", activeProjects);

        return stats;
    }
}