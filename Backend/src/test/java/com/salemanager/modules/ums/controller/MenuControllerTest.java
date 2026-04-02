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
 * 菜单接口测试
 */
@SpringBootTest(classes = BackendApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class MenuControllerTest {

    @Autowired
    private MockMvc mockMvc;

    /**
     * 测试获取菜单树
     */
    @Test
    void testGetMenuTree() throws Exception {
        mockMvc.perform(get("/api/admin/menu/tree"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray());
    }

    /**
     * 测试获取菜单列表
     */
    @Test
    void testGetMenuList() throws Exception {
        mockMvc.perform(get("/api/admin/menu"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray());
    }

    /**
     * 测试获取菜单详情
     */
    @Test
    void testGetMenuDetail() throws Exception {
        mockMvc.perform(get("/api/admin/menu/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(1));
    }
}
