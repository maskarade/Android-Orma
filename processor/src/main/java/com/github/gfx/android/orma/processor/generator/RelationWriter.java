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
import com.github.gfx.android.orma.annotation.OnConflict;
import com.github.gfx.android.orma.processor.ProcessingContext;
import com.github.gfx.android.orma.processor.exception.ProcessingException;
import com.github.gfx.android.orma.processor.model.AssociationDefinition;
import com.github.gfx.android.orma.processor.model.ColumnDefinition;
import com.github.gfx.android.orma.processor.model.SchemaDefinition;
import com.github.gfx.android.orma.processor.util.Annotations;
import com.github.gfx.android.orma.processor.util.Types;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeSpec;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;

public class RelationWriter extends BaseWriter {

    private final SchemaDefinition schema;

    private final ConditionQueryHelpers queryHelpers;

    public RelationWriter(ProcessingContext context, SchemaDefinition schema) {
        super(context);
        this.schema = schema;
        this.queryHelpers = new ConditionQueryHelpers(context, schema, getTargetClassName());
    }

    ClassName getTargetClassName() {
        return schema.getRelationClassName();
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
        TypeSpec.Builder classBuilder = TypeSpec.classBuilder(getTargetClassName());
        if (schema.isGeneric()) {
            classBuilder.addAnnotation(Annotations.suppressWarnings("rawtypes"));
        }
        classBuilder.addModifiers(Modifier.PUBLIC);
        classBuilder.superclass(Types.getRelation(schema.getModelClassName(), getTargetClassName()));

        classBuilder.addField(FieldSpec.builder(schema.getSchemaClassName(), "schema", Modifier.FINAL).build());

        classBuilder.addMethods(buildMethodSpecs());

        return classBuilder.build();
    }

    public List<MethodSpec> buildMethodSpecs() {
        List<MethodSpec> methodSpecs = new ArrayList<>();

        methodSpecs.addAll(
                new ConditionBaseMethods(context, schema, getTargetClassName())
                        .buildMethodSpecs());

        if (schema.hasPrimaryIdEqHelper()) {
            schema.getPrimaryKey().ifPresent(primaryKey -> {
                methodSpecs.add(MethodSpec.methodBuilder("reload")
                        .addAnnotation(Annotations.nonNull())
                        .addAnnotation(Annotations.checkResult())
                        .addModifiers(Modifier.PUBLIC)
                        .returns(schema.getModelClassName())
                        .addParameter(ParameterSpec.builder(schema.getModelClassName(), "model")
                                .addAnnotation(Annotations.nonNull())
                                .build())
                        .addStatement("return selector().$LEq($L).value()",
                                primaryKey.name, primaryKey.buildGetColumnExpr("model"))
                        .build());
            });
        }

        methodSpecs.add(MethodSpec.methodBuilder("upsertWithoutTransaction")
                .addAnnotations(Annotations.overrideAndNonNull())
                .addModifiers(Modifier.PUBLIC)
                .addParameter(
                        ParameterSpec.builder(schema.getModelClassName(), "model")
                                .addAnnotation(Annotations.nonNull())
                                .build())
                .returns(schema.getModelClassName())
                .addCode(buildUpsert("model"))
                .build());

        methodSpecs.add(MethodSpec.methodBuilder("selector")
                .addAnnotations(Annotations.overrideAndNonNull())
                .addModifiers(Modifier.PUBLIC)
                .returns(schema.getSelectorClassName())
                .addStatement("return new $T(this)", schema.getSelectorClassName())
                .build());

        methodSpecs.add(MethodSpec.methodBuilder("updater")
                .addAnnotations(Annotations.overrideAndNonNull())
                .addModifiers(Modifier.PUBLIC)
                .returns(schema.getUpdaterClassName())
                .addStatement("return new $T(this)", schema.getUpdaterClassName())
                .build());

        methodSpecs.add(MethodSpec.methodBuilder("deleter")
                .addAnnotations(Annotations.overrideAndNonNull())
                .addModifiers(Modifier.PUBLIC)
                .returns(schema.getDeleterClassName())
                .addStatement("return new $T(this)", schema.getDeleterClassName())
                .build());

        methodSpecs.addAll(queryHelpers.buildConditionHelpers(true));

        return methodSpecs;
    }

    private CodeBlock buildUpsert(String modelExpr) {
        CodeBlock.Builder code = CodeBlock.builder();

        Optional<ColumnDefinition> optionalPrimaryKey = schema.getPrimaryKey();

        if (!optionalPrimaryKey.isPresent()) {
            return code.addStatement("throw new $T($S)",
                    UnsupportedOperationException.class,
                    "upsert is not supported because of missing @PrimaryKey"
            ).build();
        }

        ColumnDefinition primaryKey = optionalPrimaryKey.get();

        if (!primaryKey.hasHelper(Column.Helpers.CONDITION_EQ)) {
            return code.addStatement("throw new $T($S)",
                    UnsupportedOperationException.class,
                    "upsert is not supported because of missing @PrimaryKey's CONDITION_EQ helper"
            ).build();
        }

        // build contentValues

        code.addStatement("$T contentValues = new $T()", Types.ContentValues, Types.ContentValues);

        for (ColumnDefinition column : schema.getColumnsWithoutAutoId()) {
            AssociationDefinition r = column.getAssociation();

            if (r != null) {
                SchemaDefinition associatedSchema = context.getSchemaDef(r.getModelType());
                CodeBlock associatedSchemaExpr = CodeBlock.of("$T.INSTANCE", associatedSchema.getSchemaClassName());
                CodeBlock associatedModelExpr;
                if (r.isSingleAssociation()) {
                    associatedModelExpr = CodeBlock.of("$L.get()", column.buildGetColumnExpr(modelExpr));
                } else {
                    associatedModelExpr = CodeBlock.of("$L", column.buildGetColumnExpr(modelExpr));
                }

                CodeBlock newAssociatedModelExpr = CodeBlock.of("new $T(conn, $L).upsertWithoutTransaction($L)",
                        associatedSchema.getRelationClassName(), associatedSchemaExpr, associatedModelExpr);

                ColumnDefinition associatedKey = associatedSchema.getPrimaryKey()
                        .orElseThrow(() -> new ProcessingException("No explicit primary key defined",
                                associatedSchema.getElement()));
                code.addStatement("contentValues.put($S, $L)",
                        column.getEscapedColumnName(),
                        associatedKey.buildSerializedColumnExpr("conn", newAssociatedModelExpr));
            } else {
                code.addStatement("contentValues.put($S, $L)",
                        column.getEscapedColumnName(), column.buildSerializedColumnExpr("conn", modelExpr));
            }
        }

        // only "auto = true" supports conditional UPDATE
        if (primaryKey.autoId) {
            code.beginControlFlow("if ($L != 0)", primaryKey.buildGetColumnExpr(modelExpr));
        }

        code.addStatement("int updatedRows = updater().$LEq($L).putAll(contentValues).execute()",
                primaryKey.name, primaryKey.buildGetColumnExpr(modelExpr));
        code.beginControlFlow("if (updatedRows != 0)");
        code.addStatement("return selector().$LEq($L).value()",
                primaryKey.name, primaryKey.buildGetColumnExpr(modelExpr));
        code.endControlFlow();

        if (primaryKey.autoId) {
            code.endControlFlow();
        }

        code.addStatement("long rowId = conn.insert(schema, contentValues, $T.NONE)", OnConflict.class);
        code.addStatement("return conn.findByRowId(schema, rowId)");

        return code.build();
    }
}
