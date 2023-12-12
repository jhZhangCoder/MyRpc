package org.zjh.exception;

/**
 * @author zjh
 * @description: TODO
 **/
public class ZookeeperException extends RuntimeException {
    public ZookeeperException(String message) {
        super(message);
    }

    public ZookeeperException() {
        super();
    }

    public ZookeeperException(Throwable cause) {
        super(cause);
    }
}
