package com.myself.mymq.common.dto.req;

import java.util.List;

/**
 * @author GuoZeWei
 * @date 2022/7/5  23:06
 */
public class MqMessage extends MqCommonReq{

    /**
     * 分组名称
     */
    private String groupName;
    /**
     * 标题名称
     */
    private String topic;
    /**
     * 标签
     */
    private List<String> tags;
    /**
     * 内容
     */
    private String payload;
    /**
     * 业务标识
     */
    private String bizKey;
    /**
     * 负载分片标识
     */
    private String shardingKey;

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public String getBizKey() {
        return bizKey;
    }

    public void setBizKey(String bizKey) {
        this.bizKey = bizKey;
    }

    public String getShardingKey() {
        return shardingKey;
    }

    public void setShardingKey(String shardingKey) {
        this.shardingKey = shardingKey;
    }
}
