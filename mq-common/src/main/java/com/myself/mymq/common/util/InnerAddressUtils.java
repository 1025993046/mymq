package com.myself.mymq.common.util;

import com.github.houbb.heaven.util.common.ArgUtil;
import com.myself.mymq.common.rpc.RpcAddress;

import java.util.ArrayList;
import java.util.List;

/**
 * 内部地址工具类
 * @author GuoZeWei
 * @date 2022/7/14  17:25
 */
public class InnerAddressUtils {
    private InnerAddressUtils(){}

    public static List<RpcAddress> initAddressList(String address){
        //检验字符串非空
        ArgUtil.notEmpty(address,"address");
        String[] strings=address.split(",");
        List<RpcAddress> list=new ArrayList<>();
        for(String s:strings){
            String[] infos=s.split(":");
            RpcAddress rpcAddress=new RpcAddress();
            rpcAddress.setAddress(infos[0]);
            rpcAddress.setPort(Integer.parseInt(infos[1]));
            if (infos.length>2){
                rpcAddress.setWeight(Integer.parseInt(infos[2]));
            }else{
                rpcAddress.setWeight(1);
            }
            list.add(rpcAddress);
        }
        return list;
    }
}
