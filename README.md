
# 购物商城系统

基于 Spring Boot 的购物商城系统，支持商品秒杀场景，采用 Redis、RabbitMQ 等技术实现高并发设计。

## 技术栈

- **框架**: Spring Boot 3.2.x
- **数据库**: MySQL 8.0+
- **缓存**: Redis 7.0+
- **消息队列**: RabbitMQ 3.12+
- **ORM**: MyBatis Plus 3.5.x
- **分布式锁**: Redisson

## 功能特性

- 商品管理（CRUD）
- 商品秒杀
- 订单管理
- 库存预热
- 分布式锁
- 异步订单处理

## 快速开始

### 环境要求

- JDK 21+
- Maven 3.8+
- MySQL 8.0+
- Redis 7.0+
- RabbitMQ 3.12+

### 配置说明

1. 修改 `src/main/resources/application.yml` 中的数据库连接信息
2. 确保 Redis 和 RabbitMQ 服务已启动

### 构建与运行

```bash
# 构建项目
mvn clean package -DskipTests

# 运行项目
java -jar target/shopping-mall-1.0.0.jar
```

### API 接口

**秒杀接口**:
```bash
POST /api/seckill/buy
Content-Type: application/json

{
    "productId": 1,
    "userId": 1,
    "quantity": 1
}
```

**商品列表**:
```bash
GET /api/products
```

**订单查询**:
```bash
GET /api/orders/user/{userId}
```

## 高并发设计

1. **Redis 库存预热** - 秒杀开始前将库存加载到 Redis
2. **原子扣减** - 使用 Redis DECRBY 原子操作扣减库存
3. **分布式锁** - Redisson 实现分布式锁防止超卖
4. **消息队列** - RabbitMQ 异步处理订单，削峰填谷
5. **限流控制** - 限制单个用户购买数量

## 目录结构

```
src/main/java/com/example/shoppingmall/
├── config/          # 配置类
├── controller/      # 控制器
├── service/         # 服务层
├── mapper/          # 数据访问层
├── entity/          # 实体类
├── dto/             # 数据传输对象
├── consumer/        # 消息消费者
└── exception/       # 异常处理
```

## 部署说明

详细部署说明请参考 `DEPLOYMENT.md` 文件。
