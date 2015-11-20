package com.github.gfx.android.orma.migration;

import com.github.gfx.android.orma.Schema;

import android.database.sqlite.SQLiteDatabase;

import java.util.List;

public interface MigrationEngine {

    int getVersion();

    void onMigrate(SQLiteDatabase db, List<Schema<?>> schemas, int oldVersion, int newVersion);
}
