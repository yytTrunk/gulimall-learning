# gulimall-learning

## 一 搭建数据库



## 二 创建工程模块





## 三 renren-fast生成CURD代码

### 3.1 单个服务调试通过





## 四 引入注册中心、配置中心

### 4.1 引入nacos作为注册中心

### 4.2 引入nacos作为配置中心



## 五 引入网关
### 5.1 网关注册为服务
### 5.2 配置网关

### 5.3 解决跨域



## 六 后台管理业务逻辑

### 6.1 基于renren-fast-vue构建前后端

### 6.2 实现商品分类管理

### 6.3 库存管理



## 七 全文检索elasticSearch搭建

### 7.1 商品上架



## 八 商品首页



## 九 nginx配置

### 9.1 nginx配置域名访问环境

### 9.2 nginx动静分离



## 十 性能压测工具

### 10.1 Jmeter使用

### 10.2 jvisualvm使用

### 10.3 吞吐量测试

| 测试项   | 接口名称            | 优化内容           | 平均值 | 吞吐量 |
| -------- | ------------------- | ------------------ | ------ | ------ |
| 获取列表 | /index/catalog.json | 每次都从数据库读取 |        | 56     |
| 获取列表 | /index/catalog.json | 加了redis缓存      | 6      | 167    |
|          |                     |                    |        |        |







## 十一 缓存使用

### 11.1 引入redis

### 11.2 配置从redis读取数据

```java
    public Map<String, List<Catalog2Vo>> getCatalogJson() {

        String catalogJSON = stringRedisTemplate.opsForValue().get("catalogJSON");
        if (StringUtils.isEmpty(catalogJSON)) {
            log.info("读取数据库获取");
            Map<String, List<Catalog2Vo>> catalogJsonFromDb = getCatalogJsonFromDb();
            String jsonRet = JSON.toJSONString(catalogJsonFromDb);
            stringRedisTemplate.opsForValue().set("catalogJSON", jsonRet, 1, TimeUnit.MINUTES);

            return  catalogJsonFromDb;
        }

        log.info("从redis获取");
        Map<String, List<Catalog2Vo>> parseObject = JSON.parseObject(catalogJSON,
                new TypeReference<Map<String, List<Catalog2Vo>>>() { });
        return parseObject;
    }
```

### 11.3 解决缓存穿透

未查询到，写入null值，加入过期时间

### 11.3 解决缓存击穿

通过加锁，查询到数据为空时，去数据库中查询，避免同时大量请求直接都去查询数据库，此时需要加锁，查询一次后，将结果写入缓存中，其它请求直接去读缓存，查询缓存后返回。

由本地锁优化为分布式锁

**基于朴素redis方案**

**方式1** 直接使用`setnx+delete`

```java
setnx lock_key lock_value
// do sth
delete lock_key
```

实际应用

```java
    public Map<String, List<Catalog2Vo>> getDataWithRedisLock() {
        // 使用分布式锁
        Boolean getLockSuccess = stringRedisTemplate.opsForValue().setIfAbsent("redis-lock", "111");
        if (!getLockSuccess) {
            // 加锁失败,先休息，后再重复获取
            TimeUnit.MILLISECONDS.sleep(100);
            return getDataWithRedisLock();
        } else {
            // 加锁成功
            // 执行业务....
            // 执行成功，释放锁
            stringRedisTemplate.delete("redis-lock");
            return result;
        }
    }
```

不足：服务获取锁后，执行业务，突然挂掉，锁无法释放

解决：设置过期时间

**方式2** 引入过期时间 `setnx + setex + delete`

```java
setnx lock_key lock_value
setex lock_key N lock_value  // N s超时
// do sth
delete lock_key
```

实际应用

```java
public Map<String, List<Catalog2Vo>> getCatalogJsonFromDbWithRedisLock() {
    // 使用分布式锁
    Boolean getLockSuccess = stringRedisTemplate.opsForValue().setIfAbsent("catalog-lock", "111");
    if (!getLockSuccess) {
        // 加锁失败
        try {
            TimeUnit.MILLISECONDS.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return getCatalogJsonFromDbWithRedisLock();
    } else {
        // 加锁成功
        // 设置过期时间
        stringRedisTemplate.expire("catalog-lock", 30, TimeUnit.SECONDS);
        Map<String, List<Catalog2Vo>> result = getDataFromDB();
        // 执行成功，释放锁
        stringRedisTemplate.delete("catalog-lock");

        return result;
    }
}
```

不足：setnx + setex非原子操作，使用setnx获取锁后，还没来得及使用setex设置过期时间，服务挂掉，也会出现死锁

解决：使用原子命令

**方式3** 使用命令 `SET keyName randomValue NX EX 30000`

使用原生命令将加锁、设置超时两个步骤合并为一个原子操作

NX表示只有当keyName不存在时，才能设置成功，PX 30000表示30s后锁会自动释放。当同时其它线程来获取锁，如果没有设置成功就不能加锁。

实际应用

```java
public Map<String, List<Catalog2Vo>> getCatalogJsonFromDbWithRedisLock() {
    // 使用分布式锁
        Boolean getLockSuccess = stringRedisTemplate.opsForValue().setIfAbsent("catalog-lock", "111", 30, TimeUnit.SECONDS);
    if (!getLockSuccess) {
        // 加锁失败
        try {
            TimeUnit.MILLISECONDS.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return getCatalogJsonFromDbWithRedisLock();
    } else {
        // 加锁成功
        // 设置过期时，不在此处设置过期时间
        // stringRedisTemplate.expire("catalog-lock", 30, TimeUnit.SECONDS);
        Map<String, List<Catalog2Vo>> result = getDataFromDB();
        // 执行成功，释放锁
        stringRedisTemplate.delete("catalog-lock");

        return result;
    }
}
```

不足：

1）  获取锁后，设置了10s过期时间，但是业务执行需要30s，执行阶段锁已经被释放

2）  30s后再去业务执行完，此时锁已被其它服务申请到，再去释放锁，会将其它其它服务申请到的锁错误释放掉，引起业务混乱

解决：

加锁时的value值，采用随机值UUID。

```java
public Map<String, List<Catalog2Vo>> getCatalogJsonFromDbWithRedisLock() {
    // 使用分布式锁
    Boolean getLockSuccess = stringRedisTemplate.opsForValue().setIfAbsent("catalog-lock", "111", 30, TimeUnit.SECONDS);
    if (!getLockSuccess) {
        // 加锁失败
        try {
            TimeUnit.MILLISECONDS.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return getCatalogJsonFromDbWithRedisLock();
    } else {
        // 加锁成功
        // 设置过期时间,非原子性
        //stringRedisTemplate.expire("catalog-lock", 30, TimeUnit.SECONDS);
        Map<String, List<Catalog2Vo>> result = getDataFromDB();
        // 执行成功，释放锁
        stringRedisTemplate.delete("catalog-lock");

        return result;
    }
}
```

**方式4** 

针对3中的问题1，可以设置较长时间的过期时间，保证一定能够释放锁

针对3中的问题2，设置key值时使用随机值，在释放锁时，先判断对应key的value值，是不是自己申请锁时写入的value值，如果是，就进行释放，不是就再执行释放操作。为了保证判断操作原子性，需要使用lua脚本实现。如果不具备原子性，直接使用先去读取下key的value值，进行判断，若相同再进行释放，会出现一种情况，获取到key值，与释放之间的时间，key对应值，已经被其它服务修改了，那么再去释放锁，还是会释放掉别人的锁。

官方判断代码

>  lua脚本：https://redis.io/commands/eval

```java
if redis.call("get",KEYS[1]) == ARGV[1] then
   return redis.call("del",KEYS[1])
else
   return 0
5end
```

实际应用

```java
public Map<String, List<Catalog2Vo>> getCatalogJsonFromDbWithRedisLock() {
    // 使用分布式锁
    String uuid = UUID.randomUUID().toString();
    Boolean getLockSuccess = stringRedisTemplate.opsForValue().setIfAbsent("lock", uuid, 30, TimeUnit.SECONDS);
    if (!getLockSuccess) {
        // 加锁失败
        try {
            TimeUnit.MILLISECONDS.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return getCatalogJsonFromDbWithRedisLock();
    } else {
        // 加锁成功
        // 设置过期时间,非原子性
        //stringRedisTemplate.expire("catalog-lock", 30, TimeUnit.SECONDS);
        try {
            Map<String, List<Catalog2Vo>> result = getDataFromDB();
            // 执行成功，释放锁，释放锁需要保证原子性，使用lua脚本
            // stringRedisTemplate.delete("catalog-lock");
            return result;
        } finally {
            //使用lua简本脚本解锁，保证原子性
            String script = "if redis.call('get',KEYS[1]) == ARGV[1] then  return redis.call('del',KEYS[1]) else return 0 end";
            stringRedisTemplate.execute(new DefaultRedisScript<Long>(script, Long.class), Arrays.asList("lock"), uuid);
        }
    }
}
```

**基于朴素redisson方案**

```java
public Map<String, List<Catalog2Vo>> getCatalogJsonFromDbWithRedissonLock() {

    // 占分布式锁。去redis占坑
    RLock lock = redisson.getLock("catalogJson-lock");

    lock.lock();
    Map<String, List<Catalog2Vo>> catalogJsonFromDb = null;
    try {
        catalogJsonFromDb = getDataFromDB();
    } finally {
        lock.unlock();
    }

    return catalogJsonFromDb;
}
```

## 十二 检索服务elasticSearch

### 12.1 商品筛选页跳转

### 12.2 商品筛选



## 十三 商品详情页异步编排

### 13.1 商品详情页展示

### 13.2 异步编排CompletableFuture

### 

## 十四 认证服务

### 14.1 注册

### 14.2 手机验证码（未调试）

### 14.3 用户密码登录

### 14.4 微博登录（未调试）

### 14.5 spring-session共享

### 14.6 单点登录（未完成）



## 十五 购物车服务





## 十六 订单服务

### 16.1 消息队列rabbitMQ

### 16.2 分布式事务

不使用分布式事务，只能控制本地事务，远程调用服务的事务，不能控制

#### 16.2.1 seata

##### 使用

1. 每个微服务先创建undo_log表
2. 安装事务协调器seata-server
3. 导入依赖，配置register.conf，启动Server，
4. Seata 通过代理数据源实现分支事务，如果没有注入，事务无法成功回滚，使用seata代理数据源
5. 每个微服务需要导入file.conf, registry.conf

> https://github.com/seata/seata-samples/tree/master/springcloud-jpa-seata
>

使用seata AT模式难以适用高并发情况，

#### 16.2.3 RabbitMQ

使用RabbitMQ，最大努力通知方式，保证下单、支付最终一致性

使用延迟队列处理

处理消息丢失、积压，重复问题









