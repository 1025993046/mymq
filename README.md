##基于netty实现消息队列MQ

该项目是用于个人学习，去理解mq的底层实现原理，参考了公众号：老马啸西风的相关推文

其中项目内部分为了五个层：

| 模块名   | 作用 |  
| :------------- | :----------: | 
| mq-broker |   注册中心   | 
| mq-common |   公共代码   | 
| mq-consumer |   消费者端   | 
| mq-producer |   生产者端  | 
| mq-test |   测试模块   | 

其中实现的特性有：启动状态检测、超时处理、优雅停机、消费者心跳检测、负载均衡、消费者主动拉取消息、消息的批量发送与批量处理、注册鉴权等

功能测试实现案例：
####注册中心
```java
public class BrokerMain {
    public static void main(String[] args) {
        MqBroker broker=new MqBroker();
        broker.start();
    }
}
```
刚启动注册中心：
```Test
[DEBUG] [2022-07-31 23:38:30.678] [main] [c.g.h.l.i.c.LogFactory.setImplementation] - Logging initialized using 'class com.github.houbb.log.integration.adaptors.stdout.StdOutExImpl' adapter.
[INFO] [2022-07-31 23:38:31.182] [Thread-0] [c.m.m.b.c.MqBroker.run] - MQ中间人开始启动服务端 port：9999
[INFO] [2022-07-31 23:38:38.606] [Thread-0] [c.m.m.b.c.MqBroker.run] - MQ中间人启动完成，监听【9999】端口
```

####消费者
```java
public class ConsumerMain {
    public static void main(String[] args) {
        //1.先启动消费者，再启动生产者
        final MqConsumerPush mqConsumerPush = new MqConsumerPush();
        mqConsumerPush.start();

        mqConsumerPush.subscribe("TOPIC", "TAGA");
        mqConsumerPush.registerListener(new IMqConsumerListener() {
            @Override
            public ConsumerStatus consumer(MqMessage mqMessage, IMqConsumerListenerContext context) {
                System.out.println("---------- 自定义 " + JSON.toJSONString(mqMessage));
                return ConsumerStatus.SUCCESS;
            }
        });
    }
}
```

启动消费者(部分日志)
```
[DEBUG] [2022-07-31 23:40:57.130] [nioEventLoopGroup-2-1] [c.m.m.c.s.i.i.InvokeService.addResponse] - [Invoke] 获取结果信息，seqId： dd063972aea84609b3871c85da408263 ,rpcResponse: {"requestTime":1659282057127,"traceId":"dd063972aea84609b3871c85da408263","request":false,"methodType":"C_SUBSCRIBE","json":"{\"respCode\":\"0000\",\"respMessage\":\"成功\"}"} 
[DEBUG] [2022-07-31 23:40:57.131] [nioEventLoopGroup-2-1] [c.m.m.c.s.i.i.InvokeService.addResponse] - [Invoke] seqId:dd063972aea84609b3871c85da408263 信息已经放入，通知所有等待方
[DEBUG] [2022-07-31 23:40:57.132] [nioEventLoopGroup-2-1] [c.m.m.c.s.i.i.InvokeService.addResponse] - [Invoke] seqId:dd063972aea84609b3871c85da408263 remove from request map
[DEBUG] [2022-07-31 23:40:57.132] [nioEventLoopGroup-2-1] [c.m.m.c.s.i.i.InvokeService.addResponse] - [Invoke] dd063972aea84609b3871c85da408263 notifyAll()
[DEBUG] [2022-07-31 23:40:57.135] [main] [c.m.m.c.s.i.i.InvokeService.getResponse] - [Invoke] dd063972aea84609b3871c85da408263 wait has notified!
[DEBUG] [2022-07-31 23:40:57.136] [main] [c.m.m.c.s.i.i.InvokeService.getResponse] - [Invoke] seq dd063972aea84609b3871c85da408263 对应结果已经获取： RpcMessageDto{requestTime=1659282057127, traceId='dd063972aea84609b3871c85da408263', methodType='C_SUBSCRIBE', isRequest=false, respCode='null', respMsg='null', json='{"respCode":"0000","respMessage":"成功"}'} 
```

####生产者
```java
public class ProducerMain {
    public static void main(String[] args) {
        MqProducer mqProducer = new MqProducer();
        mqProducer.appKey("test")
                .appSecret("mq");
        mqProducer.start();

        for(int i = 0; i < 20; i++) {
            MqMessage mqMessage = buildMessage(i);
            SendResult sendResult = mqProducer.send(mqMessage);
            System.out.println(JSON.toJSON(sendResult));
        }
    }

    private static MqMessage buildMessage(int i) {
        String message = "我是生产者发过来的 "+i+"号数据";
        MqMessage mqMessage = new MqMessage();
        mqMessage.setTopic("TOPIC");
        mqMessage.setTags(Arrays.asList("TAGA", "TAGB"));
        mqMessage.setPayload(message);

        return mqMessage;
    }
}
```
再启动生产者(部分日志)
```
[INFO] [2022-07-31 23:41:28.312] [main] [c.m.m.p.s.b.ProducerBrokerService.doSend] - [Producer] 发送消息 messageId： 6e8b36d91afa43cb89f0b8e762e7ebdd ，mqMessage ：{"traceId":"6e8b36d91afa43cb89f0b8e762e7ebdd","groupName":"P_DEFAULT_GROUP_NAME","methodType":"P_SEND_MSG","payload":"我是生产者发过来的 1号数据","topic":"TOPIC","tags":["TAGA","TAGB"]}
[DEBUG] [2022-07-31 23:41:28.314] [main] [c.m.m.c.s.i.i.InvokeService.addRequest] - [Invoke] start add request for seqId: 6e8b36d91afa43cb89f0b8e762e7ebdd ,timeoutMills: 5000
[DEBUG] [2022-07-31 23:41:28.315] [main] [c.m.m.p.s.b.ProducerBrokerService.callServer] - [Client] channelId 005056fffec00008-00002204-00000000-0457bc12808f5d5d-ec31a2d8 发送消息 {"requestTime":1659282088313,"traceId":"6e8b36d91afa43cb89f0b8e762e7ebdd","request":true,"methodType":"P_SEND_MSG","json":"{\"groupName\":\"P_DEFAULT_GROUP_NAME\",\"methodType\":\"P_SEND_MSG\",\"payload\":\"我是生产者发过来的 1号数据\",\"tags\":[\"TAGA\",\"TAGB\"],\"topic\":\"TOPIC\",\"traceId\":\"6e8b36d91afa43cb89f0b8e762e7ebdd\"}"} 
[DEBUG] [2022-07-31 23:41:28.322] [main] [c.m.m.c.s.i.i.InvokeService.getResponse] - [Invoke] seq 6e8b36d91afa43cb89f0b8e762e7ebdd 对应结果为空，进入等待
```

启动生产者之后，生产者已经成功将信息发送到注册中心，并且注册中心会匹配相应订阅了该分组的消费者，并发送给消费者。

查看注册中心接口消息情况：
```
[DEBUG] [2022-07-31 23:41:29.136] [pool-3-thread-1] [c.m.m.b.s.p.BrokerPushService.callServer] - [Client] channelId 005056fffec00008-00000cec-00000001-08afeda1908ee35e-cbc08705 发送消息 {"requestTime":1659282089097,"traceId":"046e7afc624d41248577783120aebf75","request":true,"methodType":"B_MESSAGE_PUSH","json":"{\"groupName\":\"P_DEFAULT_GROUP_NAME\",\"methodType\":\"B_MESSAGE_PUSH\",\"payload\":\"我是生产者发过来的 10号数据\",\"tags\":[\"TAGA\",\"TAGB\"],\"topic\":\"TOPIC\",\"traceId\":\"046e7afc624d41248577783120aebf75\"}"} 
[DEBUG] [2022-07-31 23:41:29.137] [pool-3-thread-1] [c.m.m.c.s.i.i.InvokeService.getResponse] - [Invoke] seq 046e7afc624d41248577783120aebf75 对应结果已经获取： RpcMessageDto{requestTime=1659282089103, traceId='046e7afc624d41248577783120aebf75', methodType='B_MESSAGE_PUSH', isRequest=false, respCode='null', respMsg='null', json='{"consumerStatus":"CS","respCode":"0000","respMessage":"成功"}'} 
[INFO] [2022-07-31 23:41:29.139] [pool-3-thread-1] [c.m.m.b.s.p.BrokerPushService.run] - 完成处理 channelId: 005056fffec00008-00000cec-00000001-08afeda1908ee35e-cbc08705 
[INFO] [2022-07-31 23:41:29.140] [pool-3-thread-1] [c.m.m.b.s.p.BrokerPushService.run] - 完成异步处理
```

消费者端已接受到消息并进行消费，说明消费成功：
```
[DEBUG] [2022-07-31 23:41:28.789] [nioEventLoopGroup-2-1] [c.m.m.c.h.MqConsumerHandler.dispatch] - channelId:005056fffec00008-00002eac-00000000-0ba2fe77908ee20b-41f06f54 接收到method：B_MESSAGE_PUSH 内容：{"groupName":"P_DEFAULT_GROUP_NAME","methodType":"B_MESSAGE_PUSH","payload":"我是生产者发过来的 3号数据","tags":["TAGA","TAGB"],"topic":"TOPIC","traceId":"d019514b30ca47e094bac4475cb07f2d"} 
[INFO] [2022-07-31 23:41:28.789] [nioEventLoopGroup-2-1] [c.m.m.c.h.MqConsumerHandler.dispatch] - 收到服务端消息：{"groupName":"P_DEFAULT_GROUP_NAME","methodType":"B_MESSAGE_PUSH","payload":"我是生产者发过来的 3号数据","tags":["TAGA","TAGB"],"topic":"TOPIC","traceId":"d019514b30ca47e094bac4475cb07f2d"}
---------- 自定义 {"groupName":"P_DEFAULT_GROUP_NAME","methodType":"B_MESSAGE_PUSH","payload":"我是生产者发过来的 3号数据","tags":["TAGA","TAGB"],"topic":"TOPIC","traceId":"d019514b30ca47e094bac4475cb07f2d"}
[DEBUG] [2022-07-31 23:41:28.792] [nioEventLoopGroup-2-1] [c.m.m.c.h.MqConsumerHandler.writeResponse] - [Server] channel 005056fffec00008-00002eac-00000000-0ba2fe77908ee20b-41f06f54 response {"requestTime":1659282088790,"traceId":"d019514b30ca47e094bac4475cb07f2d","request":false,"methodType":"B_MESSAGE_PUSH","json":"{\"consumerStatus\":\"CS\",\"respCode\":\"0000\",\"respMessage\":\"成功\"}"}
```

