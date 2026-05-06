package com.survey.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.survey.entity.SysUser;
import com.survey.mapper.SysUserMapper;
import com.survey.service.SysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class SysUserServiceImpl implements SysUserService {

    @Autowired
    private SysUserMapper sysUserMapper;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public SysUser login(String username, String password) {
        QueryWrapper<SysUser> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username);
        queryWrapper.eq("status", 1);
        SysUser user = sysUserMapper.selectOne(queryWrapper);
        if (user != null) {
            // 先尝试 BCrypt 验证
            if (passwordEncoder.matches(password, user.getPassword())) {
                return user;
            }
            // 兼容明文密码（用于初始账号）
            if (password.equals(user.getPassword())) {
                return user;
            }
        }
        return null;
    }
}
