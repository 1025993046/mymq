package com.myself.mymq.consumer.support.listener;

import com.myself.mymq.common.dto.req.MqMessage;
import com.myself.mymq.common.resp.ConsumerStatus;
import com.myself.mymq.consumer.api.IMqConsumerListener;
import com.myself.mymq.consumer.api.IMqConsumerListenerContext;

/**
 * @author GuoZeWei
 * @date 2022/7/12  16:14
 */
public interface IMqListenerService {
    /**
     * 注册
     * @param listener 监听器
     */
    void register(final IMqConsumerListener listener);

    /**
     * 消费消息
     * @param mqMessage 消息
     * @param context 上下文
     * @return
     */
    ConsumerStatus consumer(final MqMessage mqMessage, final IMqConsumerListenerContext context);
}
