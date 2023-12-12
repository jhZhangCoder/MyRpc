package org.zjh.transport.message;

import lombok.*;

import java.io.Serializable;

/**
 * @author zjh
 * @description: 具体的消息体： 接口名，方法名，参数列表....
 **/
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class RequestPayload implements Serializable {

    /**
     * 接口名  -  com.xizhe.HelloRpc
     */
    private String interfaceName;

    /**
     * 方法名 - sayHello()
     */
    private String method;

    /**
     * 参数类型 - java.lang.String
     */
    private Class<?>[] parameterType;

    /**
     * 具体参数 - "helloRpc"
     */
    private Object[] parameterValue;

    /**
     *  返回值 - java.lang.String
     */
    private Class<?> returnType;
}
