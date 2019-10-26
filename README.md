# <center>RocketMQ实现分布式事务-最终一致性</center>


下面就这个项目做个整体简单介绍。

## <font color=#FFD700>一、项目概述</font>

#### 1、技术架构

项目总体技术选型

```
SpringCloud(Finchley.RELEASE) + SpringBoot2.0.4 + Maven3.5.4 + RocketMQ4.3  + lombok(插件)
```

有关SpringCloud主要用到以下四个组建

```
Eureka Server + Eureka Client + Feign(服务间调用) 
```


#### 2、项目整体结构

```makefile
eureka          # 注册中心
service-order   #订单微服务  简称为服务A
service-produce #商品微服务  简称为服务B
```

各服务的启动顺序就安装上面的顺序启动。

`大致流程`

启动后、订单微服务、商品微服务都会将信息注册到注册中心。

如果访问：`localhost:8761`(注册中心地址）,以上服务都出现说明启动成功。


#### 3、分布式服务流程

用户在订单微服务（A服务）下单后，会去回调商品微服务（B服务）去减库存。这个过程需要事务的一致性。



#### 4、测试流程

页面输入：

```
http://localhost:9001/api/v1/order/save?userId=1&productId=1&total=4	
```

分布式微服务框架下分布式事务处理过程步骤如下：

A下单--------------------------B服务减库存</br>
1、A收到下单请求</br>
2、A查询商品当前库存数是否大于需要减少的库存数</br>
3、A发送half信息到MQ</br>
4、A保存本系统的订单</br>
5、A保存本系统订单成功情况下，发送commit信息到MQ</br>
  A保存本系统订单失败情况下，发送rollback信息到MQ，MQ3天后会自动删除此信息</br>
6、消费方B系统一直监听MQ，但只可见commit后的信息（half信息和rollback信息不可见）</br>
7、消费方B系统获取信息后做本系统要做的业务，如果失败则记录数据库，后续进行人工操作，整体实现分布式微服务的分布式事务，数据最终一致性有保障。</br>

