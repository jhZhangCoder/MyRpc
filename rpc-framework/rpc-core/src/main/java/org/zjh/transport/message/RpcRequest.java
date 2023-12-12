package org.zjh.transport.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author zjh
 * @description:服务调用方发送的请求内容
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RpcRequest {
    /**
     * 请求id
     */
    private Long requestId;

    /**
     * 请求类型
     */
    private byte requestType;

    /**
     * 压缩类型
     */
    private byte compressType;

    /**
     * 序列化类型
     */
    private byte serializeType;

    /**
     * 请求体
     */
    private RequestPayload requestPayload;

    private Long timestamp;
}
