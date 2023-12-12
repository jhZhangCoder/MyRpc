package org.zjh.loadbalance.impl;

import lombok.extern.slf4j.Slf4j;
import org.zjh.exception.LoadBalanceException;
import org.zjh.loadbalance.AbstractLoadBalancer;
import org.zjh.loadbalance.Selector;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author zjh
 * @description: TODO
 **/
@Slf4j
public class RoundRobinLoadBalancer extends AbstractLoadBalancer {
    @Override
    protected Selector getSelector(List<InetSocketAddress> serverList, String serviceName) {
        return new RoundRobinSelector(registry.discovery(serviceName));
    }


    private static class RoundRobinSelector implements Selector {
        private List<InetSocketAddress> serverList;

        private AtomicInteger index;

        public RoundRobinSelector(List<InetSocketAddress> serverList) {
            this.serverList = serverList;
            index =  new AtomicInteger(0);
        }

        @Override
        public InetSocketAddress getNext() {
            if(serverList == null || serverList.size() == 0) {
                log.error("负载均衡失败!服务列表为空!");
                throw new LoadBalanceException();
            }
            InetSocketAddress address = serverList.get(index.get());
            if(index.get() == serverList.size() -1 ) {
                index.set(0);
            }else {
                index.incrementAndGet();
            }
            return address;
        }
    }
}
