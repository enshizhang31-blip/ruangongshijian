# 测试规则：测试底线规范

本规则定义了项目必须遵守的测试底线，包括 TDD 流程、测试覆盖率门槛、关键路径必须 E2E 测试等要求。

---

## 1. TDD 开发流程

### 1.1 TDD 基本流程（🔴 必须遵循）

```
红 → 绿 → 重构
```

| 阶段 | 动作 | 产出 |
|------|------|------|
| 🔴 红 | 写一个失败的测试 | 测试代码 |
| 🟢 绿 | 写最少的代码让测试通过 | 产品代码 |
| 🔵 重构 | 重构代码，测试保持通过 | 优质代码 |

### 1.2 TDD 执行步骤

```java
// 1. 🔴 红：先写测试（测试必须失败）
@Test
void shouldReturnUserWhenUserExists() {
    // given
    Long userId = 1L;

    // when
    User result = userService.getUserById(userId);

    // then - 此时测试会失败，因为方法未实现
    assertThat(result).isNotNull();
    assertThat(result.getId()).isEqualTo(1L);
}

// 2. 🟢 绿：写最少的代码让测试通过
public User getUserById(Long id) {
    return new User(); // 最少代码
}

// 3. 🔵 重构：优化代码，测试保持通过
public User getUserById(Long id) {
    return userMapper.selectById(id); // 真实实现
}
```

### 1.3 TDD 适用场景

| 场景 | 是否使用 TDD |
|------|--------------|
| 新功能开发 | ✅ 必须 |
| Bug 修复 | ✅ 必须 |
| 重构 | ✅ 必须 |
| 简单配置修改 | ⚪ 可选 |
| 紧急 Hotfix | ⚪ 可选 |

---

## 2. 测试覆盖率门槛

### 2.1 覆盖率要求

| 指标 | 门槛 | 说明 |
|------|------|------|
| **行覆盖率** | ≥ 80% | 总代码行数中被测试覆盖的比例 |
| **分支覆盖率** | ≥ 70% | if/else、switch 等分支的覆盖 |
| **方法覆盖率** | ≥ 90% | 有测试的方法占总方法的比例 |
| **关键类覆盖率** | ≥ 95% | 核心业务类必须高覆盖 |

### 2.2 覆盖率统计命令

```bash
# Maven + JaCoCo
mvn jacoco:report

# 查看报告
open target/site/jacoco/index.html

# 命令行查看
mvn jacoco:report -Djacoco.report.format=csv
```

### 2.3 覆盖率检查（CI 必须）

```xml
<!-- pom.xml JaCoCo 配置 -->
<plugin>
    <groupId>org.jacoco</groupId>
    <artifactId>jacoco-maven-plugin</artifactId>
    <configuration>
        <rules>
            <rule>
                <element>CLASS</element>
                <limits>
                    <limit>
                        <metric>LINE</metric>
                        <value>0.80</value>
                    </limit>
                    <limit>
                        <metric>BRANCH</metric>
                        <value>0.70</value>
                    </limit>
                </limits>
            </rule>
        </rules>
    </configuration>
</plugin>
```

```yaml
# CI 配置示例（GitHub Actions）
- name: Check Coverage
  run: |
    mvn jacoco:report
    mvn verify -Djacoco.failOnBranchViolation=true
```

---

## 3. 关键路径 E2E 测试

### 3.1 必须 E2E 的关键路径

| 优先级 | 业务场景 | 测试说明 |
|--------|----------|----------|
| 🔴 P0 | 用户登录/注册 | 完整流程，包括验证码 |
| 🔴 P0 | 支付/订单 | 完整资金流转 |
| 🔴 P0 | 权限验证 | 角色切换、权限控制 |
| 🔴 P1 | 核心 CRUD | 增删改查完整流程 |
| 🔴 P1 | 文件上传 | 上传、处理、下载 |
| 🔴 P2 | 搜索功能 | 关键词、高亮、分页 |

### 3.2 E2E 测试示例

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UserE2ETest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserMapper userMapper;

    @BeforeEach
    void setup() {
        // 清理测试数据
        userMapper.deleteAll();
    }

    // 🔴 P0：用户注册完整流程
    @Test
    void shouldRegisterUserSuccessfully() {
        // 1. 发送注册请求
        RegisterRequest request = RegisterRequest.builder()
            .username("testuser")
            .password("Test123456")
            .email("test@example.com")
            .build();

        ResponseEntity<CommonResult> response = restTemplate.postForEntity(
            "/api/auth/register",
            request,
            CommonResult.class
        );

        // 2. 验证响应
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getCode()).isEqualTo(200);

        // 3. 验证数据库数据
        User user = userMapper.findByUsername("testuser");
        assertThat(user).isNotNull();
        assertThat(user.getEmail()).isEqualTo("test@example.com");
        assertThat(user.getPassword()).isNotEqualTo("Test123456"); // 已加密
    }

    // 🔴 P0：用户登录完整流程
    @Test
    void shouldLoginSuccessfully() {
        // 先创建用户
        User user = createTestUser();

        // 登录
        LoginRequest request = LoginRequest.builder()
            .username("testuser")
            .password("Test123456")
            .build();

        ResponseEntity<CommonResult> response = restTemplate.postForEntity(
            "/api/auth/login",
            request,
            CommonResult.class
        );

        // 验证登录成功并返回 Token
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getCode()).isEqualTo(200);
        assertThat(response.getBody().getData()).containsKey("token");
    }
}
```

### 3.3 E2E 测试规范

```java
// ✅ E2E 测试结构
class XxxE2ETest {

    @BeforeEach     // 准备测试数据
    @AfterEach      // 清理测试数据

    // 独立测试：每个测试可以独立运行
    @Test
    void testCase1() { }

    // 数据隔离：使用唯一 ID 或时间戳
    String uniqueId = UUID.randomUUID().toString();
}
```

---

## 4. 测试分层策略

### 4.1 测试金字塔

```
        /\
       /E2E\        ← 少量（10%），关键路径
      /------\
     /Integration\  ← 适量（20%），模块交互
    /------------\
   /   Unit Test  \ ← 大量（70%），核心逻辑
  /----------------\
```

### 4.2 各层测试要求

| 层级 | 数量 | 覆盖内容 | 运行速度 |
|------|------|----------|----------|
| **E2E** | 关键路径 | 完整业务流程 | 慢（秒级） |
| **集成测试** | 核心模块 | 模块间交互 | 中（百毫秒） |
| **单元测试** | 业务逻辑 | 核心算法/方法 | 快（毫秒级） |

### 4.3 单元测试示例

```java
// ✅ 单元测试：测试单个方法
class OrderDiscountCalculatorTest {

    @Test
    void shouldApplyDiscountWhenAmountExceedsThreshold() {
        // given
        BigDecimal amount = new BigDecimal("1000");
        BigDecimal discount = new BigDecimal("0.1");

        // when
        BigDecimal result = DiscountCalculator.calculate(amount, discount);

        // then
        assertThat(result).isEqualTo(new BigDecimal("900"));
    }

    @Test
    void shouldReturnOriginalAmountWhenBelowThreshold() {
        // given
        BigDecimal amount = new BigDecimal("50");

        // when
        BigDecimal result = DiscountCalculator.calculate(amount, new BigDecimal("0.1"));

        // then
        assertThat(result).isEqualTo(new BigDecimal("50")); // 无折扣
    }
}
```

---

## 5. 测试质量标准

### 5.1 测试命名规范

```java
// ✅ 好的命名：描述业务场景
class UserServiceTest {

    @Test
    void shouldReturnUserWhenUserExists() { }

    @Test
    void shouldThrowExceptionWhenUserNotFound() { }

    @Test
    void shouldEncryptPasswordBeforeSaving() { }
}

// ❌ 差的命名
@Test
void test1() { }

@Test
void testSave() { }
```

### 5.2 测试结构（AAA 模式）

```java
@Test
void shouldDoSomething() {
    // Arrange：准备数据
    User user = User.builder()
        .name("test")
        .age(20)
        .build();

    // Act：执行操作
    User saved = userService.save(user);

    // Assert：验证结果
    assertThat(saved.getId()).isNotNull();
    assertThat(saved.getName()).isEqualTo("test");
}
```

### 5.3 测试断言要求

```java
// ✅ 有意义的断言
assertThat(user.getId()).isNotNull();
assertThat(user.getName()).isEqualTo("test");
assertThat(user.getStatus()).isEqualTo(UserStatus.ACTIVE);

// ❌ 过于宽松的断言
assertThat(user).isNotNull(); // 几乎没有验证
```

---

## 6. 测试环境配置

### 6.1 测试配置文件

```
src/
├── test/
│   ├── resources/
│   │   ├── application-test.yml    # 测试配置
│   │   ├── application-dev.yml
│   │   └── data/                   # 测试数据
│   │       └── test-data.sql
```

```yaml
# application-test.yml
spring:
  datasource:
    url: jdbc:h2:mem:testdb
    driver-class-name: org.h2.Driver
  jpa:
    hibernate:
      ddl-auto: create-drop
```

### 6.2 测试数据管理

```java
// 使用 @Sql 加载测试数据
@Sql(scripts = "classpath:data/test-users.sql")
@Sql(scripts = "classpath:data/cleanup.sql", executionPhase = ExecutionPhase.AFTER_TEST_METHOD)
@Test
void shouldFindUser() { }
```

---

## 7. 测试检查清单

### 提交前必须确认

- [ ] 新功能有对应的单元测试（≥80% 覆盖）
- [ ] 关键路径有 E2E 测试
- [ ] Bug 修复有回归测试
- [ ] 测试可以通过（本地 CI）
- [ ] 测试命名清晰，描述业务场景
- [ ] 测试是独立的，不依赖执行顺序
- [ ] 测试数据已清理

### CI 必须检查

- [ ] 单元测试全部通过
- [ ] E2E 测试全部通过
- [ ] 覆盖率达标（行≥80%，分支≥70%）
- [ ] 无测试超时

---

## 8. 禁止模式

| 禁止模式 | 说明 | 正确做法 |
|----------|------|----------|
| 无测试 | 新功能无测试 | 补充测试 |
| 测试通过不验证 | assertThat(true) | 具体断言 |
| 顺序依赖 | 测试依赖执行顺序 | 测试独立 |
| 共享状态 | 静态变量污染 | 每个测试独立数据 |
| 硬编码路径 | 绝对路径 | 使用相对路径或配置 |

---

**记住**：测试是代码质量的守护者。没有测试覆盖的代码 = 潜在 Bug。
