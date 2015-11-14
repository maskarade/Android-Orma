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

    public static final String ormaPackageName = "com.github.gfx.android.orma";

    // Android standard types
    public static final ClassName String = ClassName.get(String.class);

    public static final ArrayTypeName StringArray = ArrayTypeName.of(String);

    public static final ClassName List = ClassName.get(List.class);

    public static final ClassName ArrayList = ClassName.get(ArrayList.class);

    public static final ClassName Arrays = ClassName.get(Arrays.class);

    public static final ClassName Context = ClassName.get("android.content", "Context");

    public static final ClassName ContentValues = ClassName.get("android.content", "ContentValues");

    public static final ClassName Cursor = ClassName.get("android.database", "Cursor");

    public static final ClassName SQLiteDatabase = ClassName.get("android.database.sqlite", "SQLiteDatabase");

    public static final ClassName SQLiteStatement = ClassName.get("android.database.sqlite", "SQLiteStatement");

    public static final ClassName NonNull = ClassName.get("android.support.annotation", "NonNull");

    public static final ClassName Nullable = ClassName.get("android.support.annotation", "Nullable");

    public static final ClassName Single = ClassName.get("rx", "Single");

    public static final ClassName Observable = ClassName.get("rx", "Observable");

    // Orma types
    public static final ClassName Schema = ClassName.get(ormaPackageName, "Schema");

    public static final TypeName WildcardSchema = getSchema(WildcardTypeName.subtypeOf(TypeName.OBJECT));

    public static final ClassName ColumnDef = ClassName.get(ormaPackageName, "ColumnDef");

    public static final TypeName WildcardColumnDef = getColumnDef(WildcardTypeName.subtypeOf(TypeName.OBJECT));

    public static final TypeName ColumnList = ParameterizedTypeName.get(List, WildcardColumnDef);

    public static final ClassName Relation = ClassName.get(ormaPackageName, "Relation");

    public static final ClassName OrmaConnection = ClassName.get(ormaPackageName, "OrmaConnection");

    public static final ClassName TransactionTask = ClassName.get(ormaPackageName, "TransactionTask");

    public static final ClassName Inserter = ClassName.get(ormaPackageName, "Inserter");

    public static final ClassName HasOne = ClassName.get(ormaPackageName, "HasOne");

    public static final ClassName HasMany = ClassName.get(ormaPackageName, "HasMany");

    public static final ClassName ModelBuilder = ClassName.get(ormaPackageName, "ModelBuilder");

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

    public static ParameterizedTypeName getInserter(TypeName typeName) {
        return ParameterizedTypeName.get(Inserter, typeName);
    }

    public static ParameterizedTypeName getSingle(TypeName typeName) {
        return ParameterizedTypeName.get(Single, typeName);
    }

    public static ParameterizedTypeName getObservable(TypeName typeName) {
        return ParameterizedTypeName.get(Observable, typeName);
    }

    public static ParameterizedTypeName getModelBuilder(TypeName typeName) {
        return ParameterizedTypeName.get(ModelBuilder, typeName);
    }

    public static boolean looksLikeIntegerType(TypeName type) {
        return type.equals(TypeName.INT)
                || type.equals(TypeName.LONG)
                || type.equals(TypeName.SHORT)
                || type.equals(TypeName.BYTE);
    }

    public static boolean looksLikeFloatType(TypeName type) {
        return type.equals(TypeName.FLOAT)
                || type.equals(TypeName.DOUBLE);
    }


}
