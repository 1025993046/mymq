package com.myself.mymq.common.support.status;

/**
 * @author GuoZeWei
 * @date 2022/7/13  22:31
 */
public class StatusManager implements IStatusManager{
    private boolean status;

    private boolean initFailed;


    @Override
    public boolean status() {
        return this.status;
    }

    @Override
    public IStatusManager status(boolean status) {
        this.status=status;
        return this;
    }

    @Override
    public boolean initFailed() {
        return initFailed;
    }

    @Override
    public IStatusManager initFailed(boolean initfailed) {
        this.initFailed=initfailed;
        return this;
    }
}
