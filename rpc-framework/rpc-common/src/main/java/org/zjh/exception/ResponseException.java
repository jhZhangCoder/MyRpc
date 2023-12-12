package org.zjh.exception;

/**
 * @author zjh
 * @description: TODO
 **/
public class ResponseException extends RuntimeException {
    public ResponseException() {
    }

    public ResponseException(String message) {
        super(message);
    }

    public ResponseException(byte responseCode, String type) {
    }
}
