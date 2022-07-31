package com.myself.mymq.common.support.invoke.impl;

import com.github.houbb.heaven.util.common.ArgUtil;
import com.myself.mymq.common.rpc.RpcMessageDto;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 超时检测线程
 * @author GuoZeWei
 * @date 2022/7/12  14:50
 */
public class TimeoutCheckThread implements Runnable{
    /**
     * 请求信息
     */
    private final ConcurrentHashMap<String,Long> requestMap;

    /**
     * 请求信息
     */
    private final ConcurrentHashMap<String, RpcMessageDto> responseMap;

    /**
     * 新建
     * @param requestMap 请求Map
     * @param responseMap 结果Map
     */
    public TimeoutCheckThread(ConcurrentHashMap<String, Long> requestMap, ConcurrentHashMap<String, RpcMessageDto> responseMap) {
        ArgUtil.notNull(requestMap,"requestMap");
        this.requestMap = requestMap;
        this.responseMap = responseMap;
    }

    @Override
    public void run() {
        for(Map.Entry<String,Long> entry:requestMap.entrySet()){
            long expireTime=entry.getValue();
            long currentTime=System.currentTimeMillis();
            if(currentTime>expireTime){
                final String key=entry.getKey();
                //结果设置为超时，从请求map中移除
                responseMap.putIfAbsent(key,RpcMessageDto.timeout());
                requestMap.remove(key);
            }
        }
    }
}
