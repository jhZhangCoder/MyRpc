package org.zjh.exception;

/**
 * @author zjh
 * @description: TODO
 **/
public class LoadBalanceException extends RuntimeException {
    public LoadBalanceException(Throwable cause) {
        super(cause);
    }

    public LoadBalanceException() {
    }

    public LoadBalanceException(String message) {
        super(message);
    }
}
