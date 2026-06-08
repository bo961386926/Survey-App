package com.qhiot.survey.service;

import java.util.List;

/**
 * 数据范围服务。
 * <p>
 * 负责把“当前登录人能看哪些项目/点位/任务”的判断集中在一处，避免各业务模块散落重复判断。
 */
public interface DataScopeService {

    /**
     * 当前用户是否是系统管理员。
     */
    boolean isSystemAdmin();

    /**
     * 当前用户可访问的项目ID列表。
     * 返回 null 表示不限制项目范围。
     */
    List<Long> getAccessibleProjectIds();

    /**
     * 当前用户可访问的点位ID列表。
     * 返回 null 表示不限制点位范围。
     */
    List<Long> getAccessiblePointIds();

    /**
     * 当前用户是否可访问指定项目。
     */
    boolean canAccessProject(Long projectId);

    /**
     * 当前用户是否可访问指定点位。
     */
    boolean canAccessPoint(Long pointId);

    /**
     * 当前用户是否可访问指定任务。
     */
    boolean canAccessTask(Long taskId);
}
