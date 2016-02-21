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
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;

/**
 * {@code Schema<T>} represents how a model is connected to an SQLite table.
 */
public class SchemaWriter extends BaseWriter {

    static final String COLUMNS = "$COLUMNS";

    static final String DEFAULT_RESULT_COLUMNS = "$DEFAULT_RESULT_COLUMNS";

    static final String onConflictAlgorithm = "onConflictAlgorithm";

    static final String withoutAutoId = "withoutAutoId";

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
                FieldSpec.builder(Types.getColumnDefList(schema.getModelClassName()), COLUMNS)
                        .addModifiers(publicStaticFinal)
                        .initializer(buildColumnsInitializer(columns))
                        .build()
        );

        fieldSpecs.add(
                FieldSpec.builder(Types.StringArray, DEFAULT_RESULT_COLUMNS)
                        .addModifiers(publicStaticFinal)
                        .initializer("{\n$L}", buildEscapedColumnNamesInitializer(schema, schema.hasDirectAssociations()))
                        .build()
        );

        return fieldSpecs;
    }

    private String buildSelectFromTableClause() {
        StringBuilder sb = new StringBuilder();
        sql.appendIdentifier(sb, schema.getTableName());

        sb.append(schema.getColumns().stream()
                .filter(ColumnDefinition::isDirectAssociation)
                .map(column -> {
                    StringBuilder s = new StringBuilder();
                    SchemaDefinition associatedSchema = context.getSchemaDef(column.getType());
                    ColumnDefinition primaryKey = associatedSchema.getPrimaryKey();
                    if (primaryKey != null) {
                        s.append(" JOIN ");
                        sql.appendIdentifier(s, associatedSchema.getTableName());
                        s.append(" ON ");
                        s.append(column.getEscapedColumnName(true));
                        s.append(" = ");
                        s.append(primaryKey.getEscapedColumnName(true));
                    }
                    return s;
                })
                .collect(Collectors.joining(", ")));

        return sb.toString();
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
                .addAnnotation(Annotations.override())
                .addAnnotation(c.nullable ? Annotations.nullable() : Annotations.nonNull())
                .addModifiers(Modifier.PUBLIC)
                .returns(c.getBoxType())
                .addParameter(ParameterSpec.builder(schema.getModelClassName(), "model")
                        .addAnnotation(Annotations.nonNull())
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

    public CodeBlock buildEscapedColumnNamesInitializer(SchemaDefinition schema, boolean fqn) {
        CodeBlock.Builder builder = CodeBlock.builder();

        builder.indent();

        List<ColumnDefinition> columns = schema.getColumns();

        for (int i = 0, size = columns.size(); i < size; i++) {
            ColumnDefinition column = columns.get(i);
            builder.add("$S", column.getEscapedColumnName(fqn));
            if (column.isDirectAssociation()) {
                builder.add(",\n")
                        .add(buildEscapedColumnNamesInitializer(column.getAssociatedSchema(), fqn));
            }
            if ((i + 1) != size) {
                builder.add(",\n");
            } else {
                builder.add("\n");
            }
        }

        builder.unindent();

        return builder.build();
    }

    public List<MethodSpec> buildMethodSpecs() {
        List<MethodSpec> methodSpecs = new ArrayList<>();

        methodSpecs.add(
                MethodSpec.methodBuilder("getModelClass")
                        .addAnnotations(Annotations.overrideAndNonNull())
                        .addModifiers(Modifier.PUBLIC)
                        .returns(ParameterizedTypeName.get(ClassName.get(Class.class), schema.getModelClassName()))
                        .addStatement("return $T.class", schema.getModelClassName())
                        .build()
        );

        methodSpecs.add(
                MethodSpec.methodBuilder("getTableName")
                        .addAnnotations(Annotations.overrideAndNonNull())
                        .addModifiers(Modifier.PUBLIC)
                        .returns(Types.String)
                        .addStatement("return $S", schema.getTableName())
                        .build()
        );

        methodSpecs.add(
                MethodSpec.methodBuilder("getEscapedTableName")
                        .addAnnotations(Annotations.overrideAndNonNull())
                        .addModifiers(Modifier.PUBLIC)
                        .returns(Types.String)
                        .addStatement("return $S", sql.quoteIdentifier(schema.getTableName()))
                        .build()
        );

        methodSpecs.add(
                MethodSpec.methodBuilder("getSelectFromTableClause")
                        .addAnnotations(Annotations.overrideAndNonNull())
                        .addModifiers(Modifier.PUBLIC)
                        .returns(Types.String)
                        .addStatement("return $S", buildSelectFromTableClause())
                        .build()
        );

        methodSpecs.add(
                MethodSpec.methodBuilder("getPrimaryKey")
                        .addAnnotations(Annotations.overrideAndNonNull())
                        .addModifiers(Modifier.PUBLIC)
                        .returns(Types.getColumnDef(schema.getModelClassName(), Types.WildcardType))
                        .addStatement("return $N", primaryKey)
                        .build()
        );

        methodSpecs.add(
                MethodSpec.methodBuilder("getColumns")
                        .addAnnotations(Annotations.overrideAndNonNull())
                        .addModifiers(Modifier.PUBLIC)
                        .returns(Types.getColumnDefList(schema.getModelClassName()))
                        .addStatement("return $L", COLUMNS)
                        .build()
        );

        methodSpecs.add(
                MethodSpec.methodBuilder("getDefaultResultColumns")
                        .addAnnotations(Annotations.overrideAndNonNull())
                        .addModifiers(Modifier.PUBLIC)
                        .returns(Types.StringArray)
                        .addStatement("return $L", DEFAULT_RESULT_COLUMNS)
                        .build()
        );

        methodSpecs.add(
                MethodSpec.methodBuilder("getCreateTableStatement")
                        .addAnnotations(Annotations.overrideAndNonNull())
                        .addModifiers(Modifier.PUBLIC)
                        .returns(Types.String)
                        .addStatement("return $S", schema.getCreateTableStatement())
                        .build()
        );

        methodSpecs.add(
                MethodSpec.methodBuilder("getCreateIndexStatements")
                        .addAnnotations(Annotations.overrideAndNonNull())
                        .addModifiers(Modifier.PUBLIC)
                        .returns(Types.getList(Types.String))
                        .addCode(sql.buildCreateIndexStatementsExpr(schema))
                        .build()
        );

        methodSpecs.add(
                MethodSpec.methodBuilder("getDropTableStatement")
                        .addAnnotations(Annotations.overrideAndNonNull())
                        .addModifiers(Modifier.PUBLIC)
                        .returns(Types.String)
                        .addStatement("return $S", sql.buildDropTableStatement(schema))
                        .build()
        );

        methodSpecs.add(
                MethodSpec.methodBuilder("getInsertStatement")
                        .addAnnotations(Annotations.overrideAndNonNull())
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
                        .addAnnotations(Annotations.overrideAndNonNull())
                        .addModifiers(Modifier.PUBLIC)
                        .returns(ArrayTypeName.of(TypeName.OBJECT))
                        .addParameter(
                                ParameterSpec.builder(Types.OrmaConnection, "conn")
                                        .addAnnotation(Annotations.nonNull())
                                        .build())
                        .addParameter(
                                ParameterSpec.builder(schema.getModelClassName(), "model")
                                        .addAnnotation(Annotations.nonNull())
                                        .build())
                        .addParameter(boolean.class, withoutAutoId)
                        .addCode(buildConvertToArgs())
                        .build()
        );

        methodSpecs.add(
                MethodSpec.methodBuilder("bindArgs")
                        .addAnnotation(Annotations.override())
                        .addModifiers(Modifier.PUBLIC)
                        .returns(TypeName.VOID)
                        .addParameter(
                                ParameterSpec.builder(Types.OrmaConnection, "conn")
                                        .addAnnotation(Annotations.nonNull())
                                        .build())
                        .addParameter(
                                ParameterSpec.builder(Types.SQLiteStatement, "statement")
                                        .addAnnotation(Annotations.nonNull())
                                        .build())
                        .addParameter(
                                ParameterSpec.builder(schema.getModelClassName(), "model")
                                        .addAnnotation(Annotations.nonNull())
                                        .build())
                        .addParameter(boolean.class, withoutAutoId)
                        .addCode(buildBindArgs())
                        .build()
        );

        methodSpecs.add(
                MethodSpec.methodBuilder("newModelFromCursor")
                        .addAnnotations(Annotations.overrideAndNonNull())
                        .addModifiers(Modifier.PUBLIC)
                        .returns(schema.getModelClassName())
                        .addParameter(
                                ParameterSpec.builder(Types.OrmaConnection, "conn")
                                        .addAnnotation(Annotations.nonNull())
                                        .build())
                        .addParameter(
                                ParameterSpec.builder(Types.Cursor, "cursor")
                                        .addAnnotation(Annotations.nonNull())
                                        .build())
                        .addParameter(int.class, "offset")
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

            if (c.isNullableInJava()) {
                builder.beginControlFlow("if ($L != null)", c.buildGetColumnExpr("model"));
            }
            if (c.autoId) {
                builder.beginControlFlow("if (!$L)", withoutAutoId);
            }

            if (c.isSingleAssociation()) {
                builder.addStatement("args[$L] = $L.getId()", i, c.buildGetColumnExpr("model"));
            } else if (c.isDirectAssociation()) { // direct association
                SchemaDefinition associatedSchema = c.getAssociatedSchema();
                ColumnDefinition primaryKey = associatedSchema.getPrimaryKey();
                // make errors in ColumnDefinition.java on primaryKey == null
                if (primaryKey != null) {
                    builder.addStatement("args[$L] = $L",
                            i, primaryKey.buildGetColumnExpr(c.buildGetColumnExpr("model")));
                }
            } else {
                CodeBlock rhsExpr = c.buildSerializedColumnExpr("conn", "model");
                if (c.getSerializedType().equals(TypeName.BOOLEAN)) {
                    builder.addStatement("args[$L] = $L ? 1 : 0", i, rhsExpr);
                } else {
                    builder.addStatement("args[$L] = $L", i, rhsExpr);
                }
            }

            if (c.autoId) {
                builder.endControlFlow();
            }
            if (c.isNullableInJava()) {
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

            if (c.isNullableInJava()) {
                builder.beginControlFlow("if ($L != null)", c.buildGetColumnExpr("model"));
            }
            if (c.autoId) {
                builder.beginControlFlow("if (!$L)", withoutAutoId);
            }

            if (c.isSingleAssociation()) {
                builder.addStatement("statement.bindLong($L, $L.getId())", n, c.buildGetColumnExpr("model"));
            } else {
                CodeBlock rhsExpr;
                TypeName serializedType;

                if (c.isDirectAssociation()) {
                    SchemaDefinition associatedSchema = c.getAssociatedSchema();
                    ColumnDefinition primaryKey = associatedSchema.getPrimaryKey();
                    // make errors in ColumnDefinition.java on primaryKey == null
                    if (primaryKey != null) {
                        rhsExpr = primaryKey.buildGetColumnExpr(c.buildGetColumnExpr("model"));
                        serializedType = associatedSchema.getPrimaryKey().getSerializedType();
                    } else {
                        rhsExpr = CodeBlock.builder().add("null").build(); // dummy
                        serializedType = Types.ByteArray; // dummy
                    }
                } else {
                    rhsExpr = c.buildSerializedColumnExpr("conn", "model");
                    serializedType = c.getSerializedType();
                }

                if (serializedType.equals(TypeName.BOOLEAN)) {
                    builder.addStatement("statement.bindLong($L, $L ? 1 : 0)", n, rhsExpr);
                } else if (Types.looksLikeIntegerType(serializedType)) {
                    builder.addStatement("statement.bindLong($L, $L)", n, rhsExpr);
                } else if (Types.looksLikeFloatType(serializedType)) {
                    builder.addStatement("statement.bindDouble($L, $L)", n, rhsExpr);
                } else if (serializedType.equals(Types.ByteArray)) {
                    builder.addStatement("statement.bindBlob($L, $L)", n, rhsExpr);
                } else if (serializedType.equals(Types.String)) {
                    builder.addStatement("statement.bindString($L, $L)", n, rhsExpr);
                } else {
                    throw new ProcessingException("No storage method found for " + serializedType, c.element);
                }
            }

            if (c.autoId) {
                builder.endControlFlow();
            }
            if (c.isNullableInJava()) {
                builder.endControlFlow();
                builder.beginControlFlow("else");
                builder.addStatement("statement.bindNull($L)", n);
                builder.endControlFlow();
            }
        }

        return builder.build();
    }

    private CodeBlock buildPopulateValuesIntoCursor(Function<ColumnDefinition, CodeBlock> lhsBaseGen) {
        CodeBlock.Builder builder = CodeBlock.builder();

        List<ColumnDefinition> columns = schema.getColumns();
        int offset = 0; // direct associations increase the offset
        for (int i = 0; i < columns.size(); i++) {
            ColumnDefinition c = columns.get(i);
            TypeName type = c.getUnboxType();

            CodeBlock index = CodeBlock.builder().add("offset + $L", i + offset).build();

            if (Types.isDirectAssociation(context, type)) {
                AssociationDefinition r = c.getAssociation();
                assert r != null;

                SchemaDefinition schema = context.getSchemaDef(r.modelType);
                int numberOfColumns = schema.getColumns().size();
                CodeBlock createAssociatedModelExpr = CodeBlock.builder()
                        .add("$L.newModelFromCursor(conn, cursor, $L + 1) /* consumes items: $L */",
                                schema.createSchemaInstanceExpr(), index, numberOfColumns)
                        .build();

                // Given a "Book has-a Publisher" association. The following expression should be created:
                // book.publisher = Publisher_Schema.INSTANCE.newModelFromCursor(conn, cursor, offset)
                // NOTE: lhsBaseGen.apply(c) makes, e.g. "model.", ignoring the parameter "c".
                builder.addStatement("$L$L", lhsBaseGen.apply(c), c.buildSetColumnExpr(createAssociatedModelExpr));
                offset += numberOfColumns;
            } else if (Types.isSingleAssociation(type)) {
                AssociationDefinition r = c.getAssociation();
                assert r != null;
                CodeBlock.Builder getRhsExpr = CodeBlock.builder()
                        .add("new $T<>(conn, $L, cursor.getLong($L))",
                                r.associationType, c.getAssociatedSchema().createSchemaInstanceExpr(), index);
                builder.addStatement("$L$L", lhsBaseGen.apply(c), c.buildSetColumnExpr(getRhsExpr.build()));
            } else {
                CodeBlock.Builder rhsExprBuilder = CodeBlock.builder();
                if (c.isNullableInSQL()) {
                    rhsExprBuilder.add("cursor.isNull($L) ? null : $L", index,
                            c.buildDeserializeExpr("conn", cursorGetter(c, index)));
                } else {
                    rhsExprBuilder.add(c.buildDeserializeExpr("conn", cursorGetter(c, index)));
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

    private CodeBlock cursorGetter(ColumnDefinition column, CodeBlock index) {
        TypeName type = column.getSerializedType();
        if (type.equals(TypeName.BOOLEAN)) {
            return CodeBlock.builder().add("cursor.getLong($L) != 0", index).build();
        } else if (type.equals(TypeName.BYTE)) {
            return CodeBlock.builder().add("(byte)cursor.getLong($L)", index).build();
        } else if (type.isPrimitive()) {
            String s = type.toString();
            return CodeBlock.builder().add("cursor.get$L($L)", Strings.toUpperFirst(s), index).build();
        } else if (type.equals(Types.String)) {
            return CodeBlock.builder().add("cursor.getString($L)", index).build();
        } else if (type.equals(Types.ByteArray)) {
            return CodeBlock.builder().add("cursor.getBlob($L)", index).build();
        } else {
            // FIXME: not reached?
            return CodeBlock.builder().add("cursor.getString($L)", index).build();
        }
    }
}
