package org.zjh.exception;

/**
 * @author zjh
 * @description: TODO
 **/
public class DiscoveryException extends RuntimeException {
    public DiscoveryException(String message) {
        super(message);
    }

    public DiscoveryException() {
        super();
    }

    public DiscoveryException(Throwable cause) {
        super(cause);
    }
}
