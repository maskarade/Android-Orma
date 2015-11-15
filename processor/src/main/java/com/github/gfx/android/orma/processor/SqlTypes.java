package com.github.gfx.android.orma.processor;

import com.squareup.javapoet.ArrayTypeName;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;

import java.util.HashMap;
import java.util.Map;

public class SqlTypes {

    public static final Map<TypeName, String> javaToSqlite = new HashMap<>();

    static {
        javaToSqlite.put(TypeName.INT, "INTEGER");
        javaToSqlite.put(TypeName.INT.box(), "INTEGER");
        javaToSqlite.put(TypeName.LONG, "INTEGER");
        javaToSqlite.put(TypeName.LONG.box(), "INTEGER");
        javaToSqlite.put(TypeName.SHORT, "INTEGER");
        javaToSqlite.put(TypeName.SHORT.box(), "INTEGER");
        javaToSqlite.put(TypeName.BYTE, "INTEGER");
        javaToSqlite.put(TypeName.BYTE.box(), "INTEGER");

        javaToSqlite.put(TypeName.FLOAT, "REAL");
        javaToSqlite.put(TypeName.FLOAT.box(), "REAL");
        javaToSqlite.put(TypeName.DOUBLE, "REAL");
        javaToSqlite.put(TypeName.DOUBLE.box(), "REAL");

        javaToSqlite.put(ClassName.get(String.class), "TEXT");
        javaToSqlite.put(TypeName.CHAR, "TEXT");
        javaToSqlite.put(TypeName.CHAR.box(), "TEXT");
        javaToSqlite.put(ArrayTypeName.of(TypeName.BYTE), "BLOB");

        javaToSqlite.put(TypeName.BOOLEAN, "BOOLEAN");
        javaToSqlite.put(TypeName.BOOLEAN.box(), "BOOLEAN");

        javaToSqlite.put(Types.SingleRelation, "INTEGER"); // foreign key

        // TODO: date and time types?
    }

    public static String getSqliteType(TypeName type) {
        String t = javaToSqlite.get(type);
        return t != null ? t : "BLOB";
    }
}

