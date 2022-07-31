package com.myself.mymq.common.dto.resp;

import com.myself.mymq.common.dto.req.MqMessage;

import java.util.List;

/**
 * @author GuoZeWei
 * @date 2022/7/13  23:36
 */
public class MqConsumerPullResp extends MqCommonResp{
    /**
     * 消息列表
     */
    private List<MqMessage> list;

    public List<MqMessage> getList() {
        return list;
    }

    public void setList(List<MqMessage> list) {
        this.list = list;
    }

    @Override
    public String toString() {
        return "MqConsumerPullResp{" +
                "list=" + list +
                "} " + super.toString();
    }
}
