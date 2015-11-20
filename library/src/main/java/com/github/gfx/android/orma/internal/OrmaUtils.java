package com.github.gfx.android.orma.internal;

import android.support.annotation.NonNull;

import java.util.Collection;

public class OrmaUtils {

    public interface Func1<A, R> {

        R call(A arg);
    }

    @NonNull
    public static <T> String joinBy(@NonNull String separator, @NonNull Collection<T> collection,
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

    /**
     * Quote a name by SQL 92 way (i.e. double-quotes)
     * @param name
     * @return A quoted name
     */
    @NonNull
    public static String quote(@NonNull String name) {
        if (name.startsWith("\"") || name.startsWith("`")) {
            return name;
        }
        return '"' + name + '"';
    }

    @NonNull
    public static String dequote(@NonNull String maybeQuoted) {
        if (maybeQuoted.startsWith("\"") || maybeQuoted.endsWith("\n")) {
            return maybeQuoted.substring(1, maybeQuoted.length() - 1);
        } else if (maybeQuoted.startsWith("`") || maybeQuoted.endsWith("`")) {
            return maybeQuoted.substring(1, maybeQuoted.length() - 1);
        } else {
            return maybeQuoted;
        }
    }
}
