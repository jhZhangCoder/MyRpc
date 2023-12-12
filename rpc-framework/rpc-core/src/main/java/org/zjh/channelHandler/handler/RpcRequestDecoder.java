package org.zjh.channelHandler.handler;

import ch.qos.logback.core.rolling.helper.Compressor;
import com.sun.org.apache.xml.internal.serializer.Serializer;
import com.sun.org.apache.xml.internal.serializer.SerializerFactory;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import lombok.extern.slf4j.Slf4j;
import org.zjh.MessageFormatConstant;
import org.zjh.enumeration.RequestType;
import org.zjh.transport.message.RequestPayload;
import org.zjh.transport.message.RpcRequest;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.nio.ByteOrder;
import java.util.Random;

/**
 * @author zjh
 * @description: TODO
 **/
@Slf4j
public class RpcRequestDecoder extends LengthFieldBasedFrameDecoder {
    public RpcRequestDecoder() {
        super(
                // 最大帧的长度
                MessageFormatConstant.MAX_FRAME_LENGTH,
                // 长度字段的偏移量
                MessageFormatConstant.MAGIC.length
                        + MessageFormatConstant.VERSION_LENGTH
                        + MessageFormatConstant.HEADER_FIELD_LENGTH,
                // 长度字段所占长度
                MessageFormatConstant.FULL_FIELD_LENGTH,
                // 负载的适配长度
                -(MessageFormatConstant.MAGIC.length + MessageFormatConstant.VERSION_LENGTH
                        + MessageFormatConstant.HEADER_FIELD_LENGTH + MessageFormatConstant.FULL_FIELD_LENGTH ),
                0);
    }

    @Override
    protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {

        Thread.sleep(new Random().nextInt(50));
        Object decode = super.decode(ctx, in);
        if(decode instanceof ByteBuf ) {
            ByteBuf byteBuf = (ByteBuf) decode;
            return decodeFrame(byteBuf);
        }
        throw new RuntimeException();
    }
    private Object decodeFrame(ByteBuf byteBuf) {
        // magic(4B) version(1B) headLength(2B) fullLength(4B) requestType(1B)
        // serializeType(1B) compressType(1B) RequestId(8B) body(?)

        // 1. 解析魔术
        byte[] magic = new byte[MessageFormatConstant.MAGIC.length];
        byteBuf.readBytes(magic);
        for (int i = 0; i < magic.length; i++) {
            if(magic[i] != MessageFormatConstant.MAGIC[i]) {
                throw new RuntimeException("请求不合法!");
            }
        }
        // 2. 解析版本
        byte version = byteBuf.readByte();
        if(version > MessageFormatConstant.VERSION) {
            throw new RuntimeException("不支持该请求版本！");
        }
        // 3. 解析头部长度
        short headerLength = byteBuf.readShort();
        // 4. 解析总长度
        int fullLength = byteBuf.readInt();
        // 5. 解析请求类型
        byte requestType = byteBuf.readByte();
        // 6. 解析序列化类型
        byte serializeType = byteBuf.readByte();
        // 7. 解析压缩类型
        byte compressType = byteBuf.readByte();
        // 8. 解析请求id
        Long requestId = byteBuf.readLong();

        long timestamp = byteBuf.readLong();
        // 9. 解析body
        byte[] body = new byte[fullLength - headerLength];
        byteBuf.readBytes(body);


        RpcRequest request = RpcRequest.builder()
                .serializeType(serializeType)
                .requestType(requestType)
                .compressType(compressType)
                .requestId(requestId)
                .timestamp(timestamp)
                .build();
        // 心跳请求没有负载
        if(request.getRequestType() == RequestType.HEART_BEAT.getId()) {
            return request;
        }

        try(ByteArrayInputStream bais = new ByteArrayInputStream(body);
            ObjectInputStream ois = new ObjectInputStream(bais)
        ) {
            RequestPayload payload = (RequestPayload) ois.readObject();
            request.setRequestPayload(payload);
        } catch (IOException | ClassNotFoundException e) {
            log.error("请求【{}】反序列化出现异常:",requestId,e);
        }

        log.debug("请求【{}】已经在服务端完成解码",request.getRequestId());
        return request;

    }



}
