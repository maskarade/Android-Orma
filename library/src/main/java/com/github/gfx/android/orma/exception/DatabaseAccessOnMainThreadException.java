package com.github.gfx.android.orma.exception;

@SuppressWarnings("serial")
public class DatabaseAccessOnMainThreadException extends OrmaException {

    public DatabaseAccessOnMainThreadException(String detailMessage) {
        super(detailMessage);
    }
}
