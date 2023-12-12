package org.zjh.serialize;

import org.zjh.exception.SerializeException;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author zjh
 * @description: TODO
 **/
public class SerializerFactory {
    private static final Map<String,Serializer> SERIALIZER_MAP = new ConcurrentHashMap<>(16);

    static {
        SERIALIZER_MAP.put("jdk",new JdkSerializer());
        SERIALIZER_MAP.put("hessian",new HessianSerializer());
        //
    }

    public static Serializer getSerializer(String type) {
        Serializer serializer = SERIALIZER_MAP.get(type);
        if(serializer == null) {
            throw new SerializeException("未找到相应序列化器!");
        }
        return serializer;
    }
}
