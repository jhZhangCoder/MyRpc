package org.zjh.exception;

import java.io.IOException;

/**
 * @author zjh
 * @description: TODO
 **/
public class CompressException extends RuntimeException {
    public CompressException(Throwable e) {
    }

    public CompressException() {
    }

    public CompressException(String message) {
        super(message);
    }
}
