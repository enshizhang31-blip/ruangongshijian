package com.salemanager.modules.customer.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.salemanager.modules.customer.model.CartItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 购物车 Mapper。
 *
 * cart 表只保存 sku_id，所以需要 join sku 与 goods 表拿到展示字段。
 */
@Mapper
public interface CartItemMapper extends BaseMapper<CartItem> {

    /**
     * 查询某客户购物车所有条目（含 SPU / SKU / 价格 / 图片 / 库存）。
     */
    @Select("""
            SELECT
                c.id            AS id,
                c.customer_id   AS customerId,
                c.sku_id        AS skuId,
                s.spu_id        AS spuId,
                c.quantity      AS quantity,
                c.selected      AS selected,
                s.sku_code      AS skuCode,
                s.spec_json     AS specJson,
                s.price         AS price,
                s.image_url     AS imageUrl,
                g.name          AS spuName,
                (
                    SELECT COUNT(*)
                    FROM sn_code sn
                    WHERE sn.sku_id = s.id AND sn.status = 0
                )               AS stock
            FROM cart c
            JOIN sku s ON s.id = c.sku_id
            JOIN goods g ON g.id = s.spu_id
            WHERE c.customer_id = #{customerId}
            ORDER BY c.updated_at DESC
            """)
    List<Map<String, Object>> selectCartDetailByCustomerId(@Param("customerId") Long customerId);
}
