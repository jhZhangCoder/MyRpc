package org.zjh.exception;

import java.io.IOException;

/**
 * @author zjh
 * @description: TODO
 **/
public class SerializeException extends RuntimeException {
    public SerializeException(Throwable cause) {
        super(cause);
    }

    public SerializeException() {
    }

    public SerializeException(String message) {
        super(message);
    }
}
