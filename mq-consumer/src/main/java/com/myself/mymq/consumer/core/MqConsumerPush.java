package com.myself.mymq.consumer.core;

import com.github.houbb.heaven.util.common.ArgUtil;
import com.github.houbb.load.balance.api.ILoadBalance;
import com.github.houbb.load.balance.api.impl.LoadBalances;
import com.github.houbb.log.integration.core.Log;
import com.github.houbb.log.integration.core.LogFactory;
import com.myself.mymq.common.constant.ConsumerTypeConst;
import com.myself.mymq.common.resp.MqException;
import com.myself.mymq.common.rpc.RpcChannelFuture;
import com.myself.mymq.common.support.hook.DefaultShutdownHook;
import com.myself.mymq.common.support.hook.ShutdownHooks;
import com.myself.mymq.common.support.invoke.IInvokeService;
import com.myself.mymq.common.support.invoke.impl.InvokeService;
import com.myself.mymq.common.support.status.IStatusManager;
import com.myself.mymq.common.support.status.StatusManager;
import com.myself.mymq.consumer.api.IMqConsumer;
import com.myself.mymq.consumer.api.IMqConsumerListener;
import com.myself.mymq.consumer.constant.ConsumerConst;
import com.myself.mymq.consumer.constant.ConsumerRespCode;
import com.myself.mymq.consumer.handler.MqConsumerHandler;
import com.myself.mymq.consumer.support.broker.ConsumerBrokerConfig;
import com.myself.mymq.consumer.support.broker.ConsumerBrokerService;
import com.myself.mymq.consumer.support.broker.IConsumerBrokerService;
import com.myself.mymq.consumer.support.listener.IMqListenerService;
import com.myself.mymq.consumer.support.listener.MqListenerService;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

/**
 * 推送消费策略
 * @author GuoZeWei
 * @date 2022/7/6  10:13
 */
public class MqConsumerPush extends Thread implements IMqConsumer {

    private static final Log log= LogFactory.getLog(MqConsumerPush.class);

    /**
     * 组名称
     */
    protected String groupName= ConsumerConst.DEFAULT_GROUP_NAME;

    /**
     * 中间人地址
     */
    protected String brokerAddress="127.0.0.1:9999";

    /**
     * 获取响应超时时间
     */
    protected long respTimeoutMills=5000;

    /**
     * 检查broker可用性
     */
    protected volatile boolean check = true;

    /**
     * 为剩余的请求等待时间
     */
    protected long waitMillsForRemainRequest=60*1000;

    /**
     * 调用管理类
     */
    protected final IInvokeService invokeService=new InvokeService();

    /**
     * 消息监听服务类
     */
    protected final IMqListenerService mqListenerService=new MqListenerService();


    /**
     * 状态管理类
     */
    protected final IStatusManager statusManager=new StatusManager();

    /**
     * 生产者-中间服务端服务类
     */
    protected final IConsumerBrokerService consumerBrokerService = new ConsumerBrokerService();

    /**
     * 负载均衡策略
     */
    protected ILoadBalance<RpcChannelFuture> loadBalance= LoadBalances.weightRoundRobbin();

    /**
     * 订阅最大尝试次数
     */
    protected int subscribeMaxAttempt=3;
    /**
     * 取消订阅最大尝试次数
     */
    protected int unSubscribeMaxAttempt=3;
    /**
     * 消费状态更新最大尝试次数
     */
    protected int consumerStatusMaxAttempt=3;

    /**
     * 账户标识
     */
    protected String appKey;
    /**
     * 账户密码
     */
    protected String appSecret;
    public String appKey() {
        return appKey;
    }

    public MqConsumerPush appKey(String appKey) {
        this.appKey = appKey;
        return this;
    }

    public String appSecret() {
        return appSecret;
    }

    public MqConsumerPush appSecret(String appSecret) {
        this.appSecret = appSecret;
        return this;
    }

    public MqConsumerPush consumerStatusMaxAttempt(int consumerStatusMaxAttempt) {
        this.consumerStatusMaxAttempt = consumerStatusMaxAttempt;
        return this;
    }

    public MqConsumerPush subscribeMaxAttempt(int subscribeMaxAttempt) {
        this.subscribeMaxAttempt = subscribeMaxAttempt;
        return this;
    }

    public MqConsumerPush unSubscribeMaxAttempt(int unSubscribeMaxAttempt) {
        this.unSubscribeMaxAttempt = unSubscribeMaxAttempt;
        return this;
    }

    public MqConsumerPush groupName(String groupName) {
        this.groupName = groupName;
        return this;
    }

    public MqConsumerPush brokerAddress(String brokerAddress) {
        this.brokerAddress = brokerAddress;
        return this;
    }

    public MqConsumerPush respTimeoutMills(long respTimeoutMills) {
        this.respTimeoutMills = respTimeoutMills;
        return this;
    }

    public MqConsumerPush check(boolean check) {
        this.check = check;
        return this;
    }

    public MqConsumerPush waitMillsForRemainRequest(long waitMillsForRemainRequest) {
        this.waitMillsForRemainRequest = waitMillsForRemainRequest;
        return this;
    }

    public MqConsumerPush loadBalance(ILoadBalance<RpcChannelFuture> loadBalance) {
        this.loadBalance = loadBalance;
        return this;
    }

    /**
     * 参数校验
     */
    private void paramCheck(){
        ArgUtil.notEmpty(brokerAddress,"brokerAddress");
        ArgUtil.notEmpty(groupName,"groupName");
    }

    @Override
    public void run(){
//        int port=9098;
//        // 启动服务端
//        log.info("MQ 消费者开始启动服务端 groupName: {}, port: {}, brokerAddress: {}",
//                groupName, port, brokerAddress);
//
//        EventLoopGroup bossGroup = new NioEventLoopGroup();
//        EventLoopGroup workerGroup = new NioEventLoopGroup();
//
//        try {
//            ServerBootstrap serverBootstrap = new ServerBootstrap();
//            serverBootstrap.group(workerGroup, bossGroup)
//                    .channel(NioServerSocketChannel.class)
//                    .childHandler(new ChannelInitializer<Channel>() {
//                        @Override
//                        protected void initChannel(Channel ch) throws Exception {
//                            ch.pipeline().addLast(new MqConsumerHandler());
//                        }
//                    })
//                    // 这个参数影响的是还没有被accept 取出的连接
//                    .option(ChannelOption.SO_BACKLOG, 128)
//                    // 这个参数只是过一段时间内客户端没有响应，服务端会发送一个 ack 包，以判断客户端是否还活着。
//                    .childOption(ChannelOption.SO_KEEPALIVE, true);
//
//            // 绑定端口，开始接收进来的链接
//            ChannelFuture channelFuture = serverBootstrap.bind(port).syncUninterruptibly();
//            log.info("MQ 消费者启动完成，监听【" + port + "】端口");
//
//            channelFuture.channel().closeFuture().syncUninterruptibly();
//            log.info("MQ 消费者关闭完成");
//        } catch (Exception e) {
//            log.error("MQ 消费者启动异常", e);
//            throw new MqException(ConsumerRespCode.RPC_INIT_FAILED);
//        } finally {
//            workerGroup.shutdownGracefully();
//            bossGroup.shutdownGracefully();
//        }




        //启动服务端
        log.info("MQ消费者开始启动服务端 groupName：{}，brokerAddress：{}",groupName,brokerAddress);

        //参数校验
        this.paramCheck();
        try{
            //配置信息
            ConsumerBrokerConfig config=ConsumerBrokerConfig.newInstance()
                    .groupName(groupName)
                    .brokerAddress(brokerAddress)
                    .check(check)
                    .respTimeoutMills(respTimeoutMills)
                    .invokeService(invokeService)
                    .statusManager(statusManager)
                    .mqListenerService(mqListenerService)
                    .loadBalance(loadBalance)
                    .subscribeMaxAttempt(subscribeMaxAttempt)
                    .unSubscribeMaxAttempt(unSubscribeMaxAttempt)
                    .consumerStatusMaxAttempt(consumerStatusMaxAttempt)
                    .appKey(appKey)
                    .appSecret(appSecret);

            //1.初始化
            this.consumerBrokerService.initChannelFutureList(config);

            //2.连接到服务端
            this.consumerBrokerService.registerToBroker();

            //3.标识为可用
            statusManager.status(true);

            //4.添加钩子函数
            final DefaultShutdownHook rpcShutdownHook=new DefaultShutdownHook();
            rpcShutdownHook.setStatusManager(statusManager);
            rpcShutdownHook.setInvokeService(invokeService);
            rpcShutdownHook.setWaitMillsForRemainRequest(waitMillsForRemainRequest);
            rpcShutdownHook.setDestroyable(this.consumerBrokerService);
            ShutdownHooks.rpcShutdownHook(rpcShutdownHook);

            //5.启动完成以后的事件

            this.afterInit();
            log.info("MQ消费者启动完成");
        }catch (Exception e){
            log.error("MQ 消费者启动异常");
            statusManager.initFailed(true);
            throw new MqException(ConsumerRespCode.RPC_INIT_FAILED);
        }
    }
    /**
     * 初始化完成以后
     */
    protected void afterInit() {

    }

    @Override
    public void subscribe(String topicName, String tagRegex) {
        final String consumerType=getConsumerType();
        consumerBrokerService.subscribe(topicName,tagRegex,consumerType);
    }

    @Override
    public void unSubscribe(String topicName, String tagRegex) {
        final String consumerType = getConsumerType();
        consumerBrokerService.unSubscribe(topicName, tagRegex, consumerType);
    }

    @Override
    public void registerListener(IMqConsumerListener listener) {
        this.mqListenerService.register(listener);
    }

    /**
     * 获取消费策略类型
     * @return
     */
    protected String getConsumerType(){
        return ConsumerTypeConst.PUSH;
    }
}
