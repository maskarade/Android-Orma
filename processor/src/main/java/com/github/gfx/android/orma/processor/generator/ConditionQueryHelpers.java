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

import com.github.gfx.android.orma.annotation.Column;
import com.github.gfx.android.orma.processor.ProcessingContext;
import com.github.gfx.android.orma.processor.exception.ProcessingException;
import com.github.gfx.android.orma.processor.model.AssociationDefinition;
import com.github.gfx.android.orma.processor.model.ColumnDefinition;
import com.github.gfx.android.orma.processor.model.IndexDefinition;
import com.github.gfx.android.orma.processor.model.SchemaDefinition;
import com.github.gfx.android.orma.processor.util.Annotations;
import com.github.gfx.android.orma.processor.util.SqlTypes;
import com.github.gfx.android.orma.processor.util.Strings;
import com.github.gfx.android.orma.processor.util.Types;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ArrayTypeName;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

import javax.lang.model.element.Modifier;

public class ConditionQueryHelpers {

    private final ProcessingContext context;

    private final SchemaDefinition schema;

    private final ClassName targetClassName;

    public ConditionQueryHelpers(ProcessingContext context, SchemaDefinition schema, ClassName targetClassName) {
        this.context = context;
        this.schema = schema;
        this.targetClassName = targetClassName;
    }

    public ClassName getTargetClassName() {
        return targetClassName;
    }

    public List<MethodSpec> buildConditionHelpers(boolean orderByHelpers) {
        return buildConditionHelpers(orderByHelpers, false);
    }

    public List<MethodSpec> buildConditionHelpers(boolean orderByHelpers, boolean aggregatorHelpers) {
        List<MethodSpec> methodSpecs = new ArrayList<>();
        schema.getColumns()
                .stream()
                .filter(ColumnDefinition::hasConditionHelpers)
                .forEach(column -> buildConditionHelpersForEachColumn(methodSpecs, column));

        schema.getIndexes()
                .stream()
                .filter(index -> index.columns.size() > 1)
                .forEach(index -> {
                    buildConditionHelpersForCompositeIndex(methodSpecs, index);
                });

        if (orderByHelpers) {
            schema.getColumns()
                    .stream()
                    .filter(ColumnDefinition::hasOrderHelpers)
                    .forEach(column -> buildOrderByHelpers(methodSpecs, column));

            schema.getIndexes()
                    .stream()
                    .filter(index -> index.columns.size() > 1)
                    .forEach(index -> {
                        buildOrderByHelpersForCompositeIndex(methodSpecs, index);
                    });
        }

        if (aggregatorHelpers) {
            schema.getColumnsWithoutAutoId()
                    .stream()
                    .filter(ColumnDefinition::hasAggregationHelpers)
                    .forEach(column -> buildAggregationHelpers(methodSpecs, column));
        }

        return methodSpecs;
    }

    CodeBlock serializedFieldExpr(ColumnDefinition column, ParameterSpec paramSpec) {
        AssociationDefinition r = column.getAssociation();

        boolean isAssociation = r != null;
        if (isAssociation) {
            SchemaDefinition associatedSchema = context.getSchemaDef(r.getModelType());
            ColumnDefinition primaryKey = associatedSchema.getPrimaryKey()
                    .orElseThrow(() -> new ProcessingException(
                            "Missing @PrimaryKey for " + associatedSchema.getModelClassName().simpleName(),
                            associatedSchema.getElement()));
            return primaryKey.buildSerializedColumnExpr("conn", paramSpec.name);
        } else {
            return column.applySerialization("conn", paramSpec.name);
        }

    }

    ParameterSpec buildParameterSpec(ColumnDefinition column) {
        AssociationDefinition r = column.getAssociation();

        boolean isAssociation = r != null;
        TypeName type = isAssociation ? r.getModelType() : column.getType();
        List<AnnotationSpec> paramAnnotations = column.type.isPrimitive()
                ? Collections.emptyList()
                : Collections.singletonList(Annotations.nonNull());

        return ParameterSpec.builder(type, column.name)
                .addAnnotations(paramAnnotations)
                .build();
    }

    ParameterSpec buildStringParameterSpec(String name) {
        return ParameterSpec.builder(Types.String, name)
                .addAnnotations(Collections.singletonList(Annotations.nonNull()))
                .build();
    }

    void buildConditionHelpersForEachColumn(List<MethodSpec> methodSpecs, ColumnDefinition column) {
        AssociationDefinition r = column.getAssociation();

        boolean isAssociation = r != null;
        TypeName type = isAssociation ? r.getModelType() : column.getType();

        TypeName collectionType = Types.getCollection(type.box());

        List<AnnotationSpec> paramAnnotations = column.type.isPrimitive()
                ? Collections.emptyList()
                : Collections.singletonList(Annotations.nonNull());

        ParameterSpec paramSpec = buildParameterSpec(column);

        List<AnnotationSpec> safeVarargsIfNeeded = Annotations.safeVarargsIfNeeded(column.getType());

        String columnExpr = "schema." + column.name;

        CodeBlock serializedFieldExpr = serializedFieldExpr(column, paramSpec);

        if (column.nullable && column.hasHelper(Column.Helpers.CONDITION_IS_NULL)) {
            methodSpecs.add(MethodSpec.methodBuilder(column.name + "IsNull")
                    .addModifiers(Modifier.PUBLIC)
                    .returns(targetClassName)
                    .addStatement("return where($L, $S)", columnExpr, " IS NULL")
                    .build()
            );
        }

        if (column.nullable && column.hasHelper(Column.Helpers.CONDITION_IS_NOT_NULL)) {
            methodSpecs.add(MethodSpec.methodBuilder(column.name + "IsNotNull")
                    .addModifiers(Modifier.PUBLIC)
                    .returns(targetClassName)
                    .addStatement("return where($L, $S)", columnExpr, " IS NOT NULL")
                    .build()
            );
        }

        if (column.hasHelper(Column.Helpers.CONDITION_EQ)) {
            methodSpecs.add(MethodSpec.methodBuilder(column.name + "Eq")
                    .addModifiers(Modifier.PUBLIC)
                    .addParameter(paramSpec)
                    .returns(targetClassName)
                    .addStatement("return where($L, $S, $L)", columnExpr, "=", serializedFieldExpr)
                    .build()
            );
        }

        if (isAssociation) {
            // for foreign keys
            if (column.hasHelper(Column.Helpers.CONDITION_EQ)) {
                SchemaDefinition associatedSchema = column.getAssociatedSchema();
                associatedSchema.getPrimaryKey().ifPresent(foreignKey -> {
                    String paramName = column.name + Strings.toUpperFirst(foreignKey.name);
                    CodeBlock serializedParamExpr = foreignKey.applySerialization("conn", paramName);
                    methodSpecs.add(
                            MethodSpec.methodBuilder(column.name + "Eq")
                                    .addModifiers(Modifier.PUBLIC)
                                    .addParameter(
                                            ParameterSpec.builder(foreignKey.getType(), paramName)
                                                    .addAnnotations(foreignKey.nullabilityAnnotations())
                                                    .build())
                                    .returns(targetClassName)
                                    .addStatement("return where($L, $S, $L)", columnExpr, "=", serializedParamExpr)
                                    .build()
                    );
                });
            }

            // generates only "*Eq" for associations
            return;
        }

        if (column.hasHelper(Column.Helpers.CONDITION_NOT_EQ)) {
            methodSpecs.add(MethodSpec.methodBuilder(column.name + "NotEq")
                    .addModifiers(Modifier.PUBLIC)
                    .addParameter(paramSpec)
                    .returns(targetClassName)
                    .addStatement("return where($L, $S, $L)", columnExpr, "<>",
                            serializedFieldExpr)
                    .build()
            );
        }

        if (column.needsTypeAdapter()) {
            TypeSpec serializerFunction = TypeSpec.anonymousClassBuilder("")
                    .superclass(Types.getFunction1(type.box(), column.getSerializedBoxType()))
                    .addMethod(MethodSpec.methodBuilder("apply")
                            .addAnnotation(Annotations.override())
                            .addModifiers(Modifier.PUBLIC)
                            .returns(column.getSerializedBoxType())
                            .addParameter(ParameterSpec.builder(type.box(), "value").build())
                            .addStatement("return $L", column.applySerialization("conn", "value"))
                            .build())
                    .build();

            if (column.hasHelper(Column.Helpers.CONDITION_IN)) {
                methodSpecs.add(MethodSpec.methodBuilder(column.name + "In")
                        .addModifiers(Modifier.PUBLIC)
                        .addParameter(ParameterSpec.builder(collectionType, "values")
                                .addAnnotation(Annotations.nonNull())
                                .build())
                        .returns(targetClassName)
                        .addStatement("return in(false, $L, values, $L)",
                                columnExpr, serializerFunction)
                        .build()
                );
            }

            if (column.hasHelper(Column.Helpers.CONDITION_NOT_IN)) {
                methodSpecs.add(MethodSpec.methodBuilder(column.name + "NotIn")
                        .addModifiers(Modifier.PUBLIC)
                        .addParameter(ParameterSpec.builder(collectionType, "values")
                                .addAnnotation(Annotations.nonNull())
                                .build())
                        .returns(targetClassName)
                        .addStatement("return in(true, $L, values, $L)",
                                columnExpr, serializerFunction)
                        .build()
                );
            }

        } else {
            if (column.hasHelper(Column.Helpers.CONDITION_IN)) {
                methodSpecs.add(MethodSpec.methodBuilder(column.name + "In")
                        .addModifiers(Modifier.PUBLIC)
                        .addParameter(ParameterSpec.builder(collectionType, "values")
                                .addAnnotation(Annotations.nonNull())
                                .build())
                        .returns(targetClassName)
                        .addStatement("return in(false, $L, values)",
                                columnExpr)
                        .build()
                );
            }

            if (column.hasHelper(Column.Helpers.CONDITION_NOT_IN)) {
                methodSpecs.add(MethodSpec.methodBuilder(column.name + "NotIn")
                        .addModifiers(Modifier.PUBLIC)
                        .addParameter(ParameterSpec.builder(collectionType, "values")
                                .addAnnotation(Annotations.nonNull())
                                .build())
                        .returns(targetClassName)
                        .addStatement("return in(true, $L, values)",
                                columnExpr)
                        .build()
                );
            }
        }

        if (column.hasHelper(Column.Helpers.CONDITION_IN)) {
            methodSpecs.add(MethodSpec.methodBuilder(column.name + "In")
                    .addAnnotations(safeVarargsIfNeeded)
                    .varargs(true)
                    .addModifiers(Modifier.FINAL) // to use SafeVarargs
                    .addModifiers(Modifier.PUBLIC)
                    .addParameter(ParameterSpec.builder(ArrayTypeName.of(type.box()), "values")
                            .addAnnotation(Annotations.nonNull())
                            .build())
                    .returns(targetClassName)
                    .addStatement("return $L($T.asList(values))",
                            column.name + "In", Types.Arrays)
                    .build()
            );
        }

        if (column.hasHelper(Column.Helpers.CONDITION_NOT_IN)) {
            methodSpecs.add(MethodSpec.methodBuilder(column.name + "NotIn")
                    .addAnnotations(safeVarargsIfNeeded)
                    .varargs(true)
                    .addModifiers(Modifier.FINAL) // to use @SafeVarargs
                    .addModifiers(Modifier.PUBLIC)
                    .addParameter(ParameterSpec.builder(ArrayTypeName.of(type.box()), "values")
                            .addAnnotation(Annotations.nonNull())
                            .build())
                    .returns(targetClassName)
                    .addStatement("return $L($T.asList(values))",
                            column.name + "NotIn", Types.Arrays)
                    .build()
            );
        }

        if (column.hasHelper(Column.Helpers.CONDITION_GLOB) && type.equals(Types.String) && !column.primaryKey) {
            methodSpecs.add(MethodSpec.methodBuilder(column.name + "Glob")
                    .addModifiers(Modifier.PUBLIC)
                    .addParameter(buildStringParameterSpec("pattern"))
                    .returns(targetClassName)
                    .addStatement("return where($L, $S, pattern)", columnExpr, "GLOB")
                    .build()
            );
        }

        if (column.hasHelper(Column.Helpers.CONDITION_NOT_GLOB) && type.equals(Types.String) && !column.primaryKey) {
            methodSpecs.add(MethodSpec.methodBuilder(column.name + "NotGlob")
                    .addModifiers(Modifier.PUBLIC)
                    .addParameter(buildStringParameterSpec("pattern"))
                    .returns(targetClassName)
                    .addStatement("return where($L, $S, pattern)", columnExpr, "NOT GLOB")
                    .build()
            );
        }

        if (column.hasHelper(Column.Helpers.CONDITION_LT)) {
            methodSpecs.add(MethodSpec.methodBuilder(column.name + "Lt")
                    .addModifiers(Modifier.PUBLIC)
                    .addParameter(paramSpec)
                    .returns(targetClassName)
                    .addStatement("return where($L, $S, $L)",
                            columnExpr, "<",
                            serializedFieldExpr)
                    .build()
            );
        }
        if (column.hasHelper(Column.Helpers.CONDITION_LE)) {
            methodSpecs.add(MethodSpec.methodBuilder(column.name + "Le")
                    .addModifiers(Modifier.PUBLIC)
                    .addParameter(paramSpec)
                    .returns(targetClassName)
                    .addStatement("return where($L, $S, $L)",
                            columnExpr, "<=",
                            serializedFieldExpr)
                    .build()
            );
        }
        if (column.hasHelper(Column.Helpers.CONDITION_GT)) {
            methodSpecs.add(MethodSpec.methodBuilder(column.name + "Gt")
                    .addModifiers(Modifier.PUBLIC)
                    .addParameter(paramSpec)
                    .returns(targetClassName)
                    .addStatement("return where($L, $S, $L)",
                            columnExpr, ">",
                            serializedFieldExpr)
                    .build()
            );
        }
        if (column.hasHelper(Column.Helpers.CONDITION_GE)) {
            methodSpecs.add(MethodSpec.methodBuilder(column.name + "Ge")
                    .addModifiers(Modifier.PUBLIC)
                    .addParameter(paramSpec)
                    .returns(targetClassName)
                    .addStatement("return where($L, $S, $L)",
                            columnExpr, ">=",
                            serializedFieldExpr)
                    .build()
            );
        }
        if (column.hasHelper(Column.Helpers.CONDITION_BETWEEN)
                && SqlTypes.isComparable(column.getStorageType())) {
            ParameterSpec paramSpecA = ParameterSpec.builder(type, column.name + "A")
                    .addAnnotations(paramAnnotations)
                    .build();

            ParameterSpec paramSpecB = ParameterSpec.builder(type, column.name + "B")
                    .addAnnotations(paramAnnotations)
                    .build();

            methodSpecs.add(MethodSpec.methodBuilder(column.name + "Between")
                    .addJavadoc(
                            "To build a condition <code>$L BETWEEN a AND b</code>, which is equivalent to <code>a <= $L AND $L <= b</code>.\n",
                            column.name, column.name, column.name)
                    .addModifiers(Modifier.PUBLIC)
                    .addParameter(paramSpecA)
                    .addParameter(paramSpecB)
                    .returns(targetClassName)
                    .addStatement("return whereBetween($L, $L, $L)",
                            columnExpr,
                            serializedFieldExpr(column, paramSpecA),
                            serializedFieldExpr(column, paramSpecB))
                    .build()
            );
        }
    }

    void buildOrderByHelpers(List<MethodSpec> methodSpecs, ColumnDefinition column) {
        if (column.hasHelper(Column.Helpers.ORDER_IN_ASC)) {
            methodSpecs.add(MethodSpec.methodBuilder("orderBy" + Strings.toUpperFirst(column.name) + "Asc")
                    .addModifiers(Modifier.PUBLIC)
                    .returns(getTargetClassName())
                    .addStatement("return orderBy(schema.$L.orderInAscending())", column.name)
                    .build());
        }
        if (column.hasHelper(Column.Helpers.ORDER_IN_DESC)) {
            methodSpecs.add(MethodSpec.methodBuilder("orderBy" + Strings.toUpperFirst(column.name) + "Desc")
                    .addModifiers(Modifier.PUBLIC)
                    .returns(getTargetClassName())
                    .addStatement("return orderBy(schema.$L.orderInDescending())", column.name)
                    .build()
            );
        }
    }

    void buildAggregationHelpers(List<MethodSpec> methodSpecs, ColumnDefinition column) {
        if (column.hasHelper(Column.Helpers.MIN) && Types.isNumeric(column.type)) {
            methodSpecs.add(MethodSpec.methodBuilder("minBy" + Strings.toUpperFirst(column.name))
                    .addAnnotation(Annotations.nullable())
                    .addModifiers(Modifier.PUBLIC)
                    .returns(column.getBoxType())
                    .addCode(buildAggregatorBody(column, "MIN",
                            () -> CodeBlock.of("schema.$L.getFromCursor(conn, cursor, 0)", column.name)))
                    .build()
            );
        }
        if (column.hasHelper(Column.Helpers.MAX) && Types.isNumeric(column.type)) {
            methodSpecs.add(MethodSpec.methodBuilder("maxBy" + Strings.toUpperFirst(column.name))
                    .addAnnotation(Annotations.nullable())
                    .addModifiers(Modifier.PUBLIC)
                    .returns(column.getBoxType())
                    .addCode(buildAggregatorBody(column, "MAX",
                            () -> CodeBlock.of("schema.$L.getFromCursor(conn, cursor, 0)", column.name)))
                    .build()
            );
        }
        if (column.hasHelper(Column.Helpers.SUM) && Types.isNumeric(column.type)) {
            boolean isInteger = Types.looksLikeIntegerType(column.type);
            methodSpecs.add(MethodSpec.methodBuilder("sumBy" + Strings.toUpperFirst(column.name))
                    .addAnnotation(Annotations.nullable())
                    .addModifiers(Modifier.PUBLIC)
                    .returns(isInteger ? TypeName.LONG.box() : TypeName.DOUBLE.box())
                    .addCode(buildAggregatorBody(column, "SUM",
                            () -> isInteger ? CodeBlock.of("cursor.getLong(0)") : CodeBlock.of("cursor.getDouble(0)")))
                    .build()
            );
        }
        if (column.hasHelper(Column.Helpers.AVG) && Types.isNumeric(column.type)) {
            methodSpecs.add(MethodSpec.methodBuilder("avgBy" + Strings.toUpperFirst(column.name))
                    .addAnnotation(Annotations.nullable())
                    .addModifiers(Modifier.PUBLIC)
                    .returns(TypeName.DOUBLE.box())
                    .addCode(buildAggregatorBody(column, "AVG", () -> CodeBlock.of("cursor.getDouble(0)")))
                    .build()
            );
        }
    }

    CodeBlock buildAggregatorBody(ColumnDefinition column, String funcName, Supplier<CodeBlock> gen) {
        return CodeBlock.builder()
                .addStatement("$T cursor = executeWithColumns(schema.$L.buildCallExpr($S))",
                        Types.Cursor, column.name, funcName)
                .beginControlFlow("try")
                .addStatement("cursor.moveToFirst()")
                .addStatement("return cursor.isNull(0) ? null : $L", gen.get())
                .endControlFlow()
                .beginControlFlow("finally")
                .addStatement("cursor.close()")
                .endControlFlow()
                .build();
    }

    CharSequence buildBaseNameForCompositeIndex(IndexDefinition index) {
        StringBuilder baseName = new StringBuilder();
        for (int i = 0; i < index.columns.size(); i++) {
            ColumnDefinition column = index.columns.get(i);

            if (i == 0) {
                baseName.append(column.name);
            } else {
                baseName.append("And");
                baseName.append(Strings.toUpperFirst(column.name));
            }
        }

        return baseName;
    }

    void buildConditionHelpersForCompositeIndex(List<MethodSpec> methodSpecs, IndexDefinition index) {
        // create only "==" helper
        if (!index.hasHelper(Column.Helpers.CONDITION_EQ)) {
            return;
        }

        CharSequence baseName = buildBaseNameForCompositeIndex(index);

        CodeBlock.Builder conditions = CodeBlock.builder();
        List<ParameterSpec> paramSpecs = new ArrayList<>();

        for (int i = 0; i < index.columns.size(); i++) {
            ColumnDefinition column = index.columns.get(i);

            ParameterSpec paramSpec = buildParameterSpec(column);
            CodeBlock serializedFieldExpr = serializedFieldExpr(column, paramSpec);
            if (i != 0) {
                conditions.add(".");
            }
            conditions.add("where(schema.$L, $S, $L)",
                    column.columnName, "=", serializedFieldExpr);
            paramSpecs.add(paramSpec);
        }

        MethodSpec.Builder methodSpec = MethodSpec.methodBuilder(baseName + "Eq")
                .addModifiers(Modifier.PUBLIC)
                .addParameters(paramSpecs)
                .addStatement("return $L", conditions.build())
                .returns(targetClassName);
        methodSpecs.add(methodSpec.build());
    }

    void buildOrderByHelpersForCompositeIndex(List<MethodSpec> methodSpecs, IndexDefinition index) {
        CharSequence baseName = buildBaseNameForCompositeIndex(index);

        if (index.hasHelper(Column.Helpers.ORDER_IN_ASC)){
            CodeBlock.Builder conditions = CodeBlock.builder();

            for (int i = 0; i < index.columns.size(); i++) {
                ColumnDefinition column = index.columns.get(i);

                if (i != 0) {
                    conditions.add(".");
                }
                conditions.add("orderBy(schema.$L.orderInAscending())", column.name);
            }

            MethodSpec.Builder methodSpec = MethodSpec.methodBuilder("orderBy" + baseName + "Asc")
                    .addModifiers(Modifier.PUBLIC)
                    .addStatement("return $L", conditions.build())
                    .returns(targetClassName);
            methodSpecs.add(methodSpec.build());
        }

        if (index.hasHelper(Column.Helpers.ORDER_IN_DESC)){
            CodeBlock.Builder conditions = CodeBlock.builder();

            for (int i = 0; i < index.columns.size(); i++) {
                ColumnDefinition column = index.columns.get(i);

                if (i != 0) {
                    conditions.add(".");
                }
                conditions.add("orderBy(schema.$L.orderInDescending())", column.name);
            }

            MethodSpec.Builder methodSpec = MethodSpec.methodBuilder("orderBy" + baseName + "Desc")
                    .addModifiers(Modifier.PUBLIC)
                    .addStatement("return $L", conditions.build())
                    .returns(targetClassName);
            methodSpecs.add(methodSpec.build());
        }
    }
}
