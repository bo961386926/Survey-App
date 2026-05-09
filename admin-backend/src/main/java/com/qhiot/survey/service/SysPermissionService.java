package com.qhiot.survey.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.qhiot.survey.entity.SysPermission;

import java.util.List;
import java.util.Set;

/**
 * 权限服务接口
 */
public interface SysPermissionService extends IService<SysPermission> {

    /**
     * 获取所有已启用的权限列表
     */
    List<SysPermission> listEnabled();

    /**
     * 按模块分组获取权限
     */
    List<SysPermission> listByModule(String module);

    /**
     * 获取所有权限编码
     */
    Set<String> getAllPermCodes();

    /**
     * 同步权限：将传入的权限码列表与数据库同步（新增的插入，缺失的标记禁用）
     * @param allPermCodes 当前系统中所有已注册的权限码
     */
    void syncPermissions(Set<String> allPermCodes);
}
