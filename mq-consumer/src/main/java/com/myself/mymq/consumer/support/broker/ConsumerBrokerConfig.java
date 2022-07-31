package com.myself.mymq.consumer.support.broker;

import com.github.houbb.load.balance.api.ILoadBalance;
import com.myself.mymq.common.rpc.RpcChannelFuture;
import com.myself.mymq.common.support.invoke.IInvokeService;
import com.myself.mymq.common.support.status.IStatusManager;
import com.myself.mymq.consumer.support.listener.IMqListenerService;

/**
 * @author GuoZeWei
 * @date 2022/7/13  22:42
 */
public class ConsumerBrokerConfig {
    /**
     * 分组名称
     */
    private String groupName;

    /**
     * 中间人地址
     */
    private String brokerAddress;

    /**
     * 调用管理服务
     */
    private IInvokeService invokeService;

    /**
     * 获取响应超时时间
     */
    private long respTimeoutMills;

    /**
     * 检测broker可用性
     */
    private boolean check;

    /**
     * 状态管理
     */
    private IStatusManager statusManager;

    /**
     * 监听服务类
     */
    private IMqListenerService mqListenerService;

    /**
     * 负载均衡
     */
    private ILoadBalance<RpcChannelFuture> loadBalance;

    /**
     * 订阅最大尝试次数
     */
    private int subscribeMaxAttempt=3;

    /**
     * 取消订阅最大尝试次数
     */
    private int unSubscribeMaxAttempt=3;

    /**
     * 消费状态更新最大尝试次数
     */
    private int consumerStatusMaxAttempt=3;

    /**
     * 账户标识
     */
    protected String appKey;

    /**
     * 账户密码
     */
    protected String appSecret;

    public String appKey() {
        return appKey;
    }

    public ConsumerBrokerConfig appKey(String appKey) {
        this.appKey = appKey;
        return this;
    }

    public String appSecret() {
        return appSecret;
    }

    public ConsumerBrokerConfig appSecret(String appSecret) {
        this.appSecret = appSecret;
        return this;
    }

    public int consumerStatusMaxAttempt() {
        return consumerStatusMaxAttempt;
    }

    public ConsumerBrokerConfig consumerStatusMaxAttempt(int consumerStatusMaxAttempt) {
        this.consumerStatusMaxAttempt = consumerStatusMaxAttempt;
        return this;
    }

    public static ConsumerBrokerConfig newInstance() {
        return new ConsumerBrokerConfig();
    }

    public int subscribeMaxAttempt() {
        return subscribeMaxAttempt;
    }

    public ConsumerBrokerConfig subscribeMaxAttempt(int subscribeMaxAttempt) {
        this.subscribeMaxAttempt = subscribeMaxAttempt;
        return this;
    }

    public int unSubscribeMaxAttempt() {
        return unSubscribeMaxAttempt;
    }

    public ConsumerBrokerConfig unSubscribeMaxAttempt(int unSubscribeMaxAttempt) {
        this.unSubscribeMaxAttempt = unSubscribeMaxAttempt;
        return this;
    }

    public String groupName() {
        return groupName;
    }

    public ConsumerBrokerConfig groupName(String groupName) {
        this.groupName = groupName;
        return this;
    }

    public String brokerAddress() {
        return brokerAddress;
    }

    public ConsumerBrokerConfig brokerAddress(String brokerAddress) {
        this.brokerAddress = brokerAddress;
        return this;
    }

    public IInvokeService invokeService() {
        return invokeService;
    }

    public ConsumerBrokerConfig invokeService(IInvokeService invokeService) {
        this.invokeService = invokeService;
        return this;
    }

    public long respTimeoutMills() {
        return respTimeoutMills;
    }

    public ConsumerBrokerConfig respTimeoutMills(long respTimeoutMills) {
        this.respTimeoutMills = respTimeoutMills;
        return this;
    }

    public boolean check() {
        return check;
    }

    public ConsumerBrokerConfig check(boolean check) {
        this.check = check;
        return this;
    }

    public IStatusManager statusManager() {
        return statusManager;
    }

    public ConsumerBrokerConfig statusManager(IStatusManager statusManager) {
        this.statusManager = statusManager;
        return this;
    }

    public IMqListenerService mqListenerService() {
        return mqListenerService;
    }

    public ConsumerBrokerConfig mqListenerService(IMqListenerService mqListenerService) {
        this.mqListenerService = mqListenerService;
        return this;
    }

    public ILoadBalance<RpcChannelFuture> loadBalance() {
        return loadBalance;
    }

    public ConsumerBrokerConfig loadBalance(ILoadBalance<RpcChannelFuture> loadBalance) {
        this.loadBalance = loadBalance;
        return this;
    }
}
