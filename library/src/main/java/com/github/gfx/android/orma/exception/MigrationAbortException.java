package com.github.gfx.android.orma.exception;

public class MigrationAbortException extends RuntimeException {

    public MigrationAbortException(Throwable throwable) {
        super(throwable);
    }
}
