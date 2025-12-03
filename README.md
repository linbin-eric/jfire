# Jfire

[![License](https://img.shields.io/badge/license-AGPL--3.0-blue.svg)](https://www.gnu.org/licenses/agpl-3.0.txt)
[![Java](https://img.shields.io/badge/Java-17+-green.svg)](https://openjdk.java.net/)
[![Maven](https://img.shields.io/badge/Maven-3.9+-orange.svg)](https://maven.apache.org/)

**Jfire** 是一个轻量级的 Java IOC（控制反转）和 AOP（面向切面编程）容器框架。它提供透明的对象管理、简洁高效的 AOP API 操作，同时支持透明的事务管理和缓存管理。

## 特性

- **轻量级 IOC 容器** - 提供完整的依赖注入功能，支持单例和原型两种作用域
- **强大的 AOP 支持** - 支持前置、后置、环绕、返回后、异常后五种增强方式
- **声明式事务管理** - 基于注解的事务支持，支持多种事务传播级别
- **灵活的缓存框架** - 注解驱动的缓存管理，支持 TTL 过期
- **条件注解** - 支持基于属性、类、Bean 等条件的动态 Bean 注册
- **组件扫描** - 自动扫描和注册 Bean
- **配置管理** - 支持 YAML 配置文件和属性注入

## 快速开始

### Maven 依赖

```xml
<dependency>
    <groupId>cc.jfire</groupId>
    <artifactId>jfire</artifactId>
    <version>1.0</version>
</dependency>
```

### 基础使用

#### 1. 创建配置类

```java
@Configuration
@ComponentScan("cc.jfire.app.service")
public class AppConfig {

    @Bean
    public DataSource dataSource() {
        return new HikariDataSource();
    }
}
```

#### 2. 启动容器

```java
// 方式一：基于配置类启动
ApplicationContext context = ApplicationContext.boot(AppConfig.class);

// 方式二：编程式启动
ApplicationContext context = ApplicationContext.boot();
context.register(UserService.class);
context.register(UserRepository.class);
```

#### 3. 获取 Bean

```java
// 按类型获取
UserService userService = context.getBean(UserService.class);

// 按名称获取
UserService userService = context.getBean("userService");

// 获取某类型的所有 Bean
Collection<OrderHandler> handlers = context.getBeans(OrderHandler.class);
```

## 核心功能

### 依赖注入

Jfire 支持多种依赖注入方式：

```java
@Resource
public class UserService {

    // 按类型注入
    @Resource
    private UserRepository repository;

    // 按名称注入
    @Resource(name = "primaryCache")
    private CacheManager cacheManager;

    // 可选注入（允许为 null）
    @Resource
    @CanBeNull
    private EmailService emailService;

    // 集合注入
    @Resource
    private List<EventListener> listeners;

    // 配置属性注入
    @PropertyRead("app.name")
    private String appName;
}
```

### AOP 增强

Jfire 提供五种 AOP 增强方式：

```java
@EnhanceClass("cc.jfire.app.service.*")
public class LoggingAspect {

    // 前置增强
    @Before("save*")
    public void beforeSave(ProceedPoint point) {
        System.out.println("即将保存: " + Arrays.toString(point.getParams()));
    }

    // 后置增强
    @After("*")
    public void afterMethod(ProceedPoint point) {
        System.out.println("方法执行完成: " + point.getMethod().methodName());
    }

    // 环绕增强
    @Around("update*(String,int)")
    public void aroundUpdate(ProceedPoint point) {
        System.out.println("环绕前");
        point.invoke();  // 执行目标方法
        System.out.println("环绕后，结果: " + point.getResult());
    }

    // 返回后增强
    @AfterReturning("query*")
    public void afterReturning(ProceedPoint point) {
        Object result = point.getResult();
        System.out.println("查询结果: " + result);
    }

    // 异常后增强
    @AfterThrowable("*")
    public void afterThrowable(ProceedPoint point) {
        Throwable e = point.getE();
        System.out.println("捕获异常: " + e.getMessage());
    }
}
```

#### 增强顺序控制

通过 `order` 属性控制多个增强器的执行顺序（数值越大越先执行）：

```java
@EnhanceClass(value = "cc.jfire.app.*", order = 100)
public class SecurityAspect { }

@EnhanceClass(value = "cc.jfire.app.*", order = 50)
public class LoggingAspect { }
```

### 事务管理

使用 `@Transactional` 注解声明事务方法：

```java
public class OrderService {

    @Transactional
    public void createOrder(Order order) {
        orderRepository.save(order);
        inventoryService.deductStock(order.getItems());
        // 发生异常时自动回滚
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void audit(Order order) {
        // 开启新事务，不受外层事务影响
    }
}
```

#### 事务传播级别

| 传播级别 | 说明 |
|---------|------|
| `REQUIRED` | 需要事务，没有则创建（默认） |
| `SUPPORTS` | 支持事务，没有则以非事务方式执行 |
| `MANDATORY` | 必须在事务中执行，否则抛出异常 |
| `REQUIRES_NEW` | 新建事务，挂起已有事务 |

### 缓存管理

启用缓存功能并使用注解管理缓存：

```java
@Configuration
@EnableCacheManager
public class CacheConfig { }
```

```java
@Resource
public class UserService {

    // 缓存读取：未命中则执行方法并缓存结果
    @CacheGet(value = "'user:'+id", cacheName = "users", condition = "id > 0")
    public User getUserById(int id) {
        return userRepository.findById(id);
    }

    // 缓存更新：执行方法后更新缓存
    @CachePut(value = "'user:'+user.id", cacheName = "users")
    public User updateUser(User user) {
        return userRepository.update(user);
    }

    // 缓存删除
    @CacheDelete(value = "'user:'+id", cacheName = "users")
    public void deleteUser(int id) {
        userRepository.delete(id);
    }
}
```

### 条件注解

根据条件动态注册 Bean：

```java
@Configuration
public class DataSourceConfig {

    // 属性条件
    @Bean
    @ConditionOnProperty("db.type=mysql")
    public DataSource mysqlDataSource() { }

    // 类存在条件
    @Bean
    @ConditionOnClass("com.mysql.cj.jdbc.Driver")
    public DataSource jdbcDataSource() { }

    // Bean 存在条件
    @Bean
    @ConditionOnBean(DataSource.class)
    public JdbcTemplate jdbcTemplate() { }

    // Bean 不存在条件
    @Bean
    @ConditionOnMissBeanType(DataSource.class)
    public DataSource defaultDataSource() { }
}
```

### 配置管理

#### 加载配置文件

```java
@Configuration
@PropertyPath("application.yml")
public class AppConfig { }
```

#### 添加属性

```java
@Configuration
@AddProperty(key = "app.env", value = "production")
public class EnvConfig { }
```

#### 环境选择

```java
@Configuration
@ProfileSelector("application-${profile}.yml")
public class ProfileConfig { }
```

## 项目结构

```
src/main/java/cc/jfire/jfire/
├── core/
│   ├── ApplicationContext.java      # 应用上下文接口
│   ├── DefaultApplicationContext.java # 默认实现
│   ├── aop/                          # AOP 模块
│   │   ├── EnhanceManager.java       # 增强管理器
│   │   ├── ProceedPoint.java         # 连接点接口
│   │   ├── notated/                  # AOP 注解
│   │   └── impl/                     # AOP 实现
│   ├── bean/                         # Bean 定义
│   ├── beanfactory/                  # Bean 工厂
│   ├── inject/                       # 依赖注入
│   └── prepare/                      # 容器准备
├── exception/                        # 异常类
├── util/                             # 工具类
└── helpjunit/                        # 测试辅助
```

## 注解参考

### Bean 定义注解

| 注解 | 说明 |
|------|------|
| `@Configuration` | 标记配置类 |
| `@Bean` | 定义 Bean 的方法 |
| `@Primary` | 标记首选 Bean |
| `@ComponentScan` | 组件扫描 |
| `@Import` | 导入其他配置类 |

### 依赖注入注解

| 注解 | 说明 |
|------|------|
| `@Resource` | 依赖注入（来自 baseutil） |
| `@CanBeNull` | 允许注入值为 null |
| `@PropertyRead` | 读取配置属性 |

### AOP 注解

| 注解 | 说明 |
|------|------|
| `@EnhanceClass` | 定义增强类，指定目标类匹配规则 |
| `@Before` | 前置增强 |
| `@After` | 后置增强 |
| `@Around` | 环绕增强 |
| `@AfterReturning` | 返回后增强 |
| `@AfterThrowable` | 异常后增强 |
| `@Transactional` | 事务注解 |

### 缓存注解

| 注解 | 说明 |
|------|------|
| `@EnableCacheManager` | 启用缓存管理 |
| `@CacheGet` | 缓存读取 |
| `@CachePut` | 缓存写入 |
| `@CacheDelete` | 缓存删除 |

### 条件注解

| 注解 | 说明 |
|------|------|
| `@Conditional` | 自定义条件 |
| `@ConditionOnProperty` | 属性条件 |
| `@ConditionOnClass` | 类存在条件 |
| `@ConditionOnBean` | Bean 存在条件 |
| `@ConditionOnMissBeanType` | Bean 不存在条件 |
| `@ConditionOnAnnotation` | 注解存在条件 |

## 依赖项

- **baseutil** (1.0) - 基础工具库，提供 @Resource 注解和反射工具
- **jfireEL** (1.0) - 表达式语言支持，用于缓存 Key 和条件表达式计算
- **lombok** (1.18.30) - 减少样板代码
- **log4j-slf4j-impl** (2.2) - 日志支持（provided）

## 系统要求

- Java 17 或更高版本
- Maven 3.9 或更高版本

## 许可证

本项目采用 [GNU Affero General Public License v3.0](https://www.gnu.org/licenses/agpl-3.0.txt) 许可证。

## 作者

- **jfirer** - 495561397@qq.com

## 链接

- 项目主页: http://jfirer.com
- 源码仓库: http://git.oschina.net/eric_ds
