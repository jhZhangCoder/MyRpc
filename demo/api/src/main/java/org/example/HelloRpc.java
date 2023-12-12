package org.example;

import org.zjh.annotation.TryTimes;

/**
 * @author zjh
 * @description: TODO
 **/
public interface HelloRpc {
    /**
     * 通用接口, client和server都需要依赖
     * @param msg 发送的具体消息
     * @return
     */

    @TryTimes(times = 3,intervalTime = 500)
    String sayHello(String msg);
}
