# 项目背景（为什么要写这个项目？）
目前的定时任务框架已经很成熟，从QuartZ到xxl-job，再到近几年出现的PowerJob，既然有这么多的好的实现，为什么还是选择重写一个定时任务框架呢？
开发中遇到这样的场景，业务层面需要**频繁**的创建修改**定时任务**，在考虑分布式的架构下，对于目前可以实现该功能的框架中：

- MQ的延时队列无法动态调整任务参数；
- redis的过期策略需要保存太久的key且可能会有BigKey
- xxljob没有原生的openAPI，其基于数据库锁的调度只是实现server的高可用而不是高性能；
- PowerJob的openAPI是基于http的同步阻塞调度，并且对于server的负载均衡，由于其分组隔离设计，需要开发者手动配置，在高并发下的定时任务操作下，并不能很好的调度server集群。

主流框架往往为了适配更多的场景，支持足够多的功能，往往体积大，且不易动态扩展，为了对项目有最大的控制，在解决以上业务场景的前提下，进行部分功能的修剪，也希望能更好的从中学习主流框架的设计思想，于是决定重写一个定时任务框架。

# 定位
这是一个基于 [PowerJob](https://github.com/PowerJob/PowerJob) 的重写和重构版本，修改和扩展了原始项目的功能，以更好地适配业务需求。
1. 支持定时任务**频繁创建**和任务参数**频繁动态变动**的场景（提供轻量API，并使用**内置消息队列**异步处理）
2. 支持大量**定时任务并发执行**的场景，实现**负载均衡**（分组隔离+应用级别的锁实现）
3. 主要针对小型任务 ，无需过多配置，不对任务实例进行操作

# 技术选型
```
通信 : gRPC（基于netty的nio）
序列化 ： Protobuf编码格式编解码
负载均衡 ：自己实现的注册中心NameServer
	|___ 策略 : 服务端最小调度次数策略
	|___ 交互 ：pull+push
消息队列 : 自己实现的简易消息队列
	|___ 消息发送 ： 异步+超时重试
	|___ 持久化 ：mmap+同步刷盘策略
	|___ 消息重试 ：多级延时队列+死信队列
定时调度 ： 时间轮算法
```
# 项目结构
```
├── LICENSE
├── k-job-common // 各组件的公共依赖，开发者无需感知
├── k-job-nameServer // server和worker的注册中心，提供负载均衡
├── k-job-producer //普通Jar包，提供 OpenAPI，内置消息队列的异步发送
├── k-job-server // 基于SpringBoot实现的调度服务器
├── k-job-worker-boot-starter // kjob-worker 的 spring-boot-starter ，spring boot 应用可以通用引入该依赖一键接入 kjob-server 
├── k-job-worker // 普通Jar包，接入kjob-server的应用需要依赖该Jar包
└── pom.xml
```
# 特性

## 负载均衡（解决大量定时任务并发执行场景）

在分布式系统下，解决定时任务并发执行往往考虑server集群的负载均衡（这里的**负载均衡特指server集群能够根据自身负载，动态调度worker集群**），但是对于定时任务框架，需要关注**集群下的任务重复调度问题，目前的定时任务框架大都为了解决该问题而不能较好实现负载均衡**。

通过查看源码，xxljob的调度，在每次查询数据库获取任务前，通过数据库行锁进行了全局加锁，保证同一时刻只有一个server在进行调度来避免重复调度，但是无法发挥集群server的调度能力

对于PowerJob的调度，通过分组隔离机制（详细可以看官方文档）避免了重复调度，但是同样带来了问题：同一app下的worker集群只能被一台server调度，如果该server的任务太多了呢？如果只有一个业务对应的app，如何用server集群来负载均衡呢？

基于以上问题，增加了一个注册中心nameServer模块来负责负载均衡：
![img1.png](others%2Fimages%2Fimg1.png)
**最小调度次数策略：** NameServer记录server集群状态并**维护各个server的分配任务次数**，由于server是否调度某个worker由表中数据决定，worker会在每次pull判断是否发起请求更新server中的调度关系表，并**将目前分组交由最小调度次数的server来调度**，当且仅当以下发生：
- 同一app分组下的workerNum > threshold
- 该分组对应的server的scheduleTimes > minServerScheduleTime x 2

（考虑到server的地理位置，通信效率等因素，后续可以考虑增加每个server的权重来更优分配）

**实现功能：**
1. **app组自动拆分**：可以为app设置组内worker数量阈值，超过阈值自动拆分subApp并分配负载均衡后的server
2. **worker动态分配**：对于每一个subApp，当触发pull时，根据最小调度次数策略，可以分配至负载均衡后的server，开发者无需感知subApp


以上，**解决PowerJob中同一worker分组只能被一个server调度问题**，且subApp分组可以根据server的负载，实现**动态依附**至不同server，对于可能的重复调度问题，我们只需加上App级别的锁，相对于xxl-job的全局加锁性能更好。

## 消息队列（解决任务大量创建和频繁变动场景）

其实一开始用PowerJob作为项目中的中间件，业务中的任务操作使用其openAPI。过程中感受最大的就是，我的业务只是根据任务id修改了任务参数，并不需要server的响应，为什么要同步阻塞？**可靠性应由server保证**而不是客户端的大量重试及等待。对于业务中频繁创建定时任务和改动，更应是**异步**操作。

一开始的想法是，使用grpc的futureStub进行异步发送，请求由Reactor线程监听事件，当事件可读时分配给业务线程池进行处理（**gRPC内部已经实现**）。所以需要做的似乎只是做一个Producer服务，并把stub全换成Future类型，对于jobId，我们用雪花算法拿到一个全局id就可以，无需server分配。
![img2.png](others%2Fimages%2Fimg2.png)
但是以上设计有一个致命的问题------**阻塞在BlockingQueue的请求无法ack，且server宕机存在消息丢失的可能**！这违背了消息队列的设计（入队--ack--持久化--消费），意味着只有被分配到线程（消费者）消费时，才能被ack，而活跃的线程数并不多。故**不能仅仅依赖gRPC的内部实现，需要自己实现消息队列**


### 可靠消息
以rocketMQ为例，producer的消息会先到达broker中的队列后返回ack，consumer再轮询从broker中pull重平衡处理后的消息消费。

考虑到本项目的设计**无需路由，所有的server都可以接受消息，于是不再设计broker，将server和broker结合，每个server维护自己的队列，且消费自己队列的消息**，这样还能减少一次通信。

这样可靠消息的解决就变成了：
1. producer到server的消息丢失------失败或者超时则**依次遍历所有的server**，一定能保证消息抵达，不再阐述
2. server的队列消息丢失（机器宕机）------持久化，采用同步刷盘策略，百分之百的可靠

**持久化**：同步刷盘机制借鉴了rocketMQ的**mmap**和**commitLog/consumerQueue**设计，将磁盘的文件映射到内存进行读写，每次消息进来先存到buffer后触发刷盘，成功后执行写响应的回调；用consumerQueue文件作为队列，server定时pull消费消息，详细见k-job-server.consumer.DefaultMessageStore
### 消息重试
对于producer，前面提到，为了应对大量定时任务的场景，对于任务的操作，应全部是异步的，我们引入超时机制即可，当超过一定的时间未收到ack，或者返回错误响应，选择下一个server发起重试

对于consumer（server），使用**多级延时队列**，当某个消息消费失败后，投递至下一级延迟更久的延时队列，若全都消费失败则进入**死信队列**，需要人工干预

最终实现如图所示：
![img3.png](others%2Fimages%2Fimg3.png)

**实现功能：**
1. 对于操作任务请求的**异步**操作
2. **轮询策略**实现消费的负载均衡



# 其他
附上个人总结的对于worker和server之间服务发现以及调度的流程图
## 服务发现
![img4.png](others%2Fimages%2Fimg4.png)
## 调度
![img5.png](others%2Fimages%2Fimg5.png)
