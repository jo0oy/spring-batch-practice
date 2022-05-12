package com.jo0oy.springbatchpractice.part3;

public class NotFoundNameException extends RuntimeException {
    public NotFoundNameException() {
        super();
    }

    public NotFoundNameException(String message) {
        super(message);
    }
}
