package org.zjh.transport.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author zjh
 * @description: 响应报文
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RpcResponse implements Serializable {
    private Long requestId;

    private byte compressType;

    private byte serializeType;

    private byte responseCode;

    private Object body;

    private Long timestamp;
}
