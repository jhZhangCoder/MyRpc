package org.zjh.protection;

import lombok.Data;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author zjh
 * @description: TODO
 **/
@Data
public class MyCircuitBreaker {
    private volatile boolean isOpen = false;

    private AtomicInteger totalRequest = new AtomicInteger(0);

    private AtomicInteger errorRequest = new AtomicInteger(0);

    private int maxErrorRequest;
    private float maxErrorRate;

    public MyCircuitBreaker(int maxErrorRequest, float maxErrorRate) {
        this.maxErrorRequest = maxErrorRequest;
        this.maxErrorRate = maxErrorRate;
    }

    public boolean isBreak() {
        if(isOpen) {
            return true;
        }
        if(errorRequest.get() > maxErrorRequest) {
            this.isOpen = true;
            return true;
        }
        if(errorRequest.get() > 0 && totalRequest.get() > 0 &&
                errorRequest.get()/(float)totalRequest.get() > maxErrorRate) {
            this.isOpen = true;
            return true;
        }
        return false;
    }

    public void recordRequest() {
        this.totalRequest.incrementAndGet();
    }

    public void recordException() {
        this.errorRequest.incrementAndGet();
    }

    /**
     * 重置熔断器状态
     */
    public void reset() {
        this.isOpen = false;
        this.totalRequest.set(0);
        this.errorRequest.set(0);
    }
}
