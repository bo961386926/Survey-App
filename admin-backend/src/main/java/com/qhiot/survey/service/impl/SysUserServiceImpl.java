package com.qhiot.survey.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qhiot.survey.common.BusinessException;
import com.qhiot.survey.common.util.ExcelUtil;
import com.qhiot.survey.common.util.PasswordGenerator;
import com.qhiot.survey.dto.PageResult;
import com.qhiot.survey.entity.SysRole;
import com.qhiot.survey.entity.SysUser;
import com.qhiot.survey.entity.Project;
import com.qhiot.survey.entity.ProjectMember;
import com.qhiot.survey.mapper.SysUserMapper;
import com.qhiot.survey.service.SysRoleService;
import com.qhiot.survey.service.SysUserService;
import com.qhiot.survey.service.ProjectMemberService;
import com.qhiot.survey.service.ProjectService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {

    private final PasswordEncoder passwordEncoder;
    private final StringRedisTemplate redisTemplate;
    private final ProjectMemberService projectMemberService;
    private final ProjectService projectService;
    private final SysRoleService sysRoleService;

    public SysUserServiceImpl(PasswordEncoder passwordEncoder, StringRedisTemplate redisTemplate,
                              ProjectMemberService projectMemberService, ProjectService projectService,
                              SysRoleService sysRoleService) {
        this.passwordEncoder = passwordEncoder;
        this.redisTemplate = redisTemplate;
        this.projectMemberService = projectMemberService;
        this.projectService = projectService;
        this.sysRoleService = sysRoleService;
    }

    /**
     * 最大登录失败次数
     */
    private static final int MAX_LOGIN_FAIL_COUNT = 5;

    /**
     * 锁定时长（分钟）
     */
    private static final int LOCK_DURATION_MINUTES = 30;

    @Override
    public SysUser getUserByUsername(String username) {
        return lambdaQuery().eq(SysUser::getUsername, username).one();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean createUser(SysUser user) {
        // 检查用户名是否已存在
        if (getUserByUsername(user.getUsername()) != null) {
            throw new BusinessException("用户名已存在");
        }
        user.setLoginFailCount(0);
        user.setStatus(1); // 默认启用
        return save(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateUser(SysUser user) {
        log.info("====== [用户服务] 开始更新用户 - userId: {} ======", user.getId());
        
        SysUser existing = getById(user.getId());
        if (existing == null) {
            log.error("====== [用户服务] 用户不存在 - userId: {} ======", user.getId());
            throw new BusinessException("用户不存在，ID: " + user.getId());
        }
        
        log.info("====== [用户服务] 找到用户 - userId: {}, username: {} ======", existing.getId(), existing.getUsername());
        boolean success = updateById(user);
        
        if (success) {
            log.info("====== [用户服务] 用户更新成功 - userId: {} ======", user.getId());
        } else {
            log.error("====== [用户服务] 用户更新失败 - userId: {} ======", user.getId());
        }
        
        return success;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteUser(Long id) {
        SysUser user = getById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        // 物理删除（SysUser表没有is_deleted字段）
        return removeById(id);
    }

    @Override
    public PageResult<SysUser> queryUserPage(String username, Integer status, Integer pageNum, Integer pageSize) {
        Page<SysUser> page = new Page<>(pageNum, pageSize);
        
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        
        if (StringUtils.hasText(username)) {
            wrapper.like(SysUser::getUsername, username);
        }
        if (status != null) {
            wrapper.eq(SysUser::getStatus, status);
        }
        
        wrapper.orderByDesc(SysUser::getCreateTime);
        
        Page<SysUser> resultPage = baseMapper.selectPage(page, wrapper);
        
        int totalPages = (int) Math.ceil((double) resultPage.getTotal() / resultPage.getSize());
        return new PageResult<>(
                resultPage.getRecords(),
                resultPage.getTotal(),
                (int) resultPage.getCurrent(),
                (int) resultPage.getSize(),
                totalPages
        );
    }

    @Override
    public List<SysUser> getUserList() {
        List<SysUser> users = list();
        if (users != null && !users.isEmpty()) {
            log.info("Fetched {} users. First user realName: {}", users.size(), users.get(0).getRealName());
        }
        return users;
    }

    @Override
    public boolean updateUserStatus(Long id, Integer status) {
        SysUser user = getById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        SysUser updateUser = new SysUser();
        updateUser.setId(id);
        updateUser.setStatus(status);
        return updateById(updateUser);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean resetPassword(Long id, String newPassword) {
        SysUser user = getById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        SysUser updateUser = new SysUser();
        updateUser.setId(id);
        updateUser.setPassword(newPassword);
        return updateById(updateUser);
    }

    @Override
    public void handleLoginFailure(String username) {
        String lockKey = "login:lock:" + username;
        Boolean locked = redisTemplate.opsForValue().setIfAbsent(lockKey, "1", 5, java.util.concurrent.TimeUnit.SECONDS);
        
        if (Boolean.FALSE.equals(locked)) {
            // 获取锁失败，说明有其他线程正在处理
            return;
        }
        
        try {
            SysUser user = getUserByUsername(username);
            if (user == null) {
                return;
            }

            int failCount = user.getLoginFailCount() == null ? 0 : user.getLoginFailCount();
            failCount++;

            SysUser updateUser = new SysUser();
            updateUser.setId(user.getId());
            updateUser.setLoginFailCount(failCount);

            // 达到最大失败次数，锁定账户
            if (failCount >= MAX_LOGIN_FAIL_COUNT) {
                updateUser.setLockTime(LocalDateTime.now());
                log.warn("用户登录失败次数过多，已锁定: username={}", username);
            }

            updateById(updateUser);
        } finally {
            redisTemplate.delete(lockKey);
        }
    }

    @Override
    public void handleLoginSuccess(SysUser user) {
        String lockKey = "login:success:lock:" + user.getId();
        Boolean locked = redisTemplate.opsForValue().setIfAbsent(lockKey, "1", 5, java.util.concurrent.TimeUnit.SECONDS);
        
        if (Boolean.FALSE.equals(locked)) {
            return;
        }
        
        try {
            SysUser updateUser = new SysUser();
            updateUser.setId(user.getId());
            updateUser.setLoginFailCount(0);
            updateUser.setLockTime(null);
            updateUser.setLastLoginTime(LocalDateTime.now());
            updateById(updateUser);
        } finally {
            redisTemplate.delete(lockKey);
        }
    }

    @Override
    public boolean isUserLocked(SysUser user) {
        if (user == null) {
            return true;
        }

        // 检查是否被锁定
        if (user.getLockTime() != null) {
            LocalDateTime lockTime = user.getLockTime();
            LocalDateTime unlockTime = lockTime.plusMinutes(LOCK_DURATION_MINUTES);

            // 锁定时间已过，自动解锁
            if (LocalDateTime.now().isAfter(unlockTime)) {
                SysUser updateUser = new SysUser();
                updateUser.setId(user.getId());
                updateUser.setLockTime(null);
                updateUser.setLoginFailCount(0);
                updateById(updateUser);
                return false;
            }

            return true;
        }

        return false;
    }

    @Override
    public byte[] exportUsers() {
        try {
            // 查询所有用户
            List<SysUser> users = getUserList();
            
            // 构建导出数据
            String[] headers = {"用户名", "真实姓名", "手机号", "邮箱", "角色", "状态"};
            List<Map<String, Object>> dataList = new java.util.ArrayList<>();
            
            for (SysUser user : users) {
                Map<String, Object> row = new HashMap<>();
                row.put("用户名", user.getUsername());
                row.put("真实姓名", user.getRealName());
                row.put("手机号", user.getPhone());
                row.put("邮箱", user.getEmail());
                // 从 sys_user_role 关联表获取用户的所有角色名称
                List<SysRole> roles = sysRoleService.getUserRoles(user.getId());
                String roleNames = roles.stream()
                        .map(SysRole::getRoleName)
                        .filter(name -> name != null && !name.isEmpty())
                        .collect(Collectors.joining("、"));
                row.put("角色", roleNames.isEmpty() ? "未分配" : roleNames);
                row.put("状态", user.getStatus() == 1 ? "启用" : "禁用");
                dataList.add(row);
            }
            
            // 生成Excel
            org.apache.poi.ss.usermodel.Workbook workbook = ExcelUtil.createExcel(headers, dataList);
            
            // 转换为字节数组
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            workbook.write(baos);
            workbook.close();
            
            return baos.toByteArray();
        } catch (Exception e) {
            log.error("导出用户失败", e);
            throw new BusinessException("导出用户失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Integer> importUsers(List<Map<Integer, String>> data) {
        int successCount = 0;
        int failCount = 0;
        
        for (Map<Integer, String> row : data) {
            try {
                String username = row.get(0);
                String realName = row.get(1);
                String phone = row.get(2);
                String email = row.get(3);
                String roleName = row.get(4);
                
                if (!StringUtils.hasText(username)) {
                    failCount++;
                    continue;
                }
                
                // 检查用户是否已存在
                if (getUserByUsername(username) != null) {
                    log.warn("用户已存在，跳过: {}", username);
                    failCount++;
                    continue;
                }
                
                // 创建用户，生成强密码
                SysUser user = new SysUser();
                user.setUsername(username);
                user.setRealName(realName);
                user.setPhone(phone);
                user.setEmail(email);
                
                // 生成强密码（12位，包含大小写字母、数字、特殊字符）
                String strongPassword = PasswordGenerator.generateStrongPassword();
                user.setPassword(passwordEncoder.encode(strongPassword));
                
                // TODO: 应该通过邮件或短信发送密码给用户
                log.info("创建用户: username={}, 初始密码={} (应通过邮件/短信发送)", username, strongPassword);
                
                user.setStatus(1);
                user.setLoginFailCount(0);
                user.setIsFirstLogin(1);
                
                if (save(user)) {
                    // 如果 Excel 中指定了角色名称，通过 sys_user_role 分配角色
                    if (StringUtils.hasText(roleName)) {
                        // 查询 sys_role 表中匹配的角色
                        List<SysRole> matchedRoles = sysRoleService.lambdaQuery()
                                .eq(SysRole::getRoleName, roleName)
                                .eq(SysRole::getStatus, 1)
                                .list();
                        if (!matchedRoles.isEmpty()) {
                            List<Long> roleIds = matchedRoles.stream()
                                    .map(SysRole::getId)
                                    .toList();
                            sysRoleService.assignRoleToUser(user.getId(), roleIds);
                        }
                    }
                    successCount++;
                } else {
                    failCount++;
                }
            } catch (Exception e) {
                log.error("导入用户失败", e);
                failCount++;
            }
        }
        
        Map<String, Integer> result = new HashMap<>();
        result.put("success", successCount);
        result.put("fail", failCount);
        return result;
    }
    
    @Override
    public String getUserProjectNames(Long userId) {
        if (userId == null) {
            return "";
        }
        
        // 查询用户参与的项目ID列表
        List<ProjectMember> members = projectMemberService.lambdaQuery()
                .eq(ProjectMember::getUserId, userId)
                .eq(ProjectMember::getStatus, 1) // 只查询启用的成员关系
                .list();
        
        if (members == null || members.isEmpty()) {
            return "";
        }
        
        // 获取项目名称
        List<String> projectNames = members.stream()
                .map(member -> {
                    Project project = projectService.getById(member.getProjectId());
                    return project != null ? project.getProjectName() : null;
                })
                .filter(name -> name != null && !name.isEmpty())
                .toList();
        
        // 用逗号分隔多个项目名称
        return String.join("，", projectNames);
    }
}
