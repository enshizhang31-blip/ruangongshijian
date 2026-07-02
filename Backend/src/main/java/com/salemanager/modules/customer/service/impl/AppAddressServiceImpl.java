package com.salemanager.modules.customer.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.salemanager.common.exception.BusinessException;
import com.salemanager.modules.customer.mapper.AddressMapper;
import com.salemanager.modules.customer.model.Address;
import com.salemanager.modules.customer.service.AppAddressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AppAddressServiceImpl implements AppAddressService {

    @Autowired
    private AddressMapper addressMapper;

    @Override
    public List<Address> list(Long customerId) {
        if (customerId == null) throw new BusinessException("未登录");
        return addressMapper.selectList(
                new LambdaQueryWrapper<Address>()
                        .eq(Address::getCustomerId, customerId)
                        .orderByDesc(Address::getIsDefault)
                        .orderByDesc(Address::getUpdatedAt));
    }

    @Override
    @Transactional
    public Address create(Long customerId, Address param) {
        if (customerId == null) throw new BusinessException("未登录");
        if (param == null) throw new BusinessException("参数错误");
        validateRequired(param);
        param.setId(null);
        param.setCustomerId(customerId);
        param.setStatus(param.getStatus() == null ? 1 : param.getStatus());
        if (Integer.valueOf(1).equals(param.getIsDefault())) {
            clearDefault(customerId);
        } else {
            param.setIsDefault(0);
        }
        param.setCreatedAt(LocalDateTime.now());
        param.setUpdatedAt(LocalDateTime.now());
        addressMapper.insert(param);
        return param;
    }

    @Override
    @Transactional
    public Address update(Long customerId, Long id, Address param) {
        Address exist = requireOwned(customerId, id);
        if (param == null) throw new BusinessException("参数错误");
        validateRequired(param);
        exist.setReceiverName(nullSafe(param.getReceiverName(), exist.getReceiverName()));
        exist.setPhone(nullSafe(param.getPhone(), exist.getPhone()));
        exist.setProvince(nullSafe(param.getProvince(), exist.getProvince()));
        exist.setCity(nullSafe(param.getCity(), exist.getCity()));
        exist.setDistrict(nullSafe(param.getDistrict(), exist.getDistrict()));
        exist.setDetail(nullSafe(param.getDetail(), exist.getDetail()));
        if (Integer.valueOf(1).equals(param.getIsDefault())) {
            clearDefault(customerId);
            exist.setIsDefault(1);
        }
        if (param.getStatus() != null) exist.setStatus(param.getStatus());
        exist.setUpdatedAt(LocalDateTime.now());
        addressMapper.updateById(exist);
        return exist;
    }

    @Override
    @Transactional
    public void delete(Long customerId, Long id) {
        Address exist = requireOwned(customerId, id);
        addressMapper.deleteById(exist.getId());
    }

    @Override
    @Transactional
    public void setDefault(Long customerId, Long id) {
        Address exist = requireOwned(customerId, id);
        clearDefault(customerId);
        exist.setIsDefault(1);
        exist.setUpdatedAt(LocalDateTime.now());
        addressMapper.updateById(exist);
    }

    private void clearDefault(Long customerId) {
        addressMapper.update(null, new LambdaUpdateWrapper<Address>()
                .eq(Address::getCustomerId, customerId)
                .set(Address::getIsDefault, 0));
    }

    private Address requireOwned(Long customerId, Long id) {
        Address a = addressMapper.selectById(id);
        if (a == null) throw new BusinessException("地址不存在");
        if (!customerId.equals(a.getCustomerId())) {
            throw new BusinessException("无权访问该地址");
        }
        return a;
    }

    private void validateRequired(Address a) {
        if (a.getReceiverName() == null || a.getReceiverName().trim().isEmpty()) {
            throw new BusinessException("收货人姓名不能为空");
        }
        if (a.getPhone() == null || a.getPhone().trim().isEmpty()) {
            throw new BusinessException("手机号不能为空");
        }
        if (a.getProvince() == null || a.getDetail() == null) {
            throw new BusinessException("所在地区与详细地址不能为空");
        }
    }

    private String nullSafe(String newVal, String oldVal) {
        return newVal == null ? oldVal : newVal;
    }
}
