package com.github.gfx.android.orma.exception;

@SuppressWarnings("serial")
public abstract class OrmaException extends RuntimeException {

    public OrmaException(String detailMessage) {
        super(detailMessage);
    }

    public OrmaException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public OrmaException(Throwable throwable) {
        super(throwable);
    }
}
