package com.qhiot.survey.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qhiot.survey.entity.SysTenant;
import com.qhiot.survey.mapper.SysTenantMapper;
import com.qhiot.survey.service.SysTenantService;
import org.springframework.stereotype.Service;

@Service
public class SysTenantServiceImpl extends ServiceImpl<SysTenantMapper, SysTenant> implements SysTenantService {

    @Override
    public SysTenant getByTenantCode(String tenantCode) {
        return lambdaQuery()
                .eq(SysTenant::getTenantCode, tenantCode)
                .one();
    }
}
