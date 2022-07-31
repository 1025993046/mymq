package com.myself.mymq.consumer.api;

import com.myself.mymq.common.dto.req.MqMessage;
import com.myself.mymq.common.resp.ConsumerStatus;

/** mq消费者监听器接口
 * @author GuoZeWei
 * @date 2022/7/5  22:53
 */
public interface IMqConsumerListener {

    /**
     * 消费
     * @param mqMessage 消息体
     * @param context 上下文
     * @return 结果
     */
    ConsumerStatus consumer(final MqMessage mqMessage,
                            final IMqConsumerListenerContext context);
}
