package com.myself.mymq.broker.support.valid;

import com.myself.mymq.broker.dto.BrokerRegisterReq;

/**
 * @author GuoZeWei
 * @date 2022/7/27  22:34
 */
public class BrokerRegisterValidService implements IBrokerRegisterValidService{
    @Override
    public boolean producerValid(BrokerRegisterReq registerReq) {
        return true;
    }

    @Override
    public boolean consumerValid(BrokerRegisterReq registerReq) {
        return true;
    }
}
