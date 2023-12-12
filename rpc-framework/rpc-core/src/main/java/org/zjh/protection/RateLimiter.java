package org.zjh.protection;

/**
 * @author zjh
 * @description: TODO
 **/
public interface RateLimiter {
    boolean allowRequest();
}
