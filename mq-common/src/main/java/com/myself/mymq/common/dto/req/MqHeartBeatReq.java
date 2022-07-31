package com.myself.mymq.common.dto.req;

/**
 * @author GuoZeWei
 * @date 2022/7/20  15:32
 */
public class MqHeartBeatReq extends MqCommonReq{
    /**
     * 信息
     */
    private String address;
    /**
     * 端口号
     */
    private int port;
    /**
     * 请求时间
     */
    private long time;

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }
}
