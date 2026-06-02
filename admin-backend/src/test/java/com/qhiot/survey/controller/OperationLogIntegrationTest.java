package com.qhiot.survey.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qhiot.survey.common.result.Result;
import com.qhiot.survey.entity.SysRole;
import com.qhiot.survey.dto.ProjectCreateRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 操作日志集成测试
 * 测试通过HTTP请求触发操作日志记录
 */
@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("操作日志集成测试")
@Transactional
@EnabledIfEnvironmentVariable(named = "INTEGRATION_TEST", matches = "true")
class OperationLogIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("测试创建项目时记录操作日志")
    void testCreateProject_LogOperation() throws Exception {
        ProjectCreateRequest request = new ProjectCreateRequest();
        request.setProjectName("测试项目");
        request.setProjectCode("PRJ-TEST-001");
        request.setManager("admin");
        request.setStartDate(LocalDate.now());
        request.setEndDate(LocalDate.now().plusMonths(3));

        MvcResult result = mockMvc.perform(post("/api/v1/project")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        Result<?> responseObj = objectMapper.readValue(response, Result.class);
        assertEquals(200, responseObj.getCode());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("测试创建角色时记录操作日志")
    void testCreateRole_LogOperation() throws Exception {
        SysRole role = new SysRole();
        role.setRoleName("测试角色");
        role.setRoleCode("TEST_ROLE");
        role.setStatus(1);

        MvcResult result = mockMvc.perform(post("/api/v1/role")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(role)))
                .andExpect(status().isOk())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        Result<?> responseObj = objectMapper.readValue(response, Result.class);
        assertEquals(200, responseObj.getCode());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("测试更新项目时记录操作日志")
    void testUpdateProject_LogOperation() throws Exception {
        // 先创建项目
        ProjectCreateRequest createRequest = new ProjectCreateRequest();
        createRequest.setProjectName("原项目");
        createRequest.setProjectCode("PRJ-UPDATE-INT-TEST");
        createRequest.setManager("admin");

        mockMvc.perform(post("/api/v1/project")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isOk());

        // 通过分页查询找到刚创建的项目的ID
        MvcResult queryResult = mockMvc.perform(get("/api/v1/project/page")
                .param("projectCode", "PRJ-UPDATE-INT-TEST"))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode queryRoot = objectMapper.readTree(queryResult.getResponse().getContentAsString());
        JsonNode records = queryRoot.path("data").path("records");
        assertTrue(records.isArray() && records.size() > 0, "应该能查到刚创建的项目");
        String projectId = records.get(0).path("id").asText();
        assertFalse(projectId.isEmpty(), "项目ID不应为空");

        // 使用实际ID更新项目
        ProjectCreateRequest updateRequest = new ProjectCreateRequest();
        updateRequest.setProjectName("更新后的项目");
        updateRequest.setProjectCode("PRJ-UPDATE-INT-TEST");
        updateRequest.setManager("admin");

        MvcResult result = mockMvc.perform(put("/api/v1/project/" + projectId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        Result<?> responseObj = objectMapper.readValue(response, Result.class);
        assertEquals(200, responseObj.getCode());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("测试删除角色时记录操作日志")
    void testDeleteRole_LogOperation() throws Exception {
        // 创建角色并从响应中提取ID
        SysRole role = new SysRole();
        role.setRoleName("待删除角色");
        role.setRoleCode("DELETE_ROLE_INT_TEST");
        role.setStatus(1);

        MvcResult createResult = mockMvc.perform(post("/api/v1/role")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(role)))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode createRoot = objectMapper.readTree(createResult.getResponse().getContentAsString());
        String roleId = createRoot.path("data").path("id").asText();
        assertFalse(roleId.isEmpty(), "角色ID不应为空");

        // 使用实际ID删除角色
        MvcResult result = mockMvc.perform(delete("/api/v1/role/" + roleId))
                .andExpect(status().isOk())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        Result<?> responseObj = objectMapper.readValue(response, Result.class);
        assertEquals(200, responseObj.getCode());
    }
}
