package com.salemanager.modules.customer.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.salemanager.common.exception.BusinessException;
import com.salemanager.modules.customer.mapper.CartItemMapper;
import com.salemanager.modules.customer.model.CartItem;
import com.salemanager.modules.customer.service.AppCartService;
import com.salemanager.modules.product.mapper.SkuMapper;
import com.salemanager.modules.product.model.Sku;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class AppCartServiceImpl extends ServiceImpl<CartItemMapper, CartItem> implements AppCartService {

    private static final Logger log = LoggerFactory.getLogger(AppCartServiceImpl.class);

    @Autowired
    private SkuMapper skuMapper;

    @Override
    @Transactional
    public CartItem addItem(Long customerId, Long spuId, Long skuId, Integer quantity) {
        if (customerId == null) throw new BusinessException("未登录");
        if (skuId == null) throw new BusinessException("缺少 SKU 编号");
        if (quantity == null || quantity <= 0) quantity = 1;

        Sku sku = skuMapper.selectById(skuId);
        if (sku == null) throw new BusinessException("SKU 不存在");
        if (spuId == null) spuId = sku.getSpuId();

        CartItem exist = baseMapper.selectOne(
                new LambdaQueryWrapper<CartItem>()
                        .eq(CartItem::getCustomerId, customerId)
                        .eq(CartItem::getSkuId, skuId));
        if (exist != null) {
            exist.setQuantity(exist.getQuantity() + quantity);
            exist.setSelected(1);
            exist.setUpdatedAt(LocalDateTime.now());
            baseMapper.updateById(exist);
            log.info("购物车累加 customerId={}, skuId={}, qty={}", customerId, skuId, exist.getQuantity());
            return exist;
        }

        CartItem item = new CartItem();
        item.setCustomerId(customerId);
        item.setSkuId(skuId);
        item.setSpuId(spuId);
        item.setQuantity(quantity);
        item.setSelected(1);
        item.setCreatedAt(LocalDateTime.now());
        item.setUpdatedAt(LocalDateTime.now());
        baseMapper.insert(item);
        log.info("购物车新增 customerId={}, skuId={}, qty={}", customerId, skuId, quantity);
        return item;
    }

    @Override
    public List<Map<String, Object>> listDetail(Long customerId) {
        if (customerId == null) throw new BusinessException("未登录");
        return baseMapper.selectCartDetailByCustomerId(customerId);
    }

    @Override
    @Transactional
    public void updateByCustomer(Long customerId, Long id, Integer quantity, Integer selected) {
        CartItem item = requireOwned(customerId, id);
        if (quantity != null && quantity > 0) {
            item.setQuantity(quantity);
        }
        if (selected != null) {
            item.setSelected(selected);
        }
        item.setUpdatedAt(LocalDateTime.now());
        baseMapper.updateById(item);
    }

    @Override
    @Transactional
    public void removeByCustomer(Long customerId, Long id) {
        CartItem item = requireOwned(customerId, id);
        baseMapper.deleteById(item.getId());
        log.info("购物车删除 customerId={}, id={}", customerId, id);
    }

    @Override
    @Transactional
    public void clearByCustomer(Long customerId) {
        baseMapper.delete(new LambdaQueryWrapper<CartItem>().eq(CartItem::getCustomerId, customerId));
    }

    @Override
    public CartItem requireOwned(Long customerId, Long id) {
        CartItem item = baseMapper.selectById(id);
        if (item == null) throw new BusinessException("购物车条目不存在");
        if (!customerId.equals(item.getCustomerId())) {
            throw new BusinessException("无权访问该购物车条目");
        }
        return item;
    }
}
