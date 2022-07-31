package com.myself.mymq.producer.core;

import com.github.houbb.heaven.util.common.ArgUtil;
import com.github.houbb.load.balance.api.ILoadBalance;
import com.github.houbb.load.balance.api.impl.LoadBalances;
import com.github.houbb.log.integration.core.Log;
import com.github.houbb.log.integration.core.LogFactory;
import com.myself.mymq.common.dto.req.MqMessage;
import com.myself.mymq.common.resp.MqException;
import com.myself.mymq.common.rpc.RpcChannelFuture;
import com.myself.mymq.common.support.hook.DefaultShutdownHook;
import com.myself.mymq.common.support.hook.ShutdownHooks;
import com.myself.mymq.common.support.invoke.IInvokeService;
import com.myself.mymq.common.support.invoke.impl.InvokeService;
import com.myself.mymq.common.support.status.IStatusManager;
import com.myself.mymq.common.support.status.StatusManager;
import com.myself.mymq.producer.api.IMqProducer;
import com.myself.mymq.producer.constant.ProducerConst;
import com.myself.mymq.producer.constant.ProducerRespCode;
import com.myself.mymq.producer.dto.SendBatchResult;
import com.myself.mymq.producer.dto.SendResult;
import com.myself.mymq.producer.handler.MqProducerHandler;
import com.myself.mymq.producer.support.broker.IProducerBrokerService;
import com.myself.mymq.producer.support.broker.ProducerBrokerConfig;
import com.myself.mymq.producer.support.broker.ProducerBrokerService;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

import java.util.List;

/**
 * 默认mq生产者
 * @author GuoZeWei
 * @date 2022/7/20  17:36
 */
public class MqProducer extends Thread implements IMqProducer {

    private static final Log log = LogFactory.getLog(MqProducer.class);

    /**
     * 分组名称
     */
    private String groupName= ProducerConst.DEFAULT_GROUP_NAME;

    /**
     * 中间人地址，即为broker
     */
    private String brokerAddress="127.0.0.1:9999";

    /**
     * 获取响应超时时间
     */
    private long respTimeoutMills=5000;

    /**
     * 检测broker可用性
     */
    private volatile boolean check=true;

    /**
     * 调用管理服务
     */
    private final IInvokeService invokeService=new InvokeService();

    /**
     * 状态管理类
     */
    private final IStatusManager statusManager=new StatusManager();

    /**
     * 生产者-中间服务端服务类
     */
    private final IProducerBrokerService producerBrokerService=new ProducerBrokerService();

    /**
     * 为剩余的请求等待时间
     */
    private long waitMillsForRemainRequest=60*1000;

    /**
     * 负载均衡策略
     */
    private ILoadBalance<RpcChannelFuture> loadBalance= LoadBalances.weightRoundRobbin();

    /**
     * 消息发送最大尝试次数
     */
    private int maxAttempt=3;

    /**
     * 账户标识
     */
    private String appKey;

    /**
     * 账户密码
     */
    private String appSecret;

    public MqProducer appKey(String appKey) {
        this.appKey = appKey;
        return this;
    }

    public MqProducer appSecret(String appSecret) {
        this.appSecret = appSecret;
        return this;
    }

    public MqProducer groupName(String groupName) {
        this.groupName = groupName;
        return this;
    }

    public MqProducer brokerAddress(String brokerAddress) {
        this.brokerAddress = brokerAddress;
        return this;
    }

    public MqProducer respTimeoutMills(long respTimeoutMills) {
        this.respTimeoutMills = respTimeoutMills;
        return this;
    }

    public MqProducer check(boolean check) {
        this.check = check;
        return this;
    }

    public MqProducer waitMillsForRemainRequest(long waitMillsForRemainRequest) {
        this.waitMillsForRemainRequest = waitMillsForRemainRequest;
        return this;
    }

    public MqProducer loadBalance(ILoadBalance<RpcChannelFuture> loadBalance) {
        this.loadBalance = loadBalance;
        return this;
    }

    public MqProducer maxAttempt(int maxAttempt) {
        this.maxAttempt = maxAttempt;
        return this;
    }

    /**
     * 参数校验
     */
    private void paramCheck(){
        ArgUtil.notEmpty(groupName,"groupName");
        ArgUtil.notEmpty(brokerAddress,"brokerAddress");
    }

    @Override
    public void run() {
        this.paramCheck();
        //启动服务端
        log.info("MQ生产者开始启动客户端 GROUP：{} brokerAddress：{}",groupName,brokerAddress);

        try{
            //0.配置信息
            ProducerBrokerConfig config=ProducerBrokerConfig.newInstance()
                    .groupName(groupName)
                    .brokerAddress(brokerAddress)
                    .check(check)
                    .respTimeoutMills(respTimeoutMills)
                    .invokeService(invokeService)
                    .statusManager(statusManager)
                    .loadBalance(loadBalance)
                    .maxAttempt(maxAttempt)
                    .appKey(appKey)
                    .appSecret(appSecret);
            //1.初始化
            this.producerBrokerService.initChannelFutureList(config);
            //2.连接到服务端
            this.producerBrokerService.registerToBroker();
            //3.标识为可用
            statusManager.status(true);
            //4.添加钩子函数
            final DefaultShutdownHook rpcShutdownHook=new DefaultShutdownHook();
            rpcShutdownHook.setStatusManager(statusManager);
            rpcShutdownHook.setInvokeService(invokeService);
            rpcShutdownHook.setWaitMillsForRemainRequest(waitMillsForRemainRequest);
            rpcShutdownHook.setDestroyable(this.producerBrokerService);
            ShutdownHooks.rpcShutdownHook(rpcShutdownHook);

            log.info("MQ生产者启动完成");
        }catch (Exception e){
            log.error("MQ生产者启动遇到异常",e);
            //设置为初始化失败
            statusManager.initFailed(true);
            throw new MqException(ProducerRespCode.RPC_INIT_FAILED);
        }



//        // 启动服务端
//        log.info("MQ 生产者开始启动客户端 GROUP: {}, PORT: {}, brokerAddress: {}",
//                groupName, port, brokerAddress);
//
//        EventLoopGroup workerGroup = new NioEventLoopGroup();
//
//        try {
//            Bootstrap bootstrap = new Bootstrap();
//            ChannelFuture channelFuture = bootstrap.group(workerGroup)
//                    .channel(NioSocketChannel.class)
//                    .option(ChannelOption.SO_KEEPALIVE, true)
//                    .handler(new ChannelInitializer<Channel>(){
//                        @Override
//                        protected void initChannel(Channel ch) throws Exception {
//                            ch.pipeline()
//                                    .addLast(new LoggingHandler(LogLevel.INFO))
//                                    .addLast(new MqProducerHandler());
//                        }
//                    })
//                    .connect("localhost", port)
//                    .syncUninterruptibly();
//
//            log.info("MQ 生产者启动客户端完成，监听端口：" + port);
//            channelFuture.channel().closeFuture().syncUninterruptibly();
//            log.info("MQ 生产者开始客户端已关闭");
//        } catch (Exception e) {
//            log.error("MQ 生产者启动遇到异常", e);
//            throw new MqException(ProducerRespCodee.RPC_INIT_FAILED);
//        } finally {
//            workerGroup.shutdownGracefully();
//        }
    }

    @Override
    public SendResult send(MqMessage mqMessage) {
        return this.producerBrokerService.send(mqMessage);
    }

    @Override
    public SendResult sendOneWay(MqMessage mqMessage) {
        return this.producerBrokerService.sendOneWay(mqMessage);
    }

    @Override
    public SendBatchResult sendBatch(List<MqMessage> mqMessageList) {
        return producerBrokerService.sendBatch(mqMessageList);
    }

    @Override
    public SendBatchResult sendOneWayBatch(List<MqMessage> mqMessageList) {
        return producerBrokerService.sendOneWayBatch(mqMessageList);
    }
}
