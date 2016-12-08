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
package com.github.gfx.android.orma.processor.generator;

import com.github.gfx.android.orma.annotation.OnConflict;
import com.github.gfx.android.orma.annotation.Setter;
import com.github.gfx.android.orma.processor.ProcessingContext;
import com.github.gfx.android.orma.processor.exception.ProcessingException;
import com.github.gfx.android.orma.processor.model.AssociationDefinition;
import com.github.gfx.android.orma.processor.model.ColumnDefinition;
import com.github.gfx.android.orma.processor.model.SchemaDefinition;
import com.github.gfx.android.orma.processor.util.Annotations;
import com.github.gfx.android.orma.processor.util.Strings;
import com.github.gfx.android.orma.processor.util.Types;
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
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;

/**
 * {@code Schema<T>} represents how a model is connected to an SQLite table.
 */
public class SchemaWriter extends BaseWriter {

    private static final String $defaultResultColumns = "$defaultResultColumns";

    private static final String $alias = "$alias";

    private static final String onConflictAlgorithm = "onConflictAlgorithm";

    private static final String withoutAutoId = "withoutAutoId";

    private static final Modifier[] publicStaticFinal = {
            Modifier.PUBLIC,
            Modifier.STATIC,
            Modifier.FINAL,
    };

    private static final Modifier[] publicFinal = {
            Modifier.PUBLIC,
            Modifier.FINAL,
    };

    private final SchemaDefinition schema;

    private FieldSpecDefinition primaryKeyFieldSpecDef;

    private List<FieldSpecDefinition> columns = new ArrayList<>();

    public SchemaWriter(ProcessingContext context, SchemaDefinition schema) {
        super(context);
        this.schema = schema;
    }

    @Override
    public String getPackageName() {
        return schema.getPackageName();
    }

    @Override
    public Optional<? extends Element> getElement() {
        return Optional.of(schema.getElement());
    }

    @Override
    public TypeSpec buildTypeSpec() {
        TypeSpec.Builder classBuilder = TypeSpec.classBuilder(schema.getSchemaClassName());
        if (schema.isGeneric()) {
            classBuilder.addAnnotation(Annotations.suppressWarnings("rawtypes"));
        }
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

        fieldSpecs.add(FieldSpec.builder(Types.String, $alias, Modifier.PRIVATE, Modifier.FINAL)
                .addAnnotation(Annotations.nullable())
                .build());

        schema.getColumns().forEach(columnDef -> {
            FieldSpecDefinition fieldSpecDef = buildColumnFieldSpec(columnDef);
            columns.add(fieldSpecDef);

            if (columnDef.primaryKey) {
                primaryKeyFieldSpecDef = fieldSpecDef;
            }
        });

        if (primaryKeyFieldSpecDef == null) {
            // Even if primary key is omitted, "_rowid_" is always available.
            // (WITHOUT ROWID is not supported by Orma)
            primaryKeyFieldSpecDef = buildPrimaryKeyColumn();
            fieldSpecs.add(primaryKeyFieldSpecDef.fieldSpec);
        }

        for (FieldSpecDefinition column : columns) {
            fieldSpecs.add(column.fieldSpec);
        }

        fieldSpecs.add(
                FieldSpec.builder(Types.StringArray, $defaultResultColumns)
                        .addModifiers(Modifier.PRIVATE, Modifier.FINAL)
                        .build()
        );

        return fieldSpecs;
    }

    private CodeBlock buildSelectFromTableClause() {
        CodeBlock.Builder code = CodeBlock.builder();

        CodeBlock prefix = CodeBlock.builder().build();
        CodeBlock[] joins = schema.getColumns().stream()
                .filter(ColumnDefinition::isDirectAssociation)
                .map(column -> buildJoins(prefix, column))
                .toArray(CodeBlock[]::new);

        code.add("$S", schema.getEscapedTableName());

        if (joins.length != 0) {
            code.add("+ $S + getEscapedTableAlias()\n", " AS ");
            for (CodeBlock join : joins) {
                code.add("$L\n", join);
            }
        }

        return code.build();
    }

    public CodeBlock buildJoins(CodeBlock prefix, ColumnDefinition column) {
        CodeBlock.Builder joins = CodeBlock.builder();

        SchemaDefinition associatedSchema = context.getSchemaDef(column.getType());
        associatedSchema.getPrimaryKey().ifPresent(primaryKey -> {
            joins.add("+ $S + $L$L.associationSchema.getEscapedTableAlias() + ",
                    " LEFT OUTER JOIN " + associatedSchema.getEscapedTableName() + " AS ",
                    prefix,
                    column.name
            );
            joins.add("$S + $L$L.getQualifiedName() + $S + $L$L.associationSchema.$L.getQualifiedName()\n",
                    " ON ",
                    prefix, column.name,
                    " = ",
                    prefix, column.name, primaryKey.name
            );

            // handles nested JOINs
            CodeBlock newPrefix = prefix.toBuilder().add("$L.associationSchema.", column.name).build();
            associatedSchema.getColumns()
                    .stream()
                    .filter(ColumnDefinition::isDirectAssociation)
                    .map(nestedColumn -> buildJoins(newPrefix, nestedColumn))
                    .forEach(joins::add);
        });
        return joins.build();
    }

    public FieldSpecDefinition buildColumnFieldSpec(ColumnDefinition c) {
        TypeName type = c.getType();

        CodeBlock typeInstance;
        if (type instanceof ParameterizedTypeName) {
            typeInstance = CodeBlock.of("new $T<$T>(){}.getType()", Types.TypeHolder, type);
        } else {
            typeInstance = CodeBlock.of("$T.class", type);
        }

        TypeSpec.Builder columnDefType;

        if (!c.isDirectAssociation()) {
            columnDefType = TypeSpec.anonymousClassBuilder("this, $S, $L, $S, $L",
                    c.columnName, typeInstance, c.getStorageType(), buildColumnFlags(c));
        } else {
            CodeBlock pathExpr = CodeBlock.builder()
                    .add("current != null ? current.add($S, $S) : null", c.columnName, c.getAssociatedSchema().getTableName())
                    .build();
            columnDefType = TypeSpec.anonymousClassBuilder("this, $S, $L, $S, $L, new $T($L)",
                    c.columnName, typeInstance, c.getStorageType(), buildColumnFlags(c),
                    c.getAssociatedSchema().getSchemaClassName(),
                    pathExpr);
        }
        columnDefType.superclass(c.getColumnDefType());

        // ColumnDef#get()
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

        // Define ColumnDef#getSerialized() if it uses type adapters
        MethodSpec.Builder getSerializedBuilder = MethodSpec.methodBuilder("getSerialized")
                .addAnnotations(c.nullable ? Annotations.overrideAndNullable() : Annotations.overrideAndNonNull())
                .addModifiers(Modifier.PUBLIC)
                .returns(c.getSerializedBoxType())
                .addParameter(ParameterSpec.builder(schema.getModelClassName(), "model")
                        .addAnnotation(Annotations.nonNull())
                        .build());
        if (c.element != null) {
            getSerializedBuilder.addStatement("return $L", c.buildSerializedColumnExpr("conn", "model"));
        } else {
            getSerializedBuilder.addStatement("throw new $T($S)", Types.NoValueException, "Missing @PrimaryKey definition");
        }
        columnDefType.addMethod(getSerializedBuilder.build());

        // ColumnDef#getFromCursor(Cursor, int)
        columnDefType.addMethod(MethodSpec.methodBuilder("getFromCursor")
                .addAnnotations(c.nullable ? Annotations.overrideAndNullable() : Annotations.overrideAndNonNull())
                .addModifiers(Modifier.PUBLIC)
                .returns(c.getBoxType())
                .addParameter(ParameterSpec.builder(Types.OrmaConnection, "conn")
                        .addAnnotation(Annotations.nonNull())
                        .build())
                .addParameter(ParameterSpec.builder(Types.Cursor, "cursor")
                        .addAnnotation(Annotations.nonNull())
                        .build())
                .addParameter(ParameterSpec.builder(int.class, "index").build())
                .addStatement("return $L", buildGetValueFromCursor(c, CodeBlock.of("index")))
                .build());

        return new FieldSpecDefinition(
                FieldSpec.builder(c.getColumnDefType(), c.name).addModifiers(publicFinal).build(),
                columnDefType.build());
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

    public FieldSpecDefinition buildPrimaryKeyColumn() {
        return buildColumnFieldSpec(ColumnDefinition.createDefaultPrimaryKey(schema));
    }

    public CodeBlock buildColumnsInitializer() {
        CodeBlock.Builder builder = CodeBlock.builder();

        builder.add("$T.<$T>asList(\n", Types.Arrays, Types.getColumnDef(schema.getModelClassName(), Types.WildcardType))
                .indent();

        for (int i = 0; i < columns.size(); i++) {
            builder.add("$N", columns.get(i).fieldSpec);
            if ((i + 1) != columns.size()) {
                builder.add(",\n");
            } else {
                builder.add("\n");
            }
        }

        builder.unindent().add(")");

        return builder.build();
    }

    public CodeBlock buildEscapedColumnNamesInitializer(SchemaDefinition schema, List<ColumnDefinition> upstream) {
        CodeBlock.Builder builder = CodeBlock.builder();

        builder.indent();

        List<ColumnDefinition> columns = schema.getColumns();

        for (int i = 0, size = columns.size(); i < size; i++) {
            ColumnDefinition column = columns.get(i);
            for (ColumnDefinition upstreamColumnDef : upstream) {
                builder.add("$L.associationSchema.", upstreamColumnDef.name);
            }
            builder.add("$L.getQualifiedName()", column.name);
            if (column.isDirectAssociation()) {
                builder.add(",\n");
                builder.add(buildEscapedColumnNamesInitializer(column.getAssociatedSchema(), addList(upstream, column)));
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

    private List<ColumnDefinition> addList(List<ColumnDefinition> list, ColumnDefinition value) {
        ColumnDefinition[] a = list.toArray(new ColumnDefinition[list.size() + 1]);
        a[list.size()] = value; // add
        return Arrays.asList(a);
    }

    public List<MethodSpec> buildMethodSpecs() {
        List<MethodSpec> methodSpecs = new ArrayList<>();

        if (schema.hasDirectAssociations()) {
            methodSpecs.add(
                    MethodSpec.constructorBuilder()
                            .addModifiers(Modifier.PUBLIC)
                            .addStatement("this(new $T().createPath($S))", Types.Aliases, schema.getTableName())
                            .build()
            );
        } else {
            methodSpecs.add(
                    MethodSpec.constructorBuilder()
                            .addModifiers(Modifier.PUBLIC)
                            .addStatement("this(null)")
                            .build()
            );

        }

        methodSpecs.add(
                MethodSpec.constructorBuilder()
                        .addModifiers(Modifier.PUBLIC)
                        .addParameter(ParameterSpec.builder(Types.ColumnPath, "current")
                                .addAnnotation(Annotations.nullable())
                                .build())
                        .addStatement("$L = current != null ? current.getAlias() : null", $alias)
                        .addCode(buildFieldInitializations())
                        .addStatement("$L = new String[]{\n$L}",
                                $defaultResultColumns, buildEscapedColumnNamesInitializer(schema, Collections.emptyList()))
                        .build()
        );

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
                        .addStatement("return $S", schema.getEscapedTableName())
                        .build()
        );

        methodSpecs.add(
                MethodSpec.methodBuilder("getTableAlias")
                        .addAnnotations(Annotations.overrideAndNullable())
                        .addModifiers(Modifier.PUBLIC)
                        .returns(Types.String)
                        .addStatement("return $L", $alias)
                        .build()
        );

        methodSpecs.add(
                MethodSpec.methodBuilder("getEscapedTableAlias")
                        .addAnnotations(Annotations.overrideAndNullable())
                        .addModifiers(Modifier.PUBLIC)
                        .returns(Types.String)
                        .addStatement("return $L != null ? '`' + $L + '`' : null", $alias, $alias)
                        .build()
        );

        methodSpecs.add(
                MethodSpec.methodBuilder("getSelectFromTableClause")
                        .addAnnotations(Annotations.overrideAndNonNull())
                        .addModifiers(Modifier.PUBLIC)
                        .returns(Types.String)
                        .addStatement("return $L", buildSelectFromTableClause())
                        .build()
        );

        methodSpecs.add(
                MethodSpec.methodBuilder("getPrimaryKey")
                        .addAnnotations(Annotations.overrideAndNonNull())
                        .addModifiers(Modifier.PUBLIC)
                        .returns(Types.getColumnDef(schema.getModelClassName(), Types.WildcardType))
                        .addStatement("return $N", primaryKeyFieldSpecDef.fieldSpec)
                        .build()
        );

        methodSpecs.add(
                MethodSpec.methodBuilder("getColumns")
                        .addAnnotations(Annotations.overrideAndNonNull())
                        .addModifiers(Modifier.PUBLIC)
                        .returns(Types.getColumnDefList(schema.getModelClassName()))
                        .addStatement("return $L", buildColumnsInitializer())
                        .build()
        );

        methodSpecs.add(
                MethodSpec.methodBuilder("getDefaultResultColumns")
                        .addAnnotations(Annotations.overrideAndNonNull())
                        .addModifiers(Modifier.PUBLIC)
                        .returns(Types.StringArray)
                        .addStatement("return $L", $defaultResultColumns)
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
                        .addCode(context.sqlg.buildCreateIndexStatementsExpr(schema))
                        .build()
        );

        methodSpecs.add(
                MethodSpec.methodBuilder("getDropTableStatement")
                        .addAnnotations(Annotations.overrideAndNonNull())
                        .addModifiers(Modifier.PUBLIC)
                        .returns(Types.String)
                        .addStatement("return $S", context.sqlg.buildDropTableStatement(schema))
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
                        .addCode(context.sqlg.buildInsertStatementCode(schema, onConflictAlgorithm, withoutAutoId))
                        .build()
        );

        methodSpecs.add(
                MethodSpec.methodBuilder("convertToArgs")
                        .addJavadoc("Convert models to {@code Object[]}. Provided for debugging\n")
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

    private CodeBlock buildFieldInitializations() {
        CodeBlock.Builder code = CodeBlock.builder();
        code.addStatement("this.$N = $L", primaryKeyFieldSpecDef.fieldSpec, primaryKeyFieldSpecDef.initializer);
        for (FieldSpecDefinition column : columns) {
            if (column == primaryKeyFieldSpecDef) {
                continue;
            }
            code.addStatement("this.$N = $L", column.fieldSpec, column.initializer);
        }
        return code.build();
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

            if (!c.getType().isPrimitive()) {
                builder.beginControlFlow("if ($L != null)", c.buildGetColumnExpr("model"));
            }

            if (c.autoId) {
                builder.beginControlFlow("if (!$L)", withoutAutoId);
            }

            CodeBlock rhsExpr = c.buildSerializedColumnExpr("conn", "model");
            if (c.getSerializedType().equals(TypeName.BOOLEAN)) {
                builder.addStatement("args[$L] = $L ? 1 : 0", i, rhsExpr);
            } else {
                builder.addStatement("args[$L] = $L", i, rhsExpr);
            }

            if (c.autoId) {
                builder.endControlFlow();
            }

            if (!c.getType().isPrimitive()) {
                builder.endControlFlow();

                if (!c.isNullableInJava()) {
                    // check nullability even if it is not declared as @Nullable
                    builder.beginControlFlow("else");
                    builder.addStatement("throw new $T($S + $S)", Types.IllegalArgumentException,
                            schema.getModelClassName().simpleName() + '.' + c.name,
                            " must not be null, or use @Nullable to declare it as NULL");
                    builder.endControlFlow();
                }
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
                CodeBlock rhsExpr = c.buildSerializedColumnExpr("conn", "model");
                TypeName serializedType = c.getSerializedType();

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

    private CodeBlock buildPopulateValuesFromCursor(Function<ColumnDefinition, CodeBlock> lhsBaseGen) {
        CodeBlock.Builder builder = CodeBlock.builder();

        List<ColumnDefinition> columns = schema.getColumns();
        int offset = 0; // direct associations increase the offset
        for (int i = 0; i < columns.size(); i++) {
            ColumnDefinition c = columns.get(i);
            TypeName type = c.getUnboxType();

            CodeBlock index = CodeBlock.of("offset + $L", i + offset);
            builder.addStatement("$L$L", lhsBaseGen.apply(c), c.buildSetColumnExpr(buildGetValueFromCursor(c, index)));

            if (Types.isDirectAssociation(context, type)) {
                SchemaDefinition associatedSchema = c.getAssociatedSchema();
                int consumingItemSize = associatedSchema.calculateConsumingColumnSize();
                offset += consumingItemSize;
            }
        }
        return builder.build();
    }

    CodeBlock buildGetValueFromCursor(ColumnDefinition c, CodeBlock index) {
        TypeName type = c.getUnboxType();

        if (Types.isDirectAssociation(context, type)) {
            SchemaDefinition associatedSchema = c.getAssociatedSchema();
            int consumingItemSize = associatedSchema.calculateConsumingColumnSize();
            CodeBlock.Builder createAssociatedModelExpr = CodeBlock.builder();

            if (c.isNullableInJava()) {
                // check the primary key is null or not
                createAssociatedModelExpr.add("cursor.isNull($L + $L) ? null : ", index, consumingItemSize);
            }
            createAssociatedModelExpr.add("$L.newModelFromCursor(conn, cursor, $L + 1) /* consumes items: $L */",
                    associatedSchema.createSchemaInstanceExpr(), index, consumingItemSize);
            // Given a "Book has-a Publisher" association. The following expression should be created:
            // book.publisher = Publisher_Schema.INSTANCE.newModelFromCursor(conn, cursor, offset)
            // NOTE: lhsBaseGen.apply(c) makes, e.g. "model.", ignoring the parameter "c".
            return createAssociatedModelExpr.build();
        } else if (Types.isSingleAssociation(type)) {
            AssociationDefinition r = c.getAssociation();
            assert r != null;
            return CodeBlock.builder()
                    .add("new $T<>(conn, $L, cursor.getLong($L))",
                            r.getAssociationType(), c.getAssociatedSchema().createSchemaInstanceExpr(), index)
                    .build();
        } else {
            CodeBlock.Builder rhsExprBuilder = CodeBlock.builder();
            if (c.isNullableInSQL()) {
                rhsExprBuilder.add("cursor.isNull($L) ? null : $L", index,
                        c.buildDeserializeExpr(cursorGetter(c, index)));
            } else {
                rhsExprBuilder.add(c.buildDeserializeExpr(cursorGetter(c, index)));
            }
            return rhsExprBuilder.build();
        }
    }

    private CodeBlock buildNewModelFromCursor() {
        return schema.getConstructorElement().map(constructorElement -> {
            CodeBlock.Builder block = CodeBlock.builder();

            if (schema.getColumns().size() != constructorElement.getParameters().size()) {
                context.addError("The @Setter constructor parameters must satisfy all the @Column fields",
                        constructorElement);
            }

            block.add(buildPopulateValuesFromCursor(column -> CodeBlock.of("$T ", column.getType())));

            block.addStatement("return new $T($L)", schema.getModelClassName(),
                    constructorElement.getParameters()
                            .stream()
                            .map(this::extractColumnNameFromParameterElement)
                            .collect(Collectors.joining(", ")));

            return block.build();
        }).orElseGet(() -> {
            CodeBlock.Builder block = CodeBlock.builder();

            block.addStatement("$T model = new $T()", schema.getModelClassName(), schema.getModelClassName());
            block.add(buildPopulateValuesFromCursor(column -> CodeBlock.of("model.")));
            block.addStatement("return model");

            return block.build();
        });
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
            return CodeBlock.of("cursor.getLong($L) != 0", index);
        } else if (type.equals(TypeName.BYTE)) {
            return CodeBlock.of("(byte)cursor.getLong($L)", index);
        } else if (type.isPrimitive()) {
            String s = type.toString();
            return CodeBlock.of("cursor.get$L($L)", Strings.toUpperFirst(s), index);
        } else if (type.equals(Types.String)) {
            return CodeBlock.of("cursor.getString($L)", index);
        } else if (type.equals(Types.ByteArray)) {
            return CodeBlock.of("cursor.getBlob($L)", index);
        } else {
            // FIXME: not reached?
            return CodeBlock.of("cursor.getString($L)", index);
        }
    }

    private static class FieldSpecDefinition {

        FieldSpec fieldSpec;

        TypeSpec initializer;

        FieldSpecDefinition(FieldSpec fieldSpec, TypeSpec initializer) {
            this.fieldSpec = fieldSpec;
            this.initializer = initializer;
        }
    }
}
