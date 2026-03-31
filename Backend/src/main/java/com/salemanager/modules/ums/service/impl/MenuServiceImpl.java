package com.salemanager.modules.ums.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.salemanager.common.exception.BusinessException;
import com.salemanager.modules.ums.mapper.MenuMapper;
import com.salemanager.modules.ums.model.Menu;
import com.salemanager.modules.ums.service.MenuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 菜单服务实现
 */
@Service
public class MenuServiceImpl implements MenuService {

    @Autowired
    private MenuMapper menuMapper;

    @Override
    public List<Menu> getMenuList() {
        return menuMapper.selectList(
                new LambdaQueryWrapper<Menu>()
                        .eq(Menu::getStatus, 1)
                        .orderByAsc(Menu::getSort)
        );
    }

    @Override
    public List<Menu> getMenuTree() {
        List<Menu> allMenus = getMenuList();

        // 构建父子关系
        Map<Long, List<Menu>> parentMap = allMenus.stream()
                .collect(Collectors.groupingBy(Menu::getParentId));

        // 递归构建树
        List<Menu> roots = parentMap.getOrDefault(0L, new ArrayList<>());
        for (Menu root : roots) {
            buildTree(root, parentMap);
        }

        return roots;
    }

    private void buildTree(Menu parent, Map<Long, List<Menu>> parentMap) {
        List<Menu> children = parentMap.get(parent.getId());
        if (children != null && !children.isEmpty()) {
            parent.setChildren(children);
            for (Menu child : children) {
                buildTree(child, parentMap);
            }
        }
    }

    @Override
    public Menu getMenuById(Long id) {
        Menu menu = menuMapper.selectById(id);
        if (menu == null) {
            throw new BusinessException(404, "菜单不存在");
        }
        return menu;
    }
}