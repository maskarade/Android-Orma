package com.github.gfx.android.orma.exception;

@SuppressWarnings("serial")
public class TransactionAbortException extends OrmaException {

    public TransactionAbortException(Throwable throwable) {
        super(throwable);
    }
}
