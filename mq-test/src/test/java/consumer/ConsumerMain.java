package consumer;

import com.alibaba.fastjson.JSON;
import com.myself.mymq.common.dto.req.MqMessage;
import com.myself.mymq.common.resp.ConsumerStatus;
import com.myself.mymq.consumer.api.IMqConsumerListener;
import com.myself.mymq.consumer.api.IMqConsumerListenerContext;
import com.myself.mymq.consumer.core.MqConsumerPush;

/**
 * @author GuoZeWei
 * @date 2022/7/29  22:01
 */
public class ConsumerMain {
    public static void main(String[] args) {
        //1.先启动消费者，再启动生产者
        final MqConsumerPush mqConsumerPush = new MqConsumerPush();
        mqConsumerPush.start();

        mqConsumerPush.subscribe("TOPIC", "TAGA");
        mqConsumerPush.registerListener(new IMqConsumerListener() {
            @Override
            public ConsumerStatus consumer(MqMessage mqMessage, IMqConsumerListenerContext context) {
                System.out.println("---------- 自定义 " + JSON.toJSONString(mqMessage));
                return ConsumerStatus.SUCCESS;
            }
        });
    }
}
