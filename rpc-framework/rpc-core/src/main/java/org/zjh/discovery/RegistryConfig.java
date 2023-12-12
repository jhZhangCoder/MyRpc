package org.zjh.discovery;

import org.zjh.discovery.impl.ZookeeperRegistry;
import org.zjh.exception.DiscoveryException;

/**
 * @author zjh
 * @description: TODO
 **/
public class RegistryConfig {
    private String connectString;

    public RegistryConfig(String connectString){
        this.connectString=connectString;
    }

    /**
     * 简单工厂模式 获取注册中心
     * @return 注册中心实例
     */
    public Registry getRegistry() {
        String registryType = getRegistryType(connectString, true).toLowerCase().trim();
        String host = getRegistryType(connectString,false).trim();
        if("zookeeper".equals(registryType)) {
            return new ZookeeperRegistry(host);
        }else if("nacos".equals(registryType)) {
            // todo
        }
        throw new DiscoveryException("获取注册中心失败!请检查url是否合法");
    }

    /**
     * 根据url获取注册中心类型 or host
     * @param connectString 连接url
     * @param isType 获取type or host
     * @return type or host
     */
    public String getRegistryType(String connectString,boolean isType) {
        String[] split = connectString.split("://");
        if(split.length != 2) {
            throw new DiscoveryException("注册中心url不合法!");
        }
        if(isType) {
            return split[0];
        }
        return split[1];
    }

}
