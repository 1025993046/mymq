package com.myself.mymq.broker.dto;

import io.netty.channel.Channel;

/**
 * @author GuoZeWei
 * @date 2022/7/20  15:55
 */
public class BrokerServiceEntryChannel extends ServiceEntry{
    private Channel channel;

    /**
     * 最后访问时间
     */
    private long lastAccessTime;
    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    public long getLastAccessTime() {
        return lastAccessTime;
    }

    public void setLastAccessTime(long lastAccessTime) {
        this.lastAccessTime = lastAccessTime;
    }
}
