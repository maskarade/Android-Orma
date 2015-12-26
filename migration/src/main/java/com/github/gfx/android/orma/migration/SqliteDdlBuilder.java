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
package com.github.gfx.android.orma.migration;

import com.github.gfx.android.orma.sqliteparser.SQLiteComponent;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class SqliteDdlBuilder {

    @NonNull
    public static String ensureQuoted(@NonNull String name) {
        if (name.startsWith("\"") || name.startsWith("`")) {
            return name;
        }
        return '"' + name + '"';
    }

    @NonNull
    public static String ensureNotQuoted(@NonNull String maybeQuoted) {
        if (maybeQuoted.startsWith("\"") || maybeQuoted.endsWith("\n")) {
            return maybeQuoted.substring(1, maybeQuoted.length() - 1);
        } else if (maybeQuoted.startsWith("`") || maybeQuoted.endsWith("`")) {
            return maybeQuoted.substring(1, maybeQuoted.length() - 1);
        } else {
            return maybeQuoted;
        }
    }

    @NonNull
    public String buildCreateTable(@NonNull SQLiteComponent.Name table, @NonNull List<String> columns) {
        StringBuilder sb = new StringBuilder();
        sb.append("CREATE TABLE ");
        sb.append(table);
        sb.append(" (");
        appendWithSeparator(sb, ", ", columns);
        sb.append(")");
        return sb.toString();
    }

    @NonNull
    public String buildInsertFromSelect(@NonNull SQLiteComponent.Name toTable, @NonNull SQLiteComponent.Name fromTable,
            @NonNull Collection<SQLiteComponent.Name> columns) {

        StringBuilder sb = new StringBuilder();
        sb.append("INSERT INTO ");
        sb.append(toTable);
        sb.append(" (");
        appendWithSeparator(sb, ", ", columns);
        sb.append(") SELECT ");
        appendWithSeparator(sb, ", ", columns);
        sb.append(" FROM ");
        sb.append(fromTable);

        return sb.toString();
    }

    @NonNull
    public String buildDropTable(@NonNull SQLiteComponent.Name table) {
        return "DROP TABLE " + table;
    }

    @NonNull
    public String buildRenameTable(@NonNull String fromTable, @NonNull String toTable) {
        return buildRenameTable(new SQLiteComponent.Name(fromTable), new SQLiteComponent.Name(toTable));
    }

    @NonNull
    public String buildRenameTable(@NonNull SQLiteComponent.Name fromTable, @NonNull SQLiteComponent.Name toTable) {
        return "ALTER TABLE " + fromTable + " RENAME TO " + toTable;
    }

    @NonNull
    public <T> List<String> map(Collection<T> collection, @NonNull Func<T, String> func) {
        List<String> result = new ArrayList<>(collection.size());
        for (T item : collection) {
            result.add(func.call(item));
        }
        return result;
    }

    public void appendWithSeparator(StringBuilder builder, String separator, Collection<? extends CharSequence> collection) {
        int i = 0;
        int size = collection.size();
        for (CharSequence item : collection) {
            builder.append(item);
            if ((i + 1) != size) {
                builder.append(separator);
            }
            i++;
        }
    }

    @NonNull
    public <T> String joinBy(@NonNull String separator, @NonNull Collection<T> collection,
            @NonNull Func<T, String> func) {
        StringBuilder sb = new StringBuilder();
        int i = 0;
        int size = collection.size();
        for (T item : collection) {
            sb.append(func.call(item));
            if ((i + 1) != size) {
                sb.append(separator);
            }
            i++;
        }
        return sb.toString();
    }

    public interface Func<A, R> {

        R call(A arg);
    }
}
