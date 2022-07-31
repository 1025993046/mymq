package com.myself.mymq.broker.api;

import com.myself.mymq.broker.dto.ServiceEntry;
import com.myself.mymq.common.dto.resp.MqCommonResp;
import io.netty.channel.Channel;

/**
 * 生产者注册服务 类
 * @author GuoZeWei
 * @date 2022/7/26  18:10
 */
public interface IBrokerProducerService {
    /**
     * 注册当前服务信息
     * （1）将该服务通过{@link ServiceEntry#getGroupName()}进行分组
     * 订阅了这个serviceId的所有客户端
     * @param serviceEntry 注册当前服务信息
     * @param channel channel
     * @return
     */
    MqCommonResp register(final ServiceEntry serviceEntry, Channel channel);

    /**
     * 注销当前服务信息
     * @param serviceEntry 注册当前服务信息
     * @param channel 通道
     * @return
     */
    MqCommonResp unRegister(final ServiceEntry serviceEntry,Channel channel);

    /**
     * 获取服务地址信息
     * @param channelId channel
     * @return 结果
     */
    ServiceEntry getServiceEntry(final String channelId);

    /**
     * 校验有效性
     * @param channelId 通道唯一标识
     */
    void checkValid(final String channelId);

}
