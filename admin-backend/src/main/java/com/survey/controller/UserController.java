package com.survey.controller;

import com.survey.common.Result;
import com.survey.entity.SysUser;
import com.survey.service.SysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
@CrossOrigin(origins = "*")
public class UserController {

    @Autowired
    private SysUserService sysUserService;

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody Map<String, String> loginData) {
        String username = loginData.get("username");
        String password = loginData.get("password");

        SysUser user = sysUserService.login(username, password);
        if (user != null) {
            Map<String, Object> data = new HashMap<>();
            data.put("token", "token-" + user.getId());
            data.put("userId", user.getId());
            data.put("username", user.getUsername());
            data.put("realName", user.getRealName());
            data.put("role", user.getRole());
            return Result.success(data);
        } else {
            return Result.error("用户名或密码错误");
        }
    }
}