package org.zjh;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        ZooKeeper zooKeeper = ZookeeperUtils.createZookeeper();
        // 定义节点和数据
        String basePath = "/rpc-metadata";
        String providerPath = basePath + "/providers";
        String consumerPath = basePath + "/consumers";
        ZookeeperNode baseNode = new ZookeeperNode(basePath,null);
        ZookeeperNode providerNode = new ZookeeperNode(providerPath,null);
        ZookeeperNode consumerNode = new ZookeeperNode(consumerPath,null);

        List<Object> l = new ArrayList<>();

        Arrays.asList(baseNode,providerNode,consumerNode).forEach(node -> {
            ZookeeperUtils.createNode(zooKeeper, node,null,CreateMode.PERSISTENT);
        });

        ZookeeperUtils.close(zooKeeper);

    }
}