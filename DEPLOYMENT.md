
# 购物商城项目部署指南

## 项目概述

本项目是一个基于 Spring Boot 的购物商城系统，支持商品秒杀功能，采用了以下技术栈：
- Spring Boot 3.2.x
- Redis (缓存、分布式锁)
- RabbitMQ (消息队列)
- MySQL (数据库)
- MyBatis Plus (ORM)
- Redisson (分布式锁)

## 环境要求

- Ubuntu 24.04 LTS
- JDK 21
- Maven 3.8+
- MySQL 8.0+
- Redis 7.0+
- RabbitMQ 3.12+

## 环境配置步骤

### 1. 更新系统并安装依赖

```bash
sudo apt update && sudo apt upgrade -y
sudo apt install -y openjdk-21-jdk maven mysql-server redis-server rabbitmq-server
```

### 2. 配置 MySQL

```bash
# 启动 MySQL 服务
sudo systemctl start mysql
sudo systemctl enable mysql

# 设置 root 密码
sudo mysql -u root -p
```

在 MySQL 命令行中执行：

```sql
CREATE DATABASE example_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'admin'@'localhost' IDENTIFIED BY 'password';
GRANT ALL PRIVILEGES ON example_db.* TO 'admin'@'localhost';
FLUSH PRIVILEGES;
EXIT;
```

### 3. 配置 Redis

```bash
# 启动 Redis 服务
sudo systemctl start redis-server
sudo systemctl enable redis-server

# 验证 Redis 运行
redis-cli ping
```

### 4. 配置 RabbitMQ

```bash
# 启动 RabbitMQ 服务
sudo systemctl start rabbitmq-server
sudo systemctl enable rabbitmq-server

# 启用管理插件（可选）
sudo rabbitmq-plugins enable rabbitmq_management

# 验证 RabbitMQ 运行
rabbitmqctl status
```

## 项目构建与运行

### 1. 克隆项目

```bash
cd /opt
git clone <项目仓库地址>
cd shoppingMall
```

### 2. 修改配置文件

编辑 `src/main/resources/application.yml`，根据实际环境修改数据库连接信息：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/example_db?useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
    username: admin
    password: password
```

### 3. 创建数据库表

```bash
mysql -u admin -p example_db < src/main/resources/schema.sql
mysql -u admin -p example_db < src/main/resources/data.sql
```

### 4. 构建项目

```bash
mvn clean package -DskipTests
```

### 5. 运行项目

```bash
java -jar target/shopping-mall-1.0.0.jar
```

或使用 Maven 运行：

```bash
mvn spring-boot:run
```

## API 接口说明

### 商品管理

| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/products` | GET | 获取商品列表 |
| `/api/products/{id}` | GET | 获取商品详情 |
| `/api/products` | POST | 创建商品 |
| `/api/products/{id}` | PUT | 更新商品 |
| `/api/products/{id}` | DELETE | 删除商品 |

### 秒杀接口

| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/seckill/buy` | POST | 参与秒杀 |

### 订单查询

| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/orders/user/{userId}` | GET | 获取用户订单列表 |
| `/api/orders/{id}` | GET | 获取订单详情 |

## 秒杀接口示例

**请求：**
```bash
curl -X POST http://localhost:8080/api/seckill/buy \
  -H "Content-Type: application/json" \
  -d '{
    "productId": 1,
    "userId": 1,
    "quantity": 1
  }'
```

**响应：**
```json
"秒杀成功，正在处理订单..."
```

## 高并发设计说明

### 1. Redis 缓存库存

- 秒杀开始前将库存预热到 Redis
- 使用 Redis 的原子操作 `decrby` 扣减库存
- 减少对数据库的直接访问

### 2. 分布式锁

- 使用 Redisson 实现分布式锁
- 防止超卖问题
- 设置合理的锁超时时间

### 3. 消息队列

- 使用 RabbitMQ 异步处理订单
- 削峰填谷，提高系统吞吐量
- 解耦秒杀请求和订单处理

### 4. 限流设计

- 限制单个用户的购买数量
- 防止恶意刷接口
- 可配置的限流参数

### 5. 数据库优化

- 使用乐观锁更新库存
- 合理的索引设计
- 事务控制保证数据一致性

## 故障排查

### 常见问题

1. **数据库连接失败**
   - 检查 MySQL 服务是否运行
   - 确认数据库配置信息正确
   - 检查防火墙设置

2. **Redis 连接失败**
   - 检查 Redis 服务是否运行
   - 确认 Redis 配置信息正确
   - 检查 Redis 密码配置

3. **RabbitMQ 连接失败**
   - 检查 RabbitMQ 服务是否运行
   - 确认 RabbitMQ 配置信息正确
   - 检查虚拟主机配置

4. **秒杀库存问题**
   - 检查 Redis 库存预热是否成功
   - 确认库存扣减逻辑正确
   - 检查分布式锁是否正常工作

## 性能测试建议

使用 JMeter 或压测工具进行性能测试：

```bash
# 示例：使用 Apache Bench 压测
ab -n 1000 -c 100 http://localhost:8080/api/seckill/buy
```

## 扩展建议

1. **水平扩展**：部署多个应用实例，使用负载均衡
2. **Redis 集群**：使用 Redis Cluster 提高可用性
3. **读写分离**：数据库主从复制，读写分离
4. **熔断降级**：使用 Hystrix 或 Resilience4j 实现熔断
5. **分布式 ID**：使用雪花算法生成唯一订单号
