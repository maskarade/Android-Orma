package com.github.gfx.android.orma.processor;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Modifier;

/**
 * {@code Schema<T>} represents how a model is connected to an SQLite table.
 */
public class SchemaWriter {

    static final String TABLE_NAME = "$TABLE_NAME";

    static final String COLUMNS = "$COLUMNS";

    static final String ESCAPED_COLUMN_NAMES = "$ESCAPED_COLUMN_NAMES";

    static final Modifier[] publicStaticFinal = {
            Modifier.PUBLIC,
            Modifier.STATIC,
            Modifier.FINAL,
    };

    private final SchemaDefinition schema;

    private final ProcessingEnvironment processingEnv;

    private final SqlGenerator sql = new SqlGenerator();

    FieldSpec primaryKey;

    public SchemaWriter(SchemaDefinition schema, ProcessingEnvironment processingEnv) {
        this.schema = schema;
        this.processingEnv = processingEnv;
    }

    public TypeSpec buildTypeSpec() {
        TypeSpec.Builder classBuilder = TypeSpec.classBuilder(schema.getSchemaClassName().simpleName());
        classBuilder.addAnnotation(Specs.buildGeneratedAnnotationSpec());
        classBuilder.addModifiers(Modifier.PUBLIC);
        classBuilder.addSuperinterface(Types.getSchema(schema.getModelClassName()));

        classBuilder.addFields(buildFieldSpecs());
        classBuilder.addMethods(buildMethodSpecs());

        return classBuilder.build();
    }

    public List<FieldSpec> buildFieldSpecs() {
        List<FieldSpec> fieldSpecs = new ArrayList<>();

        List<FieldSpec> columns = new ArrayList<>();

        schema.getColumns().forEach(columnDef -> {
            FieldSpec fieldSpec = buildColumnFieldSpec(columnDef);
            columns.add(fieldSpec);

            if (columnDef.primaryKey) {
                primaryKey = fieldSpec;
            }
        });

        fieldSpecs.addAll(columns);

        fieldSpecs.add(
                FieldSpec.builder(Types.String, TABLE_NAME)
                        .addModifiers(publicStaticFinal)
                        .initializer("$S", schema.tableName)
                        .build()
        );

        fieldSpecs.add(
                FieldSpec.builder(Types.ColumnList, COLUMNS)
                        .addModifiers(publicStaticFinal)
                        .initializer(buildColumnsInitializer(columns))
                        .build()
        );

        fieldSpecs.add(
                FieldSpec.builder(Types.StringArray, ESCAPED_COLUMN_NAMES)
                        .addModifiers(publicStaticFinal)
                        .initializer(buildEscapedColumnNamesInitializer())
                        .build()
        );

        return fieldSpecs;
    }

    public FieldSpec buildColumnFieldSpec(ColumnDefinition c) {
        CodeBlock initializer = CodeBlock.builder()
                .add("new $T($S, $T.class, $L, $L, $L, $L, $L, $L)",
                        c.getColumnDefType(), c.columnName, c.getRawType(),
                        c.nullable, c.primaryKey, c.autoincrement, c.autoId, c.indexed, c.unique)
                .build();
        return FieldSpec.builder(c.getColumnDefType(), c.name)
                .addModifiers(publicStaticFinal)
                .initializer(initializer)
                .build();
    }

    public CodeBlock buildColumnsInitializer(List<FieldSpec> columns) {
        CodeBlock.Builder builder = CodeBlock.builder();

        builder.add("$T.<$T>asList(\n", Types.Arrays, Types.WildcardColumnDef).indent();

        for (int i = 0; i < columns.size(); i++) {
            builder.add("$N", columns.get(i));
            if ((i + 1) != columns.size()) {
                builder.add(",\n");
            } else {
                builder.add("\n");
            }
        }

        builder.unindent().add(")");

        return builder.build();
    }

    public CodeBlock buildEscapedColumnNamesInitializer() {
        CodeBlock.Builder builder = CodeBlock.builder();

        builder.add("{\n").indent();

        List<ColumnDefinition> columns = schema.getColumns();

        for (int i = 0; i < columns.size(); i++) {
            builder.add("$S", '"' + columns.get(i).columnName + '"');
            if ((i + 1) != columns.size()) {
                builder.add(",\n");
            } else {
                builder.add("\n");
            }
        }

        builder.unindent().add("}");

        return builder.build();
    }

    public List<MethodSpec> buildMethodSpecs() {
        List<MethodSpec> methodSpecs = new ArrayList<>();

        List<AnnotationSpec> overrideAndNonNull = Arrays.asList(
                Specs.buildNonNullAnnotationSpec(),
                Specs.buildOverrideAnnotationSpec()
        );

        List<AnnotationSpec> overrideAndNullable = Arrays.asList(
                Specs.buildNullableAnnotationSpec(),
                Specs.buildOverrideAnnotationSpec()
        );

        methodSpecs.add(
                MethodSpec.methodBuilder("getTableName")
                        .addAnnotations(overrideAndNonNull)
                        .addModifiers(Modifier.PUBLIC)
                        .returns(Types.String)
                        .addStatement("return $L", TABLE_NAME)
                        .build()
        );

        if (primaryKey != null) {
            methodSpecs.add(
                    MethodSpec.methodBuilder("getPrimaryKey")
                            .addAnnotations(overrideAndNonNull)
                            .addModifiers(Modifier.PUBLIC)
                            .returns(Types.WildcardColumnDef)
                            .addStatement("return $N", primaryKey)
                            .build()
            );
        } else {
            methodSpecs.add(
                    MethodSpec.methodBuilder("getPrimaryKey")
                            .addAnnotations(overrideAndNullable)
                            .addModifiers(Modifier.PUBLIC)
                            .returns(Types.WildcardColumnDef)
                            .addStatement("return null")
                            .build()
            );
        }

        methodSpecs.add(
                MethodSpec.methodBuilder("getColumns")
                        .addAnnotations(overrideAndNonNull)
                        .addModifiers(Modifier.PUBLIC)
                        .returns(Types.ColumnList)
                        .addStatement("return $L", COLUMNS)
                        .build()
        );

        methodSpecs.add(
                MethodSpec.methodBuilder("getEscapedColumnNames")
                        .addAnnotations(overrideAndNonNull)
                        .addModifiers(Modifier.PUBLIC)
                        .returns(Types.StringArray)
                        .addStatement("return $L", ESCAPED_COLUMN_NAMES)
                        .build()
        );

        methodSpecs.add(
                MethodSpec.methodBuilder("getCreateTableStatement")
                        .addAnnotations(overrideAndNonNull)
                        .addModifiers(Modifier.PUBLIC)
                        .returns(Types.String)
                        .addStatement("return $S", sql.buildCreateTableStatement(schema))
                        .build()
        );

        methodSpecs.add(
                MethodSpec.methodBuilder("getCreateIndexStatements")
                        .addAnnotations(overrideAndNonNull)
                        .addModifiers(Modifier.PUBLIC)
                        .returns(Types.getList(Types.String))
                        .addCode(sql.buildCreateIndexStatements(schema))
                        .build()
        );

        methodSpecs.add(
                MethodSpec.methodBuilder("getDropTableStatement")
                        .addAnnotations(overrideAndNonNull)
                        .addModifiers(Modifier.PUBLIC)
                        .returns(Types.String)
                        .addStatement("return $S", sql.buildDropTableStatement(schema))
                        .build()
        );

        methodSpecs.add(
                MethodSpec.methodBuilder("getInsertStatement")
                        .addAnnotations(overrideAndNonNull)
                        .addModifiers(Modifier.PUBLIC)
                        .returns(Types.String)
                        .addStatement("return $S", sql.buildInsertStatement(schema))
                        .build()
        );

        methodSpecs.add(
                MethodSpec.methodBuilder("serializeModelToContentValues")
                        .addAnnotations(overrideAndNonNull)
                        .addModifiers(Modifier.PUBLIC)
                        .returns(Types.ContentValues)
                        .addParameter(
                                ParameterSpec.builder(schema.getModelClassName(), "model")
                                        .addAnnotation(Specs.buildNonNullAnnotationSpec())
                                        .build())
                        .addCode(buildSerializeModelToContentValues())
                        .build()
        );

        methodSpecs.add(
                MethodSpec.methodBuilder("bindArgs")
                        .addModifiers(Modifier.PUBLIC)
                        .returns(TypeName.VOID)
                        .addParameter(
                                ParameterSpec.builder(Types.SQLiteStatement, "statement")
                                        .addAnnotation(Specs.buildNonNullAnnotationSpec())
                                        .build())
                        .addParameter(
                                ParameterSpec.builder(schema.getModelClassName(), "model")
                                        .addAnnotation(Specs.buildNonNullAnnotationSpec())
                                        .build())
                        .addCode(buildBindArgs())
                        .build()
        );

        methodSpecs.add(
                MethodSpec.methodBuilder("populateValuesIntoModel")
                        .addAnnotations(overrideAndNonNull)
                        .addModifiers(Modifier.PUBLIC)
                        .returns(TypeName.VOID)
                        .addParameter(
                                ParameterSpec.builder(Types.OrmaConnection, "conn")
                                        .addAnnotation(Specs.buildNonNullAnnotationSpec())
                                        .build())
                        .addParameter(
                                ParameterSpec.builder(Types.Cursor, "cursor")
                                        .addAnnotation(Specs.buildNonNullAnnotationSpec())
                                        .build())
                        .addParameter(
                                ParameterSpec.builder(schema.getModelClassName(), "model")
                                        .addAnnotation(Specs.buildNonNullAnnotationSpec())
                                        .build())
                        .addCode(buildPopulateValuesIntoCursor())
                        .build()
        );

        methodSpecs.add(
                MethodSpec.methodBuilder("createModelFromCursor")
                        .addAnnotations(overrideAndNonNull)
                        .addModifiers(Modifier.PUBLIC)
                        .returns(schema.getModelClassName())
                        .addParameter(
                                ParameterSpec.builder(Types.OrmaConnection, "conn")
                                        .addAnnotation(Specs.buildNonNullAnnotationSpec())
                                        .build())
                        .addParameter(
                                ParameterSpec.builder(Types.Cursor, "cursor")
                                        .addAnnotation(Specs.buildNonNullAnnotationSpec())
                                        .build())
                        .addCode(buildCreateModelFromCursor())
                        .build()
        );

        return methodSpecs;
    }

    private CodeBlock buildSerializeModelToContentValues() {
        CodeBlock.Builder builder = CodeBlock.builder();

        builder.addStatement("$T contents = new $T()", Types.ContentValues, Types.ContentValues);

        schema.getColumnsWithoutAutoId().forEach(c -> {
            RelationDefinition r = c.getRelation();
            if (r == null) {
                builder.addStatement("contents.put($S, model.$L)", c.columnName, c.name);
            } else if (r.relationType.equals(Types.SingleRelation)) {
                builder.addStatement("contents.put($S, model.$L.getId())", c.columnName, c.name);
            } else {
                throw new UnsupportedOperationException(r.relationType + " is not supported");
            }
        });

        builder.addStatement("return contents");

        return builder.build();
    }

    // http://developer.android.com/intl/ja/reference/android/database/sqlite/SQLiteStatement.html
    private CodeBlock buildBindArgs() {
        CodeBlock.Builder builder = CodeBlock.builder();

        List<ColumnDefinition> columns = schema.getColumnsWithoutAutoId();
        for (int i = 0; i < columns.size(); i++) {
            int n = i + 1; // bind index starts 1
            ColumnDefinition c = columns.get(i);
            TypeName type = c.getType();
            RelationDefinition r = c.getRelation();
            boolean nullable = !type.isPrimitive() && c.nullable;

            if (nullable) {
                builder.beginControlFlow("if (model.$L != null)", c.name);
            }

            // TODO: support type adapters
            if (Types.looksLikeIntegerType(type) || type.equals(TypeName.BOOLEAN)) {
                builder.addStatement("statement.bindLong($L, model.$L)", n, c.name);
            } else if (Types.looksLikeFloatType(type)) {
                builder.addStatement("statement.bindDouble($L, model.$L)", n, c.name);
            } else if (type.equals(Types.ByteArray)) {
                builder.addStatement("statement.bindBlob($L, model.$L)", n, c.name);
            } else if (type.equals(Types.String)) {
                builder.addStatement("statement.bindString($L, model.$L)", n, c.name);
            } else if (r != null && r.relationType.equals(Types.SingleRelation)) {
                builder.addStatement("statement.bindLong($L, model.$L.getId())", n, c.name);
            } else {
                builder.addStatement("statement.bindString($L, model.$L.toString())", n, c.name);
            }

            if (nullable) {
                builder.endControlFlow();
                builder.beginControlFlow("else");
                builder.addStatement("statement.bindNull($L)", n);
                builder.endControlFlow();
            }
        }

        return builder.build();
    }

    private CodeBlock buildPopulateValuesIntoCursor() {
        CodeBlock.Builder builder = CodeBlock.builder();

        List<ColumnDefinition> columns = schema.getColumns();
        for (int i = 0; i < columns.size(); i++) {
            ColumnDefinition c = columns.get(i);
            RelationDefinition r = c.getRelation();
            if (r == null) {
                builder.addStatement("model.$L = cursor.$L($L)",
                        c.name, cursorGetter(c), i);
            } else { // SingleRelation
                builder.addStatement("model.$L = new $T<>(conn, OrmaDatabase.schema$T, cursor.getLong($L))",
                        c.name, r.relationType, r.modelType, i);
            }
        }
        return builder.build();
    }

    private CodeBlock buildCreateModelFromCursor() {
        CodeBlock.Builder builder = CodeBlock.builder();
        builder.addStatement("$T model = new $T()", schema.getModelClassName(), schema.getModelClassName()); // FIXME
        builder.addStatement("populateValuesIntoModel(conn, cursor, model)");
        builder.addStatement("return model");
        return builder.build();
    }

    private String cursorGetter(ColumnDefinition column) {
        TypeName type = column.getType();
        if (type.isPrimitive()) {
            String s = type.toString();
            return "get" + s.substring(0, 1).toUpperCase() + s.substring(1);
        } else if (type.equals(Types.String)) {
            return "getString";
        } else  if (type.equals(Types.ByteArray)){
            return "getBlob";
        } else {
            throw new UnsupportedOperationException("TODO: " + type + " is not yet supported as a column type");
        }
    }
}
