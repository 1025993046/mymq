package com.myself.mymq.common.rpc;

import com.myself.mymq.common.resp.MqCommonRespCode;

import java.io.Serializable;

/**
 * @author GuoZeWei
 * @date 2022/7/8  21:43
 */
public class RpcMessageDto implements Serializable {
    /**
     * 请求时间
     */
    private long requestTime;
    /**
     * 请求表示
     */
    private String traceId;
    /**
     * 方法类型
     */
    private String methodType;
    /**
     * 是否为请求消息
     */
    private boolean isRequest;

    private String respCode;

    private String respMsg;

    private String json;

    public static RpcMessageDto timeout() {
        RpcMessageDto dto = new RpcMessageDto();
        dto.setRespCode(MqCommonRespCode.TIMEOUT.getCode());
        dto.setRespMsg(MqCommonRespCode.TIMEOUT.getMsg());

        return dto;
    }

    @Override
    public String toString() {
        return "RpcMessageDto{" +
                "requestTime=" + requestTime +
                ", traceId='" + traceId + '\'' +
                ", methodType='" + methodType + '\'' +
                ", isRequest=" + isRequest +
                ", respCode='" + respCode + '\'' +
                ", respMsg='" + respMsg + '\'' +
                ", json='" + json + '\'' +
                '}';
    }
    public long getRequestTime() {
        return requestTime;
    }

    public void setRequestTime(long requestTime) {
        this.requestTime = requestTime;
    }

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

    public boolean isRequest() {
        return isRequest;
    }

    public void setRequest(boolean request) {
        isRequest = request;
    }

    public String getRespCode() {
        return respCode;
    }

    public void setRespCode(String respCode) {
        this.respCode = respCode;
    }

    public String getRespMsg() {
        return respMsg;
    }

    public void setRespMsg(String respMsg) {
        this.respMsg = respMsg;
    }

    public String getJson() {
        return json;
    }

    public void setJson(String json) {
        this.json = json;
    }
}