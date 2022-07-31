package com.myself.mymq.common.constant;

/**
 * @author GuoZeWei
 * @date 2022/7/5  23:12
 */
public final class MessageStatusConst {
    private MessageStatusConst(){}

    /**
     * 待消费
     * 生产者推送到broker的初始化状态
     */
    public static final String WAIT_CONSUMER="W";

    /**
     * 推送到消费端处理中
     * broker准备推送时，首先将状态更新为P，等待推送结果
     */
    public static final String TO_CONSUMER_PROCESS="TCP";

    /**
     * 推送给消费端成功
     */
    public static final String TO_CONSUMER_SUCCESS="TCS";

    /**
     * 推送给消费端失败
     */
    public static final String TO_CONSUMER_FAILED="TCF";

    /**
     * 消费完成
     */
    public static final String CONSUMER_SUCCESS="CS";

    /**
     * 消费失败
     */
    public static final String CONSUMER_FAILED="CF";

    /**
     * 稍后消费
     */
    public static final String CONSUMER_LATER="CL";
}
