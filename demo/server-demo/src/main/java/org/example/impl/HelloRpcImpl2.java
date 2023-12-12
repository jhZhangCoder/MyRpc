package org.example.impl;

import org.example.HelloRpc;
import org.zjh.annotation.RpcApi;

/**
 * @author zjh
 * @description: TODO
 **/
@RpcApi
public class HelloRpcImpl2 implements HelloRpc {
    @Override
    public String sayHello(String msg) {
        return msg;
    }
}
