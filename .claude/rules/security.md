# 安全规则：密钥与输入校验底线

本规则定义了代码中必须遵守的安全底线，包括：禁止硬编码、输入边界校验、最小权限原则。

---

## 1. 禁止硬编码

### 1.1 敏感信息硬编码（🔴 严格禁止）

以下内容**禁止**在代码中硬编码：

| 类型 | 示例 | 正确做法 |
|------|------|----------|
| API 密钥 | `API_KEY = "sk-xxx"` | 环境变量 `System.getenv("API_KEY")` |
| 数据库密码 | `password = "123456"` | 配置文件 + 加密 |
| 加密密钥 | `SECRET_KEY = "xxx"` | 密钥管理服务 |
| Token | `token = "Bearer xxx"` | 动态获取 |
| 盐值 | `SALT = "xxx"` | 安全随机生成 |

**违规示例：**
```java
// ❌ 禁止
private static final String API_KEY = "sk-abc123xxx";
private static final String DB_PASSWORD = "admin123";
String token = "eyJhbGciOiJIUzI1NiJ9...";
```

**正确示例：**
```java
// ✅ 正确
private String getApiKey() {
    return System.getenv("API_KEY");
}

@Value("${database.password}")
private String dbPassword;

// 使用密钥管理服务
String key = keyManagementService.getKey("api-key");
```

### 1.2 业务常量硬编码（🟡 尽量避免）

以下内容建议抽取为常量或配置：

```java
// ❌ 硬编码业务数值
if (count > 100) { ... }
int maxRetry = 3;
long timeout = 5000;

// ✅ 使用常量或配置
@Value("${business.max-count:100}")
private int maxCount;

private static final int MAX_RETRY = 3;
```

---

## 2. 输入边界校验

### 2.1 数值边界校验（🔴 必须）

所有数值输入必须校验上下限：

```java
// ❌ 无校验 - 可能导致溢出或异常
int age = Integer.parseInt(request.getParameter("age"));
User user = users.get(index);

// ✅ 有校验
public User getUserByIndex(String indexStr) {
    if (indexStr == null || indexStr.isEmpty()) {
        throw new IllegalArgumentException("索引不能为空");
    }

    int index;
    try {
        index = Integer.parseInt(indexStr);
    } catch (NumberFormatException e) {
        throw new IllegalArgumentException("索引必须是数字");
    }

    // 边界校验
    if (index < 0 || index >= users.size()) {
        throw new IllegalArgumentException("索引越界");
    }

    return users.get(index);
}
```

### 2.2 字符串长度校验（🔴 必须）

```java
// ❌ 无校验
String name = request.getParameter("name");
String description = form.getDescription();

// ✅ 有校验
public void validateName(String name) {
    if (name == null || name.isEmpty()) {
        throw new IllegalArgumentException("名称不能为空");
    }
    if (name.length() > 100) {
        throw new IllegalArgumentException("名称不能超过100个字符");
    }
    // 防止特殊字符
    if (!name.matches("^[\\u4e00-\\u9fa5a-zA-Z0-9_]+$")) {
        throw new IllegalArgumentException("名称包含非法字符");
    }
}
```

### 2.3 集合大小校验（🟠 建议）

```java
// ❌ 无校验 - 可能导致内存溢出
List<Item> items = parseItems(json);

// ✅ 有校验
private static final int MAX_LIST_SIZE = 1000;

public List<Item> parseItems(String json) {
    List<Item> items = objectMapper.readValue(json,
        new TypeReference<List<Item>>() {});

    if (items.size() > MAX_LIST_SIZE) {
        throw new IllegalArgumentException(
            String.format("列表大小不能超过%d", MAX_LIST_SIZE));
    }

    return items;
}
```

### 2.4 文件上传校验（🔴 必须）

```java
public UploadResult validateAndUpload(MultipartFile file) {
    // 1. 空文件校验
    if (file == null || file.isEmpty()) {
        throw new IllegalArgumentException("文件不能为空");
    }

    // 2. 文件大小校验 (例如最大 10MB)
    long maxSize = 10 * 1024 * 1024;
    if (file.getSize() > maxSize) {
        throw new IllegalArgumentException("文件大小不能超过10MB");
    }

    // 3. 文件类型校验
    String contentType = file.getContentType();
    List<String> allowedTypes = Arrays.asList(
        "image/jpeg", "image/png", "image/gif", "application/pdf");
    if (!allowedTypes.contains(contentType)) {
        throw new IllegalArgumentException("不支持的文件类型");
    }

    // 4. 文件名安全校验
    String filename = file.getOriginalFilename();
    if (filename.contains("..") || filename.contains("/") || filename.contains("\\")) {
        throw new IllegalArgumentException("文件名包含非法字符");
    }

    // 5. 扩展名校验
    String extension = getExtension(filename);
    List<String> allowedExtensions = Arrays.asList("jpg", "jpeg", "png", "gif", "pdf");
    if (!allowedExtensions.contains(extension.toLowerCase())) {
        throw new IllegalArgumentException("不支持的文件扩展名");
    }

    return upload(file);
}
```

---

## 3. 最小权限原则

### 3.1 数据库访问（🔴 必须）

```java
// ❌ 使用管理员/root账户
spring.datasource.url=jdbc:mysql://localhost:3306/db
spring.datasource.username=root
spring.datasource.password=root

// ✅ 使用最小权限账户
spring.datasource.username=app_user
# 只授予：SELECT, INSERT, UPDATE, DELETE
# 不授予：DROP, ALTER, GRANT
```

### 3.2 API 权限控制（🔴 必须）

```java
// ❌ 无权限校验
@PostMapping("/api/admin/users")
public void createUser(User user) { ... }

// ✅ 使用权限注解
@PreAuthorize("hasRole('ADMIN')")
@PostMapping("/api/admin/users")
public void createUser(User user) { ... }

// 或使用自定义权限注解
@RequirePermission(Permission.USER_CREATE)
@PostMapping("/api/admin/users")
public void createUser(User user) { ... }
```

### 3.3 文件系统权限（🟠 建议）

```java
// ❌ 使用绝对路径访问任意目录
File file = new File("/home/user/data/" + filename);

// ✅ 限制在允许的目录
private static final String ALLOWED_DIR = "/data/uploads";
private static final int MAX_PATH_DEPTH = 3;

public File validateAndGetFile(String filename) {
    // 防止路径遍历攻击
    if (filename.contains("..") || filename.contains("/") || filename.contains("\\")) {
        throw new IllegalArgumentException("非法文件名");
    }

    File file = new File(ALLOWED_DIR, filename);

    // 确保文件在允许目录内
    if (!file.getCanonicalPath().startsWith(ALLOWED_DIR)) {
        throw new IllegalArgumentException("非法访问");
    }

    return file;
}
```

### 3.4 网络访问（🟠 建议）

```java
// ❌ 无限制的网络请求
HttpClient client = HttpClient.newHttpClient();

// ✅ 设置超时和限制
HttpClient client = HttpClient.newBuilder()
    .connectTimeout(Duration.ofSeconds(10))
    .followRedirects(Redirect.NORMAL)
    .build();
```

---

## 4. 安全校验清单

在提交代码前，必须确认：

### 敏感信息
- [ ] 代码中无硬编码的密钥、密码、Token
- [ ] 敏感配置从环境变量或密钥服务读取
- [ ] 日志中无敏感信息

### 输入校验
- [ ] 所有用户输入都有边界校验
- [ ] 数值类型有上下限检查
- [ ] 字符串有长度和格式校验
- [ ] 文件上传有大小和类型校验
- [ ] 防止 SQL 注入（使用参数化查询）
- [ ] 防止 XSS（输出转义）
- [ ] 防止路径遍历

### 权限控制
- [ ] API 有权限注解
- [ ] 数据库使用最小权限账户
- [ ] 文件访问限制在允许目录
- [ ] 敏感操作有审计日志

---

## 5. 常见违规模式

| 违规类型 | 风险等级 | 示例 |
|----------|----------|------|
| 硬编码密码 | 🔴 高危 | `password = "123"` |
| SQL 拼接 | 🔴 高危 | `sql = "SELECT * FROM u WHERE id=" + id` |
| 无边界校验 | 🔴 高危 | `list.get(index)` |
| 无权限控制 | 🔴 高危 | `@PostMapping("/delete")` 无注解 |
| 路径拼接 | 🟠 中危 | `new File(dir + filename)` |
| 弱校验 | 🟠 中危 | `if (count > 0)` 无上限 |

---

**记住**：安全无小事。宁可过度校验，不可遗漏一处。
