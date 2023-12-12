package org.zjh;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.zjh.channelHandler.ConsumerChannelInitializer;

/**
 * @author zjh
 * @description: 封装客户端Bootstrap,避免每次调用重新创建
 **/
public class NettyBootstrapInitializer {
    private static Bootstrap bootstrap = new Bootstrap();

    static {
        NioEventLoopGroup group = new NioEventLoopGroup();
        bootstrap = bootstrap.group(group)
                // 选择初始化一个怎样的channel
                .channel(NioSocketChannel.class)
                .handler(new ConsumerChannelInitializer());
    }

    private NettyBootstrapInitializer() {}

    public static Bootstrap getBootstrap() {
        return bootstrap;
    }

}
