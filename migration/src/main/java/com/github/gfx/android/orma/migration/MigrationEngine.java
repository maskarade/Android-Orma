package com.github.gfx.android.orma.migration;

import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;

import java.util.List;

public interface MigrationEngine {

    int getVersion();

    void start(@NonNull SQLiteDatabase db, @NonNull List<NamedDdl> schemas);
}
