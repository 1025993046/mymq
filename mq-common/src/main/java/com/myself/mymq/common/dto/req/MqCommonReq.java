package com.myself.mymq.common.dto.req;

import java.io.Serializable;

/**
 * @author GuoZeWei
 * @date 2022/7/13  23:14
 */
public class MqCommonReq implements Serializable {
    /**
     * 请求标识
     */
    private String traceId;
    /**
     * 方法类型
     */
    private String methodType;
    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    public String getMethodType() {
        return methodType;
    }

    public void setMethodType(String methodType) {
        this.methodType = methodType;
    }


}
