package org.example;

import org.zjh.ReferenceConfig;
import org.zjh.RpcbootStrap;
import org.zjh.compress.CompressType;
import org.zjh.discovery.RegistryConfig;
import org.zjh.loadbalance.impl.RoundRobinLoadBalancer;
import org.zjh.serialize.SerializerType;

public class Main {
    public static void main(String[] args) {
        // 获取代理对象 通过ReferenceConfig封装
        ReferenceConfig<HelloRpc> reference = new ReferenceConfig();
        reference.setInterface(HelloRpc.class);

        // 代理 ：
        // 连接注册中心 -> 拉取服务列表 -> 选择一个服务并建立连接 -> 发送请求,携带一些信息(接口名,参数列表...),获得结果
        // 代理对象

        RpcbootStrap.getInstance()
                .application("first-rpc-consumer")
                .registry(new RegistryConfig("zookeeper://127.0.0.1:2181"))
                .serialize(SerializerType.HESSIAN)
                .compress(CompressType.GZIP)
                .loadBalance(new RoundRobinLoadBalancer())
                .reference(reference);

        HelloRpc proxy = reference.get();
        for (int i = 0; i < 600; i++) {
            String result = proxy.sayHello("rpc");
            System.out.println(result);
        }



    }
}