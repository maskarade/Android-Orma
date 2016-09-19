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
import com.github.gfx.android.orma.processor.model.SchemaDefinition;
import com.github.gfx.android.orma.processor.util.Annotations;
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
        List<MethodSpec> methodSpecs = new ArrayList<>();
        schema.getColumns()
                .stream()
                .filter(ColumnDefinition::hasConditionHelpers)
                .forEach(column -> buildConditionHelpersForEachColumn(methodSpecs, column));

        if (orderByHelpers) {
            schema.getColumns()
                    .stream()
                    .filter(ColumnDefinition::hasOrderHelpers)
                    .forEach(column -> buildOrderByHelpers(methodSpecs, column));
        }

        return methodSpecs;
    }

    void buildConditionHelpersForEachColumn(List<MethodSpec> methodSpecs, ColumnDefinition column) {
        AssociationDefinition r = column.getAssociation();

        boolean isAssociation = r != null;
        TypeName type = isAssociation ? r.getModelType() : column.getType();

        TypeName collectionType = Types.getCollection(type.box());

        ParameterSpec paramSpec = ParameterSpec.builder(type, column.name)
                .addAnnotations(
                        column.type.isPrimitive() ? Collections.emptyList() : Collections.singletonList(Annotations.nonNull()))
                .build();

        List<AnnotationSpec> safeVarargsIfNeeded = Annotations.safeVarargsIfNeeded(column.getType());

        String columnName = column.getEscapedColumnName();

        CodeBlock serializedFieldExpr;
        if (isAssociation) {
            SchemaDefinition associatedSchema = context.getSchemaDef(type);
            ColumnDefinition primaryKey = associatedSchema.getPrimaryKey()
                    .orElseThrow(() -> new ProcessingException(
                            "Missing @PrimaryKey for " + associatedSchema.getModelClassName().simpleName(),
                            associatedSchema.getElement()));
            serializedFieldExpr = CodeBlock.builder()
                    .add("$L /* primary key */", primaryKey.buildGetColumnExpr(paramSpec.name))
                    .build();
        } else {
            serializedFieldExpr = column.buildSerializeExpr("conn", paramSpec.name);
        }

        if (column.nullable && column.hasHelper(Column.Helpers.CONDITION_IS_NULL)) {
            methodSpecs.add(MethodSpec.methodBuilder(column.name + "IsNull")
                    .addModifiers(Modifier.PUBLIC)
                    .returns(targetClassName)
                    .addStatement("return where($S)", columnName + " IS NULL")
                    .build()
            );
        }

        if (column.nullable && column.hasHelper(Column.Helpers.CONDITION_IS_NOT_NULL)) {
            methodSpecs.add(MethodSpec.methodBuilder(column.name + "IsNotNull")
                    .addModifiers(Modifier.PUBLIC)
                    .returns(targetClassName)
                    .addStatement("return where($S)", columnName + " IS NOT NULL")
                    .build()
            );
        }

        if (column.hasHelper(Column.Helpers.CONDITION_EQ)) {
            methodSpecs.add(MethodSpec.methodBuilder(column.name + "Eq")
                    .addModifiers(Modifier.PUBLIC)
                    .addParameter(paramSpec)
                    .returns(targetClassName)
                    .addStatement("return where($S, $L)", columnName + " = ?", serializedFieldExpr)
                    .build()
            );
        }

        if (isAssociation) {
            // for foreign keys
            if (column.hasHelper(Column.Helpers.CONDITION_EQ)) {
                SchemaDefinition associatedSchema = column.getAssociatedSchema();
                associatedSchema.getPrimaryKey().ifPresent(foreignKey -> {
                    String paramName = column.name + Strings.toUpperFirst(foreignKey.name);
                    methodSpecs.add(
                            MethodSpec.methodBuilder(column.name + "Eq")
                                    .addModifiers(Modifier.PUBLIC)
                                    .addParameter(
                                            ParameterSpec.builder(foreignKey.getType(), paramName)
                                                    .addAnnotations(foreignKey.nullabilityAnnotations())
                                                    .build())
                                    .returns(targetClassName)
                                    .addStatement("return where($S, $L)", columnName + " = ?", paramName)
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
                    .addStatement("return where($S, $L)", columnName + " <> ?",
                            serializedFieldExpr)
                    .build()
            );
        }

        if (column.needsTypeAdapter()) {
            TypeSpec serializerFunction = TypeSpec.anonymousClassBuilder("")
                    .superclass(Types.getFunc1(type.box(), column.getSerializedBoxType()))
                    .addMethod(MethodSpec.methodBuilder("call")
                            .addAnnotation(Annotations.override())
                            .addModifiers(Modifier.PUBLIC)
                            .returns(column.getSerializedBoxType())
                            .addParameter(ParameterSpec.builder(type.box(), "value").build())
                            .addStatement("return $L", column.buildSerializeExpr("conn", "value"))
                            .build())
                    .build();

            if (column.hasHelper(Column.Helpers.CONDITION_IN)) {
                methodSpecs.add(MethodSpec.methodBuilder(column.name + "In")
                        .addModifiers(Modifier.PUBLIC)
                        .addParameter(ParameterSpec.builder(collectionType, "values")
                                .addAnnotation(Annotations.nonNull())
                                .build())
                        .returns(targetClassName)
                        .addStatement("return in(false, $S, values, $L)",
                                columnName, serializerFunction)
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
                        .addStatement("return in(true, $S, values, $L)",
                                columnName, serializerFunction)
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
                        .addStatement("return in(false, $S, values)",
                                columnName)
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
                        .addStatement("return in(true, $S, values)",
                                columnName)
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

        if (column.hasHelper(Column.Helpers.CONDITION_LT)) {
            methodSpecs.add(MethodSpec.methodBuilder(column.name + "Lt")
                    .addModifiers(Modifier.PUBLIC)
                    .addParameter(paramSpec)
                    .returns(targetClassName)
                    .addStatement("return where($S, $L)",
                            columnName + " < ?",
                            serializedFieldExpr)
                    .build()
            );
        }
        if (column.hasHelper(Column.Helpers.CONDITION_LE)) {
            methodSpecs.add(MethodSpec.methodBuilder(column.name + "Le")
                    .addModifiers(Modifier.PUBLIC)
                    .addParameter(paramSpec)
                    .returns(targetClassName)
                    .addStatement("return where($S, $L)",
                            columnName + " <= ?",
                            serializedFieldExpr)
                    .build()
            );
        }
        if (column.hasHelper(Column.Helpers.CONDITION_GT)) {
            methodSpecs.add(MethodSpec.methodBuilder(column.name + "Gt")
                    .addModifiers(Modifier.PUBLIC)
                    .addParameter(paramSpec)
                    .returns(targetClassName)
                    .addStatement("return where($S, $L)",
                            columnName + " > ?",
                            serializedFieldExpr)
                    .build()
            );
        }
        if (column.hasHelper(Column.Helpers.CONDITION_GE)) {
            methodSpecs.add(MethodSpec.methodBuilder(column.name + "Ge")
                    .addModifiers(Modifier.PUBLIC)
                    .addParameter(paramSpec)
                    .returns(targetClassName)
                    .addStatement("return where($S, $L)",
                            columnName + " >= ?",
                            serializedFieldExpr)
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

}
