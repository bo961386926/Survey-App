package com.qhiot.survey.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.qhiot.survey.entity.SysTenant;

/**
 * 租户服务接口
 */
public interface SysTenantService extends IService<SysTenant> {

    SysTenant getByTenantCode(String tenantCode);
}
