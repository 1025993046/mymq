package com.myself.mymq.broker.utils;

import com.myself.mymq.broker.dto.BrokerServiceEntryChannel;
import com.myself.mymq.broker.dto.ServiceEntry;
import com.myself.mymq.common.rpc.RpcChannelFuture;
import io.netty.channel.Channel;

/**
 * @author GuoZeWei
 * @date 2022/7/20  15:52
 */
public class InnerChannelUtils {

    private InnerChannelUtils(){}

    /**
     * 构建基本服务地址
     * @param rpcChannelFuture 信息
     * @return 结果
     */
    public static ServiceEntry buildServiceEntry(RpcChannelFuture rpcChannelFuture){
        ServiceEntry serviceEntry=new ServiceEntry();
        serviceEntry.setAddress(rpcChannelFuture.getAddress());
        serviceEntry.setPort(rpcChannelFuture.getPort());
        serviceEntry.setWeight(rpcChannelFuture.getWeight());
        return serviceEntry;
    }

    public static BrokerServiceEntryChannel buildEntryChannel(ServiceEntry serviceEntry,
                                                              Channel channel){
        BrokerServiceEntryChannel result=new BrokerServiceEntryChannel();
        result.setChannel(channel);
        result.setGroupName(serviceEntry.getGroupName());
        result.setAddress(serviceEntry.getAddress());
        result.setPort(serviceEntry.getPort());
        result.setWeight(serviceEntry.getWeight());
        return result;
    }

}
