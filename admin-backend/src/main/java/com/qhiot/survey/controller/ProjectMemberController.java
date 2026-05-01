package com.qhiot.survey.controller;

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
@Tag(name = "项目成员管理")
@RestController
@RequestMapping("/api/project/{projectId}/members")
@RequiredArgsConstructor
public class ProjectMemberController {

    private final ProjectMemberService projectMemberService;

    @Operation(summary = "获取项目成员列表")
    @GetMapping
    public Result<List<ProjectMember>> getProjectMembers(@PathVariable Long projectId) {
        List<ProjectMember> members = projectMemberService.getProjectMembers(projectId);
        return Result.success(members);
    }

    @Operation(summary = "添加项目成员")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Boolean> addMember(
            @PathVariable Long projectId,
            @Parameter(description = "用户ID") @RequestParam Long userId,
            @Parameter(description = "角色: admin/collector/auditor/viewer") @RequestParam String role) {
        boolean success = projectMemberService.addMember(projectId, userId, role);
        return success ? Result.success(true) : Result.error("添加失败，用户可能已是项目成员");
    }

    @Operation(summary = "批量添加项目成员")
    @PostMapping("/batch")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Integer> batchAddMembers(
            @PathVariable Long projectId,
            @RequestBody Map<String, Object> request) {
        @SuppressWarnings("unchecked")
        List<Long> userIds = (List<Long>) request.get("userIds");
        String role = (String) request.get("role");
        int count = projectMemberService.batchAddMembers(projectId, userIds, role);
        return Result.success(count);
    }

    @Operation(summary = "移除项目成员")
    @DeleteMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Boolean> removeMember(@PathVariable Long projectId, @PathVariable Long userId) {
        boolean success = projectMemberService.removeMember(projectId, userId);
        return success ? Result.success(true) : Result.error("移除失败");
    }

    @Operation(summary = "更新成员角色")
    @PutMapping("/{userId}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Boolean> updateMemberRole(
            @PathVariable Long projectId,
            @PathVariable Long userId,
            @Parameter(description = "新角色") @RequestParam String role) {
        boolean success = projectMemberService.updateMemberRole(projectId, userId, role);
        return success ? Result.success(true) : Result.error("更新失败，用户可能不是项目成员");
    }

    @Operation(summary = "检查用户是否是项目成员")
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