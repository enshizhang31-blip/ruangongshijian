# TDD Command - 测试驱动开发

> 遵循红-绿-重构流程：先写测试（RED），再实现（GREEN），最后重构（REFACTOR）

## TDD 流程

### 🔴 RED - 写失败的测试

**目标**：先写一个会失败的测试，明确我们要实现什么

```
## 步骤 1: 写测试（RED）

编写一个测试用例，描述你希望代码实现的功能。
此时测试应该 FAIL，因为功能尚未实现。

示例：
```java
@Test
void shouldReturnUserWhenUserExists() {
    // given
    Long userId = 1L;

    // when
    User result = userService.getUserById(userId);

    // then - 此时测试会失败
    assertThat(result).isNotNull();
    assertThat(result.getId()).isEqualTo(1L);
}
```
```

### 🟢 GREEN - 写最少的代码让测试通过

**目标**：用最少的代码让测试通过，不求完美

```
## 步骤 2: 实现代码（GREEN）

编写最简单的代码让测试通过。
不要考虑优化，只求通过测试。

示例：
```java
public User getUserById(Long id) {
    return new User(); // 最少代码，先让测试通过
}
```
```

### 🔵 REFACTOR - 重构

**目标**：优化代码质量，同时保持测试通过

```
## 步骤 3: 重构（REFACTOR）

在测试通过的基础上，重构代码使其更好：
- 提取重复代码
- 改善命名
- 优化性能
- 添加真实业务逻辑

示例：
```java
public User getUserById(Long id) {
    return userMapper.selectById(id); // 真实实现
}
```
```

## 执行规则

1. **严格顺序**：必须按 RED → GREEN → REFACTOR 顺序执行
2. **小步前进**：每次只实现一个小功能
3. **测试先行**：不写未通过测试的代码（紧急修复除外）
4. **保持绿色**：重构后所有测试必须继续通过

## 触发条件

当用户请求：
- 实现新功能
- 添加新接口
- 编写业务逻辑

## 交互流程

```
1. 用户：帮我实现 xxx 功能

2. 我：
   ## TDD 流程

   ### 🔴 RED - 写测试
   请确认要测试的功能点：
   - [功能点 1]
   - [功能点 2]

3. 用户：确认

4. 我：编写测试（RED）→ 运行测试（失败）→ 编写实现（GREEN）→ 重构（REFACTOR）
```

## 注意事项

- 如果测试失败，检查是测试问题还是实现问题
- 如果实现卡住，可能需要重新审视测试设计
- 重构可以在多个 GREEN 循环后统一进行
