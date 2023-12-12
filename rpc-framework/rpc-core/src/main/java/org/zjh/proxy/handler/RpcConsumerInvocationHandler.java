package org.zjh.proxy.handler;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import lombok.extern.slf4j.Slf4j;
import org.zjh.NettyBootstrapInitializer;
import org.zjh.RpcbootStrap;
import org.zjh.annotation.TryTimes;
import org.zjh.discovery.Registry;
import org.zjh.enumeration.RequestType;
import org.zjh.exception.NetWorkException;
import org.zjh.protection.MyCircuitBreaker;
import org.zjh.transport.message.RequestPayload;
import org.zjh.transport.message.RpcRequest;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author zjh
 * @description: TODO
 **/
@Slf4j
public class RpcConsumerInvocationHandler implements InvocationHandler {
    private Registry registry;

    private Class<?> interfaceRef;

    public RpcConsumerInvocationHandler() {
    }

    public RpcConsumerInvocationHandler(Registry registry, Class<?> interfaceRef) {
        this.registry = registry;
        this.interfaceRef = interfaceRef;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        InetSocketAddress address = RpcbootStrap.getInstance().getConfiguration().getLoadBalancer()
                .selectServiceAddress(interfaceRef.getName());
        // 获取重试参数
        int tryTimes = 0;
        int intervalTime = 0;
        TryTimes annotation = method.getAnnotation(TryTimes.class);
        if (annotation != null) {
            tryTimes = annotation.times();
            intervalTime = annotation.intervalTime();
        }
        RpcRequest request = null;
        // 获取当前地址的熔断器
        Map<SocketAddress, MyCircuitBreaker> everyIpRateCircuitBreaker = RpcbootStrap.getInstance().getConfiguration().getEveryIpRateCircuitBreaker();
        MyCircuitBreaker myCircuitBreaker = everyIpRateCircuitBreaker.get(address);
        if (myCircuitBreaker == null) {
            myCircuitBreaker = new MyCircuitBreaker(10,0.5f);
            everyIpRateCircuitBreaker.put(address, myCircuitBreaker);
        }
        while(tryTimes > 0) {
            try {
                if(myCircuitBreaker.isBreak()) {
                    Timer timer = new Timer();
                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            RpcbootStrap.getInstance().getConfiguration()
                                    .getEveryIpRateCircuitBreaker().get(address).reset();
                        }
                    },5000);
                    log.error("服务【{}】被熔断",interfaceRef.getName());
                    throw new RuntimeException("服务被熔断");
                }

                RequestPayload requestPayload = RequestPayload.builder()
                        .interfaceName(interfaceRef.getName())
                        .method(method.getName())
                        .parameterType(method.getParameterTypes())
                        .parameterValue(args)
                        .returnType(method.getReturnType())
                        .build();

                request = RpcRequest.builder()
                        .requestId(RpcbootStrap.getInstance().getConfiguration().getIdGenerator().getId())
                        .requestType(RequestType.REQUEST.getId())
                        .compressType(RpcbootStrap.getInstance().getConfiguration().getCompressType())
                        .serializeType(RpcbootStrap.getInstance().getConfiguration().getSerializeType())
                        .requestPayload(requestPayload)
                        .timestamp(System.currentTimeMillis())
                        .build();

                RpcbootStrap.REQUEST_THREAD_LOCAL.set(request);
                // 负载均衡  从服务列表寻找一个可用服务
                Thread.sleep(200);

                log.debug("consumer发现服务【{}】可用主机:{}", interfaceRef.getName(), address);

                // 获取可用channel
                Channel channel = getAvailableChannel(address);
                log.debug("获取了与【{}】建立的连接通道,准备发送数据", address.toString());

                CompletableFuture<Object> future = new CompletableFuture<>();
                // 将future暴露出去 等到服务端提供响应时候调用complete方法
                RpcbootStrap.PENDING_REQUEST.put(request.getRequestId(), future);
                channel.writeAndFlush(request)
                        .addListener((ChannelFutureListener) promise -> {
                            // 当数据已经写完 promise就结束了
                            // 我们需要的是 数据写完后 服务端给的返回值
                            if (!promise.isSuccess()) {
                                future.completeExceptionally(promise.cause());
                            }
                        });
                RpcbootStrap.REQUEST_THREAD_LOCAL.remove();

                // 阻塞获取服务端提供的响应
                Object result = future.get(10, TimeUnit.SECONDS);
                myCircuitBreaker.recordRequest();
                return result;
            }catch(Exception e) {
                tryTimes--;
                Thread.sleep(intervalTime);
                if(tryTimes<=0) {
                    log.error("发生网络异常,请求发送失败");
                    break;
                }
            }
        }
        return null;
    }

    private Channel getAvailableChannel(InetSocketAddress address) {
        // 从缓存中读取channel
        Channel channel = RpcbootStrap.CHANNEL_CACHE.get(address);
        if (channel == null) {
            // 同步操作: await 会阻塞直到连接成功返回
            // Channel channelNew = NettyBootstrapInitializer.getBootstrap()
            //              .connect(address).await().channel();

            // 异步操作: addListener,通过CompletableFuture获取子线程操作结果
            CompletableFuture<Channel> channelFuture = new CompletableFuture<>();
            NettyBootstrapInitializer.getBootstrap().connect(address).addListener(
                    (ChannelFutureListener) promise -> {
                        if (promise.isDone()) {
                            channelFuture.complete(promise.channel());
                        }else if(!promise.isSuccess()) {
                            channelFuture.completeExceptionally(promise.cause());
                        }
                    });
            // 阻塞获取channel
            try {
                channel = channelFuture.get(3, TimeUnit.SECONDS);
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                log.error("获取通道时发生异常,address:{}",address);
                throw new NetWorkException("获取channel发生异常!");
            }
            if (channel == null) {
                log.error("获取通道时发生异常,address:{}",address);
                throw new NetWorkException("获取channel发生异常!");
            }
            RpcbootStrap.CHANNEL_CACHE.put(address,channel);
        }
        return channel;
    }
}
