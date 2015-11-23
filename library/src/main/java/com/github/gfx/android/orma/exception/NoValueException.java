package com.github.gfx.android.orma.exception;

@SuppressWarnings("serial")
public class NoValueException extends OrmaException {

    public NoValueException(String detailMessage) {
        super(detailMessage);
    }

    public NoValueException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }
}
