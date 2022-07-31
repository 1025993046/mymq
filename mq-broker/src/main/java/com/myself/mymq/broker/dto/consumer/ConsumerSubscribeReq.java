package com.myself.mymq.broker.dto.consumer;

import com.myself.mymq.common.dto.req.MqCommonReq;

/**
 * 消费者注册入参
 * @author GuoZeWei
 * @date 2022/7/19  16:48
 */
public class ConsumerSubscribeReq extends MqCommonReq {
    /**
     * 分组类型
     */
    private String groupName;
    /**
     * 标题名称
     */
    private String topicName;
    /**
     * 标签正则
     */
    private String tagRegex;
    /**
     * 消费者类型
     */
    private String consumerType;
    public String getConsumerType() {
        return consumerType;
    }

    public void setConsumerType(String consumerType) {
        this.consumerType = consumerType;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getTopicName() {
        return topicName;
    }

    public void setTopicName(String topicName) {
        this.topicName = topicName;
    }

    public String getTagRegex() {
        return tagRegex;
    }

    public void setTagRegex(String tagRegex) {
        this.tagRegex = tagRegex;
    }
}
