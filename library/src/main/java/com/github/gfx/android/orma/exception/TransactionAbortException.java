package com.github.gfx.android.orma.exception;

public class TransactionAbortException extends RuntimeException {

    public TransactionAbortException(Throwable throwable) {
        super(throwable);
    }
}
