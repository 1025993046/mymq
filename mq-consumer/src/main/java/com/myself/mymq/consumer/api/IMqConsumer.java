package com.myself.mymq.consumer.api;

/**
 * mq消费者接口
 * @author GuoZeWei
 * @date 2022/7/5  22:28
 */
public interface IMqConsumer {

    /**
     * 订阅
     * @param topicName topic名称
     * @param tagRegex 标签正则
     */
    void subscribe(String topicName,String tagRegex);

    /**
     * 取消订阅
     * @param topicName topic名称
     * @param tagRegex 标签正则
     */
    void unSubscribe(String topicName,String tagRegex);


    /**
     * 注册监听器
     * @param listener 监听器
     */
    void registerListener(final IMqConsumerListener listener);
}
