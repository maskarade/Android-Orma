package com.github.gfx.android.orma;

import com.github.gfx.android.orma.exception.TransactionAbortException;

public abstract class TransactionTask {

    public abstract void execute() throws Exception;

    public void onError(Exception exception) {
        throw new TransactionAbortException(exception);
    }
}
