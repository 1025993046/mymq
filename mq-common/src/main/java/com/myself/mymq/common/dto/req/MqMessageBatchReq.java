package com.myself.mymq.common.dto.req;

import java.util.List;

/**
 * @author GuoZeWei
 * @date 2022/7/23  21:20
 */
public class MqMessageBatchReq extends MqCommonReq{
    private List<MqMessage> mqMessageList;
    public List<MqMessage> getMqMessageList() {
        return mqMessageList;
    }

    public void setMqMessageList(List<MqMessage> mqMessageList) {
        this.mqMessageList = mqMessageList;
    }
}
