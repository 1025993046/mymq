package com.myself.mymq.common.support.hook;

/**
 * rpc关闭hook
 * @author GuoZeWei
 * @date 2022/7/22  15:05
 */
public interface RpcShutdownHook {

    /**
     * 钩子函数实现
     */
    void hook();
}
