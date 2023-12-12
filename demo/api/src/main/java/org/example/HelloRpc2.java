package org.example;

/**
 * @author zjh
 * @description: TODO
 **/
public interface HelloRpc2{
    /**
     * 通用接口, client和server都需要依赖
     * @param msg 发送的具体消息
     * @return
     */
    String sayHello(String msg);
}
