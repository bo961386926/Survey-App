package com.qhiot.survey.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Knife4j接口文档配置
 * 访问地址: http://localhost:8080/doc.html
 */
@Configuration
public class Knife4jConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("青泓项目勘察系统 API文档")
                        .version("1.0.0")
                        .description("青泓项目勘察信息采集与审核系统接口文档")
                        .contact(new Contact()
                                .name("青泓技术团队")
                                .email("dev@qinghong.cn")))
                .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
                .schemaRequirement("Bearer Authentication",
                        new SecurityScheme()
                                .name("Bearer Authentication")
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT"));
    }

    /**
     * 认证与安全分组：认证管理、健康检查
     */
    @Bean
    public GroupedOpenApi authApi() {
        return GroupedOpenApi.builder()
                .group("01-认证与安全")
                .displayName("认证与安全")
                .pathsToMatch("/api/v1/auth/**", "/api/v1/health/**")
                .build();
    }

    /**
     * 项目管理分组：项目、成员、标段
     */
    @Bean
    public GroupedOpenApi projectApi() {
        return GroupedOpenApi.builder()
                .group("02-项目管理")
                .displayName("项目管理")
                .pathsToMatch("/api/v1/project/**", "/api/v1/section/**")
                .build();
    }

    /**
     * 勘察数据分组：点位、结果、模板、纠偏、离线同步
     */
    @Bean
    public GroupedOpenApi surveyDataApi() {
        return GroupedOpenApi.builder()
                .group("03-勘察数据")
                .displayName("勘察数据")
                .pathsToMatch("/api/v1/point/**", "/api/v1/result/**", "/api/v1/template/**",
                        "/api/v1/location-correction/**", "/api/v1/offline-sync/**")
                .build();
    }

    /**
     * 审核中心分组
     */
    @Bean
    public GroupedOpenApi auditApi() {
        return GroupedOpenApi.builder()
                .group("04-审核中心")
                .displayName("审核中心")
                .pathsToMatch("/api/v1/audit/**")
                .build();
    }

    /**
     * 任务管理分组
     */
    @Bean
    public GroupedOpenApi taskApi() {
        return GroupedOpenApi.builder()
                .group("05-任务管理")
                .displayName("任务管理")
                .pathsToMatch("/api/v1/task/**")
                .build();
    }

    /**
     * 系统管理分组：用户、角色、字典、日志、统计
     */
    @Bean
    public GroupedOpenApi systemApi() {
        return GroupedOpenApi.builder()
                .group("06-系统管理")
                .displayName("系统管理")
                .pathsToMatch("/api/v1/user/**", "/api/v1/role/**", "/api/v1/dict/**",
                        "/api/v1/dictionary/**", "/api/v1/dictionary-data/**",
                        "/api/v1/statistics/**", "/api/v1/log/**")
                .build();
    }

    /**
     * 导出与文件分组
     */
    @Bean
    public GroupedOpenApi exportApi() {
        return GroupedOpenApi.builder()
                .group("07-导出与文件")
                .displayName("导出与文件")
                .pathsToMatch("/api/v1/export/**", "/api/v1/file/**")
                .build();
    }

    /**
     * 协作与消息分组
     */
    @Bean
    public GroupedOpenApi collaborationApi() {
        return GroupedOpenApi.builder()
                .group("08-协作与消息")
                .displayName("协作与消息")
                .pathsToMatch("/api/v1/collab/**", "/api/v1/message/**")
                .build();
    }
}
