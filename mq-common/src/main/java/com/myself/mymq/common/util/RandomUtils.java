package com.myself.mymq.common.util;

import com.github.houbb.heaven.annotation.CommonEager;
import com.github.houbb.heaven.util.lang.StringUtil;
import com.github.houbb.heaven.util.util.CollectionUtil;
import com.github.houbb.load.balance.api.ILoadBalance;
import com.github.houbb.load.balance.api.impl.LoadBalanceContext;
import com.github.houbb.load.balance.support.server.IServer;

import java.util.List;
import java.util.Objects;

/**
 * @author GuoZeWei
 * @date 2022/7/19  16:42
 */
@CommonEager
public class RandomUtils {

    public static <T extends IServer> T loadBalance(final ILoadBalance<T> loadBalance,
                                                    final List<T> list,String key){
        if(CollectionUtil.isEmpty(list)){
            return null;
        }
        if(StringUtil.isEmpty(key)){
            LoadBalanceContext<T> loadBalanceContext=LoadBalanceContext.<T>newInstance()
                    .servers(list);
            return loadBalance.select(loadBalanceContext);
        }
        //获取code
        int hashCode= Objects.hash(key);
        int index=hashCode % list.size();
        return list.get(index);
    }
}
