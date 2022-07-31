package com.myself.mymq.common.support.hook;

/**
 * @author GuoZeWei
 * @date 2022/7/22  15:19
 */
public final class ShutdownHooks {
    private ShutdownHooks(){}

    /**
     * 添加 rpc shutdown hook
     * @param rpcShutdownHook
     */
    public static void rpcShutdownHook(final RpcShutdownHook rpcShutdownHook){
        Runtime.getRuntime().addShutdownHook(new Thread(){
            @Override
            public void run(){
                rpcShutdownHook.hook();
            }
        });
    }
}
