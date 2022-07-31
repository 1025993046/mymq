package com.myself.mymq.broker.support.push;

/**
 * 消息推送服务
 * @author GuoZeWei
 * @date 2022/7/27  22:00
 */
public interface IBrokerPushService {

    /**
     * 异步推送
     * @param context 消息
     */
    void asyncPush(final BrokerPushContext context);
}
