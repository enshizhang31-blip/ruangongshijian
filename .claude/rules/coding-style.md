# 编码风格规则：代码组织底线

本规则定义了代码组织的基本底线，包括文件大小、函数长度、错误处理、命名规范等强制要求。

---

## 1. 文件组织

### 1.1 文件大小限制（🟡 建议）

| 类型           | 最大行数 | 说明                      |
| -------------- | -------- | ------------------------- |
| **Java 类**    | 500 行   | 包含注释和空行            |
| **Vue 组件**   | 300 行   | template + script + style |
| **Controller** | 200 行   | 仅包含路由定义和参数校验  |
| **Service**    | 300 行   | 业务逻辑                  |
| **Mapper**     | 150 行   | 数据库操作                |

### 1.2 文件命名规范

```
# Java
UserController.java      # Controller
UserService.java         # Service
UserServiceImpl.java     # Service 实现
UserMapper.java          # Mapper 接口
User.java                # 实体类
UserVO.java              # View Object
UserDTO.java             # Data Transfer Object

# Vue
UserList.vue             # 列表页
UserForm.vue             # 表单页
UserDetail.vue           # 详情页
useUser.js               # Hooks (use 开头)
```

### 1.3 包结构规范

```
com.salemanager.modules.{module}/
├── controller/
│   └── XxxController.java
├── service/
│   ├── XxxService.java
│   └── impl/
│       └── XxxServiceImpl.java
├── mapper/
│   └── XxxMapper.java
├── model/
│   └── Xxx.java
├── dto/
│   └── XxxDTO.java
├── vo/
│   └── XxxVO.java
├── param/
│   └── XxxParam.java
└── enums/
    └── XxxStatusEnum.java
```

**模块划分示例**：

- `com.salemanager.modules.ums` - 用户管理
- `com.salemanager.modules.product` - 商品管理
- `com.salemanager.modules.customer` - 客户管理
- `com.salemanager.modules.sale` - 销售管理

---

## 2. 函数/方法规范

### 2.1 方法长度限制（🔴 强制）

| 层级                | 最大行数 | 说明               |
| ------------------- | -------- | ------------------ |
| **Controller 方法** | 30 行    | 仅做参数校验和调用 |
| **Service 方法**    | 50 行    | 业务逻辑           |
| **工具方法**        | 20 行    | 纯逻辑处理         |
| **Private 方法**    | 30 行    | 私有辅助方法       |

**超长方法处理**：

```java
// ❌ 超过 50 行的方法
public void processOrder(Order order) {
    // 校验订单 (10行)
    // 校验库存 (10行)
    // 计算价格 (10行)
    // 创建订单 (10行)
    // 发送通知 (10行)
    // ... 共 50+ 行
}

// ✅ 拆分后的方法
public void processOrder(Order order) {
    validateOrder(order);
    checkInventory(order);
    calculatePrice(order);
    createOrder(order);
    sendNotification(order);
}

private void validateOrder(Order order) { /* ... */ }
private void checkInventory(Order order) { /* ... */ }
private void calculatePrice(Order order) { /* ... */ }
private void createOrder(Order order) { /* ... */ }
private void sendNotification(Order order) { /* ... */ }
```

### 2.2 方法参数限制（🟡 建议）

```java
// ✅ 最佳：0-3 个参数
public User getUserById(Long id) { }

public User createUser(String name, String email) { }

// 建议：4 个以内
public Result createUser(String name, String email, String phone, String address) { }

// ⚠️ 警告：超过 4 个参数，考虑拆分
public User createUser(UserCreateParam param) { }
```

### 2.3 方法职责单一（🔴 强制）

```java
// ❌ 一个方法做多件事
public void saveUserAndSendEmailAndLog() {
    saveUser();
    sendEmail();
    log();
}

// ✅ 单一职责
public void saveUser(User user) { }
public void sendWelcomeEmail(User user) { }
public void logUserCreation(User user) { }
```

---

## 3. 错误处理规范

### 3.1 异常处理原则（🔴 强制）

| 原则             | 说明                       |
| ---------------- | -------------------------- |
| **捕获具体异常** | 避免 `catch (Exception e)` |
| **不吞掉异常**   | 至少记录日志               |
| **异常上抛**     | 业务异常抛出给上层         |
| **统一异常处理** | 使用 `@ControllerAdvice`   |

```java
// ✅ 正确：捕获具体异常
try {
    userMapper.insert(user);
} catch (DuplicateKeyException e) {
    throw new BusinessException("用户名已存在");
} catch (DataAccessException e) {
    log.error("数据库错误", e);
    throw new BusinessException("保存失败，请稍后重试");
}

// ❌ 错误：捕获所有异常
try {
    userMapper.insert(user);
} catch (Exception e) {
    // 吞掉异常 - 危险！
}

// ❌ 错误：捕获 Throwable
try {
    userMapper.insert(user);
} catch (Throwable t) {
    throw new RuntimeException(t);
}
```

### 3.2 返回值规范（🔴 强制）

```java
// ✅ 返回 Result 而非 null
public Result<User> getUserById(Long id) {
    User user = userMapper.selectById(id);
    if (user == null) {
        return Result.fail("用户不存在");
    }
    return Result.success(user);
}

// ❌ 返回 null 导致 NPE
public User getUserById(Long id) {
    return userMapper.selectById(id); // 可能返回 null
}
```

### 3.3 业务异常定义

```java
// 业务异常类
public class BusinessException extends RuntimeException {
    private String code;

    public BusinessException(String message) {
        super(message);
        this.code = "BUSINESS_ERROR";
    }

    public BusinessException(String code, String message) {
        super(message);
        this.code = code;
    }
}

// 使用示例
if (user == null) {
    throw new BusinessException("USER_NOT_FOUND", "用户不存在");
}
```

---

## 4. 命名规范

### 4.1 命名原则

| 类型       | 规则         | 示例                             |
| ---------- | ------------ | -------------------------------- |
| **类名**   | 大驼峰，名词 | `UserService`, `OrderController` |
| **方法名** | 小驼峰，动词 | `getUserById`, `saveOrder`       |
| **变量名** | 小驼峰       | `userName`, `orderList`          |
| **常量**   | 全大写下划线 | `MAX_RETRY_COUNT`                |
| **枚举**   | 大驼峰       | `UserStatusEnum`                 |

### 4.2 命名规范

```java
// ✅ 好的命名
private UserService userService;
private List<User> userList;
private static final int MAX_RETRY = 3;

// ❌ 差的命名
private UserService us;        // 缩写
private List<User> u;          // 无意义
private static final int MR;   // 无意义
```

### 4.3 命名禁用词

```java
// ❌ 禁止使用
temp, tmp          // 临时变量
data, obj          // 模糊命名
a, b, c            // 单字母
flag               // 使用 hasXxx/isXxx
```

---

## 5. 代码格式

### 5.1 缩进和空格（🔴 强制）

```java
// ✅ 正确格式
public void method() {
    if (condition) {
        doSomething();
    }
}

// ❌ 错误格式
public void method(){
if(condition){
doSomething();
}
}
```

### 5.2 空行规范

```java
public class UserService {

    // 类之间空一行
    @Autowired
    private UserMapper userMapper;

    // 方法之间空一行
    public User getUserById(Long id) {
        return userMapper.selectById(id);
    }

    public User createUser(User user) {
        return userMapper.insert(user);
    }

    // 私有方法之间空一行
    private void validateUser(User user) { }

    private void logCreation(User user) { }
}
```

### 5.3 行长度限制（🟡 建议）

- 每行不超过 **120** 个字符
- 超长换行对齐

```java
// ✅ 合理换行
String url = "https://api.example.com/v1/users"
    + "?page=" + page
    + "&size=" + size;
```

---

## 6. 注释规范

### 6.1 必须注释的情况（🔴 强制）

```java
/**
 * 处理用户订单
 *
 * @param orderId 订单ID
 * @return 处理结果
 */
public Result processOrder(Long orderId) { }

// 业务逻辑复杂的必须注释
// 校验用户VIP状态和优惠券是否可用
if (user.isVIP() && coupon.isAvailable()) {
    // VIP 用户享受 8 折优惠
    price = price.multiply(new BigDecimal("0.8"));
}
```

### 6.2 禁止注释的情况

```java
// ❌ 禁止：显而易见
// 循环遍历用户列表
for (User user : userList) { }

// ❌ 禁止：注释掉的代码
// if (user.isAdmin()) {
//     return adminPage;
// }
```

---

## 7. 日志规范

### 7.1 日志级别使用

| 级别      | 使用场景               |
| --------- | ---------------------- |
| **ERROR** | 业务异常需要处理       |
| **WARN**  | 可恢复的异常或需要关注 |
| **INFO**  | 关键业务流程           |
| **DEBUG** | 开发调试信息           |

```java
// ✅ 正确使用
log.info("用户登录成功, userId={}", userId);
log.warn("库存不足, productId={}, currentStock={}", productId, stock);
log.error("订单创建失败", e);
```

### 7.2 日志内容规范

```java
// ✅ 包含关键信息
log.info("创建订单成功, orderId={}, userId={}", orderId, userId);

// ❌ 敏感信息不记录
log.info("用户密码: {}", password);  // 禁止！
log.info("Token: {}", token);        // 禁止！
```

---

## 8. 检查清单

### 提交前确认

- [ ] 文件行数未超标（Java ≤500 行，Vue ≤300 行）
- [ ] 方法长度未超标（≤50 行）
- [ ] 方法参数 ≤4 个
- [ ] 异常捕获具体，不吞异常
- [ ] 返回值不为 null（使用 Result 包装）
- [ ] 命名符合规范
- [ ] 关键逻辑有注释
- [ ] 日志不含敏感信息

### 代码审查关注点

- [ ] 无重复代码（超过 3 处需抽取）
- [ ] 无硬编码（常量/配置外置）
- [ ] 无空指针风险
- [ ] 无资源泄漏（文件/连接）
- [ ] 事务边界合理

---

## 9. 禁止模式

| 禁止       | 示例                   | 正确             |
| ---------- | ---------------------- | ---------------- |
| 魔法数字   | `if (count > 100)`     | `MAX_COUNT` 常量 |
| 硬编码路径 | `"/usr/local/data"`    | 配置读取         |
| 过长方法   | 100+ 行方法            | 拆分方法         |
| 空方法体   | `// TODO`              | 实现或删除       |
| System.out | `System.out.println()` | 使用日志         |

---

**记住**：代码是给人看的，其次才是给机器执行。清晰 > 聪明。
