package com.salemanager.modules.ums.controller;

import com.salemanager.backend.BackendApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 角色接口测试
 */
@SpringBootTest(classes = BackendApplication.class)
@AutoConfigureMockMvc(addFilters = false)
@Transactional
class RoleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testGetRoleList() throws Exception {
        mockMvc.perform(get("/api/admin/role"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    void testGetRoleDetail() throws Exception {
        mockMvc.perform(get("/api/admin/role/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.name").exists());
    }
}
