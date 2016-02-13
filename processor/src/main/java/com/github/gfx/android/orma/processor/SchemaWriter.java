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
package com.github.gfx.android.orma.processor;

import com.github.gfx.android.orma.annotation.OnConflict;
import com.github.gfx.android.orma.annotation.Setter;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ArrayTypeName;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;

/**
 * {@code Schema<T>} represents how a model is connected to an SQLite table.
 */
public class SchemaWriter extends BaseWriter {

    static final String TABLE_NAME = "$TABLE_NAME";

    static final String COLUMNS = "$COLUMNS";

    static final String ESCAPED_COLUMN_NAMES = "$ESCAPED_COLUMN_NAMES";

    static final String onConflictAlgorithm = "onConflictAlgorithm";

    static final String withoutAutoId = "withoutAutoId";

    static final String offset = "offset";

    static final Modifier[] publicStaticFinal = {
            Modifier.PUBLIC,
            Modifier.STATIC,
            Modifier.FINAL,
    };

    private final SchemaDefinition schema;

    FieldSpec primaryKey;

    public SchemaWriter(ProcessingContext context, SchemaDefinition schema) {
        super(context);
        this.schema = schema;
    }

    @Override
    public TypeSpec buildTypeSpec() {
        TypeSpec.Builder classBuilder = TypeSpec.classBuilder(schema.getSchemaClassName().simpleName());
        classBuilder.addModifiers(Modifier.PUBLIC);
        classBuilder.addSuperinterface(Types.getSchema(schema.getModelClassName()));

        classBuilder.addFields(buildFieldSpecs());
        classBuilder.addMethods(buildMethodSpecs());

        return classBuilder.build();
    }

    public List<FieldSpec> buildFieldSpecs() {
        List<FieldSpec> fieldSpecs = new ArrayList<>();

        fieldSpecs.add(FieldSpec.builder(schema.getSchemaClassName(), "INSTANCE", publicStaticFinal)
                .initializer("$T.register(new $T())", Types.Schemas, schema.getSchemaClassName())
                .build());

        List<FieldSpec> columns = new ArrayList<>();

        schema.getColumns().forEach(columnDef -> {
            FieldSpec fieldSpec = buildColumnFieldSpec(columnDef);
            columns.add(fieldSpec);

            if (columnDef.primaryKey) {
                primaryKey = fieldSpec;
            }
        });

        if (primaryKey == null) {
            // Even if primary key is omitted, "_rowid_" is always available.
            // (WITHOUT ROWID is not supported by Orma)
            primaryKey = buildPrimaryKeyColumn();
            fieldSpecs.add(primaryKey);
        }

        fieldSpecs.addAll(columns);

        fieldSpecs.add(
                FieldSpec.builder(Types.String, TABLE_NAME)
                        .addModifiers(publicStaticFinal)
                        .initializer("$S", schema.tableName)
                        .build()
        );

        fieldSpecs.add(
                FieldSpec.builder(Types.getColumnDefList(schema.getModelClassName()), COLUMNS)
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
        TypeName type = c.getType();

        CodeBlock typeInstance;
        if (type instanceof ParameterizedTypeName) {
            typeInstance = CodeBlock.builder()
                    .add("new $T<$T>(){}.getType()", Types.TypeHolder, type
                    ).build();
        } else {
            typeInstance = CodeBlock.builder()
                    .add("$T.class", type)
                    .build();
        }

        TypeSpec.Builder columnDefType = TypeSpec.anonymousClassBuilder("INSTANCE, $S, $L, $S, $L",
                c.columnName, typeInstance, c.getStorageType(), buildColumnFlags(c));
        columnDefType.superclass(c.getColumnDefType());
        MethodSpec.Builder getBuilder = MethodSpec.methodBuilder("get")
                .addAnnotation(Specs.overrideAnnotationSpec())
                .addAnnotation(c.nullable ? Specs.nullableAnnotation() : Specs.nonNullAnnotationSpec())
                .addModifiers(Modifier.PUBLIC)
                .returns(c.getBoxType())
                .addParameter(ParameterSpec.builder(schema.getModelClassName(), "model")
                        .addAnnotation(Specs.nonNullAnnotationSpec())
                        .build());
        if (c.element != null) {
            getBuilder.addStatement("return $L", c.buildGetColumnExpr("model"));
        } else {
            getBuilder.addStatement("throw new $T($S)", Types.NoValueException, "Missing @PrimaryKey definition");
        }
        columnDefType.addMethod(getBuilder.build());

        return FieldSpec.builder(c.getColumnDefType(), c.name)
                .addModifiers(publicStaticFinal)
                .initializer("$L", columnDefType.build())
                .build();
    }

    public CodeBlock buildColumnFlags(ColumnDefinition c) {
        CodeBlock.Builder builder = CodeBlock.builder();
        boolean some = false;

        if (c.primaryKey) {
            builder.add("$T.PRIMARY_KEY", Types.ColumnDef);
            some = true;
        }

        if (c.autoId) {
            if (some) {
                builder.add(" | ");
            }
            builder.add("$T.AUTO_VALUE", Types.ColumnDef);
            some = true;
        }

        if (c.autoincrement) {
            if (some) {
                builder.add(" | ");
            }
            builder.add("$T.AUTOINCREMENT", Types.ColumnDef);
            some = true;
        }

        if (c.nullable) {
            if (some) {
                builder.add(" | ");
            }
            builder.add("$T.NULLABLE", Types.ColumnDef);
            some = true;
        }
        if (c.indexed) {
            if (some) {
                builder.add(" | ");
            }
            builder.add("$T.INDEXED", Types.ColumnDef);
            some = true;
        }

        if (c.unique) {
            if (some) {
                builder.add(" | ");
            }
            builder.add("$T.UNIQUE", Types.ColumnDef);
            some = true;
        }

        if (!some) {
            builder.add("0");
        }
        return builder.build();
    }

    public FieldSpec buildPrimaryKeyColumn() {
        return buildColumnFieldSpec(ColumnDefinition.createDefaultPrimaryKey(schema));
    }


    public CodeBlock buildColumnsInitializer(List<FieldSpec> columns) {
        CodeBlock.Builder builder = CodeBlock.builder();

        builder.add("$T.<$T>asList(\n", Types.Arrays, Types.getColumnDef(schema.getModelClassName(), Types.WildcardType))
                .indent();

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
                Specs.nonNullAnnotationSpec(),
                Specs.overrideAnnotationSpec()
        );

        methodSpecs.add(
                MethodSpec.methodBuilder("getModelClass")
                        .addAnnotations(overrideAndNonNull)
                        .addModifiers(Modifier.PUBLIC)
                        .returns(ParameterizedTypeName.get(ClassName.get(Class.class), schema.getModelClassName()))
                        .addStatement("return $T.class", schema.getModelClassName())
                        .build()
        );

        methodSpecs.add(
                MethodSpec.methodBuilder("getTableName")
                        .addAnnotations(overrideAndNonNull)
                        .addModifiers(Modifier.PUBLIC)
                        .returns(Types.String)
                        .addStatement("return $L", TABLE_NAME)
                        .build()
        );

        methodSpecs.add(
                MethodSpec.methodBuilder("getEscapedTableName")
                        .addAnnotations(overrideAndNonNull)
                        .addModifiers(Modifier.PUBLIC)
                        .returns(Types.String)
                        .addStatement("return '\"' + $L + '\"'", TABLE_NAME)
                        .build()
        );

        methodSpecs.add(
                MethodSpec.methodBuilder("getPrimaryKey")
                        .addAnnotations(overrideAndNonNull)
                        .addModifiers(Modifier.PUBLIC)
                        .returns(Types.getColumnDef(schema.getModelClassName(), Types.WildcardType))
                        .addStatement("return $N", primaryKey)
                        .build()
        );

        methodSpecs.add(
                MethodSpec.methodBuilder("getColumns")
                        .addAnnotations(overrideAndNonNull)
                        .addModifiers(Modifier.PUBLIC)
                        .returns(Types.getColumnDefList(schema.getModelClassName()))
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
                        .addParameter(ParameterSpec.builder(int.class, onConflictAlgorithm)
                                .addAnnotation(OnConflict.class)
                                .build())
                        .addParameter(ParameterSpec.builder(boolean.class, withoutAutoId)
                                .build())
                        .returns(Types.String)
                        .addCode(sql.buildInsertStatementCode(schema, onConflictAlgorithm, withoutAutoId))
                        .build()
        );

        methodSpecs.add(
                MethodSpec.methodBuilder("convertToArgs")
                        .addJavadoc("Provided for debugging\n")
                        .addAnnotations(overrideAndNonNull)
                        .addModifiers(Modifier.PUBLIC)
                        .returns(ArrayTypeName.of(TypeName.OBJECT))
                        .addParameter(
                                ParameterSpec.builder(Types.OrmaConnection, "conn")
                                        .addAnnotation(Specs.nonNullAnnotationSpec())
                                        .build())
                        .addParameter(
                                ParameterSpec.builder(schema.getModelClassName(), "model")
                                        .addAnnotation(Specs.nonNullAnnotationSpec())
                                        .build())
                        .addParameter(boolean.class, withoutAutoId)
                        .addCode(buildConvertToArgs())
                        .build()
        );

        methodSpecs.add(
                MethodSpec.methodBuilder("bindArgs")
                        .addAnnotation(Override.class)
                        .addModifiers(Modifier.PUBLIC)
                        .returns(TypeName.VOID)
                        .addParameter(
                                ParameterSpec.builder(Types.OrmaConnection, "conn")
                                        .addAnnotation(Specs.nonNullAnnotationSpec())
                                        .build())
                        .addParameter(
                                ParameterSpec.builder(Types.SQLiteStatement, "statement")
                                        .addAnnotation(Specs.nonNullAnnotationSpec())
                                        .build())
                        .addParameter(
                                ParameterSpec.builder(schema.getModelClassName(), "model")
                                        .addAnnotation(Specs.nonNullAnnotationSpec())
                                        .build())
                        .addParameter(boolean.class, withoutAutoId)
                        .addParameter(int.class, offset)
                        .addCode(buildBindArgs())
                        .build()
        );

        methodSpecs.add(
                MethodSpec.methodBuilder("newModelFromCursor")
                        .addAnnotations(overrideAndNonNull)
                        .addModifiers(Modifier.PUBLIC)
                        .returns(schema.getModelClassName())
                        .addParameter(
                                ParameterSpec.builder(Types.OrmaConnection, "conn")
                                        .addAnnotation(Specs.nonNullAnnotationSpec())
                                        .build())
                        .addParameter(
                                ParameterSpec.builder(Types.Cursor, "cursor")
                                        .addAnnotation(Specs.nonNullAnnotationSpec())
                                        .build())
                        .addCode(buildNewModelFromCursor())
                        .build()
        );

        return methodSpecs;
    }

    private CodeBlock buildConvertToArgs() {
        CodeBlock.Builder builder = CodeBlock.builder();

        List<ColumnDefinition> columns = schema.getColumns();
        List<ColumnDefinition> columnsWithoutAutoId = schema.getColumnsWithoutAutoId();

        if (columns.size() == columnsWithoutAutoId.size()) {
            builder.addStatement("$T args = new $T[$L]", ArrayTypeName.of(TypeName.OBJECT), TypeName.OBJECT, columns.size());
        } else {
            builder.addStatement("$T args = new $T[$L ? $L : $L]", ArrayTypeName.of(TypeName.OBJECT), TypeName.OBJECT,
                    withoutAutoId, columnsWithoutAutoId.size(), columns.size());
        }

        for (int i = 0; i < columns.size(); i++) {
            ColumnDefinition c = columns.get(i);
            AssociationDefinition r = c.getAssociation();

            if (c.isNullableInJava()) {
                builder.beginControlFlow("if ($L != null)", c.buildGetColumnExpr("model"));
            }
            if (c.autoId) {
                builder.beginControlFlow("if (!$L)", withoutAutoId);
            }

            CodeBlock rhsExpr = c.buildSerializedColumnExpr("conn", "model");

            if (r != null && r.associationType.equals(Types.SingleAssociation)) {
                builder.addStatement("args[$L] = $L.getId()", i, c.buildGetColumnExpr("model"));
            } else if (c.getSerializedType().equals(TypeName.BOOLEAN)) {
                builder.addStatement("args[$L] = $L ? 1 : 0", i, rhsExpr);
            } else {
                builder.addStatement("args[$L] = $L", i, rhsExpr);
            }

            if (c.autoId) {
                builder.endControlFlow();
            }
            if (c.isNullableInJava()) {
                builder.endControlFlow();
                builder.beginControlFlow("else");
                builder.endControlFlow();
            }
        }

        builder.addStatement("return args");
        return builder.build();
    }

    // http://developer.android.com/intl/ja/reference/android/database/sqlite/SQLiteStatement.html
    private CodeBlock buildBindArgs() {
        CodeBlock.Builder builder = CodeBlock.builder();

        List<ColumnDefinition> columns = schema.getColumns();
        for (int i = 0; i < columns.size(); i++) {
            int n = i + 1; // bind index starts 1
            ColumnDefinition c = columns.get(i);
            TypeName serializedType = c.getSerializedType();
            AssociationDefinition r = c.getAssociation();

            if (c.isNullableInJava()) {
                builder.beginControlFlow("if ($L != null)", c.buildGetColumnExpr("model"));
            }
            if (c.autoId) {
                builder.beginControlFlow("if (!$L)", withoutAutoId);
            }

            CodeBlock rhsExpr = c.buildSerializedColumnExpr("conn", "model");

            if (serializedType.equals(TypeName.BOOLEAN)) {
                builder.addStatement("statement.bindLong($L + $L, $L ? 1 : 0)", offset, n, rhsExpr);
            } else if (Types.looksLikeIntegerType(serializedType)) {
                builder.addStatement("statement.bindLong($L + $L, $L)", offset, n, rhsExpr);
            } else if (Types.looksLikeFloatType(serializedType)) {
                builder.addStatement("statement.bindDouble($L + $L, $L)", offset, n, rhsExpr);
            } else if (serializedType.equals(Types.ByteArray)) {
                builder.addStatement("statement.bindBlob($L + $L, $L)", offset, n, rhsExpr);
            } else if (serializedType.equals(Types.String)) {
                builder.addStatement("statement.bindString($L + $L, $L)", offset, n, rhsExpr);
            } else if (r != null && r.associationType.equals(Types.SingleAssociation)) {
                builder.addStatement("statement.bindLong($L + $L, $L.getId())", offset, n, c.buildGetColumnExpr("model"));
            } else {
                builder.addStatement("statement.bindString($L + $L, $L)", offset, n, rhsExpr);

                // TODO: throw the following errors in v2.0
                // throw new ProcessingException("No storage method found for " + serializedType, c.element);
            }

            if (c.autoId) {
                builder.endControlFlow();
            }
            if (c.isNullableInJava()) {
                builder.endControlFlow();
                builder.beginControlFlow("else");
                builder.addStatement("statement.bindNull($L + $L)", n, offset);
                builder.endControlFlow();
            }
        }

        return builder.build();
    }

    private CodeBlock buildPopulateValuesIntoCursor(Function<ColumnDefinition, CodeBlock> lhsBaseGen) {
        CodeBlock.Builder builder = CodeBlock.builder();

        List<ColumnDefinition> columns = schema.getColumns();
        for (int i = 0; i < columns.size(); i++) {
            ColumnDefinition c = columns.get(i);
            TypeName type = c.getUnboxType();

            if (Types.isDirectAssociation(context, type)) {
                ClassName className = (ClassName) type;
                String singleAssocType = "SingleAssociation<" + className.simpleName() + ">";
                context.addError("Direct association is not yet supported. Use " + singleAssocType + " instead.", c.element);
            } else if (Types.isSingleAssociation(type)) {
                AssociationDefinition r = c.getAssociation();
                CodeBlock.Builder getRhsExpr = CodeBlock.builder()
                        .add("new $T<>(conn, $L, cursor.getLong($L))",
                                r.associationType, context.getSchemaInstanceExpr(r.modelType), i);
                builder.addStatement("$L$L", lhsBaseGen.apply(c), c.buildSetColumnExpr(getRhsExpr.build()));
            } else {
                CodeBlock.Builder rhsExprBuilder = CodeBlock.builder();
                if (c.isNullableInSQL()) {
                    rhsExprBuilder.add("cursor.isNull($L) ? null : $L", i, c.buildDeserializeExpr("conn", cursorGetter(c, i)));
                } else {
                    rhsExprBuilder.add(c.buildDeserializeExpr("conn", cursorGetter(c, i)));
                }
                builder.addStatement("$L$L", lhsBaseGen.apply(c), c.buildSetColumnExpr(rhsExprBuilder.build()));
            }
        }
        return builder.build();
    }

    private CodeBlock buildNewModelFromCursor() {
        CodeBlock.Builder builder = CodeBlock.builder();
        if (schema.hasDefaultConstructor()) {
            builder.addStatement("$T model = new $T()", schema.getModelClassName(), schema.getModelClassName());
            builder.add(buildPopulateValuesIntoCursor(column -> CodeBlock.builder().add("model.").build()));
            builder.addStatement("return model");
        } else {

            if (schema.getColumns().size() != schema.constructorElement.getParameters().size()) {
                context.addError("The @Setter constructor parameters must satisfy all the @Column fields",
                        schema.constructorElement);
            }

            builder.add(buildPopulateValuesIntoCursor(
                    column -> CodeBlock.builder().add("$T ", column.getType()).build()));

            builder.addStatement("return new $T($L)", schema.getModelClassName(),
                    schema.constructorElement.getParameters()
                            .stream()
                            .map(this::extractColumnNameFromParameterElement)
                            .collect(Collectors.joining(", ")));
        }
        return builder.build();
    }

    private String extractColumnNameFromParameterElement(VariableElement parameterElement) {
        Setter setter = parameterElement.getAnnotation(Setter.class);
        if (setter != null && !Strings.isEmpty(setter.value())) {
            return schema.findColumnByColumnName(setter.value())
                    .map(column -> column.name)
                    .orElseGet(() -> parameterElement.getSimpleName().toString());
        }
        return parameterElement.getSimpleName().toString();
    }

    private String cursorGetter(ColumnDefinition column, int position) {
        TypeName type = column.getSerializedType();
        if (type.equals(TypeName.BOOLEAN)) {
            return "cursor.getLong(" + position + ") != 0";
        } else if (type.equals(TypeName.BYTE)) {
            return "(byte)cursor.getShort(" + position + ")";
        } else if (type.isPrimitive()) {
            String s = type.toString();
            return "cursor.get" + s.substring(0, 1).toUpperCase() + s.substring(1) + "(" + position + ")";
        } else if (type.equals(Types.String)) {
            return "cursor.getString(" + position + ")";
        } else if (type.equals(Types.ByteArray)) {
            return "cursor.getBlob(" + position + ")";
        } else {
            return "cursor.getString(" + position + ")"; // handled by type adapters
        }
    }
}
