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
package com.github.gfx.android.orma.internal;

import com.github.gfx.android.orma.OrmaConnection;
import com.github.gfx.android.orma.Schema;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public abstract class OrmaConditionBase<T, C extends OrmaConditionBase<?, ?>> {

    protected final OrmaConnection conn;

    protected final Schema<T> schema;

    protected String whereConjunction = " AND ";

    @Nullable
    protected StringBuilder whereClause;

    @Nullable
    protected List<String> bindArgs;

    public OrmaConditionBase(OrmaConnection conn, Schema<T> schema) {
        this.conn = conn;
        this.schema = schema;
    }

    protected void bindArgs(@NonNull Object... args) {
        if (bindArgs == null) {
            bindArgs = new ArrayList<>(args.length);
        }

        for (Object arg : args) {
            if (arg == null) {
                bindArgs.add(null);
            } else if (arg instanceof Boolean) {
                bindArgs.add((Boolean) arg ? "1" : "0");
            } else {
                bindArgs.add(arg.toString());
            }
        }
    }

    /**
     * Builds general `where` clause with arguments.
     * e.g. {@code where("title = ? OR title = ?", a, b)}
     *
     * @param conditions SQLite's WHERE conditions.
     * @param args       Arguments bound to the {@code conditions}.
     * @return The receiver itself.
     */
    @SuppressWarnings("unchecked")
    public C where(@NonNull String conditions, @NonNull Object... args) {
        if (whereClause == null) {
            whereClause = new StringBuilder(conditions.length() + 2);
        } else {
            whereClause.append(whereConjunction);
        }

        whereClause.append('(');
        whereClause.append(conditions);
        whereClause.append(')');

        bindArgs(args);
        return (C) this;
    }

    @SuppressWarnings("unchecked")
    public C where(@NonNull String conditions, @NonNull Collection<?> args) {
        return where(conditions, args.toArray());
    }

    @SuppressWarnings("unchecked")
    protected <ColumnType> C in(boolean not, @NonNull String columnName, @NonNull Collection<ColumnType> values) {
        StringBuilder clause = new StringBuilder();

        clause.append(columnName);
        if (not) {
            clause.append(" NOT ");
        }
        clause.append(" IN (");
        for (int i = 0, size = values.size(); i < size; i++) {
            clause.append('?');

            if ((i + 1) != values.size()) {
                clause.append(", ");
            }
        }
        clause.append(')');

        return where(clause.toString(), values);
    }

    @SuppressWarnings("unchecked")
    public C and() {
        whereConjunction = " AND ";
        return (C) this;
    }

    @SuppressWarnings("unchecked")
    public C or() {
        whereConjunction = " OR ";
        return (C) this;
    }

    @Nullable
    protected String getWhereClause() {
        return whereClause != null ? whereClause.toString() : null;
    }

    @Nullable
    protected String[] getBindArgs() {
        if (bindArgs != null) {
            String[] array = new String[bindArgs.size()];
            return bindArgs.toArray(array);
        } else {
            return null;
        }
    }
}
