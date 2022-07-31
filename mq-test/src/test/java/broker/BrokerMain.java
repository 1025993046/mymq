package broker;

import com.myself.mymq.broker.core.MqBroker;

/**
 * @author GuoZeWei
 * @date 2022/7/29  21:58
 */
public class BrokerMain {
    public static void main(String[] args) {
        MqBroker broker=new MqBroker();
        broker.start();
    }
}
