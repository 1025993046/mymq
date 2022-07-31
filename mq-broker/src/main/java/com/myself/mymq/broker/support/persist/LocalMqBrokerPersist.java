package com.myself.mymq.broker.support.persist;

import com.alibaba.fastjson.JSON;
import com.github.houbb.heaven.util.util.CollectionUtil;
import com.github.houbb.heaven.util.util.MapUtil;
import com.github.houbb.heaven.util.util.regex.RegexUtil;
import com.github.houbb.log.integration.core.Log;
import com.github.houbb.log.integration.core.LogFactory;
import com.myself.mymq.broker.dto.persist.MqMessagePersistPut;
import com.myself.mymq.common.constant.MessageStatusConst;
import com.myself.mymq.common.dto.req.MqConsumerPullReq;
import com.myself.mymq.common.dto.req.MqMessage;
import com.myself.mymq.common.dto.req.component.MqConsumerUpdateStatusDto;
import com.myself.mymq.common.dto.resp.MqCommonResp;
import com.myself.mymq.common.dto.resp.MqConsumerPullResp;
import com.myself.mymq.common.resp.MqCommonRespCode;
import io.netty.channel.Channel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 本地持久化策略
 * @author GuoZeWei
 * @date 2022/7/27  21:22
 */
public class LocalMqBrokerPersist implements IMqBrokerPersist{
    private static final Log log = LogFactory.getLog(LocalMqBrokerPersist.class);

    /**
     * 队列
     * 这里只是简化实现，暂时不考虑并发等问题
     */
    private final Map<String,List<MqMessagePersistPut>> map=new ConcurrentHashMap<>();

    //1.接收
    //2.持久化
    //3.通知消费

    @Override
    public MqCommonResp put(MqMessagePersistPut put) {
        this.doPut(put);
        MqCommonResp commonResp=new MqCommonResp();
        commonResp.setRespCode(MqCommonRespCode.SUCCESS.getCode());
        commonResp.setRespMessage(MqCommonRespCode.SUCCESS.getMsg());
        return commonResp;
    }
    private void doPut(MqMessagePersistPut put){
        log.info("put elem ：{}", JSON.toJSON(put));

        MqMessage mqMessage=put.getMqMessage();
        final String topic=mqMessage.getTopic();
        //放入元素
        MapUtil.putToListMap(map,topic,put);
    }

    @Override
    public MqCommonResp putBatch(List<MqMessagePersistPut> putList) {
        //构建列表
        for(MqMessagePersistPut put:putList){
            this.doPut(put);
        }

        MqCommonResp commonResp = new MqCommonResp();
        commonResp.setRespCode(MqCommonRespCode.SUCCESS.getCode());
        commonResp.setRespMessage(MqCommonRespCode.SUCCESS.getMsg());
        return commonResp;
    }

    @Override
    public MqCommonResp updateStatus(String messageId, String consumerGroupName, String status) {
        //这里性能比较差，所以不适合用于生产，仅仅作为测试
        this.doUpdateStatus(messageId, consumerGroupName, status);

        MqCommonResp commonResp = new MqCommonResp();
        commonResp.setRespCode(MqCommonRespCode.SUCCESS.getCode());
        commonResp.setRespMessage(MqCommonRespCode.SUCCESS.getMsg());
        return commonResp;
    }
    private void doUpdateStatus(String messageId,String consumerGroupName,String status){
        //这里性能比较差，所以不可以用于生产，仅仅作为测试验证
        for (List<MqMessagePersistPut> list:map.values()){
            for(MqMessagePersistPut put:list){
                MqMessage mqMessage=put.getMqMessage();
                if(mqMessage.getTraceId().equals(messageId)){
                    put.setMessageStatus(status);
                    break;
                }
            }
        }
    }

    @Override
    public MqCommonResp updateStatusBatch(List<MqConsumerUpdateStatusDto> statusDtoList) {
        for(MqConsumerUpdateStatusDto statusDto:statusDtoList){
            this.doUpdateStatus(statusDto.getMessageId(),statusDto.getConsumerGroupName(),
                    statusDto.getMessageStatus());

        }
        MqCommonResp commonResp = new MqCommonResp();
        commonResp.setRespCode(MqCommonRespCode.SUCCESS.getCode());
        commonResp.setRespMessage(MqCommonRespCode.SUCCESS.getMsg());
        return commonResp;
    }

    //pull,这里是模拟实现数据库的持久化
    @Override
    public MqConsumerPullResp pull(MqConsumerPullReq pullReq, Channel channel) {
        //1.拉取匹配的信息
        //2.状态更新为代理中
        //3.如何更新对应的消费状态

        //获取状态为W的订单
        final int fetchSize=pullReq.getSize();
        final String topic=pullReq.getTopicName();
        final String tagRegex=pullReq.getTagRegex();

        List<MqMessage> resultList=new ArrayList<>();
        List<MqMessagePersistPut> putList=map.get(topic);
        //性能比较差
        if(CollectionUtil.isNotEmpty(putList)){
            for(MqMessagePersistPut put:putList){
                if(!isEnableStatus(put)){
                    continue;
                }

                final MqMessage mqMessage=put.getMqMessage();
                List<String> tagList=mqMessage.getTags();
                if(RegexUtil.hasMatch(tagList,tagRegex)){
                    //设置为处理中
                    //TODO：消息的最终状态什么时候更新呢？
                    //可以给broker一个ACK
                    put.setMessageStatus(MessageStatusConst.TO_CONSUMER_PROCESS);
                    resultList.add(mqMessage);
                }

                if(resultList.size()>=fetchSize){
                    break;
                }
            }
        }
        MqConsumerPullResp resp = new MqConsumerPullResp();
        resp.setRespCode(MqCommonRespCode.SUCCESS.getCode());
        resp.setRespMessage(MqCommonRespCode.SUCCESS.getMsg());
        resp.setList(resultList);
        return resp;
    }

    private boolean isEnableStatus(final MqMessagePersistPut persistPut){
        final String status=persistPut.getMessageStatus();
        //数据库可以设计一个字段，比如待消费时间，进行排序
        //这里只是简化实现，仅用于测试
        List<String> statusList= Arrays.asList(MessageStatusConst.WAIT_CONSUMER,
                MessageStatusConst.CONSUMER_LATER);
        return statusList.contains(status);
    }
}
