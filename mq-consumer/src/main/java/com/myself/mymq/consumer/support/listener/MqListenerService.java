package com.myself.mymq.consumer.support.listener;

import com.alibaba.fastjson.JSON;
import com.github.houbb.log.integration.core.Log;
import com.github.houbb.log.integration.core.LogFactory;
import com.myself.mymq.common.dto.req.MqMessage;
import com.myself.mymq.common.resp.ConsumerStatus;
import com.myself.mymq.consumer.api.IMqConsumerListener;
import com.myself.mymq.consumer.api.IMqConsumerListenerContext;

/**
 * @author GuoZeWei
 * @date 2022/7/12  16:17
 */
public class MqListenerService implements IMqListenerService{
    private static final Log log= LogFactory.getLog(MqListenerService.class);

    private IMqConsumerListener mqConsumerListener;

    @Override
    public void register(IMqConsumerListener listener) {
        this.mqConsumerListener=listener;
    }

    @Override
    public ConsumerStatus consumer(MqMessage mqMessage, IMqConsumerListenerContext context) {
        if (mqConsumerListener==null){
            log.warn("当前监听类为空，直接忽略处理。message：{}", JSON.toJSON(mqMessage));
            return ConsumerStatus.SUCCESS;
        }else{
            return mqConsumerListener.consumer(mqMessage,context);
        }
    }
}
