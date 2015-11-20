package com.github.gfx.android.orma.migration;

import android.database.sqlite.SQLiteDatabase;

import java.util.List;

public interface MigrationEngine {

    int getVersion();

    void onMigrate(SQLiteDatabase db, List<NamedDdl> namedDDLs, int oldVersion, int newVersion);
}
