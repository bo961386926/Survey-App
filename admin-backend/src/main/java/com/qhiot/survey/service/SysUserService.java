package com.qhiot.survey.service;

import com.qhiot.survey.dto.PageResult;
import com.qhiot.survey.entity.SysUser;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

public interface SysUserService extends IService<SysUser> {

    /**
     * 根据用户名获取用户
     */
    SysUser getUserByUsername(String username);

    /**
     * 创建用户
     */
    boolean createUser(SysUser user);

    /**
     * 更新用户
     */
    boolean updateUser(SysUser user);

    /**
     * 删除用户（逻辑删除）
     */
    boolean deleteUser(Long id);

    /**
     * 分页查询用户列表
     */
    PageResult<SysUser> queryUserPage(String username, Integer status, Integer pageNum, Integer pageSize);

    /**
     * 获取用户列表
     */
    List<SysUser> getUserList();

    /**
     * 更新用户状态
     */
    boolean updateUserStatus(Long id, Integer status);

    /**
     * 重置用户密码
     */
    boolean resetPassword(Long id, String newPassword);

    /**
     * 处理登录失败，增加失败次数，达到阈值后锁定
     * @param username 用户名
     */
    void handleLoginFailure(String username);

    /**
     * 处理登录成功，重置失败次数
     * @param user 用户
     */
    void handleLoginSuccess(SysUser user);

    /**
     * 检查用户是否被锁定
     * @param user 用户
     * @return true-已锁定 false-未锁定
     */
    boolean isUserLocked(SysUser user);

    /**
     * 导出用户数据为Excel字节数组
     * @return Excel字节数组
     */
    byte[] exportUsers();

    /**
     * 从Excel导入用户数据
     * @param data 用户数据列表
     * @return 导入结果：成功数量和失败数量
     */
    Map<String, Integer> importUsers(List<Map<Integer, String>> data);
    
    /**
     * 获取用户负责的项目名称（多个项目用逗号分隔）
     * @param userId 用户ID
     * @return 项目名称字符串
     */
    String getUserProjectNames(Long userId);
}
