package com.salemanager.modules.ums.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 员工接口测试
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    /**
     * 测试获取员工列表
     */
    @Test
    void testGetUserList() throws Exception {
        mockMvc.perform(get("/api/admin/user"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray());
    }

    /**
     * 测试获取员工详情
     */
    @Test
    void testGetUserDetail() throws Exception {
        mockMvc.perform(get("/api/admin/user/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(1));
    }

    /**
     * 测试新增员工
     */
    @Test
    void testCreateUser() throws Exception {
        String json = """
                {
                    "username": "testuser",
                    "password": "123456",
                    "realName": "测试用户",
                    "roleId": 3,
                    "phone": "13800138000"
                }
                """;

        mockMvc.perform(post("/api/admin/user")
                        .contentType("application/json")
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }
}
