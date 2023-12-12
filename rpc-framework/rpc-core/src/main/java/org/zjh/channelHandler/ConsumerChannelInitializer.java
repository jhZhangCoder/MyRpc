package org.zjh.channelHandler;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.zjh.channelHandler.handler.MySimpleChannelInboundHandler;
import org.zjh.channelHandler.handler.RpcRequestEncoder;
import org.zjh.channelHandler.handler.RpcResponseDecoder;

/**
 * @author zjh
 * @description: 客户端Channel初始化器
 **/
public class ConsumerChannelInitializer extends ChannelInitializer<SocketChannel> {

    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        socketChannel.pipeline()
                // 出站 netty自带log
                .addLast(new LoggingHandler(LogLevel.DEBUG))
                // 出站 编码器
                .addLast(new RpcRequestEncoder())
                // 入站解码器
                .addLast(new RpcResponseDecoder())
                // 处理结果
                .addLast(new MySimpleChannelInboundHandler());
    }
}
