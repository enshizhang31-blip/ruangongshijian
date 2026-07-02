package com.salemanager.modules.customer.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.salemanager.common.exception.BusinessException;
import com.salemanager.common.util.JwtUtil;
import com.salemanager.modules.customer.mapper.CustomerMapper;
import com.salemanager.modules.customer.model.Customer;
import com.salemanager.modules.customer.service.AppAuthService;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class AppAuthServiceImpl implements AppAuthService {

    private static final Logger log = LoggerFactory.getLogger(AppAuthServiceImpl.class);

    private static final String DEMO_SALT = "demo-uniapp-salt";
    private static final String DEMO_PHONE = "demo";
    private static final String DEMO_PASSWORD = "123456";

    @Autowired
    private CustomerMapper customerMapper;

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public Map<String, Object> wechatLogin(String code) {
        if (code == null || code.isEmpty()) {
            throw new BusinessException("授权code不能为空");
        }
        String openid = "wx_" + code;
        Customer customer = customerMapper.selectOne(
                new LambdaQueryWrapper<Customer>().eq(Customer::getOpenid, openid));
        if (customer == null) {
            customer = new Customer();
            customer.setOpenid(openid);
            customer.setNickname("用户" + UUID.randomUUID().toString().substring(0, 8));
            customer.setMemberLevel(1);
            customer.setBalance(BigDecimal.ZERO);
            customer.setPoints(0);
            customer.setTotalConsume(BigDecimal.ZERO);
            customer.setTotalPoints(0);
            customer.setStatus(1);
            customer.setCreatedAt(LocalDateTime.now());
            customer.setUpdatedAt(LocalDateTime.now());
            customerMapper.insert(customer);
            log.info("新会员注册 openid={}, id={}", openid, customer.getId());
        }
        return buildAuthResult(customer);
    }

    @Override
    public Map<String, Object> registerByPhone(String phone, String password, String nickname) {
        if (phone == null || phone.isEmpty()) {
            throw new BusinessException("手机号不能为空");
        }
        if (password == null || password.length() < 6) {
            throw new BusinessException("密码至少 6 位");
        }
        Customer exist = customerMapper.selectOne(
                new LambdaQueryWrapper<Customer>().eq(Customer::getPhone, phone));
        if (exist != null) {
            throw new BusinessException("该手机号已注册");
        }
        Customer customer = new Customer();
        customer.setPhone(phone);
        customer.setPassword(hashPassword(password));
        customer.setNickname(nickname != null && !nickname.isEmpty() ? nickname : "用户" + phone.substring(Math.max(0, phone.length() - 4)));
        customer.setMemberLevel(1);
        customer.setBalance(BigDecimal.ZERO);
        customer.setPoints(0);
        customer.setTotalConsume(BigDecimal.ZERO);
        customer.setTotalPoints(0);
        customer.setStatus(1);
        customer.setCreatedAt(LocalDateTime.now());
        customer.setUpdatedAt(LocalDateTime.now());
        customerMapper.insert(customer);
        log.info("演示版注册成功 phone={}, id={}", phone, customer.getId());
        return buildAuthResult(customer);
    }

    @Override
    public Map<String, Object> loginByPhone(String phone, String password) {
        if (phone == null || password == null) {
            throw new BusinessException("手机号/密码不能为空");
        }
        Customer customer = customerMapper.selectOne(
                new LambdaQueryWrapper<Customer>().eq(Customer::getPhone, phone));
        if (customer == null) {
            throw new BusinessException("账号不存在，请先注册");
        }
        if (customer.getPassword() == null || !customer.getPassword().equals(hashPassword(password))) {
            throw new BusinessException("密码错误");
        }
        if (customer.getStatus() != null && customer.getStatus() != 1) {
            throw new BusinessException("账号已被禁用");
        }
        return buildAuthResult(customer);
    }

    @PostConstruct
    public void seedDemoUser() {
        try {
            Customer exist = customerMapper.selectOne(
                    new LambdaQueryWrapper<Customer>().eq(Customer::getPhone, DEMO_PHONE));
            if (exist == null) {
                Customer demo = new Customer();
                demo.setPhone(DEMO_PHONE);
                demo.setPassword(hashPassword(DEMO_PASSWORD));
                demo.setNickname("演示账号");
                demo.setMemberLevel(4);
                demo.setBalance(new BigDecimal("9999.00"));
                demo.setPoints(9999);
                demo.setTotalConsume(new BigDecimal("5000.00"));
                demo.setTotalPoints(9999);
                demo.setStatus(1);
                demo.setCreatedAt(LocalDateTime.now());
                demo.setUpdatedAt(LocalDateTime.now());
                customerMapper.insert(demo);
                log.info("已创建演示账号 demo / 123456（钻石会员、余额 9999）");
            }
        } catch (Exception e) {
            log.warn("演示账号初始化失败（可忽略）：{}", e.getMessage());
        }
    }

    private Map<String, Object> buildAuthResult(Customer customer) {
        String token = jwtUtil.generateToken(customer.getId(), customer.getNickname());
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("member", customer);
        return result;
    }

    private String hashPassword(String raw) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest((DEMO_SALT + raw).getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            throw new BusinessException("密码哈希失败");
        }
    }
}
