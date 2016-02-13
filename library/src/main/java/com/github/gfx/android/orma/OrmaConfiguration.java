/*
 * Copyright (c) 2015 FUJI Goro (gfx).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.gfx.android.orma;

import com.github.gfx.android.orma.adapter.TypeAdapter;
import com.github.gfx.android.orma.adapter.TypeAdapterRegistry;
import com.github.gfx.android.orma.migration.MigrationEngine;
import com.github.gfx.android.orma.migration.SchemaDiffMigration;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * This class represents Orma options, and it is the base class of {@code OrmaDatabase.Builder}.
 */
@SuppressWarnings("unchecked")
public abstract class OrmaConfiguration<T extends OrmaConfiguration<?>> {

    @NonNull
    final Context context;

    final boolean debug;

    @Nullable
    String name;

    @SuppressWarnings("deprecated")
    TypeAdapterRegistry typeAdapterRegistry;

    MigrationEngine migrationEngine;

    boolean foreignKeys = true;

    boolean wal = true;

    boolean trace;

    boolean tryParsingSql;

    AccessThreadConstraint readOnMainThread;

    AccessThreadConstraint writeOnMainThread;

    public OrmaConfiguration(@NonNull Context context) {
        this.context = context.getApplicationContext();
        this.debug = extractDebuggable(context);
        this.name = context.getPackageName() + ".orma.db";

        // debug flags

        trace = debug;
        tryParsingSql = debug;

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

    /**
     * Replaces the database name. {@code null} for on-memory databases.
     *
     * @param name A filename or {@code null}
     * @return the receiver itself
     */
    public T name(@Nullable String name) {
        this.name = name;
        return (T) this;
    }

    /**
     * Adds type adapters. You can override the defaults.
     *
     * @param typeAdapters Custom type adapters to add
     * @return the receiver itself
     */
    @Deprecated
    @SuppressWarnings("deprecated")
    public T typeAdapters(@NonNull TypeAdapter<?>... typeAdapters) {
        if (typeAdapterRegistry == null) {
            typeAdapterRegistry = new TypeAdapterRegistry();
            typeAdapterRegistry.addAll(TypeAdapterRegistry.defaultTypeAdapters());
        }
        typeAdapterRegistry.addAll(typeAdapters);
        return (T) this;
    }

    /**
     * Replaces the migration engine with your own. {@link SchemaDiffMigration} is the default.
     *
     * @param migrationEngine A migration engine to replace the default.
     * @return the receiver itself
     */
    public T migrationEngine(@NonNull MigrationEngine migrationEngine) {
        this.migrationEngine = migrationEngine;
        return (T) this;
    }

    /**
     * Controls write-ahead logging in SQLite. The default is {@code true}.
     *
     * @param wal {@code true} to enable WAL
     * @return the receiver itself
     * @see <a href="http://sqlite.org/wal.html">Write-Ahead Logging in SQLite</a>
     */
    public T writeAheadLogging(boolean wal) {
        this.wal = wal;
        return (T) this;
    }

    /**
     * Controls {@code foreign_keys} support in SQLite. The default is {@code true}.
     *
     * @param foreignKeys {@code true} to enable {@code foreign_keys}
     * @return The receiver itself
     * @see <a href="https://www.sqlite.org/foreignkeys.html">https://www.sqlite.org/foreignkeys.html</a>
     */
    public T foreignKeys(boolean foreignKeys) {
        this.foreignKeys = foreignKeys;
        return (T) this;
    }

    /**
     * If true, each `CREATE TABLE` statement used in Orma is passed to internal SQLite parser before executed.
     * The SQLite parser is used in migration, so the flag ensures the statement is migration ready.
     *
     * The default depends on the application's {@code BuildConfig.DEBUG}, but do not use this flag unless you know
     * what you do. This is provided to test Orma itself.
     *
     * @param tryParsingSql {@code true} to try parsing SQL for each `CREATE TABLE` statement
     * @return the receiver itself
     */
    public T tryParsingSql(boolean tryParsingSql) {
        this.tryParsingSql = tryParsingSql;
        return (T) this;
    }

    /**
     * If {@code true}, each SQL is logged to console before executed.
     *
     * @param trace {@code true} to enable SQL tracing
     * @return the receiver itself
     */
    public T trace(boolean trace) {
        this.trace = trace;
        return (T) this;
    }

    /**
     * Sets {@link AccessThreadConstraint} for reading.
     *
     * @param readOnMainThread A constraint for reading
     * @return the receiver itself
     */
    public T readOnMainThread(AccessThreadConstraint readOnMainThread) {
        this.readOnMainThread = readOnMainThread;
        return (T) this;
    }

    /**
     * Sets {@link AccessThreadConstraint} for writing.
     *
     * @param writeOnMainThread A constraint for writing
     * @return the receiver itself
     */
    public T writeOnMainThread(AccessThreadConstraint writeOnMainThread) {
        this.writeOnMainThread = writeOnMainThread;
        return (T) this;
    }

    @SuppressWarnings("deprecated")
    protected T fillDefaults() {

        if (migrationEngine == null) {
            migrationEngine = new SchemaDiffMigration(context, trace);
        }

        if (typeAdapterRegistry == null) {
            typeAdapterRegistry = new TypeAdapterRegistry();
            typeAdapterRegistry.addAll(TypeAdapterRegistry.defaultTypeAdapters());
        }

        return (T) this;
    }
}
