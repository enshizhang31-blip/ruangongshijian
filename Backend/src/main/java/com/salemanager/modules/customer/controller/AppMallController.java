package com.salemanager.modules.customer.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.salemanager.common.exception.BusinessException;
import com.salemanager.common.result.Result;
import com.salemanager.modules.customer.mapper.CustomerMapper;
import com.salemanager.modules.customer.model.Address;
import com.salemanager.modules.customer.model.Customer;
import com.salemanager.modules.customer.service.AppAddressService;
import com.salemanager.modules.customer.service.AppCartService;
import com.salemanager.modules.customer.service.AppOrderService;
import com.salemanager.modules.product.mapper.GoodsCategoryMapper;
import com.salemanager.modules.product.mapper.SkuMapper;
import com.salemanager.modules.product.mapper.SpecNameMapper;
import com.salemanager.modules.product.mapper.SpecValueMapper;
import com.salemanager.modules.product.model.Goods;
import com.salemanager.modules.product.model.GoodsCategory;
import com.salemanager.modules.product.model.Sku;
import com.salemanager.modules.product.model.SpecName;
import com.salemanager.modules.product.model.SpecValue;
import com.salemanager.modules.product.service.ProductService;
import com.salemanager.modules.product.service.SkuService;
import com.salemanager.modules.sn.mapper.SnCodeMapper;
import com.salemanager.modules.sn.model.SnCode;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 小程序 (C 端) 接口：
 *  - /api/app/category/list / tree
 *  - /api/app/spu/list / detail
 *  - /api/app/sku/list / detail
 *  - /api/app/cart/...
 *  - /api/app/order/...
 *  - /api/app/address/...
 *  - /api/app/member/info
 *  - /api/app/sn/query
 *
 * 所有需要 customer 的接口都从 request.getAttribute("userId") 读取，
 * 即 JwtAuthFilter 解析 Token 后注入的 customerId。
 */
@RestController
@RequestMapping("/api/app")
public class AppMallController {

    private static final Logger log = LoggerFactory.getLogger(AppMallController.class);

    @Autowired private GoodsCategoryMapper goodsCategoryMapper;
    @Autowired private ProductService productService;
    @Autowired private SkuService skuService;
    @Autowired private AppCartService appCartService;
    @Autowired private AppOrderService appOrderService;
    @Autowired private AppAddressService appAddressService;
    @Autowired private CustomerMapper customerMapper;
    @Autowired private SkuMapper skuMapper;
    @Autowired private SnCodeMapper snCodeMapper;
    @Autowired private SpecNameMapper specNameMapper;
    @Autowired private SpecValueMapper specValueMapper;

    // ======================= 分类 / 商品 =======================

    @GetMapping("/category/tree")
    public Result<List<Map<String, Object>>> categoryTree() {
        List<GoodsCategory> all = goodsCategoryMapper.selectList(
                new LambdaQueryWrapper<GoodsCategory>().orderByAsc(GoodsCategory::getSort));
        return Result.success(buildTree(all));
    }

    @GetMapping("/category/list")
    public Result<List<GoodsCategory>> categoryList() {
        List<GoodsCategory> all = goodsCategoryMapper.selectList(
                new LambdaQueryWrapper<GoodsCategory>().orderByAsc(GoodsCategory::getSort));
        return Result.success(all);
    }

    @GetMapping("/spu/list")
    public Result<Map<String, Object>> spuList(@RequestParam(required = false) String keyword,
                                               @RequestParam(required = false) Long categoryId,
                                               @RequestParam(defaultValue = "1") Integer page,
                                               @RequestParam(defaultValue = "20") Integer pageSize) {
        // 仅展示上架(1)的 SPU
        List<Goods> list = productService.getProductList(keyword, categoryId, 1, page, pageSize);
        Long total = productService.getProductCount(keyword, categoryId, 1);

        List<Map<String, Object>> data = new ArrayList<>();
        for (Goods g : list) {
            Map<String, Object> vo = new HashMap<>();
            vo.put("id", g.getId());
            vo.put("name", g.getName());
            vo.put("categoryId", g.getCategoryId());
            vo.put("brand", g.getBrand());
            vo.put("imageUrl", g.getImageUrl());
            vo.put("shortDesc", g.getShortDesc());
            vo.put("description", g.getDescription());
            vo.put("minPrice", minPriceOfSku(g.getId()));
            vo.put("stockCount", g.getStockCount() == null ? 0 : g.getStockCount());
            vo.put("skuCount", g.getSkuCount() == null ? 0 : g.getSkuCount());
            data.add(vo);
        }

        Map<String, Object> pageObj = new HashMap<>();
        pageObj.put("total", total == null ? 0 : total);
        pageObj.put("list", data);
        pageObj.put("page", page);
        pageObj.put("pageSize", pageSize);
        return Result.success(pageObj);
    }

    @GetMapping("/spu/detail/{id}")
    public Result<Map<String, Object>> spuDetail(@PathVariable Long id) {
        Goods g = productService.getProductById(id);
        if (g == null || (g.getStatus() != null && g.getStatus() != 1)) {
            // 小程序允许查看下架（demo）但 status 显式返回
        }
        List<Sku> skus = skuService.getSkuListBySpuId(id);
        // 加载规格名 / 规格值映射
        java.util.Map<Long, String> nameMap = null;
        java.util.Map<Long, String> valueMap = null;
        try {
            java.util.Map<String, java.util.Map<Long, String>> specMaps = loadSpecMaps(skus);
            nameMap = specMaps.get("names");
            valueMap = specMaps.get("values");
        } catch (Exception ex) {
            log.warn("loadSpecMaps failed: {}", ex.getMessage());
        }
        List<Map<String, Object>> skuVos = new ArrayList<>();
        for (Sku s : skus) {
            skuVos.add(toSkuVO(s, nameMap, valueMap));
        }
        Map<String, Object> vo = new HashMap<>();
        vo.put("id", g.getId());
        vo.put("name", g.getName());
        vo.put("categoryId", g.getCategoryId());
        vo.put("brand", g.getBrand());
        vo.put("imageUrl", g.getImageUrl());
        vo.put("images", g.getImages());
        vo.put("shortDesc", g.getShortDesc());
        vo.put("description", g.getDescription());
        vo.put("status", g.getStatus());
        // 补齐库存 / 销量 / 价格 / skuCount，便于前端展示
        vo.put("stock", g.getStockCount() == null ? 0 : g.getStockCount());
        vo.put("stockCount", g.getStockCount() == null ? 0 : g.getStockCount());
        vo.put("salesCount", g.getSalesCount() == null ? 0 : g.getSalesCount());
        vo.put("skuCount", g.getSkuCount() == null ? 0 : g.getSkuCount());
        if (skus.size() > 0) {
            vo.put("price", skus.get(0).getPrice());
            vo.put("minPrice", skus.stream().map(s -> s.getPrice() == null ? 0.0 : s.getPrice().doubleValue())
                    .min(Double::compare).orElse(0.0));
        } else {
            vo.put("price", 0.0);
            vo.put("minPrice", 0.0);
        }
        // 拼装 specs: [{id, name, values: [{id, value}]}]
        // 顺序按 spec_name.sort 升序
        List<Map<String, Object>> specs = new ArrayList<>();
        if (nameMap != null && valueMap != null) {
            try {
                // 收集该 SPU 实际用到的 specIds
                java.util.Set<Long> usedSpecIds = new java.util.HashSet<>();
                com.fasterxml.jackson.databind.ObjectMapper om = new com.fasterxml.jackson.databind.ObjectMapper();
                for (Sku s : skus) {
                    String sj = s.getSpecJson();
                    if (sj == null || sj.isEmpty()) continue;
                    try {
                        java.util.Map<String, String> m = om.readValue(sj, java.util.Map.class);
                        if (m != null) for (String k : m.keySet()) {
                            try { usedSpecIds.add(Long.valueOf(k)); } catch (Exception ignore) {}
                        }
                    } catch (Exception ignore) {}
                }
                if (!usedSpecIds.isEmpty()) {
                    List<SpecName> specNames = specNameMapper.selectBatchIds(usedSpecIds);
                    // 按 sort 升序、id 升序
                    specNames.sort((a, b) -> {
                        Integer sa = a.getSort() == null ? 0 : a.getSort();
                        Integer sb = b.getSort() == null ? 0 : b.getSort();
                        int c = Integer.compare(sa, sb);
                        if (c != 0) return c;
                        return Long.compare(a.getId() == null ? 0L : a.getId(), b.getId() == null ? 0L : b.getId());
                    });
                    for (SpecName sn : specNames) {
                        Map<String, Object> sVo = new HashMap<>();
                        sVo.put("id", sn.getId());
                        sVo.put("name", sn.getName());
                        // 收集这个 specName 下被 SKU 用到的 valueIds
                        java.util.Set<Long> usedValueIds = new java.util.HashSet<>();
                        for (Sku s : skus) {
                            String sj = s.getSpecJson();
                            if (sj == null || sj.isEmpty()) continue;
                            try {
                                java.util.Map<String, String> m = om.readValue(sj, java.util.Map.class);
                                if (m != null) {
                                    String v = m.get(String.valueOf(sn.getId()));
                                    if (v != null) {
                                        try { usedValueIds.add(Long.valueOf(v)); } catch (Exception ignore) {}
                                    }
                                }
                            } catch (Exception ignore) {}
                        }
                        List<Map<String, Object>> values = new ArrayList<>();
                        if (!usedValueIds.isEmpty()) {
                            List<SpecValue> specValues = specValueMapper.selectBatchIds(usedValueIds);
                            // 按 sort 升序、id 升序
                            specValues.sort((a, b) -> {
                                Integer sa = a.getSort() == null ? 0 : a.getSort();
                                Integer sb = b.getSort() == null ? 0 : b.getSort();
                                int c = Integer.compare(sa, sb);
                                if (c != 0) return c;
                                return Long.compare(a.getId() == null ? 0L : a.getId(), b.getId() == null ? 0L : b.getId());
                            });
                            for (SpecValue sv : specValues) {
                                Map<String, Object> vVo = new HashMap<>();
                                vVo.put("id", sv.getId());
                                vVo.put("value", sv.getValue());
                                values.add(vVo);
                            }
                        }
                        sVo.put("values", values);
                        specs.add(sVo);
                    }
                }
            } catch (Exception ex) {
                log.warn("build specs failed: {}", ex.getMessage());
            }
        }
        vo.put("specs", specs);
        vo.put("skus", skuVos);
        return Result.success(vo);
    }

    @GetMapping("/sku/list")
    public Result<List<Map<String, Object>>> skuList(@RequestParam Long spuId) {
        List<Sku> skus = skuService.getSkuListBySpuId(spuId);
        List<Map<String, Object>> result = new ArrayList<>();
        for (Sku s : skus) result.add(toSkuVO(s));
        return Result.success(result);
    }

    @GetMapping("/sku/detail/{id}")
    public Result<Map<String, Object>> skuDetail(@PathVariable Long id) {
        Sku s = skuMapper.selectById(id);
        if (s == null) throw new BusinessException("SKU 不存在");
        return Result.success(toSkuVO(s));
    }

    // ======================= 购物车 =======================

    @GetMapping("/cart/list")
    public Result<List<Map<String, Object>>> cartList(HttpServletRequest req) {
        return Result.success(appCartService.listDetail(customerId(req)));
    }

    @PostMapping("/cart/add")
    public Result<Object> cartAdd(HttpServletRequest req, @RequestBody Map<String, Object> body) {
        Long spuId = toLong(body.get("spuId"));
        Long skuId = toLong(body.get("skuId"));
        Integer quantity = toInt(body.get("quantity"));
        if (skuId == null) throw new BusinessException("缺少 SKU 编号");
        return Result.success(appCartService.addItem(customerId(req), spuId, skuId, quantity));
    }

    @PostMapping("/cart/update")
    public Result<Object> cartUpdate(HttpServletRequest req, @RequestBody Map<String, Object> body) {
        Long id = toLong(body.get("id"));
        Integer quantity = toInt(body.get("quantity"));
        Integer selected = toInt(body.get("selected"));
        appCartService.updateByCustomer(customerId(req), id, quantity, selected);
        return Result.success(null);
    }

    @DeleteMapping("/cart/remove/{id}")
    public Result<Object> cartRemove(HttpServletRequest req, @PathVariable Long id) {
        appCartService.removeByCustomer(customerId(req), id);
        return Result.success(null);
    }

    @PostMapping("/cart/select-all")
    public Result<Object> cartSelectAll(HttpServletRequest req, @RequestBody Map<String, Object> body) {
        Boolean selected = (Boolean) body.get("selected");
        Integer flag = Boolean.TRUE.equals(selected) ? 1 : 0;
        List<Map<String, Object>> items = appCartService.listDetail(customerId(req));
        for (Map<String, Object> it : items) {
            Object cid = it.get("id");
            if (cid == null) continue;
            try {
                appCartService.updateByCustomer(customerId(req), Long.valueOf(cid.toString()), null, flag);
            } catch (Exception ignore) {}
        }
        return Result.success(null);
    }

    // ======================= 订单 =======================

    @GetMapping("/order/list")
    public Result<List<Map<String, Object>>> orderList(HttpServletRequest req,
                                                       @RequestParam(required = false) Integer status,
                                                       @RequestParam(defaultValue = "1") Integer page,
                                                       @RequestParam(defaultValue = "20") Integer pageSize) {
        return Result.success(appOrderService.listByCustomer(customerId(req), status, page, pageSize));
    }

    @GetMapping("/order/detail/{id}")
    public Result<Map<String, Object>> orderDetail(HttpServletRequest req, @PathVariable Long id) {
        return Result.success(appOrderService.getDetail(customerId(req), id));
    }

    @PostMapping("/order/create")
    public Result<Map<String, Object>> orderCreate(HttpServletRequest req, @RequestBody Map<String, Object> body) {
        AppOrderService.CreateOrderParam param = new AppOrderService.CreateOrderParam();
        param.items = new java.util.ArrayList<>();
        param.addressId = toLong(body.get("addressId"));
        Object itemsObj = body.get("items");
        if (itemsObj instanceof List) {
            List<?> raw = (List<?>) itemsObj;
            for (Object o : raw) {
                if (o instanceof Map) {
                    Map<?, ?> m = (Map<?, ?>) o;
                    AppOrderService.Item it = new AppOrderService.Item();
                    it.spuId = toLong(m.get("spuId"));
                    it.skuId = toLong(m.get("skuId"));
                    it.quantity = toInt(m.get("quantity"));
                    param.items.add(it);
                }
            }
        }
        Long orderId = appOrderService.createOrder(customerId(req), param);
        Map<String, Object> r = new HashMap<>();
        r.put("orderId", orderId);
        return Result.success(r);
    }

    @PostMapping("/order/pay")
    public Result<Object> orderPay(HttpServletRequest req, @RequestBody Map<String, Object> body) {
        Long id = toLong(body.get("orderId"));
        Integer payType = toInt(body.get("payType"));
        if (id == null) throw new BusinessException("缺少 orderId");
        appOrderService.payOrder(customerId(req), id, payType);
        return Result.success(null);
    }

    @PostMapping("/order/cancel")
    public Result<Object> orderCancel(HttpServletRequest req, @RequestBody Map<String, Object> body) {
        Long id = toLong(body.get("orderId"));
        if (id == null) throw new BusinessException("缺少 orderId");
        appOrderService.cancelOrder(customerId(req), id);
        return Result.success(null);
    }

    @PostMapping("/order/confirm-receive")
    public Result<Object> orderReceive(HttpServletRequest req, @RequestBody Map<String, Object> body) {
        Long id = toLong(body.get("orderId"));
        if (id == null) throw new BusinessException("缺少 orderId");
        appOrderService.deliverThenReceive(customerId(req), id);
        return Result.success(null);
    }

    @PostMapping("/order/ship")
    public Result<Object> orderShip(HttpServletRequest req, @RequestBody Map<String, Object> body) {
        Long id = toLong(body.get("orderId"));
        if (id == null) throw new BusinessException("缺少 orderId");
        appOrderService.shipOrder(customerId(req), id);
        return Result.success(null);
    }

    @PostMapping("/order/refund-complete")
    public Result<Object> orderRefundComplete(HttpServletRequest req, @RequestBody Map<String, Object> body) {
        Long id = toLong(body.get("orderId"));
        if (id == null) throw new BusinessException("缺少 orderId");
        appOrderService.manualCompleteRefund(customerId(req), id);
        return Result.success(null);
    }

    @PostMapping("/order/refund")
    public Result<Map<String, Object>> orderRefund(HttpServletRequest req, @RequestBody Map<String, Object> body) {
        Long id = toLong(body.get("orderId"));
        String reason = body.get("reason") == null ? null : body.get("reason").toString();
        if (id == null) throw new BusinessException("缺少 orderId");
        Long refundId = appOrderService.applyRefund(customerId(req), id, reason);
        Map<String, Object> r = new HashMap<>();
        r.put("refundOrderId", refundId);
        return Result.success(r);
    }

    // ======================= 地址 =======================

    @GetMapping("/address/list")
    public Result<List<Address>> addressList(HttpServletRequest req) {
        return Result.success(appAddressService.list(customerId(req)));
    }

    @PostMapping("/address/create")
    public Result<Address> addressCreate(HttpServletRequest req, @RequestBody Address body) {
        return Result.success(appAddressService.create(customerId(req), body));
    }

    @PostMapping("/address/update/{id}")
    public Result<Address> addressUpdate(HttpServletRequest req, @PathVariable Long id, @RequestBody Address body) {
        return Result.success(appAddressService.update(customerId(req), id, body));
    }

    @DeleteMapping("/address/remove/{id}")
    public Result<Object> addressRemove(HttpServletRequest req, @PathVariable Long id) {
        appAddressService.delete(customerId(req), id);
        return Result.success(null);
    }

    @PostMapping("/address/default/{id}")
    public Result<Object> addressDefault(HttpServletRequest req, @PathVariable Long id) {
        appAddressService.setDefault(customerId(req), id);
        return Result.success(null);
    }

    // ======================= 会员 / 个人信息 =======================

    @GetMapping("/member/info")
    public Result<Map<String, Object>> memberInfo(HttpServletRequest req) {
        Long cid = customerId(req);
        Customer c = customerMapper.selectById(cid);
        if (c == null) throw new BusinessException("会员不存在");
        Map<String, Object> result = new HashMap<>();
        result.put("id", c.getId());
        result.put("nickname", c.getNickname());
        result.put("phone", c.getPhone());
        result.put("avatar", c.getAvatar());
        result.put("memberLevel", c.getMemberLevel());
        result.put("balance", c.getBalance());
        result.put("points", c.getPoints());
        result.put("totalConsume", c.getTotalConsume());
        result.put("totalPoints", c.getTotalPoints());
        result.put("status", c.getStatus());
        return Result.success(result);
    }

    // ======================= SN 公开查询 =======================

    @GetMapping("/sn/query")
    public Result<List<Map<String, Object>>> snQuery(@RequestParam(required = false) String sn,
                                                    @RequestParam(required = false) Long skuId,
                                                    @RequestParam(defaultValue = "20") Integer limit) {
        LambdaQueryWrapper<SnCode> w = new LambdaQueryWrapper<>();
        if (sn != null && !sn.isEmpty()) w.eq(SnCode::getSnCode, sn);
        if (skuId != null) w.eq(SnCode::getSkuId, skuId);
        w.last("LIMIT " + Math.max(1, Math.min(limit, 100)));
        List<SnCode> list = snCodeMapper.selectList(w);
        List<Map<String, Object>> data = new ArrayList<>();
        for (SnCode c : list) {
            Map<String, Object> m = new HashMap<>();
            m.put("id", c.getId());
            m.put("snCode", c.getSnCode());
            m.put("skuId", c.getSkuId());
            m.put("status", c.getStatus());
            m.put("producedAt", c.getInboundAt());
            m.put("createdAt", c.getCreatedAt());
            data.add(m);
        }
        return Result.success(data);
    }

    // ============================== 私有辅助 ==============================

    private Long customerId(HttpServletRequest req) {
        Object v = req.getAttribute("userId");
        if (v == null) throw new BusinessException("未登录或会话已过期");
        return Long.valueOf(v.toString());
    }

    private Map<String, Object> toSkuVO(Sku s) {
        return toSkuVO(s, null, null);
    }

    private Map<String, Object> toSkuVO(Sku s, java.util.Map<Long, String> specNameMap, java.util.Map<Long, String> specValueMap) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", s.getId());
        m.put("spuId", s.getSpuId());
        m.put("skuCode", s.getSkuCode());
        m.put("specJson", s.getSpecJson());
        m.put("price", s.getPrice());
        m.put("costPrice", s.getCostPrice());
        m.put("unit", s.getUnit());
        m.put("imageUrl", s.getImageUrl());
        m.put("status", s.getStatus());
        m.put("stock", s.getStock());
        m.put("specText", buildSpecText(s.getSpecJson(), specNameMap, specValueMap));
        return m;
    }

    /**
     * 将 specJson（如 {"1":"1","2":"3"}）解析成"颜色:红色 / 尺码:XL"
     */
    private String buildSpecText(String specJson, java.util.Map<Long, String> specNameMap, java.util.Map<Long, String> specValueMap) {
        if (specJson == null || specJson.isEmpty() || "null".equalsIgnoreCase(specJson.trim())) return "";
        if (specNameMap == null || specValueMap == null) return "";
        // 使用 Jackson 解析
        try {
            com.fasterxml.jackson.databind.ObjectMapper om = new com.fasterxml.jackson.databind.ObjectMapper();
            java.util.Map<String, String> map = om.readValue(specJson, java.util.Map.class);
            if (map == null || map.isEmpty()) return "";
            java.util.List<String> parts = new java.util.ArrayList<>();
            for (java.util.Map.Entry<String, String> e : map.entrySet()) {
                Long specId = Long.valueOf(e.getKey());
                Long valueId = Long.valueOf(e.getValue());
                String specName = specNameMap.getOrDefault(specId, "");
                String specValue = specValueMap.getOrDefault(valueId, "");
                if (specName == null) specName = "";
                if (specValue == null) specValue = "";
                if (specName.isEmpty() && specValue.isEmpty()) continue;
                if (specName.isEmpty()) {
                    parts.add(specValue);
                } else if (specValue.isEmpty()) {
                    parts.add(specName);
                } else {
                    parts.add(specName + ":" + specValue);
                }
            }
            return String.join(" / ", parts);
        } catch (Exception ex) {
            log.warn("parse specJson failed: {}", ex.getMessage());
            return specJson;
        }
    }

    /**
     * 从一批 SKU 提取出用到的所有 specId / valueId，并查库得到名/值映射
     */
    private java.util.Map<String, java.util.Map<Long, String>> loadSpecMaps(List<Sku> skus) {
        java.util.Set<Long> specIds = new java.util.HashSet<>();
        java.util.Set<Long> valueIds = new java.util.HashSet<>();
        com.fasterxml.jackson.databind.ObjectMapper om = new com.fasterxml.jackson.databind.ObjectMapper();
        for (Sku s : skus) {
            String specJson = s.getSpecJson();
            if (specJson == null || specJson.isEmpty() || "null".equalsIgnoreCase(specJson.trim())) continue;
            try {
                java.util.Map<String, String> map = om.readValue(specJson, java.util.Map.class);
                if (map != null) {
                    for (java.util.Map.Entry<String, String> e : map.entrySet()) {
                        try {
                            specIds.add(Long.valueOf(e.getKey()));
                            valueIds.add(Long.valueOf(e.getValue()));
                        } catch (Exception ignore) {}
                    }
                }
            } catch (Exception ignore) {}
        }
        java.util.Map<Long, String> nameMap = new java.util.HashMap<>();
        if (!specIds.isEmpty()) {
            List<SpecName> names = specNameMapper.selectBatchIds(specIds);
            for (SpecName n : names) nameMap.put(n.getId(), n.getName());
        }
        java.util.Map<Long, String> valueMap = new java.util.HashMap<>();
        if (!valueIds.isEmpty()) {
            List<SpecValue> values = specValueMapper.selectBatchIds(valueIds);
            for (SpecValue v : values) valueMap.put(v.getId(), v.getValue());
        }
        java.util.Map<String, java.util.Map<Long, String>> out = new java.util.HashMap<>();
        out.put("names", nameMap);
        out.put("values", valueMap);
        return out;
    }

    private BigDecimal minPriceOfSku(Long spuId) {
        if (spuId == null) return null;
        List<Sku> skus = skuService.getSkuListBySpuId(spuId);
        BigDecimal min = null;
        for (Sku s : skus) {
            if (s.getPrice() != null && (min == null || s.getPrice().compareTo(min) < 0)) {
                min = s.getPrice();
            }
        }
        return min;
    }

    private List<Map<String, Object>> buildTree(List<GoodsCategory> all) {
        // 假设 GoodsCategory 有 id / parentId / name / icon / sort
        Map<Long, List<GoodsCategory>> grouped = new HashMap<>();
        for (GoodsCategory c : all) {
            grouped.computeIfAbsent(c.getParentId() == null ? 0L : c.getParentId(), k -> new ArrayList<>()).add(c);
        }
        return toNodes(0L, grouped);
    }

    private List<Map<String, Object>> toNodes(Long parentId, Map<Long, List<GoodsCategory>> grouped) {
        List<Map<String, Object>> result = new ArrayList<>();
        List<GoodsCategory> children = grouped.getOrDefault(parentId, new ArrayList<>());
        for (GoodsCategory c : children) {
            Map<String, Object> node = new HashMap<>();
            node.put("id", c.getId());
            node.put("parentId", c.getParentId());
            node.put("name", c.getName());
            node.put("icon", c.getIcon());
            node.put("sort", c.getSort());
            node.put("children", toNodes(c.getId(), grouped));
            result.add(node);
        }
        return result;
    }

    private Long toLong(Object o) {
        if (o == null) return null;
        if (o instanceof Number) return ((Number) o).longValue();
        try {
            return Long.valueOf(o.toString());
        } catch (Exception ignored) {}
        return null;
    }

    private Integer toInt(Object o) {
        if (o == null) return null;
        if (o instanceof Number) return ((Number) o).intValue();
        try {
            return Integer.valueOf(o.toString());
        } catch (Exception ignored) {}
        return null;
    }

}
