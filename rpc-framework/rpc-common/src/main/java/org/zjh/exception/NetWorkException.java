package org.zjh.exception;

/**
 * @author zjh
 * @description: TODO
 **/
public class NetWorkException extends RuntimeException {
    public NetWorkException(String message) {
        super(message);
    }

    public NetWorkException() {
        super();
    }

    public NetWorkException(Throwable cause) {
        super(cause);
    }
}
