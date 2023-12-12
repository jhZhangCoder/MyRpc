package org.zjh.channelHandler.handler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.extern.slf4j.Slf4j;
import org.zjh.MessageFormatConstant;
import org.zjh.compress.CompressFactory;
import org.zjh.compress.CompressType;
import org.zjh.compress.Compressor;
import org.zjh.serialize.Serializer;
import org.zjh.serialize.SerializerFactory;
import org.zjh.serialize.SerializerType;
import org.zjh.transport.message.RpcResponse;

/**
 * @author zjh
 * @description: 消息出站经过的第一个handler
 * magic(4B) version(1B) headLength(2B) fullLength(4B) code(1B)
 * serializeType(1B) compressType(1B) RequestId(8B) body(?)
 **/
@Slf4j
public class RpcResponseEncoder extends MessageToByteEncoder<RpcResponse> {
    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, RpcResponse rpcResponse, ByteBuf byteBuf) throws Exception {
        byte serializeType = rpcResponse.getSerializeType();
        byte compressType = rpcResponse.getCompressType();
        byte[] bodyBytes = getBodyBytes(rpcResponse.getBody(),serializeType);
        int start = bodyBytes.length;
        bodyBytes = compress(bodyBytes,compressType);
        int end = bodyBytes.length;
        if(bodyBytes.length != 0) {
            log.debug("请求【{}】已经在服务端使用[{}]完成压缩,压缩前:[{}],压缩后:[{}]"
                    , rpcResponse.getRequestId(), CompressType.getNameByType(compressType), start, end);
        }
        // 魔术值 4B 内容: lrpc
        byteBuf.writeBytes(MessageFormatConstant.MAGIC);
        // 版本 1B 内容：1
        byteBuf.writeByte(MessageFormatConstant.VERSION);
        // 头部大小 2B , 值：22
        byteBuf.writeShort(MessageFormatConstant.HEADER_LENGTH);
        // 总长度 header + body
        byteBuf.writeInt(MessageFormatConstant.HEADER_LENGTH + bodyBytes.length);
        // 响应码 1B
        byteBuf.writeByte(rpcResponse.getResponseCode());
        // 序列化类型 1B
        byteBuf.writeByte(serializeType);
        // 压缩类型 1B
        byteBuf.writeByte(rpcResponse.getCompressType());
        // 请求id 8B
        byteBuf.writeLong(rpcResponse.getRequestId());
        byteBuf.writeLong(rpcResponse.getTimestamp());

        // body 不是null 才写
        if(rpcResponse.getBody() != null) {
            byteBuf.writeBytes(bodyBytes);
        }
        if(bodyBytes.length != 0) {
            log.debug("请求【{}】的响应已在服务端完成编码", rpcResponse.getRequestId());
        }
    }

    private byte[] compress(byte[] bodyBytes, byte compressType) {
        Compressor compressor = CompressFactory.getCompressor(CompressType.getNameByType(compressType));
        return compressor.compress(bodyBytes);
    }

    private byte[] getBodyBytes(Object body, byte serializeType) {
        Serializer serializer = SerializerFactory.getSerializer(SerializerType.getNameByType(serializeType));
        return serializer.serialize(body);
    }
}
