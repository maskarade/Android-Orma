package com.github.gfx.android.orma;

import com.github.gfx.android.orma.exception.TransactionAbortException;

import android.support.annotation.NonNull;

public abstract class TransactionTask {

    public abstract void execute() throws Exception;

    public void onError(@NonNull Exception exception) {
        throw new TransactionAbortException(exception);
    }
}
