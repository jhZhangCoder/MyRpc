package org.zjh.loadbalance;

import java.net.InetSocketAddress;
import java.util.List;

/**
 * @author zjh
 * @description: TODO
 **/
public interface LoadBalancer {
    /**
     * 根据服务名 选择一个可用服务
     * @param serviceName 服务名
     */
    InetSocketAddress selectServiceAddress(String serviceName);

    void reBalance(String serviceName, List<InetSocketAddress> addressList);
}
