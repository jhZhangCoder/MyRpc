package org.zjh.channelHandler.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import org.zjh.exception.ResponseException;
import org.zjh.protection.MyCircuitBreaker;
import org.zjh.RpcbootStrap;
import org.zjh.enumeration.ResponseCode;
import org.zjh.transport.message.RpcResponse;

import java.util.concurrent.CompletableFuture;

/**
 * @author zjh
 * @description: TODO
 **/
@Slf4j
public class MySimpleChannelInboundHandler extends SimpleChannelInboundHandler<RpcResponse> {
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, RpcResponse response) throws Exception {

        byte responseCode = response.getResponseCode();
        Object body = response.getBody();
        CompletableFuture<Object> future = RpcbootStrap.PENDING_REQUEST.get(response.getRequestId());
        MyCircuitBreaker myCircuitBreaker = (MyCircuitBreaker) RpcbootStrap.getInstance().getConfiguration()
                .getEveryIpRateCircuitBreaker().get(channelHandlerContext.channel().remoteAddress());
        if(responseCode == ResponseCode.FAIL.getId()) {
            myCircuitBreaker.recordException();
            future.complete(null);
            log.error("请求【{}】失败", response.getRequestId());
            throw new ResponseException(responseCode,ResponseCode.FAIL.getType());
        }else if(responseCode == ResponseCode.SUCCESS.getId()) {
            log.debug("客户端收到响应:{}", body);
            future.complete(body);
        }else if(responseCode == ResponseCode.RATE_LIMIT.getId()) {
            myCircuitBreaker.recordException();
            future.complete(null);
            log.error("请求【{}】被限流", response.getRequestId());
            throw new ResponseException(responseCode,ResponseCode.RATE_LIMIT.getType());
        }else if(responseCode == ResponseCode.SUCCESS_HEART_BEAT.getId()) {
            // 心跳请求
            future.complete(response.getTimestamp());
        }else if(responseCode == ResponseCode.RESOURCE_NOT_FOUND.getId()) {
            myCircuitBreaker.recordException();
            future.complete(null);
            log.error("请求【{}】找不到资源", response.getRequestId());
            throw new ResponseException(responseCode,ResponseCode.RESOURCE_NOT_FOUND.getType());
        }

    }
}
