package org.zjh.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.*;
import org.zjh.Constant;
import org.zjh.ZookeeperNode;
import org.zjh.exception.ZookeeperException;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * @author zjh
 * @description: TODO
 **/
@Slf4j
public class ZookeeperUtils {

    public static ZooKeeper createZookeeper() {
        String connectString = Constant.DEFAULT_ZK_CONNECT;
        int timeout = Constant.TIME_OUT;
        return createZookeeper(connectString,timeout);
    }

    /*
     * @Author zjh
     * @Description 创建zk实例
     * @Param [connectString, timeout]
     * @return org.apache.zookeeper.ZooKeeper
     **/
    public static ZooKeeper createZookeeper(String connectString,int timeout){
        CountDownLatch countDownLatch = new CountDownLatch(1);
        // 创建zookeeper实例
        ZooKeeper zooKeeper = null;
        try {
            zooKeeper = new ZooKeeper(connectString, timeout, watchedEvent -> {
                // 只有连接成功才会放行
                if (watchedEvent.getState()== Watcher.Event.KeeperState.SyncConnected){
                    log.debug("客户端连接成功!");
                    countDownLatch.countDown();
                }
            });
            countDownLatch.await();
            return zooKeeper;
        } catch (IOException | InterruptedException e) {
            log.error("创建zookeeper实例发生错误:",e);
            throw new ZookeeperException();
        }
    }

    /*
     * @Author zjh
     * @Description 判断节点是否存在
     * @Param [zooKeeper, nodePath, watcher]
     * @return boolean
     **/
    public static boolean exist(ZooKeeper zooKeeper,String nodePath,Watcher watcher) {
        try {
            return zooKeeper.exists(nodePath,watcher) != null;
        } catch (KeeperException | InterruptedException e) {
            log.error("判断节点[{}]是否存在发生异常",nodePath,e);
            throw new ZookeeperException(e);
        }
    }

    /*
     * @Author zjh
     * @Description 创建一个ZK节点
     * @Param [zooKeeper, zookeeperNode, o, persistent]
     * @return void
     **/
    public static boolean createNode(ZooKeeper zooKeeper, ZookeeperNode node,Watcher watcher,CreateMode createMode) {
        try {
            if(zooKeeper.exists(node.getNodePath(),watcher) == null) {
                String result = zooKeeper.create(node.getNodePath(), node.getData(),
                        ZooDefs.Ids.OPEN_ACL_UNSAFE, createMode);
                log.info("节点【{}】创建成功",result);
                return true;
            }else {
                log.error("节点【{}】已存在",node.getNodePath());
                return false;
            }
        } catch (KeeperException | InterruptedException e) {
            log.error("创建节点【{}】发生异常:",node.getNodePath(),e);
            throw new ZookeeperException();
        }
    }

    public static List<String> getChildren(ZooKeeper zooKeeper, String serviceNode, Watcher watcher) {
        try {
            return zooKeeper.getChildren(serviceNode, watcher);
        } catch (KeeperException | InterruptedException e) {
            log.error("获取【{}】子节点出现异常",serviceNode,e);
            throw new ZookeeperException(e);
        }
    }
}
