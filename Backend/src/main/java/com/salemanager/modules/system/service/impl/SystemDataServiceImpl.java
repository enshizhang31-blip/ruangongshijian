package com.salemanager.modules.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.salemanager.modules.customer.mapper.AddressMapper;
import com.salemanager.modules.customer.mapper.CustomerMapper;
import com.salemanager.modules.customer.mapper.MemberLevelConfigMapper;
import com.salemanager.modules.customer.model.Address;
import com.salemanager.modules.customer.model.Customer;
import com.salemanager.modules.customer.model.MemberLevelConfig;
import com.salemanager.modules.product.mapper.GoodsCategoryMapper;
import com.salemanager.modules.product.mapper.GoodsMapper;
import com.salemanager.modules.product.mapper.SkuMapper;
import com.salemanager.modules.product.mapper.SpecNameMapper;
import com.salemanager.modules.product.mapper.SpecValueMapper;
import com.salemanager.modules.product.model.Goods;
import com.salemanager.modules.product.model.GoodsCategory;
import com.salemanager.modules.product.model.Sku;
import com.salemanager.modules.product.model.SpecName;
import com.salemanager.modules.product.model.SpecValue;
import com.salemanager.modules.sale.mapper.BalanceRecordMapper;
import com.salemanager.modules.sale.mapper.PointsRecordMapper;
import com.salemanager.modules.sale.mapper.SaleOrderItemMapper;
import com.salemanager.modules.sale.mapper.SaleOrderMapper;
import com.salemanager.modules.sale.model.BalanceRecord;
import com.salemanager.modules.sale.model.PointsRecord;
import com.salemanager.modules.sale.model.SaleOrder;
import com.salemanager.modules.sale.model.SaleOrderItem;
import com.salemanager.modules.sn.mapper.SnCodeMapper;
import com.salemanager.modules.sn.mapper.SnCodeLogMapper;
import com.salemanager.modules.sn.model.SnCode;
import com.salemanager.modules.system.param.AppendDataParam;
import com.salemanager.modules.system.service.SystemDataService;
import com.salemanager.modules.ums.mapper.AdminUserMapper;
import com.salemanager.modules.ums.mapper.DepartmentMapper;
import com.salemanager.modules.ums.mapper.MenuMapper;
import com.salemanager.modules.ums.mapper.RoleMapper;
import com.salemanager.modules.ums.model.AdminUser;
import com.salemanager.modules.ums.model.Department;
import com.salemanager.modules.ums.model.Menu;
import com.salemanager.modules.ums.model.Role;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class SystemDataServiceImpl implements SystemDataService {

    private static final Logger log = LoggerFactory.getLogger(SystemDataServiceImpl.class);
    private static final DateTimeFormatter ORDER_DATE = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final String ADMIN_BCRYPT = "$2a$10$9Fr2OrEUefhFyxvFEviIg.kHuXtbYVT44sQyFk9MSaAbDksT1Z.mK";

    private static final String SURNAMES = "张李王赵钱孙周吴郑冯陈楚卫蒋沈韩杨朱秦尤许何吕施";
    private static final List<String> STREETS = Arrays.asList(
            "科技园路1号", "天河路100号", "建国路88号", "陆家嘴金融中心",
            "文三路", "中山北路", "天府大道100号", "珞瑜路200号"
    );

    private static final List<String> BRANDS = Arrays.asList("Apple", "HUAWEI", "Xiaomi", "OPPO", "vivo", "Samsung", "Dell", "Lenovo");
    private static final List<String> CATEGORY_NAMES = Arrays.asList("数码产品", "办公用品", "智能家居", "生活电器", "配件周边");
    private static final List<SpecTemplate> SPECS = Arrays.asList(
            new SpecTemplate("颜色", Arrays.asList("黑色", "白色", "蓝色", "红色", "绿色", "金色", "银色")),
            new SpecTemplate("容量", Arrays.asList("64G", "128G", "256G", "512G", "1TB")),
            new SpecTemplate("版本", Arrays.asList("标准版", "Pro版", "Max版", "Ultra版"))
    );

    @Autowired private GoodsCategoryMapper categoryMapper;
    @Autowired private GoodsMapper goodsMapper;
    @Autowired private SkuMapper skuMapper;
    @Autowired private SpecNameMapper specNameMapper;
    @Autowired private SpecValueMapper specValueMapper;
    @Autowired private SnCodeMapper snCodeMapper;
    @Autowired private SnCodeLogMapper snCodeLogMapper;
    @Autowired private CustomerMapper customerMapper;
    @Autowired private AddressMapper addressMapper;
    @Autowired private MemberLevelConfigMapper levelConfigMapper;
    @Autowired private SaleOrderMapper orderMapper;
    @Autowired private SaleOrderItemMapper orderItemMapper;
    @Autowired private BalanceRecordMapper balanceMapper;
    @Autowired private PointsRecordMapper pointsMapper;
    @Autowired private DepartmentMapper departmentMapper;
    @Autowired private AdminUserMapper adminUserMapper;
    @Autowired private RoleMapper roleMapper;
    @Autowired private MenuMapper menuMapper;
    @Autowired private ObjectMapper objectMapper;

    // ==================== clear ====================

    @Override
    @Transactional
    public Map<String, Object> clearData() {
        snCodeLogMapper.delete(new LambdaQueryWrapper<>());
        snCodeMapper.delete(new LambdaQueryWrapper<>());
        orderItemMapper.delete(new LambdaQueryWrapper<>());
        orderMapper.delete(new LambdaQueryWrapper<>());
        balanceMapper.delete(new LambdaQueryWrapper<>());
        pointsMapper.delete(new LambdaQueryWrapper<>());
        addressMapper.delete(new LambdaQueryWrapper<>());
        customerMapper.delete(new LambdaQueryWrapper<>());
        levelConfigMapper.delete(new LambdaQueryWrapper<>());
        skuMapper.delete(new LambdaQueryWrapper<>());
        specValueMapper.delete(new LambdaQueryWrapper<>());
        specNameMapper.delete(new LambdaQueryWrapper<>());
        goodsMapper.delete(new LambdaQueryWrapper<>());
        categoryMapper.delete(new LambdaQueryWrapper<>());
        departmentMapper.delete(new LambdaQueryWrapper<>());

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("message", "所有业务数据已清空（保留UMS核心数据）");
        return result;
    }

    // ==================== init ====================

    @Override
    @Transactional
    public Map<String, Object> initData() {
        Long catCount = categoryMapper.selectCount(new LambdaQueryWrapper<>());
        if (catCount > 0) {
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("message", "数据已初始化，请先调用 /clear 清空后再执行");
            result.put("skipped", true);
            return result;
        }

        initRoles();
        initAdminUser();
        initMenus();
        initCategories();
        initSpecs();
        initGoodsAndSkus();
        initSnCodes();
        initLevelConfigs();
        initCustomers();
        initAddresses();
        initDepartments();
        initOrders();

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("message", "基础数据初始化完成");
        return result;
    }

    private void initRoles() {
        roleMapper.insert(buildRole("超级管理员", "SUPER_ADMIN", "拥有所有权限",
                "[\"spu:view\",\"spu:add\",\"spu:edit\",\"spu:delete\",\"spu:import\",\"spu:export\",\"spu:status\",\"sku:view\",\"sku:add\",\"sku:edit\",\"sku:delete\",\"category:view\",\"category:add\",\"category:edit\",\"category:delete\",\"spec:view\",\"spec:add\",\"spec:edit\",\"spec:delete\",\"sn:view\",\"sn:add\",\"sn:import\",\"sn:export\",\"sn:generate\",\"sn:query\",\"sn:status\",\"customer:view\",\"customer:detail\",\"customer:edit\",\"customer:balance\",\"customer:points\",\"customer:disable\",\"order:view\",\"order:detail\",\"order:process\",\"order:refund\",\"statistics:view\",\"system:user\",\"system:role\",\"system:menu\",\"system:log\"]",
                "[\"/dashboard\",\"/product\",\"/product/list\",\"/sn\",\"/sn/list\",\"/order\",\"/order/list\",\"/customer\",\"/customer/list\",\"/statistics\",\"/system\",\"/system/user\",\"/system/role\"]"));
        roleMapper.insert(buildRole("运营主管", "OPERATOR", "商品、订单、客户、数据统计管理",
                "[\"spu:view\",\"spu:add\",\"spu:edit\",\"spu:delete\",\"spu:import\",\"spu:export\",\"sku:view\",\"sku:add\",\"sku:edit\",\"sku:delete\",\"sn:view\",\"sn:add\",\"sn:import\",\"sn:export\",\"sn:query\",\"category:view\",\"category:add\",\"category:edit\",\"order:view\",\"order:edit\",\"customer:view\",\"customer:add\",\"customer:edit\",\"statistics:view\"]",
                "[\"/dashboard\",\"/product\",\"/sn\",\"/order\",\"/customer\",\"/statistics\"]"));
        roleMapper.insert(buildRole("录入员", "INPUTTER", "商品新增、SN码查询、操作记录",
                "[\"spu:add\",\"spu:edit\",\"sn:add\",\"sn:import\",\"sn:query\",\"sn:view\",\"category:add\",\"category:edit\"]",
                "[\"/dashboard\",\"/product\",\"/sn\"]"));
    }

    private Role buildRole(String name, String code, String desc, String perms, String routes) {
        Role role = new Role();
        role.setName(name);
        role.setCode(code);
        role.setDescription(desc);
        role.setPermissions(perms);
        role.setRoutes(routes);
        role.setIsPreset(1);
        role.setStatus(1);
        role.setCreatedAt(LocalDateTime.now());
        role.setUpdatedAt(LocalDateTime.now());
        return role;
    }

    private void initAdminUser() {
        AdminUser admin = new AdminUser();
        admin.setUsername("admin");
        admin.setPassword(ADMIN_BCRYPT);
        admin.setRealName("系统管理员");
        admin.setPermissions("[\"spu:view\",\"spu:add\",\"spu:edit\",\"spu:delete\",\"spu:import\",\"spu:export\",\"spu:status\",\"sku:view\",\"sku:add\",\"sku:edit\",\"sku:delete\",\"category:view\",\"category:add\",\"category:edit\",\"category:delete\",\"spec:view\",\"spec:add\",\"spec:edit\",\"spec:delete\",\"sn:view\",\"sn:add\",\"sn:import\",\"sn:export\",\"sn:generate\",\"sn:query\",\"sn:status\",\"customer:view\",\"customer:detail\",\"customer:edit\",\"customer:balance\",\"customer:points\",\"customer:disable\",\"order:view\",\"order:detail\",\"order:process\",\"order:refund\",\"statistics:view\",\"system:user\",\"system:role\",\"system:menu\",\"system:log\"]");
        admin.setRoutes("[\"/dashboard\",\"/product\",\"/product/list\",\"/sn\",\"/sn/list\",\"/order\",\"/order/list\",\"/customer\",\"/customer/list\",\"/statistics\",\"/system\",\"/system/user\",\"/system/role\"]");
        admin.setStatus(1);
        admin.setCreatedAt(LocalDateTime.now());
        admin.setUpdatedAt(LocalDateTime.now());
        adminUserMapper.insert(admin);
    }

    private void initMenus() {
        List<Menu> menus = new ArrayList<>();
        menus.add(buildMenu("仪表盘", "/dashboard", "dashboard/index", "HomeIcon", 1, 0L, 1, "dashboard:view"));
        menus.add(buildMenu("商品管理", "/product", null, "CubeIcon", 2, 0L, 1, "product:view"));
        menus.add(buildMenu("商品列表", "/product/list", "product/ProductList", null, 1, 2L, 1, "spu:view"));
        menus.add(buildMenu("SN码管理", "/sn", null, "TagIcon", 3, 0L, 1, "sn:view"));
        menus.add(buildMenu("SN码列表", "/sn/list", "sn/SnList", null, 1, 4L, 1, "sn:view"));
        menus.add(buildMenu("订单管理", "/order", null, "ShoppingCartIcon", 4, 0L, 1, "order:view"));
        menus.add(buildMenu("订单列表", "/order/list", "sale/SaleOrderList", null, 1, 6L, 1, "order:view"));
        menus.add(buildMenu("客户管理", "/customer", null, "UsersIcon", 5, 0L, 1, "customer:view"));
        menus.add(buildMenu("客户列表", "/customer/list", "customer/CustomerList", null, 1, 8L, 1, "customer:view"));
        menus.add(buildMenu("数据统计", "/statistics", null, "ChartBarIcon", 6, 0L, 1, "statistics:view"));
        menus.add(buildMenu("系统管理", "/system", null, "Cog6ToothIcon", 100, 0L, 1, "system:view"));
        menus.add(buildMenu("员工管理", "/system/user", "system/UserList", null, 1, 11L, 1, "system:user"));
        menus.add(buildMenu("角色管理", "/system/role", "system/RoleList", null, 2, 11L, 1, "system:role"));
        for (Menu menu : menus) {
            menuMapper.insert(menu);
        }
    }

    private Menu buildMenu(String name, String path, String component, String icon, int sort, Long parentId, int type, String permission) {
        Menu menu = new Menu();
        menu.setName(name);
        menu.setPath(path);
        menu.setComponent(component);
        menu.setIcon(icon);
        menu.setSort(sort);
        menu.setParentId(parentId);
        menu.setType(type);
        menu.setPermission(permission);
        menu.setStatus(1);
        menu.setCreatedAt(LocalDateTime.now());
        return menu;
    }

    // ==================== product init ====================

    private void initCategories() {
        String[][] cats = {{"手机", "0", "1"}, {"电脑", "0", "2"}, {"平板", "0", "3"}, {"配件", "0", "4"}, {"智能穿戴", "0", "5"}};
        for (String[] c : cats) {
            insertCategory(c[0], 0L, Integer.parseInt(c[2]));
        }
        insertCategory("iPhone", 1L, 1);
        insertCategory("Android", 1L, 2);
    }

    private void insertCategory(String name, Long parentId, int sort) {
        GoodsCategory cat = new GoodsCategory();
        cat.setName(name);
        cat.setParentId(parentId);
        cat.setSort(sort);
        cat.setStatus(1);
        cat.setCreatedAt(LocalDateTime.now());
        cat.setUpdatedAt(LocalDateTime.now());
        categoryMapper.insert(cat);
    }

    private void initSpecs() {
        String[] specNames = {"颜色", "版本", "容量", "尺寸", "材质"};
        for (int i = 0; i < specNames.length; i++) {
            SpecName sn = new SpecName();
            sn.setName(specNames[i]);
            sn.setSort(i + 1);
            sn.setCreatedAt(LocalDateTime.now());
            sn.setUpdatedAt(LocalDateTime.now());
            specNameMapper.insert(sn);
        }
    }

    private void initGoodsAndSkus() {
        String[][] goodsData = {
                {"iPhone 15", "1", "Apple", "https://example.com/iphone15.jpg", "苹果 iPhone 15 智能手机，配备 A16 仿生芯片"},
                {"iPhone 15 Pro", "6", "Apple", "https://example.com/iphone15pro.jpg", "苹果 iPhone 15 Pro 专业版，配备 A17 Pro 芯片"},
                {"iPhone 15 Pro Max", "6", "Apple", "https://example.com/iphone15promax.jpg", "苹果 iPhone 15 Pro Max 顶配版"},
                {"MacBook Pro 14", "2", "Apple", "https://example.com/mbp14.jpg", "苹果 MacBook Pro 14寸，配备 M3 芯片"},
                {"MacBook Air 13", "2", "Apple", "https://example.com/mba13.jpg", "苹果 MacBook Air 13寸，轻薄便携"},
                {"iPad Pro 12.9", "3", "Apple", "https://example.com/ipadpro.jpg", "苹果 iPad Pro 12.9寸，支持 Apple Pencil"},
                {"AirPods Pro", "4", "Apple", "https://example.com/airpods.jpg", "苹果 AirPods Pro 第二代，主动降噪"},
                {"AirPods Max", "4", "Apple", "https://example.com/airpodsmax.jpg", "苹果 AirPods Max 头戴式耳机"},
                {"Apple Watch Series 9", "5", "Apple", "https://example.com/watch9.jpg", "苹果 Apple Watch Series 9 智能手表"},
                {"Samsung S24 Ultra", "7", "Samsung", "https://example.com/s24u.jpg", "三星 Galaxy S24 Ultra 旗舰手机"},
                {"iPad Air", "3", "Apple", "https://example.com/ipadair.jpg", "苹果 iPad Air M2 芯片，轻薄设计"},
                {"Apple Pencil Pro", "4", "Apple", "https://example.com/pencilpro.jpg", "苹果 Apple Pencil Pro，支持悬停和触感反馈"},
        };
        for (String[] g : goodsData) {
            Goods goods = new Goods();
            goods.setName(g[0]);
            goods.setCategoryId(Long.parseLong(g[1]));
            goods.setBrand(g[2]);
            goods.setImageUrl(g[3]);
            goods.setDescription(g[4]);
            goods.setStatus(1);
            goods.setCreatedAt(LocalDateTime.now());
            goods.setUpdatedAt(LocalDateTime.now());
            goodsMapper.insert(goods);
        }
        initSkus();
    }

    private void initSkus() {
        // iPhone 15 (goods_id=1): 6 SKUs
        insertSku(1L, "IP15-128-BLK", "{"颜色":"黑色","内存":"128GB"}", 5999, 5000);
        insertSku(1L, "IP15-256-BLK", "{"颜色":"黑色","内存":"256GB"}", 6999, 5800);
        insertSku(1L, "IP15-128-WHT", "{"颜色":"白色","内存":"128GB"}", 5999, 5000);
        insertSku(1L, "IP15-256-WHT", "{"颜色":"白色","内存":"256GB"}", 6999, 5800);
        insertSku(1L, "IP15-128-BLU", "{"颜色":"蓝色","内存":"128GB"}", 5999, 5000);
        insertSku(1L, "IP15-128-GRN", "{"颜色":"绿色","内存":"128GB"}", 5999, 5000);
        // iPhone 15 Pro (goods_id=2): 4 SKUs
        insertSku(2L, "IP15P-256-BLK", "{"颜色":"黑色钛金属","内存":"256GB"}", 8999, 7500);
        insertSku(2L, "IP15P-512-BLK", "{"颜色":"黑色钛金属","内存":"512GB"}", 9999, 8300);
        insertSku(2L, "IP15P-256-WHT", "{"颜色":"白色钛金属","内存":"256GB"}", 8999, 7500);
        insertSku(2L, "IP15P-1T-BLK", "{"颜色":"黑色钛金属","内存":"1TB"}", 11999, 10000);
        // iPhone 15 Pro Max (goods_id=3): 3 SKUs
        insertSku(3L, "IP15PM-256-BLK", "{"颜色":"黑色钛金属","内存":"256GB"}", 9999, 8300);
        insertSku(3L, "IP15PM-512-BLK", "{"颜色":"黑色钛金属","内存":"512GB"}", 10999, 9100);
        insertSku(3L, "IP15PM-1T-BLK", "{"颜色":"黑色钛金属","内存":"1TB"}", 12999, 10800);
        // MacBook Pro 14 (goods_id=4): 4 SKUs
        insertSku(4L, "MBP14-M3-512", "{"芯片":"M3","内存":"18GB","硬盘":"512GB"}", 12999, 11000);
        insertSku(4L, "MBP14-M3-1T", "{"芯片":"M3","内存":"18GB","硬盘":"1TB"}", 14999, 12500);
        insertSku(4L, "MBP14-M3P-512", "{"芯片":"M3 Pro","内存":"18GB","硬盘":"512GB"}", 16999, 14000);
        insertSku(4L, "MBP14-M3P-1T", "{"芯片":"M3 Pro","内存":"36GB","硬盘":"1TB"}", 19999, 16500);
        // MacBook Air 13 (goods_id=5): 3 SKUs
        insertSku(5L, "MBA13-M3-256", "{"芯片":"M3","内存":"8GB","硬盘":"256GB"}", 8999, 7500);
        insertSku(5L, "MBA13-M3-512", "{"芯片":"M3","内存":"8GB","硬盘":"512GB"}", 9999, 8300);
        insertSku(5L, "MBA13-M3-256-SPC", "{"芯片":"M3","内存":"8GB","硬盘":"256GB","颜色":"星光色"}", 9499, 7900);
        // iPad Pro 12.9 (goods_id=6): 3 SKUs
        insertSku(6L, "IPDP12-256-WLF", "{"颜色":"深空灰","存储":"256GB","网络":"Wi-Fi"}", 8499, 7000);
        insertSku(6L, "IPDP12-512-WLF", "{"颜色":"深空灰","存储":"512GB","网络":"Wi-Fi"}", 9299, 7700);
        insertSku(6L, "IPDP12-256-CEL", "{"颜色":"深空灰","存储":"256GB","网络":"蜂窝版"}", 9699, 8000);
        // AirPods Pro (goods_id=7): 2 SKUs
        insertSku(7L, "APP2-WHT", "{"颜色":"白色","型号":"第二代"}", 1899, 1400);
        insertSku(7L, "APP2-BLK", "{"颜色":"黑色","型号":"第二代"}", 1899, 1400);
        // AirPods Max (goods_id=8): 4 SKUs
        insertSku(8L, "APM-SPC", "{"颜色":"星光色"}", 4399, 3600);
        insertSku(8L, "APM-GRY", "{"颜色":"深空灰"}", 4399, 3600);
        insertSku(8L, "APM-BLU", "{"颜色":"蓝色"}", 4399, 3600);
        insertSku(8L, "APM-GRN", "{"颜色":"绿色"}", 4399, 3600);
        // Apple Watch (goods_id=9): 4 SKUs
        insertSku(9L, "AW9-45-BLK", "{"颜色":"午夜色","尺寸":"45mm"}", 3199, 2600);
        insertSku(9L, "AW9-45-SLV", "{"颜色":"银色","尺寸":"45mm"}", 3199, 2600);
        insertSku(9L, "AW9-41-BLK", "{"颜色":"午夜色","尺寸":"41mm"}", 2999, 2400);
        insertSku(9L, "AW9-41-GLD", "{"颜色":"金色","尺寸":"41mm"}", 3199, 2600);
        // Samsung S24 Ultra (goods_id=10): 3 SKUs
        insertSku(10L, "S24U-256-BLK", "{"颜色":"钛黑","内存":"256GB"}", 9699, 8000);
        insertSku(10L, "S24U-512-BLK", "{"颜色":"钛黑","内存":"512GB"}", 10699, 8800);
        insertSku(10L, "S24U-1T-BLK", "{"颜色":"钛黑","内存":"1TB"}", 12699, 10500);
        // iPad Air (goods_id=11): 4 SKUs
        insertSku(11L, "IADAIR-M2-128", "{"芯片":"M2","颜色":"星光色","存储":"128GB"}", 4799, 4000);
        insertSku(11L, "IADAIR-M2-256", "{"芯片":"M2","颜色":"星光色","存储":"256GB"}", 5599, 4600);
        insertSku(11L, "IADAIR-M2-128-BLU", "{"芯片":"M2","颜色":"蓝色","存储":"128GB"}", 4799, 4000);
        insertSku(11L, "IADAIR-M2-256-BLU", "{"芯片":"M2","颜色":"蓝色","存储":"256GB"}", 5599, 4600);
        // Apple Pencil Pro (goods_id=12): 1 SKU
        insertSku(12L, "APP-PNP", "{"颜色":"白色"}", 999, 700);
    }

    private void insertSku(Long spuId, String code, String specJson, int price, int cost) {
        Sku sku = new Sku();
        sku.setSpuId(spuId);
        sku.setSkuCode(code);
        sku.setSpecJson(specJson);
        sku.setPrice(new BigDecimal(price).setScale(2));
        sku.setCostPrice(new BigDecimal(cost).setScale(2));
        sku.setUnit("台");
        sku.setStatus(1);
        sku.setCreatedAt(LocalDateTime.now());
        sku.setUpdatedAt(LocalDateTime.now());
        skuMapper.insert(sku);
    }

    private void initSnCodes() {
        insertSn("SNIP150001", 1L, 1L, "iPhone 15", "IP15-128-BLK", "{"颜色":"黑色","内存":"128GB"}", 5999);
        insertSn("SNIP150002", 1L, 1L, "iPhone 15", "IP15-128-BLK", "{"颜色":"黑色","内存":"128GB"}", 5999);
        insertSn("SNIP150003", 1L, 1L, "iPhone 15", "IP15-128-BLK", "{"颜色":"黑色","内存":"128GB"}", 5999);
        insertSn("SNIP150004", 2L, 1L, "iPhone 15", "IP15-256-BLK", "{"颜色":"黑色","内存":"256GB"}", 6999);
        insertSn("SNIP150005", 3L, 1L, "iPhone 15", "IP15-128-WHT", "{"颜色":"白色","内存":"128GB"}", 5999);
        insertSn("SNIP15P001", 7L, 2L, "iPhone 15 Pro", "IP15P-256-BLK", "{"颜色":"黑色钛金属","内存":"256GB"}", 8999);
        insertSn("SNIP15P002", 7L, 2L, "iPhone 15 Pro", "IP15P-256-BLK", "{"颜色":"黑色钛金属","内存":"256GB"}", 8999);
        insertSn("SNIP15P003", 8L, 2L, "iPhone 15 Pro", "IP15P-512-BLK", "{"颜色":"黑色钛金属","内存":"512GB"}", 9999);
        insertSn("SNMBP14001", 14L, 4L, "MacBook Pro 14", "MBP14-M3-512", "{"芯片":"M3","内存":"18GB","硬盘":"512GB"}", 12999);
        insertSn("SNMBP14002", 14L, 4L, "MacBook Pro 14", "MBP14-M3-512", "{"芯片":"M3","内存":"18GB","硬盘":"512GB"}", 12999);
        insertSn("SNAPP2001", 24L, 7L, "AirPods Pro", "APP2-WHT", "{"颜色":"白色","型号":"第二代"}", 1899);
        insertSn("SNAPP2002", 24L, 7L, "AirPods Pro", "APP2-WHT", "{"颜色":"白色","型号":"第二代"}", 1899);
        insertSn("SNAPP2003", 24L, 7L, "AirPods Pro", "APP2-WHT", "{"颜色":"白色","型号":"第二代"}", 1899);
        insertSn("SNAPP2004", 25L, 7L, "AirPods Pro", "APP2-BLK", "{"颜色":"黑色","型号":"第二代"}", 1899);
        insertSn("SNAPP2005", 25L, 7L, "AirPods Pro", "APP2-BLK", "{"颜色":"黑色","型号":"第二代"}", 1899);
        insertSn("SNAW90001", 30L, 9L, "Apple Watch Series 9", "AW9-45-BLK", "{"颜色":"午夜色","尺寸":"45mm"}", 3199);
        insertSn("SNAW90002", 30L, 9L, "Apple Watch Series 9", "AW9-45-BLK", "{"颜色":"午夜色","尺寸":"45mm"}", 3199);
        insertSn("SNS24U001", 34L, 10L, "Samsung S24 Ultra", "S24U-256-BLK", "{"颜色":"钛黑","内存":"256GB"}", 9699);
        insertSn("SNS24U002", 34L, 10L, "Samsung S24 Ultra", "S24U-256-BLK", "{"颜色":"钛黑","内存":"256GB"}", 9699);
        insertSn("SNS24U003", 35L, 10L, "Samsung S24 Ultra", "S24U-512-BLK", "{"颜色":"钛黑","内存":"512GB"}", 10699);
    }

    private void insertSn(String snCode, Long skuId, Long spuId, String spuName, String skuCode, String specJson, int price) {
        SnCode sn = new SnCode();
        sn.setSnCode(snCode);
        sn.setSkuId(skuId);
        sn.setSpuId(spuId);
        sn.setSpuName(spuName);
        sn.setSkuCode(skuCode);
        sn.setSpecJson(specJson);
        sn.setPrice(new BigDecimal(price).setScale(2));
        sn.setStatus(0);
        sn.setSource(1);
        sn.setCreatedAt(LocalDateTime.now());
        sn.setUpdatedAt(LocalDateTime.now());
        snCodeMapper.insert(sn);
    }

    private void initLevelConfigs() {
        insertLevelConfig(1, "普通会员", 0, 1.00, 1);
        insertLevelConfig(2, "银卡会员", 1000, 0.95, 1);
        insertLevelConfig(3, "金卡会员", 5000, 0.90, 2);
        insertLevelConfig(4, "钻石会员", 20000, 0.85, 3);
    }

    private void insertLevelConfig(int level, String name, int threshold, double discount, int rate) {
        MemberLevelConfig cfg = new MemberLevelConfig();
        cfg.setLevel(level);
        cfg.setName(name);
        cfg.setConsumeThreshold(new BigDecimal(threshold).setScale(2));
        cfg.setDiscount(new BigDecimal(discount).setScale(2));
        cfg.setPointsRate(rate);
        cfg.setStatus(1);
        cfg.setCreatedAt(LocalDateTime.now());
        cfg.setUpdatedAt(LocalDateTime.now());
        levelConfigMapper.insert(cfg);
    }

    private void initCustomers() {
        String[][] customers = {
                {"张三", "13800138001", "2", "9001.00", "1300", "5000.00", "500", "2026-03-01T10:00:00"},
                {"李四", "13800138002", "1", "1501.00", "180", "800.00", "80", "2026-03-05T14:00:00"},
                {"王五", "13800138003", "3", "20001.00", "2700", "25000.00", "2500", "2026-02-15T09:00:00"},
                {"赵六", "13800138004", "1", "201.00", "370", "200.00", "20", "2026-03-20T16:00:00"},
                {"钱七", "13800138005", "4", "50002.00", "6800", "80000.00", "8000", "2026-01-10T11:00:00"},
                {"孙八", "13800138006", "2", "-2399.00", "1100", "6000.00", "600", "2026-02-28T20:00:00"},
                {"周九", "13800138007", "1", "3000.00", "450", "3500.00", "350", "2026-03-10T10:00:00"},
                {"吴十", "13800138008", "2", "5000.00", "900", "8000.00", "800", "2026-02-20T15:00:00"},
                {"郑十一", "13800138009", "3", "15000.00", "3500", "30000.00", "3000", "2026-01-25T09:00:00"},
                {"冯十二", "13800138010", "1", "800.00", "120", "1500.00", "150", "2026-03-15T14:00:00"},
                {"陈十三", "13800138011", "4", "80000.00", "15000", "120000.00", "12000", "2025-12-01T10:00:00"},
                {"楚十四", "13800138012", "2", "6000.00", "1200", "12000.00", "1200", "2026-02-05T11:00:00"},
        };
        for (String[] c : customers) {
            Customer customer = new Customer();
            customer.setNickname(c[0]);
            customer.setPhone(c[1]);
            customer.setMemberLevel(Integer.parseInt(c[2]));
            customer.setBalance(new BigDecimal(c[3]));
            customer.setPoints(Integer.parseInt(c[4]));
            customer.setTotalConsume(new BigDecimal(c[5]));
            customer.setTotalPoints(Integer.parseInt(c[6]));
            customer.setStatus(1);
            customer.setCreatedAt(LocalDateTime.parse(c[7]));
            customer.setUpdatedAt(LocalDateTime.now());
            customerMapper.insert(customer);
        }
    }

    private void initAddresses() {
        String[][] addrs = {
                {"1", "张三", "13800138001", "广东省", "深圳市", "南山区", "科技园路1号", "2026-03-01T10:00:00"},
                {"1", "张三", "13800138001", "广东省", "深圳市", "福田区", "深业上城", "2026-03-10T10:00:00"},
                {"2", "李四", "13800138002", "广东省", "广州市", "天河区", "天河路100号", "2026-03-05T14:00:00"},
                {"3", "王五", "13800138003", "北京市", "北京市", "朝阳区", "建国路88号", "2026-02-15T09:00:00"},
                {"4", "赵六", "13800138004", "上海市", "上海市", "浦东新区", "陆家嘴金融中心", "2026-03-20T16:00:00"},
                {"5", "钱七", "13800138005", "浙江省", "杭州市", "西湖区", "文三路", "2026-01-10T11:00:00"},
                {"6", "孙八", "13800138006", "江苏省", "南京市", "鼓楼区", "中山北路", "2026-02-28T20:00:00"},
                {"7", "周九", "13800138007", "四川省", "成都市", "武侯区", "天府大道100号", "2026-03-10T10:00:00"},
                {"8", "吴十", "13800138008", "湖北省", "武汉市", "洪山区", "珞瑜路200号", "2026-02-20T15:00:00"},
                {"9", "郑十一", "13800138009", "浙江省", "宁波市", "鄞州区", "钱湖北路", "2026-01-25T09:00:00"},
                {"10", "冯十二", "13800138010", "山东省", "青岛市", "市南区", "香港中路", "2026-03-15T14:00:00"},
                {"11", "陈十三", "13800138011", "香港", "香港", "中西区", "中环金融街", "2025-12-01T10:00:00"},
                {"12", "楚十四", "13800138012", "台湾", "台北市", "大安区", "忠孝东路", "2026-02-05T11:00:00"},
        };
        int idx = 0;
        for (String[] a : addrs) {
            Address addr = new Address();
            addr.setCustomerId(Long.parseLong(a[0]));
            addr.setReceiverName(a[1]);
            addr.setPhone(a[2]);
            addr.setProvince(a[3]);
            addr.setCity(a[4]);
            addr.setDistrict(a[5]);
            addr.setDetail(a[6]);
            // 每个客户的第一个地址设为默认
            addr.setIsDefault(idx == 0 || !a[0].equals(addrs[idx - 1][0]) ? 1 : 0);
            addr.setStatus(1);
            addr.setCreatedAt(LocalDateTime.parse(a[7]));
            addr.setUpdatedAt(LocalDateTime.now());
            addressMapper.insert(addr);
            idx++;
        }
    }

    private void initDepartments() {
        String[] depts = {"总经理", "销售部", "市场部", "技术部", "财务部"};
        for (int i = 0; i < depts.length; i++) {
            Department dept = new Department();
            dept.setName(depts[i]);
            dept.setParentId(0L);
            dept.setSort(i + 1);
            dept.setStatus(1);
            dept.setCreatedAt(LocalDateTime.now());
            dept.setUpdatedAt(LocalDateTime.now());
            departmentMapper.insert(dept);
        }
    }

    private void initOrders() {
        String[][] orders = {
                {"ORD202604010001", "1", "张三", "5999.00", "0", "5999.00", "3", "2026-04-01T10:00:00", "2", "张三", "13800138001", "广东省 深圳市 南山区 科技园路1号", "2026-04-01T10:00:00", "2026-04-01T10:05:00"},
                {"ORD202604020001", "2", "李四", "1899.00", "100", "1799.00", "1", "2026-04-02T14:30:00", "2", "李四", "13800138002", "广东省 广州市 天河区 天河路100号", "2026-04-02T14:30:00", "2026-04-02T14:35:00"},
                {"ORD202604030001", "3", "王五", "8999.00", "500", "8499.00", "2", "2026-04-03T09:15:00", "1", "王五", "13800138003", "北京市 朝阳区 建国路88号", "2026-04-03T09:15:00", "2026-04-03T09:20:00"},
                {"ORD202604040001", "4", "赵六", "3199.00", "0", "3199.00", "3", "2026-04-04T16:00:00", "1", "赵六", "13800138004", "上海市 浦东新区 陆家嘴金融中心", "2026-04-04T16:00:00", "2026-04-04T16:05:00"},
                {"ORD202604050001", "5", "钱七", "19998.00", "1000", "18998.00", "2", "2026-04-05T11:30:00", "3", "钱七", "13800138005", "浙江省 杭州市 西湖区 文三路", "2026-04-05T11:30:00", "2026-04-05T11:35:00"},
                {"ORD202604060001", "6", "孙八", "5999.00", "0", "5999.00", "3", "2026-04-06T20:00:00", "1", "孙八", "13800138006", "江苏省 南京市 鼓楼区 中山北路", "2026-04-06T20:00:00", "2026-04-07T10:00:00"},
                {"ORD202603150001", "7", "周九", "8999.00", "0", "8999.00", "1", "2026-03-15T10:00:00", "2", "周九", "13800138007", "四川省 成都市 武侯区 天府大道100号", "2026-03-15T10:00:00", "2026-03-15T10:05:00"},
                {"ORD202603180001", "8", "吴十", "16999.00", "500", "16499.00", "2", "2026-03-18T15:00:00", "2", "吴十", "13800138008", "湖北省 武汉市 洪山区 珞瑜路200号", "2026-03-18T15:00:00", "2026-03-18T15:05:00"},
                {"ORD202603200001", "9", "郑十一", "3199.00", "0", "3199.00", "3", "2026-03-20T09:00:00", "1", "郑十一", "13800138009", "浙江省 宁波市 鄞州区 钱湖北路", "2026-03-20T09:00:00", "2026-03-20T09:05:00"},
                {"ORD202603220001", "10", "冯十二", "1899.00", "0", "1899.00", "1", "2026-03-22T14:00:00", "1", "冯十二", "13800138010", "山东省 青岛市 市南区 香港中路", "2026-03-22T14:00:00", "2026-03-22T14:05:00"},
                {"ORD202603250001", "11", "陈十三", "4399.00", "200", "4199.00", "2", "2026-03-25T10:00:00", "2", "陈十三", "13800138011", "香港 中西区 中环金融街", "2026-03-25T10:00:00", "2026-03-25T10:05:00"},
                {"ORD202603280001", "12", "楚十四", "9999.00", "0", "9999.00", "3", "2026-03-28T11:00:00", "1", "楚十四", "13800138012", "台湾 台北市 大安区 忠孝东路", "2026-03-28T11:00:00", "2026-03-28T11:05:00"},
                {"ORD202604070001", "7", "周九", "5999.00", "0", "5999.00", "3", "2026-04-07T10:00:00", "1", "周九", "13800138007", "四川省 成都市 武侯区 天府大道100号", "2026-04-07T10:00:00", "2026-04-07T10:05:00"},
                {"ORD202604070002", "8", "吴十", "2999.00", "0", "2999.00", "1", "2026-04-07T11:00:00", "1", "吴十", "13800138008", "湖北省 武汉市 洪山区 珞瑜路200号", "2026-04-07T11:00:00", "2026-04-07T11:05:00"},
        };
        for (String[] o : orders) {
            SaleOrder order = new SaleOrder();
            order.setOrderNo(o[0]);
            order.setCustomerId(Long.parseLong(o[1]));
            order.setCustomerName(o[2]);
            order.setTotalAmount(new BigDecimal(o[3]));
            order.setDiscountAmount(new BigDecimal(o[4]));
            order.setPayAmount(new BigDecimal(o[5]));
            order.setPayType(Integer.parseInt(o[6]));
            order.setPaidAt(LocalDateTime.parse(o[7]));
            order.setStatus(Integer.parseInt(o[8]));
            order.setReceiverName(o[9]);
            order.setReceiverPhone(o[10]);
            order.setReceiverAddress(o[11]);
            order.setCreatedAt(LocalDateTime.parse(o[12]));
            order.setUpdatedAt(LocalDateTime.parse(o[13]));
            orderMapper.insert(order);
        }
        initOrderItems();
        updateSnStatusForOrders();
        initBalanceRecords();
        initPointsRecords();
    }

    private void initOrderItems() {
        String[][] items = {
                {"1", "ORD202604010001", "1", "iPhone 15", "{"颜色":"黑色","内存":"128GB"}", "https://example.com/iphone15.jpg", "5999.00", "1", "5999.00", "[1]", "2026-04-01T10:00:00"},
                {"2", "ORD202604020001", "21", "AirPods Pro", "{"颜色":"白色","型号":"第二代"}", "https://example.com/airpods.jpg", "1899.00", "1", "1899.00", "[21]", "2026-04-02T14:30:00"},
                {"3", "ORD202604030001", "4", "iPhone 15 Pro", "{"颜色":"黑色钛金属","内存":"256GB"}", "https://example.com/iphone15pro.jpg", "8999.00", "1", "8999.00", "[4]", "2026-04-03T09:15:00"},
                {"4", "ORD202604040001", "27", "Apple Watch Series 9", "{"颜色":"午夜色","尺寸":"45mm"}", "https://example.com/watch9.jpg", "3199.00", "1", "3199.00", "[27]", "2026-04-04T16:00:00"},
                {"5", "ORD202604050001", "4", "iPhone 15 Pro", "{"颜色":"黑色钛金属","内存":"256GB"}", "https://example.com/iphone15pro.jpg", "8999.00", "1", "8999.00", "[4,5]", "2026-04-05T11:30:00"},
                {"5", "ORD202604050001", "5", "iPhone 15 Pro", "{"颜色":"黑色钛金属","内存":"512GB"}", "https://example.com/iphone15pro.jpg", "9999.00", "1", "9999.00", "[5]", "2026-04-05T11:30:00"},
                {"6", "ORD202604060001", "1", "iPhone 15", "{"颜色":"黑色","内存":"128GB"}", "https://example.com/iphone15.jpg", "5999.00", "1", "5999.00", "[2]", "2026-04-06T20:00:00"},
                {"7", "ORD202603150001", "13", "MacBook Pro 14", "{"芯片":"M3 Pro","内存":"18GB","硬盘":"512GB"}", "https://example.com/mbp14.jpg", "16999.00", "1", "16999.00", "[13]", "2026-03-15T10:00:00"},
                {"8", "ORD202603180001", "38", "iPad Air", "{"芯片":"M2","颜色":"蓝色","存储":"128GB"}", "https://example.com/ipadair.jpg", "4799.00", "1", "4799.00", "[38]", "2026-03-18T15:00:00"},
                {"8", "ORD202603180001", "24", "AirPods Pro", "{"颜色":"白色","型号":"第二代"}", "https://example.com/airpods.jpg", "1899.00", "1", "1899.00", "[24,25]", "2026-03-18T15:00:00"},
                {"8", "ORD202603180001", "41", "Apple Pencil Pro", "{"颜色":"白色"}", "https://example.com/pencilpro.jpg", "999.00", "1", "999.00", "[41]", "2026-03-18T15:00:00"},
                {"9", "ORD202603200001", "30", "Apple Watch Series 9", "{"颜色":"午夜色","尺寸":"45mm"}", "https://example.com/watch9.jpg", "3199.00", "1", "3199.00", "[30]", "2026-03-20T09:00:00"},
                {"10", "ORD202603220001", "25", "AirPods Pro", "{"颜色":"黑色","型号":"第二代"}", "https://example.com/airpods.jpg", "1899.00", "1", "1899.00", "[25]", "2026-03-22T14:00:00"},
                {"11", "ORD202603250001", "39", "iPad Air", "{"芯片":"M2","颜色":"蓝色","存储":"128GB"}", "https://example.com/ipadair.jpg", "4799.00", "1", "4799.00", "[39]", "2026-03-25T10:00:00"},
                {"12", "ORD202603280001", "8", "iPhone 15 Pro", "{"颜色":"黑色钛金属","内存":"512GB"}", "https://example.com/iphone15pro.jpg", "9999.00", "1", "9999.00", "[8]", "2026-03-28T11:00:00"},
                {"13", "ORD202604070001", "2", "iPhone 15", "{"颜色":"黑色","内存":"128GB"}", "https://example.com/iphone15.jpg", "5999.00", "1", "5999.00", "[3]", "2026-04-07T10:00:00"},
                {"14", "ORD202604070002", "40", "iPad Air", "{"芯片":"M2","颜色":"蓝色","存储":"256GB"}", "https://example.com/ipadair.jpg", "5599.00", "1", "5599.00", "[40]", "2026-04-07T11:00:00"},
        };
        for (String[] it : items) {
            SaleOrderItem item = new SaleOrderItem();
            item.setOrderId(Long.parseLong(it[0]));
            item.setOrderNo(it[1]);
            item.setSkuId(Long.parseLong(it[2]));
            item.setSpuName(it[3]);
            item.setSkuSpec(it[4]);
            item.setSkuImage(it[5]);
            item.setPrice(new BigDecimal(it[6]));
            item.setQuantity(Integer.parseInt(it[7]));
            item.setSubtotal(new BigDecimal(it[8]));
            item.setSnCodeIds(it[9]);
            item.setCreatedAt(LocalDateTime.parse(it[10]));
            orderItemMapper.insert(item);
        }
    }

    private void updateSnStatusForOrders() {
        String[][] updates = {
                {"SNIP150001", "1", "2026-04-01T10:00:00"}, {"SNAPP2001", "1", "2026-04-02T14:30:00"},
                {"SNIP15P001", "1", "2026-04-03T09:15:00"}, {"SNAW90001", "1", "2026-04-04T16:00:00"},
                {"SNIP15P002", "1", "2026-04-05T11:30:00"}, {"SNIP15P003", "1", "2026-04-05T11:30:00"},
                {"SNIP150002", "1", "2026-04-06T20:00:00"}, {"SNMBP14001", "1", "2026-03-15T10:00:00"},
                {"SNAPP2002", "1", "2026-03-18T15:00:00"}, {"SNAPP2003", "1", "2026-03-18T15:00:00"},
                {"SNAPP2004", "1", "2026-03-18T15:00:00"}, {"SNAW90002", "1", "2026-03-20T09:00:00"},
                {"SNAPP2005", "1", "2026-03-22T14:00:00"}, {"SNIP150003", "1", "2026-04-07T10:00:00"},
                {"SNAPP2005", "1", "2026-04-07T11:00:00"},
        };
        for (String[] u : updates) {
            List<SnCode> snList = snCodeMapper.selectList(new LambdaQueryWrapper<SnCode>().eq(SnCode::getSnCode, u[0]));
            for (SnCode sn : snList) {
                sn.setStatus(Integer.parseInt(u[1]));
                if (u.length > 2 && u[2] != null) {
                    sn.setSoldAt(LocalDateTime.parse(u[2]));
                }
                snCodeMapper.updateById(sn);
            }
        }
    }

    private void initBalanceRecords() {
        String[][] records = {
                {"1", "2", "-5999.00", "10000.00", "4001.00", "order", "1", "订单 ORD202604010001", "2026-04-01T10:05:00"},
                {"2", "2", "-1799.00", "2300.00", "501.00", "order", "2", "订单 ORD202604020001", "2026-04-02T14:35:00"},
                {"3", "2", "-8499.00", "18500.00", "10001.00", "order", "3", "订单 ORD202604030001", "2026-04-03T09:20:00"},
                {"4", "2", "-3199.00", "3400.00", "201.00", "order", "4", "订单 ORD202604040001", "2026-04-04T16:05:00"},
                {"5", "2", "-18998.00", "69000.00", "50002.00", "order", "5", "订单 ORD202604050001", "2026-04-05T11:35:00"},
                {"6", "2", "-5999.00", "3600.00", "-2399.00", "order", "6", "订单 ORD202604060001", "2026-04-06T20:05:00"},
                {"7", "2", "-8999.00", "12000.00", "3001.00", "order", "7", "订单 ORD202603150001", "2026-03-15T10:05:00"},
                {"8", "2", "-16499.00", "21000.00", "4501.00", "order", "8", "订单 ORD202603180001", "2026-03-18T15:05:00"},
                {"9", "2", "-3199.00", "18200.00", "15001.00", "order", "9", "订单 ORD202603200001", "2026-03-20T09:05:00"},
                {"10", "2", "-1899.00", "2700.00", "801.00", "order", "10", "订单 ORD202603220001", "2026-03-22T14:05:00"},
                {"11", "2", "-4199.00", "84200.00", "80001.00", "order", "11", "订单 ORD202603250001", "2026-03-25T10:05:00"},
                {"12", "2", "-9999.00", "16000.00", "6001.00", "order", "12", "订单 ORD202603280001", "2026-03-28T11:05:00"},
                {"7", "2", "-5999.00", "3001.00", "-2998.00", "order", "13", "订单 ORD202604070001", "2026-04-07T10:05:00"},
                {"8", "2", "-2999.00", "4501.00", "1502.00", "order", "14", "订单 ORD202604070002", "2026-04-07T11:05:00"},
                {"1", "1", "5000.00", "4001.00", "9001.00", "recharge", null, "余额充值", "2026-04-02T10:00:00"},
                {"2", "1", "1000.00", "501.00", "1501.00", "recharge", null, "余额充值", "2026-04-03T15:00:00"},
                {"3", "1", "10000.00", "10001.00", "20001.00", "recharge", null, "余额充值", "2026-04-04T09:00:00"},
                {"7", "1", "6000.00", "-2998.00", "3002.00", "recharge", null, "余额充值", "2026-04-08T10:00:00"},
        };
        for (String[] r : records) {
            BalanceRecord br = new BalanceRecord();
            br.setCustomerId(Long.parseLong(r[0]));
            br.setType(Integer.parseInt(r[1]));
            br.setAmount(new BigDecimal(r[2]));
            br.setBalanceBefore(new BigDecimal(r[3]));
            br.setBalanceAfter(new BigDecimal(r[4]));
            br.setSource(r[5]);
            br.setSourceId(r[6] != null && !r[6].equals("null") ? Long.parseLong(r[6]) : null);
            br.setRemark(r[7]);
            br.setCreatedAt(LocalDateTime.parse(r[8]));
            balanceMapper.insert(br);
        }
    }

    private void initPointsRecords() {
        String[][] records = {
                {"1", "1", "600", "800", "order", "1", "订单 ORD202604010001 积分", "2026-04-01T10:05:00"},
                {"2", "1", "180", "230", "order", "2", "订单 ORD202604020001 积分", "2026-04-02T14:35:00"},
                {"3", "1", "1700", "2700", "order", "3", "订单 ORD202604030001 积分", "2026-04-03T09:20:00"},
                {"4", "1", "320", "370", "order", "4", "订单 ORD202604040001 积分", "2026-04-04T16:05:00"},
                {"5", "1", "3800", "6800", "order", "5", "订单 ORD202604050001 积分", "2026-04-05T11:35:00"},
                {"6", "1", "600", "1100", "order", "6", "订单 ORD202604060001 积分", "2026-04-06T20:05:00"},
                {"7", "1", "1700", "2150", "order", "7", "订单 ORD202603150001 积分", "2026-03-15T10:05:00"},
                {"8", "1", "3300", "4200", "order", "8", "订单 ORD202603180001 积分", "2026-03-18T15:05:00"},
                {"9", "1", "640", "4200", "order", "9", "订单 ORD202603200001 积分", "2026-03-20T09:05:00"},
                {"10", "1", "190", "280", "order", "10", "订单 ORD202603220001 积分", "2026-03-22T14:05:00"},
                {"11", "1", "880", "15880", "order", "11", "订单 ORD202603250001 积分", "2026-03-25T10:05:00"},
                {"12", "1", "2000", "3200", "order", "12", "订单 ORD202603280001 积分", "2026-03-28T11:05:00"},
                {"7", "1", "600", "1050", "order", "13", "订单 ORD202604070001 积分", "2026-04-07T10:05:00"},
                {"8", "1", "300", "450", "order", "14", "订单 ORD202604070002 积分", "2026-04-07T11:05:00"},
                {"1", "1", "500", "1300", "adjust", null, "积分调整", "2026-04-03T10:00:00"},
                {"2", "2", "-50", "180", "redeem", null, "积分兑换商品", "2026-04-04T11:00:00"},
        };
        for (String[] r : records) {
            PointsRecord pr = new PointsRecord();
            pr.setCustomerId(Long.parseLong(r[0]));
            pr.setType(Integer.parseInt(r[1]));
            pr.setAmount(Integer.parseInt(r[2]));
            pr.setBalance(Integer.parseInt(r[3]));
            pr.setSource(r[4]);
            pr.setSourceId(r[5] != null && !r[5].equals("null") ? Long.parseLong(r[5]) : null);
            pr.setRemark(r[6]);
            pr.setCreatedAt(LocalDateTime.parse(r[7]));
            pointsMapper.insert(pr);
        }
    }

    // ==================== append ====================

    @Override
    @Transactional
    public Map<String, Object> appendData(AppendDataParam param) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("module", param.getModule());
        result.put("batchCode", buildBatchCode());

        switch (param.getModule()) {
            case "product":
                result.put("productCount", appendProducts(param.getCount()));
                break;
            case "customer":
                result.put("customerCount", appendCustomers(param.getCount()));
                break;
            case "sn":
                result.put("snCount", appendSnCodesOnly(param.getCount()));
                break;
            case "order":
                result.put("orderCount", appendOrders(param.getCount()));
                break;
            case "all":
                result.put("productCount", appendProducts(param.getCount()));
                result.put("customerCount", appendCustomers(Math.max(5, param.getCount() / 2)));
                result.put("orderCount", appendOrders(Math.max(3, param.getCount() / 3)));
                break;
            default:
                result.put("error", "未知模块: " + param.getModule());
        }
        return result;
    }

    private int appendProducts(int count) {
        List<GoodsCategory> cats = categoryMapper.selectList(new LambdaQueryWrapper<>());
        String batch = buildBatchCode();
        for (int i = 0; i < count; i++) {
            Goods goods = buildMockGoods(batch, i + 1, cats);
            goodsMapper.insert(goods);
            int skuPerGoods = ThreadLocalRandom.current().nextInt(1, 5);
            for (int j = 0; j < skuPerGoods; j++) {
                Sku sku = buildMockSku(goods, batch, i + 1, j + 1);
                skuMapper.insert(sku);
                int snPerSku = ThreadLocalRandom.current().nextInt(1, 4);
                for (int k = 0; k < snPerSku; k++) {
                    snCodeMapper.insert(buildMockSnCode(sku, goods, j + 1, k + 1));
                }
            }
        }
        return count;
    }

    private Goods buildMockGoods(String batch, int idx, List<GoodsCategory> cats) {
        Goods goods = new Goods();
        goods.setName(String.format("模拟商品 %s-%03d", batch, idx));
        goods.setCategoryId(cats.get(idx % cats.size()).getId());
        goods.setBrand(BRANDS.get(idx % BRANDS.size()));
        goods.setImageUrl("https://dummyimage.com/600x600/edf2ff/1f2937&text=MOCK+" + idx);
        goods.setDescription("批量追加的模拟商品数据");
        goods.setStatus(1);
        goods.setCreatedAt(LocalDateTime.now());
        goods.setUpdatedAt(LocalDateTime.now());
        return goods;
    }

    private Sku buildMockSku(Goods goods, String batch, int goodsIdx, int skuIdx) {
        Sku sku = new Sku();
        sku.setSpuId(goods.getId());
        sku.setSkuCode(String.format("MOCK-%s-%03d-%02d", batch, goodsIdx, skuIdx));
        sku.setSpecJson(buildRandomSpecJson());
        int price = 599 + goodsIdx * 19 + skuIdx * 7;
        sku.setPrice(new BigDecimal(price).setScale(2));
        sku.setCostPrice(new BigDecimal(price - 120).setScale(2));
        sku.setUnit("件");
        sku.setStatus(1);
        sku.setCreatedAt(LocalDateTime.now());
        sku.setUpdatedAt(LocalDateTime.now());
        return sku;
    }

    private SnCode buildMockSnCode(Sku sku, Goods goods, int skuIdx, int snIdx) {
        SnCode sn = new SnCode();
        sn.setSnCode(String.format("%s-SN-%02d-%02d", sku.getSkuCode(), skuIdx, snIdx));
        sn.setSkuId(sku.getId());
        sn.setSpuId(goods.getId());
        sn.setSpuName(goods.getName());
        sn.setSkuCode(sku.getSkuCode());
        sn.setSpecJson(sku.getSpecJson());
        sn.setPrice(sku.getPrice());
        sn.setStatus(0);
        sn.setSource(3);
        sn.setCreatedAt(LocalDateTime.now());
        sn.setUpdatedAt(LocalDateTime.now());
        return sn;
    }

    private String buildRandomSpecJson() {
        try {
            Map<String, String> spec = new LinkedHashMap<>();
            SpecTemplate t = SPECS.get(ThreadLocalRandom.current().nextInt(SPECS.size()));
            spec.put(t.name, t.values.get(ThreadLocalRandom.current().nextInt(t.values.size())));
            SpecTemplate t2 = SPECS.get(ThreadLocalRandom.current().nextInt(SPECS.size()));
            spec.put(t2.name, t2.values.get(ThreadLocalRandom.current().nextInt(t2.values.size())));
            return objectMapper.writeValueAsString(spec);
        } catch (JsonProcessingException e) {
            return "{}";
        }
    }

    private int appendCustomers(int count) {
        for (int i = 0; i < count; i++) {
            Customer customer = buildMockCustomer();
            customerMapper.insert(customer);
            Address addr = buildMockAddress(customer);
            addressMapper.insert(addr);
            if (ThreadLocalRandom.current().nextBoolean()) {
                Address addr2 = buildMockAddress(customer);
                addressMapper.insert(addr2);
            }
        }
        return count;
    }

    private Customer buildMockCustomer() {
        Customer c = new Customer();
        String name = String.valueOf(SURNAMES.charAt(ThreadLocalRandom.current().nextInt(SURNAMES.length())));
        c.setNickname(name + "测试" + (10000 + ThreadLocalRandom.current().nextInt(90000)));
        c.setPhone("139" + String.format("%08d", ThreadLocalRandom.current().nextInt(100000000)));
        int level = ThreadLocalRandom.current().nextInt(1, 5);
        c.setMemberLevel(level);
        c.setBalance(new BigDecimal(ThreadLocalRandom.current().nextInt(100, 50000)).setScale(2));
        c.setPoints(ThreadLocalRandom.current().nextInt(0, 5000));
        c.setTotalConsume(new BigDecimal(ThreadLocalRandom.current().nextInt(100, 20000)).setScale(2));
        c.setTotalPoints(ThreadLocalRandom.current().nextInt(0, 2000));
        c.setStatus(1);
        c.setCreatedAt(LocalDateTime.now());
        c.setUpdatedAt(LocalDateTime.now());
        return c;
    }

    private Address buildMockAddress(Customer customer) {
        int idx = ThreadLocalRandom.current().nextInt(STREETS.size());
        Address addr = new Address();
        addr.setCustomerId(customer.getId());
        addr.setReceiverName(customer.getNickname());
        addr.setPhone(customer.getPhone());
        addr.setProvince("广东省");
        addr.setCity("深圳市");
        addr.setDistrict("南山区");
        addr.setDetail(STREETS.get(idx));
        addr.setIsDefault(0);
        addr.setStatus(1);
        addr.setCreatedAt(LocalDateTime.now());
        addr.setUpdatedAt(LocalDateTime.now());
        return addr;
    }

    private int appendSnCodesOnly(int count) {
        List<Sku> skus = skuMapper.selectList(new LambdaQueryWrapper<>());
        List<Goods> goodsList = goodsMapper.selectList(new LambdaQueryWrapper<>());
        Map<Long, Goods> goodsMap = new HashMap<>();
        for (Goods g : goodsList) goodsMap.put(g.getId(), g);

        for (Sku sku : skus) {
            if (count <= 0) break;
            Goods goods = goodsMap.get(sku.getSpuId());
            if (goods == null) continue;
            int num = Math.min(count, ThreadLocalRandom.current().nextInt(1, 6));
            for (int i = 0; i < num; i++) {
                SnCode sn = new SnCode();
                sn.setSnCode("SN-MOCK-" + buildBatchCode() + "-" + sku.getId() + "-" + i);
                sn.setSkuId(sku.getId());
                sn.setSpuId(goods.getId());
                sn.setSpuName(goods.getName());
                sn.setSkuCode(sku.getSkuCode());
                sn.setSpecJson(sku.getSpecJson());
                sn.setPrice(sku.getPrice());
                sn.setStatus(0);
                sn.setSource(3);
                sn.setCreatedAt(LocalDateTime.now());
                sn.setUpdatedAt(LocalDateTime.now());
                snCodeMapper.insert(sn);
                count--;
            }
        }
        return count;
    }

    private int appendOrders(int count) {
        List<Customer> customers = customerMapper.selectList(new LambdaQueryWrapper<>());
        List<Sku> skus = skuMapper.selectList(new LambdaQueryWrapper<>());
        List<Address> addresses = addressMapper.selectList(new LambdaQueryWrapper<>());
        if (customers.isEmpty() || skus.isEmpty()) return 0;

        int created = 0;
        for (int i = 0; i < count; i++) {
            Customer customer = customers.get(ThreadLocalRandom.current().nextInt(customers.size()));
            Sku sku = skus.get(ThreadLocalRandom.current().nextInt(skus.size()));

            BigDecimal price = sku.getPrice() != null ? sku.getPrice() : new BigDecimal("999.00");
            BigDecimal discount = ThreadLocalRandom.current().nextBoolean() ? BigDecimal.ZERO : new BigDecimal(ThreadLocalRandom.current().nextInt(10, 200)).setScale(2);
            BigDecimal payAmount = price.subtract(discount);
            if (payAmount.compareTo(BigDecimal.ZERO) < 0) payAmount = BigDecimal.ZERO;

            String orderNo = "ORD" + buildBatchCode();
            SaleOrder order = new SaleOrder();
            order.setOrderNo(orderNo);
            order.setCustomerId(customer.getId());
            order.setCustomerName(customer.getNickname());
            order.setTotalAmount(price);
            order.setDiscountAmount(discount);
            order.setPayAmount(payAmount);
            order.setPayType(ThreadLocalRandom.current().nextInt(1, 4));
            LocalDateTime now = LocalDateTime.now();
            order.setPaidAt(now.minusMinutes(5));
            order.setStatus(ThreadLocalRandom.current().nextInt(1, 3));
            order.setReceiverName(customer.getNickname());
            order.setReceiverPhone(customer.getPhone());
            order.setReceiverAddress("广东省 深圳市 南山区 科技园路1号");
            order.setCreatedAt(now);
            order.setUpdatedAt(now);
            orderMapper.insert(order);

            SaleOrderItem item = new SaleOrderItem();
            item.setOrderId(order.getId());
            item.setOrderNo(orderNo);
            item.setSkuId(sku.getId());
            item.setSpuName("模拟商品");
            item.setSkuSpec(sku.getSpecJson());
            item.setPrice(price);
            item.setQuantity(1);
            item.setSubtotal(price);
            item.setCreatedAt(now);
            orderItemMapper.insert(item);
            created++;
        }
        return created;
    }

    private String buildBatchCode() {
        return ORDER_DATE.format(LocalDateTime.now()) + "-" + ThreadLocalRandom.current().nextInt(100, 999);
    }

    private static final class SpecTemplate {
        private final String name;
        private final List<String> values;

        private SpecTemplate(String name, List<String> values) {
            this.name = name;
            this.values = values;
        }
    }
}
