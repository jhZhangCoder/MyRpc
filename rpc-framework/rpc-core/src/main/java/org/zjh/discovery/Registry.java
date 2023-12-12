package org.zjh.discovery;

import org.zjh.ServiceConfig;

import java.net.InetSocketAddress;
import java.util.List;

/**
 * @author zjh
 * @description: TODO
 **/
public interface Registry {
    /*
     * @Author zjh
     * @Description 注册服务
     * @Param [serviceConfig]
     * @return void
     **/
    void registry(ServiceConfig<?> serviceConfig);

    /*
     * @Author zjh
     * @Description 从注册中心拉取可用服务列表
     * @Param [serviceName]
     * @return java.net.InetSocketAddress
     **/
    List<InetSocketAddress> discovery(String serviceName);
}
