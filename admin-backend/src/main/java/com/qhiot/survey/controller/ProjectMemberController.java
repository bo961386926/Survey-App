package com.qhiot.survey.controller;

import com.qhiot.survey.common.annotation.OperationLog;
import com.qhiot.survey.common.result.Result;
import com.qhiot.survey.entity.ProjectMember;
import com.qhiot.survey.service.ProjectMemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 项目成员管理控制器
 */
@Tag(name = "项目成员管理", description = "项目成员增删改查、角色分配等接口")
@RestController
@RequestMapping("/api/v1/project/{projectId}/members")
@RequiredArgsConstructor
public class ProjectMemberController {

    private final ProjectMemberService projectMemberService;

    @Operation(summary = "获取项目成员列表", description = "获取指定项目下的所有成员列表")
    @GetMapping
    public Result<List<ProjectMember>> getProjectMembers(@PathVariable Long projectId) {
        List<ProjectMember> members = projectMemberService.getProjectMembers(projectId);
        return Result.success(members);
    }

    @Operation(summary = "添加项目成员", description = "向项目添加单个成员并指定角色")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @OperationLog(module = "项目成员", action = "添加", description = "添加项目成员, 项目ID: #projectId, 用户ID: #userId, 角色: #role", riskLevel = 1)
    public Result<Boolean> addMember(
            @PathVariable Long projectId,
            @Parameter(description = "用户ID") @RequestParam Long userId,
            @Parameter(description = "角色: admin/collector/auditor/viewer") @RequestParam String role) {
        boolean success = projectMemberService.addMember(projectId, userId, role);
        return success ? Result.success(true) : Result.error("添加失败，用户可能已是项目成员");
    }

    @Operation(summary = "批量添加项目成员", description = "批量向项目添加多个成员，指定统一角色")
    @PostMapping("/batch")
    @PreAuthorize("hasRole('ADMIN')")
    @OperationLog(module = "项目成员", action = "批量添加", description = "批量添加项目成员, 项目ID: #projectId, 数量: #userIds.size()", riskLevel = 1)
    public Result<Integer> batchAddMembers(
            @PathVariable Long projectId,
            @RequestBody Map<String, Object> request) {
        @SuppressWarnings("unchecked")
        List<Long> userIds = (List<Long>) request.get("userIds");
        String role = (String) request.get("role");
        int count = projectMemberService.batchAddMembers(projectId, userIds, role);
        return Result.success(count);
    }

    @Operation(summary = "移除项目成员", description = "从项目中移除指定成员")
    @DeleteMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    @OperationLog(module = "项目成员", action = "移除", description = "移除项目成员, 项目ID: #projectId, 用户ID: #userId", riskLevel = 1)
    public Result<Boolean> removeMember(@PathVariable Long projectId, @PathVariable Long userId) {
        boolean success = projectMemberService.removeMember(projectId, userId);
        return success ? Result.success(true) : Result.error("移除失败");
    }

    @Operation(summary = "更新成员角色", description = "更新项目成员的角色，如admin/collector/auditor/viewer")
    @PutMapping("/{userId}/role")
    @PreAuthorize("hasRole('ADMIN')")
    @OperationLog(module = "项目成员", action = "更新角色", description = "更新项目成员角色, 项目ID: #projectId, 用户ID: #userId, 新角色: #role", riskLevel = 1)
    public Result<Boolean> updateMemberRole(
            @PathVariable Long projectId,
            @PathVariable Long userId,
            @Parameter(description = "新角色") @RequestParam String role) {
        boolean success = projectMemberService.updateMemberRole(projectId, userId, role);
        return success ? Result.success(true) : Result.error("更新失败，用户可能不是项目成员");
    }

    @Operation(summary = "检查用户是否是项目成员", description = "检查指定用户是否属于项目成员并返回其角色")
    @GetMapping("/check/{userId}")
    public Result<Map<String, Object>> checkMember(@PathVariable Long projectId, @PathVariable Long userId) {
        boolean isMember = projectMemberService.isProjectMember(projectId, userId);
        String role = projectMemberService.getUserRoleInProject(projectId, userId);
        return Result.success(Map.of(
                "isMember", isMember,
                "role", role != null ? role : ""
        ));
    }
}