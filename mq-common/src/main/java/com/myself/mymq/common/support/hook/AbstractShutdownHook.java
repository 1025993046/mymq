package com.myself.mymq.common.support.hook;

import com.github.houbb.log.integration.core.Log;
import com.github.houbb.log.integration.core.LogFactory;

/**
 * rpc关闭hook
 * （1）可以添加对应的hook管理类
 * @author GuoZeWei
 * @date 2022/7/22  15:04
 */
public abstract class AbstractShutdownHook implements RpcShutdownHook{
    /**
     * AbstractShutdownHook logger
     */
    private static final Log LOG = LogFactory.getLog(AbstractShutdownHook.class);

    @Override
    public void hook(){
        LOG.info("[Shutdown Hook] start");
        this.doHook();
        LOG.info("[Shutdown Hook] end");
    }

    /**
     * 执行hook操作
     */
    protected abstract void doHook();
}
