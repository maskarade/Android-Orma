package com.github.gfx.android.orma;

public class TransactionAbortException extends RuntimeException {

    public TransactionAbortException(Throwable throwable) {
        super(throwable);
    }
}
