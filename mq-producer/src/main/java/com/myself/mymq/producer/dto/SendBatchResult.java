package com.myself.mymq.producer.dto;

import com.myself.mymq.producer.constant.SendStatus;

import java.util.List;

/**
 * @author GuoZeWei
 * @date 2022/7/20  17:35
 */
public class SendBatchResult {
    /**
     * 消息唯一标识
     */
    private List<String> messageIds;

    /**
     * 发送状态
     */
    private SendStatus status;

    public static SendBatchResult of(List<String> messageIds, SendStatus status) {
        SendBatchResult result = new SendBatchResult();
        result.setMessageIds(messageIds);
        result.setStatus(status);

        return result;
    }

    public List<String> getMessageIds() {
        return messageIds;
    }

    public void setMessageIds(List<String> messageIds) {
        this.messageIds = messageIds;
    }

    public SendStatus getStatus() {
        return status;
    }

    public void setStatus(SendStatus status) {
        this.status = status;
    }
}
