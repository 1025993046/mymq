package com.myself.mymq.common.dto.req;

import com.myself.mymq.common.dto.req.component.MqConsumerUpdateStatusDto;

import java.util.List;

/**
 * 批量更新状态入参
 * @author GuoZeWei
 * @date 2022/7/20  15:45
 */
public class MqConsumerUpdateStatusBatchReq extends MqCommonReq{
    private List<MqConsumerUpdateStatusDto> statusList;

    public List<MqConsumerUpdateStatusDto> getStatusList() {
        return statusList;
    }

    public void setStatusList(List<MqConsumerUpdateStatusDto> statusList) {
        this.statusList = statusList;
    }
}
