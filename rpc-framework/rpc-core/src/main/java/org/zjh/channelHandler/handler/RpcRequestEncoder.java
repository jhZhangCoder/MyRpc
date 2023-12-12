package org.zjh.channelHandler.handler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import io.netty.util.concurrent.EventExecutorGroup;
import lombok.extern.slf4j.Slf4j;
import org.zjh.MessageFormatConstant;
import org.zjh.enumeration.RequestType;
import org.zjh.transport.message.RequestPayload;
import org.zjh.transport.message.RpcRequest;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

/**
 * @author zjh
 * @description: 消息出站经过的第一个handler
 **/
@Slf4j
public class RpcRequestEncoder extends MessageToByteEncoder<RpcRequest> {
    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, RpcRequest rpcRequest, ByteBuf byteBuf) throws Exception {
        byte serializeType = rpcRequest.getSerializeType();
        byte compressType = rpcRequest.getCompressType();
        byte[] bodyBytes = getBodyBytes(rpcRequest.getRequestPayload());
        // 魔术值 4B 内容: lrpc
        byteBuf.writeBytes(MessageFormatConstant.MAGIC);
        // 版本 1B 内容：1
        byteBuf.writeByte(MessageFormatConstant.VERSION);
        // 头部大小 2B , 值：22
        byteBuf.writeShort(MessageFormatConstant.HEADER_LENGTH);
        // 总长度 header + body
        byteBuf.writeInt(MessageFormatConstant.HEADER_LENGTH + bodyBytes.length);
        // 请求类型 1B
        byteBuf.writeByte(rpcRequest.getRequestType());
        // 序列化类型 1B
        byteBuf.writeByte(serializeType);
        // 压缩类型 1B
        byteBuf.writeByte(rpcRequest.getCompressType());
        // 请求id 8B
        byteBuf.writeLong(rpcRequest.getRequestId());
        // body 不是心跳请求 才需要写body
        byteBuf.writeLong(rpcRequest.getTimestamp());
        if(rpcRequest.getRequestType() != RequestType.HEART_BEAT.getId()) {
            byteBuf.writeBytes(bodyBytes);
        }
        if(rpcRequest.getRequestType() != RequestType.HEART_BEAT.getId()) {
            log.debug("请求【{}】已经在客户端完成编码", rpcRequest.getRequestId());
        }
    }

    private byte[] getBodyBytes(RequestPayload requestPayload) {
                // 针对不同的消息类型作处理（心跳没有payload）
        if (requestPayload == null) {
            return new byte[0];
        }
        // object 序列化成字节数组
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream outputStream = new ObjectOutputStream(baos);
            outputStream.writeObject(requestPayload);
            return baos.toByteArray();
        } catch (IOException e) {
            log.error("序列化时发生异常,requestPayload:{}",requestPayload.toString());
            throw new RuntimeException();
        }
    }
}
