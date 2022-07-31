package com.myself.mymq.broker.dto;

import com.myself.mymq.common.rpc.RpcAddress;

/**
 * @author GuoZeWei
 * @date 2022/7/19  16:12
 */
public class ServiceEntry extends RpcAddress {
    /**
     * 分组名称
     */
    private String groupName;

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    @Override
    public String toString() {
        return "ServiceEntry{" +
                "groupName='" + groupName + '\'' +
                "} " + super.toString();
    }
}
