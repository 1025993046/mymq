package com.myself.mymq.common.rpc;

import io.netty.channel.ChannelFuture;

/**
 * @author GuoZeWei
 * @date 2022/7/13  23:09
 */
public class RpcChannelFuture extends RpcAddress{
    /**
     * channel future 信息
     */
    private ChannelFuture channelFuture;

    public ChannelFuture getChannelFuture() {
        return channelFuture;
    }

    public void setChannelFuture(ChannelFuture channelFuture) {
        this.channelFuture = channelFuture;
    }
}
