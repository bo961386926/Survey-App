package com.survey.service;

import com.survey.entity.SysUser;

public interface SysUserService {
    SysUser login(String username, String password);
}