package com.myself.mymq.producer.api;

import com.myself.mymq.common.dto.req.MqMessage;
import com.myself.mymq.producer.dto.SendBatchResult;
import com.myself.mymq.producer.dto.SendResult;

import java.util.List;

/**
 * 消息发送接口
 * @author GuoZeWei
 * @date 2022/7/20  17:24
 */
public interface IMqProducer {
    /**
     * 同步发送消息
     * @param mqMessage 消息类型
     * @return 结果
     */
    SendResult send(final MqMessage mqMessage);

    /**
     * 单向发送消息
     * @param mqMessage 消息类型
     * @return 结果
     */
    SendResult sendOneWay(final MqMessage mqMessage);

    /**
     * 同步发送消息-批量
     * @param mqMessageList 消息类型
     * @return 结果
     */
    SendBatchResult sendBatch(final List<MqMessage> mqMessageList);

    /**
     * 单向发送消息-批量
     * @param mqMessageList 消息类型
     * @return 结果
     */
    SendBatchResult sendOneWayBatch(final List<MqMessage> mqMessageList);
}
