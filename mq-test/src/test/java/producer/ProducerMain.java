package producer;

import com.alibaba.fastjson.JSON;
import com.myself.mymq.common.dto.req.MqMessage;
import com.myself.mymq.producer.core.MqProducer;
import com.myself.mymq.producer.dto.SendResult;

import java.util.Arrays;

/**
 * @author GuoZeWei
 * @date 2022/7/29  22:08
 */
public class ProducerMain {
    public static void main(String[] args) {
        MqProducer mqProducer = new MqProducer();
        mqProducer.appKey("test")
                .appSecret("mq");
        mqProducer.start();

        for(int i = 0; i < 20; i++) {
            MqMessage mqMessage = buildMessage(i);
            SendResult sendResult = mqProducer.send(mqMessage);
            System.out.println(JSON.toJSON(sendResult));
        }
    }

    private static MqMessage buildMessage(int i) {
        String message = "我是生产者发过来的 "+i+"号数据";
        MqMessage mqMessage = new MqMessage();
        mqMessage.setTopic("TOPIC");
        mqMessage.setTags(Arrays.asList("TAGA", "TAGB"));
        mqMessage.setPayload(message);

        return mqMessage;
    }
}
