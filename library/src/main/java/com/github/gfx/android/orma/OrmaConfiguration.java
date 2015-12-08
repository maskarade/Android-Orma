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
public class OrmaConfiguration<T extends OrmaConfiguration<?>> {

    @NonNull
    final Context context;

    final boolean debug;

    @Nullable
    String name;

    TypeAdapterRegistry typeAdapterRegistry;

    MigrationEngine migrationEngine;

    boolean wal = true;

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

    public T name(@Nullable String name) {
        this.name = name;
        return (T) this;
    }

    public T typeAdapters(@NonNull TypeAdapter<?>... typeAdapters) {
        if (typeAdapterRegistry == null) {
            typeAdapterRegistry = new TypeAdapterRegistry();
            typeAdapterRegistry.addAll(TypeAdapterRegistry.defaultTypeAdapters());
        }
        typeAdapterRegistry.addAll(typeAdapters);
        return (T) this;
    }

    public T migrationEngine(@NonNull MigrationEngine migrationEngine) {
        this.migrationEngine = migrationEngine;
        return (T) this;
    }

    /**
     * @see <a href="http://sqlite.org/wal.html">Write-Ahead Logging in SQLite</a>
     */
    public T writeAheadLogging(boolean wal) {
        this.wal = wal;
        return (T) this;
    }

    /**
     * If true, SQL executions are logged to console.
     */
    public T trace(boolean trace) {
        this.trace = trace;
        return (T) this;
    }

    /**
     * Sets {@link AccessThreadConstraint} for reading.
     */
    public T readOnMainThread(AccessThreadConstraint readOnMainThread) {
        this.readOnMainThread = readOnMainThread;
        return (T) this;
    }

    /**
     * Sets {@link AccessThreadConstraint} for writing.
     */
    public T writeOnMainThread(AccessThreadConstraint writeOnMainThread) {
        this.writeOnMainThread = writeOnMainThread;
        return (T) this;
    }

    protected T fillDefaults() {

        if (migrationEngine == null) {
            migrationEngine = new SchemaDiffMigration(context, debug);
        }

        if (typeAdapterRegistry == null) {
            typeAdapterRegistry = new TypeAdapterRegistry();
            typeAdapterRegistry.addAll(TypeAdapterRegistry.defaultTypeAdapters());
        }

        return (T) this;
    }
}
