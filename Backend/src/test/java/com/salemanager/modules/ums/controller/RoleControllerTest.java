package com.salemanager.modules.ums.controller;

import com.salemanager.common.config.BackendApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 角色接口测试
 */
@SpringBootTest(classes = BackendApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RoleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    /**
     * 测试获取角色列表
     */
    @Test
    void testGetRoleList() throws Exception {
        mockMvc.perform(get("/api/admin/role"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray());
    }

    /**
     * 测试获取角色详情
     */
    @Test
    void testGetRoleDetail() throws Exception {
        // 预设角色ID为1
        mockMvc.perform(get("/api/admin/role/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.name").exists());
    }
}
