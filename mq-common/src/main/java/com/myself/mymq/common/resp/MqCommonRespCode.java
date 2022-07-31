package com.myself.mymq.common.resp;
import com.github.houbb.heaven.response.respcode.RespCode;
/**
 * 响应码接口定义
 * @author GuoZeWei
 * @date 2022/7/8  21:48
 */
public enum MqCommonRespCode implements RespCode{
    SUCCESS("0000","成功"),
    FAIL("9999","失败"),
    TIMEOUT("8888","超时"),
    RPC_GET_RESP_FAILED("10001","RPC 获取响应失败"),
    REGISTER_TO_BROKER_FAILED("10002","注册到 Broker 失败"),
    P_REGISTER_TO_BROKER_FAILED("P00001","生产者注册到 Broker 失败"),
    P_INIT_FAILED("P00002","生产者初始化失败"),
    C_REGISTER_TO_BROKER_FAILED("C00001","消费者注册到 Broker 失败"),
    C_INIT_FAILED("C00002","消费者初始化失败");

    private final String code;
    private final String msg;
    MqCommonRespCode(String code, String msg) {
        this.code = code;
        this.msg = msg;
    }


    public String getCode() {
        return code;
    }


    public String getMsg() {
        return msg;
    }
}