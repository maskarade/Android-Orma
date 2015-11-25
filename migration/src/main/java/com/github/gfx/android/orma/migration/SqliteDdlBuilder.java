package com.github.gfx.android.orma.migration;

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
    public String buildCreateTable(@NonNull String table, @NonNull List<String> columns) {
        StringBuilder sb = new StringBuilder();
        sb.append("CREATE TABLE ");
        sb.append(ensureQuoted(table));
        sb.append(" (");
        appendWithSeparator(sb, ", ", columns);
        sb.append(")");
        return sb.toString();
    }

    @NonNull
    public String buildInsertFromSelect(@NonNull String toTable, @NonNull String fromTable,
            @NonNull Collection<String> columns) {

        StringBuilder sb = new StringBuilder();
        sb.append("INSERT INTO ");
        sb.append(ensureQuoted(toTable));
        sb.append(" (");
        String quotedColumnNames = joinBy(", ", columns, new SqliteDdlBuilder.Func1<String, String>() {
            @Override
            public String call(String name) {
                return SqliteDdlBuilder.ensureQuoted(name);
            }
        });
        sb.append(quotedColumnNames);
        sb.append(") SELECT ");
        sb.append(quotedColumnNames);
        sb.append(" FROM ");
        sb.append(ensureQuoted(fromTable));

        return sb.toString();
    }

    @NonNull
    public String buildDropTable(@NonNull String table) {
        return "DROP TABLE " + ensureQuoted(table);
    }

    @NonNull
    public String buildRenameTable(@NonNull String fromTable, @NonNull String toTable) {
        return "ALTER TABLE " + ensureQuoted(fromTable) + " RENAME TO " + ensureQuoted(toTable);
    }

    @NonNull
    public <T> List<String> map(Collection<T> collection, @NonNull Func1<T, String> func) {
        List<String> result = new ArrayList<>(collection.size());
        for (T item : collection) {
            result.add(func.call(item));
        }
        return result;
    }

    public void appendWithSeparator(StringBuilder builder, String separator, Collection<?> collection) {
        int i = 0;
        int size = collection.size();
        for (Object item : collection) {
            builder.append(item);
            if ((i + 1) != size) {
                builder.append(separator);
            }
            i++;
        }
    }

    @NonNull
    public <T> String joinBy(@NonNull String separator, @NonNull Collection<T> collection,
            @NonNull Func1<T, String> func) {
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

    public interface Func1<A, R> {

        R call(A arg);
    }
}
