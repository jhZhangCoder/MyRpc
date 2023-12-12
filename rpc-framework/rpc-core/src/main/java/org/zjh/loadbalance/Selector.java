package org.zjh.loadbalance;

import java.net.InetSocketAddress;

/**
 * @author zjh
 * @description: TODO
 **/
public interface Selector {
    InetSocketAddress getNext();

}
