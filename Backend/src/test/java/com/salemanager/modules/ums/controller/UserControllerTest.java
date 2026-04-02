package com.salemanager.modules.ums.controller;

import com.salemanager.backend.BackendApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 员工接口测试
 */
@SpringBootTest(classes = BackendApplication.class)
@AutoConfigureMockMvc(addFilters = false)
@Transactional
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testGetUserList() throws Exception {
        mockMvc.perform(get("/api/admin/user"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.list").isArray())
                .andExpect(jsonPath("$.data.pagination").exists());
    }

    @Test
    void testGetUserDetail() throws Exception {
        mockMvc.perform(get("/api/admin/user/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(1));
    }

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
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }
}
