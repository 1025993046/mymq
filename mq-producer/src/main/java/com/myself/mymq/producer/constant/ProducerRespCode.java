package com.myself.mymq.producer.constant;

import com.github.houbb.heaven.response.respcode.RespCode;

/**
 * @author GuoZeWei
 * @date 2022/7/23  21:15
 */
public enum  ProducerRespCode  implements RespCode {
    RPC_INIT_FAILED("P00001","生产者启动失败"),
    MSG_SEND_FAILED("P00002","生产者消息发送失败");

    private final String code;
    private final String msg;

    ProducerRespCode(String code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getMsg() {
        return msg;
    }
}
