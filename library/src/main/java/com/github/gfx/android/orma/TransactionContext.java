package com.github.gfx.android.orma;

import com.github.gfx.android.orma.exception.TransactionAbortException;

import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;

public class TransactionContext {

    final SQLiteDatabase db;

    public TransactionContext(@NonNull SQLiteDatabase db) {
        this.db = db;
    }

    public void setTransactionSuccessful() {
        if (!db.inTransaction()) {
            throw new TransactionAbortException("Not in transaction; maybe you have switched the execution thread?");
        }
        db.setTransactionSuccessful();
    }

    public void endTransaction() {
        db.endTransaction();
    }

    public void yieldIfContendedSafely() {
        db.yieldIfContendedSafely();
    }

    public void yieldIfContendedSafely(long sleepAfterYieldDelay) {
        db.yieldIfContendedSafely(sleepAfterYieldDelay);
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
    }
}
