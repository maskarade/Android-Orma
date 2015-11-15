package com.github.gfx.android.orma.internal;

import com.github.gfx.android.orma.SingleRelation;

import android.support.annotation.NonNull;

import java.util.HashMap;
import java.util.Map;

/**
 * https://www.sqlite.org/datatype3.html
 */
public class OrmaDataTypes {
    public static final Map<Class<?>, String> javaToSqlite = new HashMap<>();

    static {
        javaToSqlite.put(Integer.class, "INTEGER");
        javaToSqlite.put(int.class, "INTEGER");
        javaToSqlite.put(Long.class, "INTEGER");
        javaToSqlite.put(long.class, "INTEGER");
        javaToSqlite.put(Short.class, "INTEGER");
        javaToSqlite.put(short.class, "INTEGER");
        javaToSqlite.put(Byte.class, "INTEGER");
        javaToSqlite.put(byte.class, "INTEGER");

        javaToSqlite.put(Float.class, "REAL");
        javaToSqlite.put(float.class, "REAL");
        javaToSqlite.put(Double.class, "REAL");
        javaToSqlite.put(double.class, "REAL");

        javaToSqlite.put(String.class, "TEXT");
        javaToSqlite.put(byte[].class, "BLOB");

        javaToSqlite.put(Boolean.class, "BOOLEAN");
        javaToSqlite.put(boolean.class, "BOOLEAN");

        javaToSqlite.put(SingleRelation.class, "INTEGER");

        // TODO: DateTime
    }

    @NonNull
    public static String getSqliteType(Class<?> type) {
        String t = javaToSqlite.get(type);
        return t != null ? t : "BLOB";
    }
}
