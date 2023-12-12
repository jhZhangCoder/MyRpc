package org.zjh.config;

import lombok.Data;
import org.apache.commons.lang3.concurrent.CircuitBreaker;
import org.zjh.IdGenerator;
import org.zjh.ProtocolConfig;
import org.zjh.compress.CompressType;
import org.zjh.compress.Compressor;
import org.zjh.discovery.RegistryConfig;
import org.zjh.loadbalance.LoadBalancer;
import org.zjh.protection.MyCircuitBreaker;
import org.zjh.protection.RateLimiter;
import org.zjh.serialize.Serializer;
import org.zjh.serialize.SerializerType;
import org.zjh.spi.SpiResolver;

import java.net.SocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author zjh
 * @description: TODO
 **/
@Data
public class Configuration {
    private int port = 8099;

    private String appName;

    private ProtocolConfig protocolConfig;

    private RegistryConfig registryConfig = new RegistryConfig("zookeeper://127.0.0.1:2181");

    // id生成器
    private IdGenerator idGenerator = new IdGenerator(1,2);

    // 默认使用jdk方式进行序列化
    private byte serializeType = SerializerType.JDK.getType();

    // 默认使用gzip进行压缩
    private byte compressType = CompressType.GZIP.getType();

    private Serializer serializer;

    private Compressor compressor;

    private LoadBalancer loadBalancer;

    private Map<SocketAddress, RateLimiter> everyIpRateLimiter = new ConcurrentHashMap<>();

    private Map<SocketAddress, MyCircuitBreaker> everyIpRateCircuitBreaker = new ConcurrentHashMap<>();

    public Configuration() {
        // spi 发现相关配置
        SpiResolver spiResolver = new SpiResolver();
        spiResolver.loadFromSpi(this);

        // xml 读取相关配置
        XmlResolver xmlResolver = new XmlResolver();
        xmlResolver.loadFromXMl(this);

        System.out.println();
    }




    public static void main(String[] args) {
        new Configuration();
    }
}
