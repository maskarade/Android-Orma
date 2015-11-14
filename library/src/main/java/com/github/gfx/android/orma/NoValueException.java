package com.github.gfx.android.orma;

@SuppressWarnings("serial")
public class NoValueException extends RuntimeException {

    public NoValueException(String detailMessage) {
        super(detailMessage);
    }

    public NoValueException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }
}
