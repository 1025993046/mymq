package com.myself.mymq.producer.dto;

import com.myself.mymq.producer.constant.SendStatus;

/**
 * @author GuoZeWei
 * @date 2022/7/20  17:26
 */
public class SendResult {
    /**
     * 消息唯一标识
     */
    private String messageId;
    /**
     * 发送状态
     */
    private SendStatus status;

    public static SendResult of(String messageId, SendStatus status) {
        SendResult result = new SendResult();
        result.setMessageId(messageId);
        result.setStatus(status);

        return result;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public SendStatus getStatus() {
        return status;
    }

    public void setStatus(SendStatus status) {
        this.status = status;
    }
}
