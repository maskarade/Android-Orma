package com.github.gfx.android.orma.processor;

import com.squareup.javapoet.ArrayTypeName;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.WildcardTypeName;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Types {

    public static String ormaPackageName = "com.github.gfx.android.orma";

    // Android standard types
    public static ClassName String = ClassName.get(String.class);
    public static ArrayTypeName StringArray = ArrayTypeName.of(String);
    public static ClassName List = ClassName.get(List.class);
    public static ClassName ArrayList = ClassName.get(ArrayList.class);
    public static ClassName Arrays = ClassName.get(Arrays.class);
    public static ClassName Context = ClassName.get("android.content", "Context");
    public static ClassName ContentValues = ClassName.get("android.content", "ContentValues");
    public static ClassName Cursor = ClassName.get("android.database", "Cursor");
    public static ClassName SQLiteDatabase = ClassName.get("android.database.sqlite", "SQLiteDatabase");
    public static ClassName NonNull = ClassName.get("android.support.annotation", "NonNull");
    public static ClassName Nullable = ClassName.get("android.support.annotation", "Nullable");

    // Orma types
    public static ClassName Schema = ClassName.get(ormaPackageName, "Schema");
    public static TypeName WildcardSchema = getSchema(WildcardTypeName.subtypeOf(TypeName.OBJECT));
    public static ClassName ColumnDef = ClassName.get(ormaPackageName, "ColumnDef");
    public static TypeName WildcardColumnDef = getColumnDef(WildcardTypeName.subtypeOf(TypeName.OBJECT));
    public static TypeName ColumnList = ParameterizedTypeName.get(List, WildcardColumnDef);
    public static ClassName Relation = ClassName.get(ormaPackageName, "Relation");
    public static ClassName OrmaConnection = ClassName.get(ormaPackageName, "OrmaConnection");
    public static ClassName TransactionTask = ClassName.get(ormaPackageName, "TransactionTask");

    public static ParameterizedTypeName getSchema(TypeName modelType) {
        return ParameterizedTypeName.get(Schema, modelType);
    }

    public static ParameterizedTypeName getColumnDef(TypeName typeName) {
        return ParameterizedTypeName.get(ColumnDef, typeName);
    }

    public static ParameterizedTypeName getRelation(TypeName modelType, TypeName concreteRelationType) {
        return ParameterizedTypeName.get(Relation, modelType, concreteRelationType);
    }

    public static ParameterizedTypeName getList(TypeName typeName) {
        return ParameterizedTypeName.get(List, typeName);
    }
}
