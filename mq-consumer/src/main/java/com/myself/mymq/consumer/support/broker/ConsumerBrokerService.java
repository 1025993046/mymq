package com.myself.mymq.consumer.support.broker;

import com.alibaba.fastjson.JSON;
import com.github.houbb.heaven.util.net.NetUtil;
import com.github.houbb.heaven.util.util.DateUtil;
import com.github.houbb.heaven.util.util.RandomUtil;
import com.github.houbb.id.core.util.IdHelper;
import com.github.houbb.load.balance.api.ILoadBalance;
import com.github.houbb.log.integration.core.Log;
import com.github.houbb.log.integration.core.LogFactory;
import com.github.houbb.sisyphus.core.core.Retryer;
import com.myself.mymq.broker.dto.BrokerRegisterReq;
import com.myself.mymq.broker.dto.ServiceEntry;
import com.myself.mymq.broker.dto.consumer.ConsumerSubscribeReq;
import com.myself.mymq.broker.dto.consumer.ConsumerUnSubscribeReq;
import com.myself.mymq.broker.utils.InnerChannelUtils;
import com.myself.mymq.common.constant.MethodType;
import com.myself.mymq.common.dto.req.*;
import com.myself.mymq.common.dto.req.component.MqConsumerUpdateStatusDto;
import com.myself.mymq.common.dto.resp.MqCommonResp;
import com.myself.mymq.common.dto.resp.MqConsumerPullResp;
import com.myself.mymq.common.resp.ConsumerStatus;
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
import com.myself.mymq.consumer.constant.ConsumerRespCode;
import com.myself.mymq.consumer.handler.MqConsumerHandler;
import com.myself.mymq.consumer.support.listener.IMqListenerService;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;

import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author GuoZeWei
 * @date 2022/7/13  23:43
 */
public class ConsumerBrokerService implements IConsumerBrokerService{

    private static final Log log= LogFactory.getLog(ConsumerBrokerService.class);

    /**
     * ????????????
     */
    private String groupName;

    /**
     * ???????????????
     */
    private String brokerAddress;

    /**
     * ??????????????????
     */
    private IInvokeService invokeService;

    /**
     * ????????????????????????
     */
    private long respTimeoutMills;

    /**
     * ????????????
     */
    private List<RpcChannelFuture> channelFutureList;

    /**
     * ??????broker?????????
     */
    private boolean check;

    /**
     * ????????????
     */
    private IStatusManager statusManager;

    /**
     * ???????????????
     */
    private IMqListenerService mqListenerService;

    /**
     * ??????????????????
     */
    private final ScheduledExecutorService scheduledExecutorService= Executors.newSingleThreadScheduledExecutor();

    /**
     * ??????????????????
     */
    private ILoadBalance<RpcChannelFuture> loadBalance;

    /**
     * ????????????????????????
     */
    private int subscribeMaxAttempt;

    /**
     * ??????????????????????????????
     */
    private int unSubscribeMaxAttempt;

    /**
     * ????????????????????????????????????
     */
    private int consumerStatusMaxAttempt;

    /**
     * ????????????
     */
    protected String appKey;

    /**
     * ????????????
     */
    protected String appSecret;

    @Override
    public void initChannelFutureList(ConsumerBrokerConfig config) {
        //1.???????????????
        this.invokeService=config.invokeService();
        this.check=config.check();
        this.respTimeoutMills=config.respTimeoutMills();
        this.brokerAddress=config.brokerAddress();
        this.groupName=config.groupName();
        this.statusManager=config.statusManager();
        this.mqListenerService=config.mqListenerService();
        this.loadBalance=config.loadBalance();
        this.subscribeMaxAttempt=config.subscribeMaxAttempt();
        this.unSubscribeMaxAttempt=config.unSubscribeMaxAttempt();
        this.consumerStatusMaxAttempt=config.consumerStatusMaxAttempt();
        this.appKey=config.appKey();
        this.appSecret=config.appSecret();
        //2.?????????
        this.channelFutureList= ChannelFutureUtils.initChannelFutureList(brokerAddress,
                initChannelHandler(),check);
        //3.???????????????
        this.initHeartbeat();
    }

    /**
     * ???????????????
     */
    private void initHeartbeat(){
        //????????????????????????5s???????????????
        scheduledExecutorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                heartbeat();;
            }
        },5,5, TimeUnit.SECONDS);
    }

    private ChannelHandler initChannelHandler(){
        final ByteBuf delimiterBuf= DelimiterUtil.getByteBuf(DelimiterUtil.DELIMITER);

        final MqConsumerHandler mqProducerHandler=new MqConsumerHandler();
        mqProducerHandler.setInvokeService(invokeService);
        mqProducerHandler.setMqListenerService(mqListenerService);

        //handler??????????????????????????????????????????@Shareable??????????????????????????????
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
            brokerRegisterReq.setMethodType(MethodType.C_REGISTER);
            brokerRegisterReq.setTraceId(IdHelper.uuid32());
            brokerRegisterReq.setAppKey(appKey);
            brokerRegisterReq.setAppSecret(appSecret);

            log.info("[Register] ???????????????broker???{}", JSON.toJSON(brokerRegisterReq));
            final Channel channel=channelFuture.getChannelFuture().channel();
            MqCommonResp resp=callServer(channel,brokerRegisterReq,MqCommonResp.class);
            log.info("[Register] ???????????????broker???{}",JSON.toJSON(resp));
            if(MqCommonRespCode.SUCCESS.getCode().equals(resp.getRespCode())){
                successCount++;
            }
        }
        if(successCount<=0 && check){
            log.error("[?????? broker ????????????????????????????????? 0");
            throw new MqException(MqCommonRespCode.C_REGISTER_TO_BROKER_FAILED);
        }
    }

    @Override
    public <T extends MqCommonReq, R extends MqCommonResp>
    R callServer(Channel channel, T commonReq, Class<R> respClass) {
        final String traceId=commonReq.getTraceId();
        final long requestTime=System.currentTimeMillis();

        RpcMessageDto rpcMessageDto=new RpcMessageDto();
        rpcMessageDto.setTraceId(traceId);
        rpcMessageDto.setRequestTime(requestTime);
        rpcMessageDto.setJson(JSON.toJSONString(commonReq));
        rpcMessageDto.setMethodType(commonReq.getMethodType());
        rpcMessageDto.setRequest(true);

        //??????????????????
        invokeService.addRequest(traceId,respTimeoutMills);

        //??????channel
        //?????????????????????????????????????????????
        //????????????????????????
        ByteBuf byteBuf=DelimiterUtil.getMessageDelimiterBuffer(rpcMessageDto);

        //??????????????????channel
        channel.writeAndFlush(byteBuf);

        String channelId= ChannelUtil.getChannelId(channel);
        log.debug("[Client] channelId {} ???????????? {} ",channelId,JSON.toJSON(rpcMessageDto));
//        channel.closeFuture().syncUninterruptibly();

        if (respClass==null){
            log.debug("[Client] ??????????????? one-way ?????????????????????");
            return null;
        }else{
            //channelHandler ????????????????????????
            RpcMessageDto messageDto=invokeService.getResponse(traceId);
            if(MqCommonRespCode.TIMEOUT.getCode().equals(messageDto.getRespCode())){
                throw new MqException(MqCommonRespCode.TIMEOUT);
            }
            String respJson=messageDto.getJson();
            return JSON.parseObject(respJson,respClass);
        }
    }

    @Override
    public Channel getChannel(String key) {
        //??????????????????
        while(!statusManager.status()){
            if (statusManager.initFailed()){
                log.error("???????????????");
                throw new MqException(MqCommonRespCode.C_INIT_FAILED);
            }
            log.debug("??????????????????????????????");
            DateUtil.sleep(100);
        }
        RpcChannelFuture rpcChannelFuture= RandomUtils.loadBalance(loadBalance,
                channelFutureList,key);
        return rpcChannelFuture.getChannelFuture().channel();
    }

    @Override
    public void subscribe(String topicName, String tagRegex, String consumerType) {
        final ConsumerSubscribeReq req=new ConsumerSubscribeReq();
        String messageId=IdHelper.uuid32();
        req.setTraceId(messageId);
        req.setMethodType(MethodType.C_SUBSCRIBE);
        req.setTopicName(topicName);
        req.setTagRegex(tagRegex);
        req.setGroupName(groupName);
        req.setConsumerType(consumerType);
        //????????????
        Retryer.<String>newInstance()
                .maxAttempt(subscribeMaxAttempt)
                .callable(new Callable<String>() {
                    @Override
                    public String call() throws Exception {
                        Channel channel=getChannel(null);
                        MqCommonResp resp=callServer(channel,req,MqCommonResp.class);
                        if(!MqCommonRespCode.SUCCESS.getCode().equals(resp.getRespCode())){
                            throw new MqException(ConsumerRespCode.SUBSCRIBE_FAILED);
                        }
                        return resp.getRespCode();
                    }
                }).retryCall();
    }



    @Override
    public void unSubscribe(String topicName, String tagRegex, String consumerType) {
        final ConsumerUnSubscribeReq req=new ConsumerUnSubscribeReq();
        String messageId=IdHelper.uuid32();
        req.setTraceId(messageId);
        req.setMethodType(MethodType.C_UN_SUBSCRIBE);
        req.setTopicName(topicName);
        req.setTagRegex(tagRegex);
        req.setGroupName(groupName);
        req.setConsumerType(consumerType);

        //??????????????????
        Retryer.<String>newInstance()
                .maxAttempt(unSubscribeMaxAttempt)
                .callable(new Callable<String>() {
                    @Override
                    public String call() throws Exception {
                        Channel channel=getChannel(null);
                        MqCommonResp resp=callServer(channel,req,MqCommonResp.class);
                        if(!MqCommonRespCode.SUCCESS.equals(resp.getRespCode())){
                            throw new MqException(ConsumerRespCode.UN_SUBSCRIBE_FAILED);
                        }
                        return resp.getRespCode();
                    }
                });
    }

    @Override
    public void heartbeat() {
        final MqHeartBeatReq req=new MqHeartBeatReq();
        final String traceId=IdHelper.uuid32();
        req.setTraceId(traceId);
        req.setMethodType(MethodType.C_HEARTBEAT);
        req.setAddress(NetUtil.getLocalHost());
        req.setPort(0);
        req.setTime(System.currentTimeMillis());

        /**
         *
         * ????????????????????????
         *
         */
//        log.debug("[HEARTBEAT] ??????????????????????????? {}",JSON.toJSON(req));
        //????????????
        for(RpcChannelFuture channelFuture:channelFutureList){
            try{
                Channel channel=channelFuture.getChannelFuture().channel();
                callServer(channel,req,null);
            }catch (Exception exception){
                log.error("[HEARTBEAT] ????????????????????????",exception);
            }
        }
    }

    @Override
    public MqConsumerPullResp pull(String topicName, String tagRegex, int fetchSize) {
        MqConsumerPullReq req=new MqConsumerPullReq();
        req.setSize(fetchSize);
        req.setGroupName(groupName);
        req.setTagRegex(tagRegex);
        req.setTopicName(topicName);
        final String traceId=IdHelper.uuid32();
        req.setTraceId(traceId);
        req.setMethodType(MethodType.C_MESSAGE_PULL);
        Channel channel=getChannel(null);
        return this.callServer(channel,req,MqConsumerPullResp.class);
    }

    @Override
    public MqCommonResp consumerStatusAck(String messageId, ConsumerStatus consumerStatus) {
        final MqConsumerUpdateStatusReq req=new MqConsumerUpdateStatusReq();
        req.setMessageId(messageId);
        req.setMessageStatus(consumerStatus.getCode());
        req.setConsumerGroupName(groupName);

        final String traceId=IdHelper.uuid32();
        req.setTraceId(traceId);
        req.setMethodType(MethodType.C_CONSUMER_STATUS);
        //??????
        return Retryer.<MqCommonResp>newInstance()
                .maxAttempt(consumerStatusMaxAttempt)
                .callable(new Callable<MqCommonResp>() {
                    @Override
                    public MqCommonResp call() throws Exception {
                        Channel channel=getChannel(null);
                        MqCommonResp resp=callServer(channel,req,MqCommonResp.class);
                        if(!MqCommonRespCode.SUCCESS.getCode().equals(resp.getRespCode())){
                            throw new MqException(ConsumerRespCode.CONSUMER_STATUS_ACK_FAILED);
                        }
                        return resp;
                    }
                }).retryCall();
    }

    @Override
    public MqCommonResp consumerStatusAckBatch(List<MqConsumerUpdateStatusDto> statusDtoList) {
        final MqConsumerUpdateStatusBatchReq req=new MqConsumerUpdateStatusBatchReq();
        req.setStatusList(statusDtoList);
        final String tarceId=IdHelper.uuid32();
        req.setTraceId(tarceId);
        req.setMethodType(MethodType.C_CONSUMER_STATUS_BATCH);

        //??????
        return Retryer.<MqCommonResp>newInstance()
                .maxAttempt(consumerStatusMaxAttempt)
                .callable(new Callable<MqCommonResp>() {
                    @Override
                    public MqCommonResp call() throws Exception {
                        Channel channel=getChannel(null);
                        MqCommonResp resp=callServer(channel,req,MqCommonResp.class);
                        if(!MqCommonRespCode.SUCCESS.getCode().equals(resp.getRespCode())){
                            throw new MqException(ConsumerRespCode.CONSUMER_STATUS_ACK_BATCH_FAILED);
                        }
                        return resp;
                    }
                }).retryCall();
    }

    @Override
    public void destroyAll() {
        for(RpcChannelFuture channelFuture:channelFutureList){
            Channel channel=channelFuture.getChannelFuture().channel();
            final String channelId=ChannelUtil.getChannelId(channel);
            log.info("???????????????{}",channelId);

            ServiceEntry serviceEntry= InnerChannelUtils.buildServiceEntry(channelFuture);

            BrokerRegisterReq brokerRegisterReq=new BrokerRegisterReq();
            brokerRegisterReq.setServiceEntry(serviceEntry);
            String messageId=IdHelper.uuid32();
            brokerRegisterReq.setTraceId(messageId);
            brokerRegisterReq.setMethodType(MethodType.C_UN_REGISTER);
            this.callServer(channel,brokerRegisterReq,null);
            log.info("???????????????{}",channelId);
        }
    }
}
