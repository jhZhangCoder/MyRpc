package org.zjh.channelHandler.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import org.zjh.RpcbootStrap;
import org.zjh.ServiceConfig;
import org.zjh.enumeration.RequestType;
import org.zjh.enumeration.ResponseCode;
import org.zjh.protection.RateLimiter;
import org.zjh.protection.TokenBucketRateLimiter;
import org.zjh.transport.message.RequestPayload;
import org.zjh.transport.message.RpcRequest;
import org.zjh.transport.message.RpcResponse;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.SocketAddress;
import java.util.Map;

/**
 * @author zjh
 * @description: TODO
 **/
@Slf4j
public class MethodCallHandler extends SimpleChannelInboundHandler<RpcRequest> {
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, RpcRequest rpcRequest) throws Exception {

        // 封装部分响应
        RpcResponse response = RpcResponse.builder()
                .compressType(rpcRequest.getCompressType())
                .requestId(rpcRequest.getRequestId())
                .serializeType(rpcRequest.getSerializeType())
                .timestamp(System.currentTimeMillis())
                .responseCode(ResponseCode.RESOURCE_NOT_FOUND.getId()).build();
        SocketAddress socketAddress = channelHandlerContext.channel().remoteAddress();
        Map<SocketAddress, RateLimiter> everyIpRateLimiter = RpcbootStrap.getInstance().getConfiguration().getEveryIpRateLimiter();
        RateLimiter rateLimiter = everyIpRateLimiter.get(socketAddress);
        if (rateLimiter == null) {
            rateLimiter = (RateLimiter) new TokenBucketRateLimiter(5,1);
            everyIpRateLimiter.put(socketAddress,rateLimiter);
        }

        if(!rateLimiter.allowRequest()) {
            response.setResponseCode(ResponseCode.RATE_LIMIT.getId());
        }else if(rpcRequest.getRequestType() == RequestType.HEART_BEAT.getId()) {
            // 心跳类型 直接写回
            response.setResponseCode(ResponseCode.SUCCESS_HEART_BEAT.getId());
        }else {
            RequestPayload requestPayload = rpcRequest.getRequestPayload();
            Object result = null;
            try {
                result = callTargetMethod(requestPayload);
                response.setResponseCode(ResponseCode.SUCCESS.getId());
                response.setBody(result);
            } catch (Exception e) {
                response.setResponseCode(ResponseCode.FAIL.getId());
            }
        }
        channelHandlerContext.channel().writeAndFlush(response);

    }

    private Object callTargetMethod(RequestPayload requestPayload) {
        String interfaceName = requestPayload.getInterfaceName();
        String method = requestPayload.getMethod();
        Class<?>[] parameterType = requestPayload.getParameterType();
        Object[] parameterValue = requestPayload.getParameterValue();
        // 从服务列表缓存中获取服务
        ServiceConfig<?> serviceConfig = RpcbootStrap.SERVER_LIST.get(interfaceName);
        Object ref = serviceConfig.getRef();
        Method targetMethod = null;
        Object result = null;
        try {
            Class<?> targetInterface = ref.getClass();
            targetMethod = targetInterface.getMethod(method, parameterType);
            result = targetMethod.invoke(ref, parameterValue);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            log.error("执行【{}】目标方法【{}】发生异常",interfaceName,method,e);
            throw new RuntimeException("执行目标方法发生异常");
        }
        return result;
    }
}
