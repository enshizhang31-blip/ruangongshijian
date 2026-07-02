package com.salemanager.modules.customer.service;

import com.salemanager.modules.customer.model.Address;

import java.util.List;

public interface AppAddressService {
    List<Address> list(Long customerId);
    Address create(Long customerId, Address param);
    Address update(Long customerId, Long id, Address param);
    void delete(Long customerId, Long id);
    void setDefault(Long customerId, Long id);
}
