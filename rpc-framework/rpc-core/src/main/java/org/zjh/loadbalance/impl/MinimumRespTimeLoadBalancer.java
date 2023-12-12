package org.zjh.loadbalance.impl;

import lombok.extern.slf4j.Slf4j;
import org.zjh.RpcbootStrap;
import org.zjh.loadbalance.AbstractLoadBalancer;
import org.zjh.loadbalance.Selector;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Set;

/**
 * @author zjh
 * @description: TODO
 **/
@Slf4j
public class MinimumRespTimeLoadBalancer extends AbstractLoadBalancer {
    @Override
    protected Selector getSelector(List<InetSocketAddress> serverList, String serviceName) {
        return new MinimumRespTimeSelector();
    }


    private static class MinimumRespTimeSelector implements Selector {
        @Override
        public InetSocketAddress getNext() {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            log.debug("--------------响应时间treemap-------------");
            Set<Long> time = RpcbootStrap.ANSWER_TIME_CACHE.keySet();
            for (Long t : time) {
                log.debug("与主机【{}】的响应时间:[{}]",RpcbootStrap.ANSWER_TIME_CACHE.get(t),t);
            }
            if(!RpcbootStrap.ANSWER_TIME_CACHE.isEmpty()) {
                Long minimum = RpcbootStrap.ANSWER_TIME_CACHE.firstKey();
                InetSocketAddress address = (InetSocketAddress) RpcbootStrap.ANSWER_TIME_CACHE
                        .get(minimum).remoteAddress();
                log.debug("选取了主机【{}】,响应时间为:[{}]", address, minimum);
                return address;
            }
            throw new RuntimeException();
        }
    }
}
