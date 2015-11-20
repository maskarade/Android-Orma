package com.github.gfx.android.orma.migration;

public class MigrationAbortException extends RuntimeException {

    public MigrationAbortException(Throwable throwable) {
        super(throwable);
    }
}
