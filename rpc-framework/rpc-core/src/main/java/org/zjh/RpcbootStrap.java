package org.zjh;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;
import org.zjh.annotation.RpcApi;
import org.zjh.channelHandler.handler.RpcRequestDecoder;
import org.zjh.compress.CompressType;
import org.zjh.config.Configuration;
import org.zjh.discovery.Registry;
import org.zjh.discovery.RegistryConfig;
import org.zjh.heartbeat.HeartBeatDetector;
import org.zjh.loadbalance.LoadBalancer;
import org.zjh.serialize.SerializerType;
import org.zjh.transport.message.RpcRequest;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.InetSocketAddress;
import java.net.URL;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @author zjh
 * @description: TODO
 **/
@Slf4j
public class RpcbootStrap {

    private static final RpcbootStrap instance = new RpcbootStrap();

    private Configuration configuration;
    // 维护一个已经发布的服务列表 key: interface的全限定名 value:serviceConfig
    public static final Map<String,ServiceConfig<?>> SERVER_LIST = new ConcurrentHashMap<>(16);
    // 维护一个Channel缓存, InetSocketAddress作为key需要重写equals和hashcode
    public static final Map<InetSocketAddress, Channel> CHANNEL_CACHE = new ConcurrentHashMap<>(16);

    public static final SortedMap<Long,Channel> ANSWER_TIME_CACHE = new TreeMap<>();
    // 维护一个对外挂起的completablefuture
    public static final Map<Long, CompletableFuture<Object>> PENDING_REQUEST = new ConcurrentHashMap<>(128);

    public static ThreadLocal<RpcRequest> REQUEST_THREAD_LOCAL = new ThreadLocal<>();



    private RpcbootStrap() {
        configuration = new Configuration();
    }

    public static RpcbootStrap getInstance() {
        return instance;
    }
    
    /*
     * @Author zjh
     * @Description  定义当前应用的名称
     * @Param [appName]
     * @return org.zjh.RpcbootStrap
     **/
    public RpcbootStrap application(String appName){
        this.configuration.setAppName(appName);
        return this;
    }

    /*
     * @Author zjh
     * @Description 配置注册中心
     * @Param [registryConfig]
     * @return org.zjh.RpcbootStrap
     **/
    public RpcbootStrap registry(RegistryConfig registryConfig){
        this.configuration.setRegistryConfig(registryConfig);
        return this;
    }


    /*
     * @Author zjh
     * @Description 配置当前序列化协议
     * @Param [protocolConfig]
     * @return org.zjh.RpcbootStrap
     **/
    public RpcbootStrap protocol(ProtocolConfig protocolConfig) {
        this.configuration.setProtocolConfig(protocolConfig);
        log.debug("当前工程使用了: {} 协议进行序列化",protocolConfig.getProtocolName());
        return this;
    }

    /*
     * @Author zjh
     * @Description 配置一个负载均衡器
     * @Param [loadBalancer]
     * @return org.zjh.RpcbootStrap
     **/
    public RpcbootStrap loadBalance(LoadBalancer loadBalancer) {
        this.configuration.setLoadBalancer(loadBalancer);
        return this;
    }

    /*
     * @Author zjh
     * @Description 发布服务,将接口-> 实现 注册到服务中心
     * @Param [service]
     * @return org.zjh.RpcbootStrap
     **/
    public RpcbootStrap publish(ServiceConfig<?> service){
        this.configuration.getRegistryConfig().getRegistry().registry(service);
        SERVER_LIST.put(service.getInterface().getName(),service);
        return this;
    }


    /*
     * @Author zjh
     * @Description 批量发布
     * @Param [services]
     * @return org.zjh.RpcbootStrap
     **/
    public RpcbootStrap publish(List<ServiceConfig<?>> services){
        services.forEach(this::publish);
        return this;
    }

    /*
     * @Author zjh
     * @Description 发布netty服务
     * @Param []
     * @return void
     **/
    public void start() {
        // 1. 创建eventloop , 老板只负责接活，之后将请求分发至worker
        NioEventLoopGroup boss = new NioEventLoopGroup(2);
        NioEventLoopGroup worker = new NioEventLoopGroup(10);
        try {
            // 2. 需要一个服务器引导程序
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            // 3. 配置服务器
            serverBootstrap = serverBootstrap.group(boss,worker)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline()
                                    .addLast(new LoggingHandler())
                                    .addLast(new RpcRequestDecoder());
                        }
                    });
            // 4. 绑定端口
            ChannelFuture channelFuture = serverBootstrap.bind(Constant.port).sync();
            channelFuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            try {
                boss.shutdownGracefully().sync();
                worker.shutdownGracefully().sync();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * ------------------------------------ 服务调用方api --------------------------------------------------
     */
    public RpcbootStrap reference(ReferenceConfig<?> reference) {
        reference.setRegistryConfig(this.configuration.getRegistryConfig());
        // 开启对该服务的心跳检测
        HeartBeatDetector.detectHeartBeat(reference.getInterface().getName());
        return this;
    }


    public RpcbootStrap serialize(SerializerType serializerType) {
        this.configuration.setSerializeType(serializerType.getType());
        return this;
    }

    public Registry getRegistry() {
        return this.configuration.getRegistryConfig().getRegistry();
    }

    public RpcbootStrap compress(CompressType gzip) {
        this.configuration.setCompressType(gzip.getType());
        return this;
    }

    public RpcbootStrap scan(String packageName) {
        // 获取通过packageName得到其下的所有类的全限定名
        List<String> names = getAllClassName(packageName);
        // 通过反射获取目标接口，构建具体实现
        List<? extends Class<?>> classes = names.stream().map(name -> {
            try {
                return Class.forName(name);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }).filter(clazz -> clazz.getAnnotation(RpcApi.class) != null).collect(Collectors.toList());
        for (Class<?> clazz : classes) {
            Class<?>[] interfaces = clazz.getInterfaces();
            Object instance = null;
            try {
                instance = clazz.getConstructor().newInstance();
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
            List<ServiceConfig<?>> serviceConfigs = new ArrayList<>();
            for (Class<?> anInterface : interfaces) {
                ServiceConfig<Object> serviceConfig = new ServiceConfig();
                serviceConfig.setInterface(anInterface);
                serviceConfig.setRef(instance);
                serviceConfigs.add(serviceConfig);
                publish(serviceConfig);
                log.debug("【{}】已通过包扫描进行发布",anInterface);
            }
        }

        return this;
    }

    private List<String> getAllClassName(String packageName) {
        String basePath = packageName.replaceAll("\\.","/");
        System.out.println(basePath);
        URL resource = ClassLoader.getSystemClassLoader().getResource(basePath);
        if (resource == null) {
            throw new RuntimeException();
        }
        String absolutePath = resource.getPath();
        System.out.println(absolutePath);
        List<String> classNames = new ArrayList<>();
        classNames = recursionFile(absolutePath,classNames);
        return classNames;
    }

    private List<String> recursionFile(String absolutePath, List<String> classNames) {
        File file = new File(absolutePath);
        if(file.isDirectory()) {
            File[] children = file.listFiles(pathname -> pathname.isDirectory() || pathname.getPath().contains(".class"));
            if (children == null || children.length == 0) {
                return classNames;
            }
            for (File child : children) {
                if(child.isDirectory()) {
                    recursionFile(child.getAbsolutePath(),classNames);
                }else {
                    String className = getClassNameByAbsolutePath(child.getAbsolutePath());
                    classNames.add(className);
                }
            }
        }else {
            String className = getClassNameByAbsolutePath(absolutePath);
            classNames.add(className);
        }
        return classNames;
    }

    private String getClassNameByAbsolutePath(String absolutePath) {
        // D:\Java-course\rpc\framework\core\target\classes\com\xizhe\watch\UpAndDownWatcher.class
        // -> com\xizhe\watch\UpAndDownWatcher.class
        // -> com.xizhe.watch.UpAndDownWatcher
        String[] split = absolutePath.split("classes\\\\");
        String path = split[split.length - 1];
        String[] split1 = path.replaceAll("\\\\", ".").split("\\.class");
        return split1[split1.length -1];
    }

    public static void main(String[] args) {
        RpcbootStrap.getInstance().getAllClassName("com.xizhe");
    }

    public Configuration getConfiguration() {
        return configuration;
    }





    

}
