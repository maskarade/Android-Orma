package com.github.gfx.android.orma;

import com.github.gfx.android.orma.adapter.TypeAdapterRegistry;
import com.github.gfx.android.orma.migration.MigrationEngine;
import com.github.gfx.android.orma.migration.SchemaDiffMigration;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public class OrmaConfiguration {

    @NonNull
    final Context context;

    @Nullable
    String name;

    TypeAdapterRegistry typeAdapterRegistry;

    MigrationEngine migrationEngine;

    boolean wal = true;

    final boolean debug;

    boolean trace;

    AccessThreadConstraint readOnMainThread;

    AccessThreadConstraint writeOnMainThread;

    public OrmaConfiguration(@NonNull Context context) {
        this.context = context.getApplicationContext();
        this.debug = extractDebuggable(context);
        this.name = context.getPackageName() + ".orma.db";

        // debug flags

        trace = debug;

        if (debug) {
            readOnMainThread = AccessThreadConstraint.WARNING;
            writeOnMainThread = AccessThreadConstraint.FATAL;
        } else {
            readOnMainThread = AccessThreadConstraint.NONE;
            writeOnMainThread = AccessThreadConstraint.NONE;
        }
    }

    static boolean extractDebuggable(Context context) {
        return (context.getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE)
                == ApplicationInfo.FLAG_DEBUGGABLE;
    }


    public OrmaConfiguration name(@Nullable String name) {
        this.name = name;
        return this;
    }

    public OrmaConfiguration migrationEngine(@NonNull MigrationEngine migrationEngine) {
        this.migrationEngine = migrationEngine;
        return this;
    }

    public OrmaConfiguration writeAheadLogging(boolean wal) {
        this.wal = wal;
        return this;
    }

    public OrmaConfiguration trace(boolean trace) {
        this.trace = trace;
        return this;
    }

    public OrmaConfiguration readConstraint(AccessThreadConstraint readOnMainThread) {
        this.readOnMainThread = readOnMainThread;
        return this;
    }

    public OrmaConfiguration writeConstraint(AccessThreadConstraint writeOnMainThread) {
        this.writeOnMainThread = writeOnMainThread;
        return this;
    }

    public OrmaConfiguration fillDefaults() {

        if (migrationEngine == null) {
            migrationEngine = new SchemaDiffMigration(context, debug);
        }

        if (typeAdapterRegistry == null) {
            typeAdapterRegistry = new TypeAdapterRegistry();
            typeAdapterRegistry.addAll(TypeAdapterRegistry.defaultTypeAdapters());
        }

        return this;
    }
}
