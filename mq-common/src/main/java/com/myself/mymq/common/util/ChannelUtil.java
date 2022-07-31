package com.myself.mymq.common.util;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;

/**
 * channel工具类
 * @author GuoZeWei
 * @date 2022/7/18  21:02
 */
public class ChannelUtil {
    private ChannelUtil(){}


    /**
     * 获取channel标识
     * @param channel 管道
     * @return 结果
     */
    public static String getChannelId(Channel channel){
        return channel.id().asLongText();
    }

    /**
     * 获取channel标识
     * @param ctx 管道
     * @return 结果
     */
    public static String getChannelId(ChannelHandlerContext ctx){
        return getChannelId(ctx.channel());
    }

}
