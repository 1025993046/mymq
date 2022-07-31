package com.myself.mymq.broker.support.api;

import com.github.houbb.log.integration.core.Log;
import com.github.houbb.log.integration.core.LogFactory;
import com.myself.mymq.broker.api.IBrokerProducerService;
import com.myself.mymq.broker.dto.BrokerServiceEntryChannel;
import com.myself.mymq.broker.dto.ServiceEntry;
import com.myself.mymq.broker.resp.MqBrokerRespCode;
import com.myself.mymq.broker.utils.InnerChannelUtils;
import com.myself.mymq.common.dto.resp.MqCommonResp;
import com.myself.mymq.common.resp.MqCommonRespCode;
import com.myself.mymq.common.resp.MqException;
import com.myself.mymq.common.util.ChannelUtil;
import io.netty.channel.Channel;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 生产者注册服务类
 * @author GuoZeWei
 * @date 2022/7/27  17:59
 */
public class LocalBrokerProducerService implements IBrokerProducerService {
    private static final Log log = LogFactory.getLog(LocalBrokerProducerService.class);

    private final Map<String, BrokerServiceEntryChannel> registerMap=new ConcurrentHashMap<>();



    @Override
    public MqCommonResp register(ServiceEntry serviceEntry, Channel channel) {
        final String channelId= ChannelUtil.getChannelId(channel);
        BrokerServiceEntryChannel entryChannel= InnerChannelUtils.buildEntryChannel(serviceEntry, channel);
        registerMap.put(channelId,entryChannel);
        //这里的数据里有通道channel、分组名称groupName、地址Address、端口port、权重weight

        MqCommonResp resp = new MqCommonResp();
        resp.setRespCode(MqCommonRespCode.SUCCESS.getCode());
        resp.setRespMessage(MqCommonRespCode.SUCCESS.getMsg());
        return resp;
    }

    @Override
    public MqCommonResp unRegister(ServiceEntry serviceEntry, Channel channel) {
        final String channelId=ChannelUtil.getChannelId(channel);
        registerMap.remove(channelId);

        MqCommonResp resp = new MqCommonResp();
        resp.setRespCode(MqCommonRespCode.SUCCESS.getCode());
        resp.setRespMessage(MqCommonRespCode.SUCCESS.getMsg());
        return resp;
    }

    @Override
    public ServiceEntry getServiceEntry(String channelId) {
        return registerMap.get(channelId);
    }

    @Override
    public void checkValid(String channelId) {
        if(!registerMap.containsKey(channelId)){
            log.error("channelId:{} 未注册",channelId);
            throw new MqException(MqBrokerRespCode.P_REGISTER_CHANNEL_NOT_VALID);
        }
    }
}
