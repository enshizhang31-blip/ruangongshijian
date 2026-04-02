package com.salemanager.modules.ums.controller;

import com.salemanager.backend.BackendApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 菜单接口测试
 */
@SpringBootTest(classes = BackendApplication.class)
@AutoConfigureMockMvc(addFilters = false)
@Transactional
class MenuControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testGetMenuTree() throws Exception {
        mockMvc.perform(get("/api/admin/menu/tree"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    void testGetMenuList() throws Exception {
        mockMvc.perform(get("/api/admin/menu"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    void testGetMenuDetail() throws Exception {
        mockMvc.perform(get("/api/admin/menu/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(1));
    }
}
