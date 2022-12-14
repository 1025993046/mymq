package com.myself.mymq.producer.support.broker;

import com.alibaba.fastjson.JSON;
import com.github.houbb.heaven.util.util.DateUtil;
import com.github.houbb.id.core.util.IdHelper;
import com.github.houbb.load.balance.api.ILoadBalance;
import com.github.houbb.log.integration.core.Log;
import com.github.houbb.log.integration.core.LogFactory;
import com.github.houbb.sisyphus.api.core.Retry;
import com.github.houbb.sisyphus.core.core.Retryer;
import com.myself.mymq.broker.dto.BrokerRegisterReq;
import com.myself.mymq.broker.dto.ServiceEntry;
import com.myself.mymq.broker.utils.InnerChannelUtils;
import com.myself.mymq.common.constant.MethodType;
import com.myself.mymq.common.dto.req.MqCommonReq;
import com.myself.mymq.common.dto.req.MqMessage;
import com.myself.mymq.common.dto.req.MqMessageBatchReq;
import com.myself.mymq.common.dto.resp.MqCommonResp;
import com.myself.mymq.common.resp.MqCommonRespCode;
import com.myself.mymq.common.resp.MqException;
import com.myself.mymq.common.rpc.RpcChannelFuture;
import com.myself.mymq.common.rpc.RpcMessageDto;
import com.myself.mymq.common.support.invoke.IInvokeService;
import com.myself.mymq.common.support.status.IStatusManager;
import com.myself.mymq.common.util.ChannelFutureUtils;
import com.myself.mymq.common.util.ChannelUtil;
import com.myself.mymq.common.util.DelimiterUtil;
import com.myself.mymq.common.util.RandomUtils;
import com.myself.mymq.producer.constant.ProducerRespCode;
import com.myself.mymq.producer.constant.SendStatus;
import com.myself.mymq.producer.dto.SendBatchResult;
import com.myself.mymq.producer.dto.SendResult;
import com.myself.mymq.producer.handler.MqProducerHandler;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * @author GuoZeWei
 * @date 2022/7/22  16:59
 */
public class ProducerBrokerService implements IProducerBrokerService{
    private static final Log log = LogFactory.getLog(ProducerBrokerService.class);
    /**
     * 分组名称
     */
    private String groupName;

    /**
     * 中间人地址
     */
    private String brokerAddress;

    /**
     * 调用管理服务
     */
    private IInvokeService invokeService;

    /**
     * 获取响应超时时间
     */
    private long respTimeoutMills;

    /**
     * 请求列表
     */
    private List<RpcChannelFuture> channelFutureList;

    /**
     * 检测 broker 可用性
     */
    private boolean check;

    /**
     * 状态管理
     */
    private IStatusManager statusManager;

    /**
     * 负载均衡策略
     */
    private ILoadBalance<RpcChannelFuture> loadBalance;

    /**
     * 消息发送最大尝试次数
     */
    private int maxAttempt = 3;

    /**
     * 账户标识
     */
    private String appKey;

    /**
     * 账户密码
     */
    private String appSecret;

    @Override
    public void initChannelFutureList(ProducerBrokerConfig config) {
        //1.配置初始化
        this.invokeService=config.invokeService();
        this.check=config.check();
        this.respTimeoutMills=config.respTimeoutMills();
        this.brokerAddress=config.brokerAddress();
        this.groupName=config.groupName();
        this.statusManager=config.statusManager();
        this.loadBalance=config.loadBalance();
        this.maxAttempt=config.maxAttempt();
        this.appKey=config.appKey();
        this.appSecret=config.appSecret();
        //2.初始化
        this.channelFutureList= ChannelFutureUtils.initChannelFutureList(brokerAddress,
                initChannelHandler(),check);
    }

    private ChannelHandler initChannelHandler(){
        final ByteBuf delimiterBuf= DelimiterUtil.getByteBuf(DelimiterUtil.DELIMITER);

        final MqProducerHandler mqProducerHandler=new MqProducerHandler();
        mqProducerHandler.setInvokeService(invokeService);

        //handler实际上会被多次调用，如果不是@Shareable，应该每次都重新创建
        ChannelHandler handler=new ChannelInitializer<Channel>() {

            @Override
            protected void initChannel(Channel ch) throws Exception {
                ch.pipeline()
                        .addLast(new DelimiterBasedFrameDecoder(DelimiterUtil.LENGTH,delimiterBuf))
                        .addLast(mqProducerHandler);
            }
        };
        return handler;
    }

    @Override
    public void registerToBroker() {
        int successCount=0;
        for(RpcChannelFuture channelFuture:this.channelFutureList){
            ServiceEntry serviceEntry=new ServiceEntry();
            serviceEntry.setGroupName(groupName);
            serviceEntry.setAddress(channelFuture.getAddress());
            serviceEntry.setPort(channelFuture.getPort());
            serviceEntry.setWeight(channelFuture.getWeight());

            BrokerRegisterReq brokerRegisterReq=new BrokerRegisterReq();
            brokerRegisterReq.setServiceEntry(serviceEntry);
            brokerRegisterReq.setMethodType(MethodType.P_REGISTER);
            brokerRegisterReq.setTraceId(IdHelper.uuid32());
            brokerRegisterReq.setAppKey(appKey);
            brokerRegisterReq.setAppSecret(appSecret);

            log.info("[Register] 开始注册到 broker: {}", JSON.toJSON(brokerRegisterReq));
            final Channel channel=channelFuture.getChannelFuture().channel();
            MqCommonResp resp=callServer(channel,brokerRegisterReq,MqCommonResp.class);
            log.info("[Register] 完成注册到 broker：{}",JSON.toJSON(resp));

            if (MqCommonRespCode.SUCCESS.getCode().equals(resp.getRespCode())){
                successCount++;
            }
        }
        if (successCount<=0 && check){
            log.error("校验 broker 可用性,可连接成功数为0");
            throw new MqException(MqCommonRespCode.P_REGISTER_TO_BROKER_FAILED);
        }
    }

    @Override
    public <T extends MqCommonReq, R extends MqCommonResp> R callServer(Channel channel, T commonReq, Class<R> respClass) {
        final String traceId =commonReq.getTraceId();
        final long requestTime=System.currentTimeMillis();

        RpcMessageDto rpcMessageDto=new RpcMessageDto();
        rpcMessageDto.setTraceId(traceId);
        rpcMessageDto.setRequestTime(requestTime);
        rpcMessageDto.setJson(JSON.toJSONString(commonReq));
        rpcMessageDto.setMethodType(commonReq.getMethodType());
        rpcMessageDto.setRequest(true);
        //添加调用服务
        invokeService.addRequest(traceId,respTimeoutMills);
        //遍历channel
        //关闭当前线程，以获取对应的信息
        //使用序列化的方式
        ByteBuf byteBuf=DelimiterUtil.getMessageDelimiterBuffer(rpcMessageDto);
        //负载均衡获取channel
        channel.writeAndFlush(byteBuf);

        String channelId= ChannelUtil.getChannelId(channel);
        log.debug("[Client] channelId {} 发送消息 {} ",channelId,JSON.toJSON(rpcMessageDto));

        if(respClass==null){
            log.debug("[Client] 当前消息为one-way消息，忽略响应");
            return null;
        }else{
            //channelHandler 中获取对应的响应
            RpcMessageDto messageDto=invokeService.getResponse(traceId);
            if (MqCommonRespCode.TIMEOUT.getCode().equals(messageDto.getRespCode())){
                throw new MqException(MqCommonRespCode.TIMEOUT);
            }
            String respJson=messageDto.getJson();
            return JSON.parseObject(respJson,respClass);
        }
    }

    @Override
    public Channel getChannel(String key) {
        //等待启动完成
        while(!statusManager.status()){
            if(statusManager.initFailed()){
                log.error("初始化失败");
                throw new MqException(MqCommonRespCode.P_INIT_FAILED);
            }
            log.debug("等待初始化完成。。。");
            DateUtil.sleep(100);
        }
        RpcChannelFuture rpcChannelFuture= RandomUtils.loadBalance(this.loadBalance,channelFutureList,key);
        return rpcChannelFuture.getChannelFuture().channel();
    }

    @Override
    public SendResult send(final MqMessage mqMessage) {
        final String messageId=IdHelper.uuid32();
        mqMessage.setTraceId(messageId);
        mqMessage.setMethodType(MethodType.P_SEND_MSG);
        mqMessage.setGroupName(groupName);
        return Retryer.<SendResult>newInstance()
                .maxAttempt(maxAttempt)
                .callable(new Callable<SendResult>() {
                    @Override
                    public SendResult call() throws Exception {
                        return doSend(messageId,mqMessage);
                    }
                }).retryCall();
    }

    private SendResult doSend(String messageId,MqMessage mqMessage){
        log.info("[Producer] 发送消息 messageId： {} ，mqMessage ：{}",messageId,JSON.toJSON(mqMessage));
        Channel channel=getChannel(mqMessage.getShardingKey());
        MqCommonResp resp=callServer(channel,mqMessage,MqCommonResp.class);
        if(MqCommonRespCode.SUCCESS.getCode().equals(resp.getRespCode())){
            return SendResult.of(messageId, SendStatus.SUCCESS);
        }
        throw new MqException(ProducerRespCode.MSG_SEND_FAILED);
    }


    @Override
    public SendResult sendOneWay(MqMessage mqMessage) {
        String messageId=IdHelper.uuid32();
        mqMessage.setTraceId(messageId);
        mqMessage.setMethodType(MethodType.P_SEND_MSG_ONE_WAY);
        mqMessage.setGroupName(groupName);

        Channel channel=getChannel(mqMessage.getShardingKey());
        this.callServer(channel,mqMessage,null);
        return SendResult.of(messageId,SendStatus.SUCCESS);
    }

    @Override
    public SendBatchResult sendBatch(List<MqMessage> mqMessageList) {
        final List<String> messageIdList=this.fillMessageList(mqMessageList);
        final MqMessageBatchReq batchReq=new MqMessageBatchReq();
        batchReq.setMqMessageList(mqMessageList);
        String traceId=IdHelper.uuid32();
        batchReq.setTraceId(traceId);
        batchReq.setMethodType(MethodType.P_SEND_MSG_BATCH);

        return Retryer.<SendBatchResult>newInstance()
                .maxAttempt(maxAttempt)
                .callable(new Callable<SendBatchResult>() {
                    @Override
                    public SendBatchResult call() throws Exception {
                        return doSendBatch(messageIdList,batchReq,false);
                    }
                }).retryCall();
    }

    private SendBatchResult doSendBatch(List<String> messageIdList,
                                        MqMessageBatchReq batchReq,
                                        boolean oneWay){
        log.info("[Producer] 批量发送消息 messageIdList： {}，batchReq：{}，oneWay：{}",
                messageIdList,JSON.toJSON(batchReq),oneWay);
        //以第一个sharding-key为准
        //后续会被忽略
        MqMessage mqMessage=batchReq.getMqMessageList().get(0);
        Channel channel=getChannel(mqMessage.getShardingKey());

        //one-way
        if(oneWay){
            log.warn("[Producer] ONE-WAY send,ignore result");
            return SendBatchResult.of(messageIdList,SendStatus.SUCCESS);
        }
        MqCommonResp resp=callServer(channel,batchReq,MqCommonResp.class);
        if(MqCommonRespCode.SUCCESS.getCode().equals(resp.getRespCode())){
            return SendBatchResult.of(messageIdList,SendStatus.SUCCESS);
        }
        throw new MqException(ProducerRespCode.MSG_SEND_FAILED);
    }

    private List<String> fillMessageList(final List<MqMessage> mqMessageList){
        List<String> idList=new ArrayList<>(mqMessageList.size());
        for(MqMessage mqMessage:mqMessageList){
            String messageId=IdHelper.uuid32();
            mqMessage.setTraceId(messageId);
            mqMessage.setGroupName(groupName);
            idList.add(messageId);
        }
        return idList;
    }

    @Override
    public SendBatchResult sendOneWayBatch(List<MqMessage> mqMessageList) {
        List<String> messageIdLIst=this.fillMessageList(mqMessageList);

        MqMessageBatchReq batchReq=new MqMessageBatchReq();
        batchReq.setMqMessageList(mqMessageList);
        String traceId=IdHelper.uuid32();
        batchReq.setTraceId(traceId);
        batchReq.setMethodType(MethodType.P_SEND_MSG_ONE_WAY_BATCH);
        return doSendBatch(messageIdLIst,batchReq,true);
    }

    @Override
    public void destroyAll() {
        for(RpcChannelFuture channelFuture:channelFutureList){
            Channel channel=channelFuture.getChannelFuture().channel();
            final String channelId=ChannelUtil.getChannelId(channel);
            log.info("开始注销：{}",channelId);

            ServiceEntry serviceEntry= InnerChannelUtils.buildServiceEntry(channelFuture);

            BrokerRegisterReq brokerRegisterReq=new BrokerRegisterReq();
            brokerRegisterReq.setServiceEntry(serviceEntry);
            String messageId=IdHelper.uuid32();
            brokerRegisterReq.setTraceId(messageId);
            brokerRegisterReq.setMethodType(MethodType.P_UN_REGISTER);
            this.callServer(channel,brokerRegisterReq,null);
            log.info("完成注销：{}",channelId);
        }
    }
}
