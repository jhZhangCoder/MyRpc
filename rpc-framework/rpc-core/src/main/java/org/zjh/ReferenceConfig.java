package org.zjh;

import org.zjh.discovery.Registry;
import org.zjh.discovery.RegistryConfig;
import org.zjh.proxy.handler.RpcConsumerInvocationHandler;

import java.lang.reflect.Proxy;
import java.net.InetSocketAddress;

/**
 * @author zjh
 * @description: TODO
 **/
public class ReferenceConfig<T> {
    private Class<T> interfaceRef;

    private RegistryConfig registryConfig;

    public RegistryConfig getRegistryConfig() {
        return registryConfig;
    }

    public void setRegistryConfig(RegistryConfig registryConfig) {
        this.registryConfig = registryConfig;
    }

    public Class<T> getInterface() {
        return interfaceRef;
    }

    public void setInterface(Class<T> interfaceRef) {
        this.interfaceRef = interfaceRef;
    }

    public T get(){
        ClassLoader classLoader=Thread.currentThread().getContextClassLoader();
        Class[] classes=new Class[]{interfaceRef};

        // 生成代理对象
        Object helloProxy = Proxy.newProxyInstance(classLoader, classes,
                new RpcConsumerInvocationHandler(registryConfig.getRegistry(),interfaceRef));
        return (T) helloProxy;
    }
}
