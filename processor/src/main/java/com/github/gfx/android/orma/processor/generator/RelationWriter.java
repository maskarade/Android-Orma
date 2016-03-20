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

import com.github.gfx.android.orma.processor.util.Annotations;
import com.github.gfx.android.orma.processor.model.ColumnDefinition;
import com.github.gfx.android.orma.processor.ProcessingContext;
import com.github.gfx.android.orma.processor.model.SchemaDefinition;
import com.github.gfx.android.orma.processor.util.Strings;
import com.github.gfx.android.orma.processor.util.Types;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import javax.lang.model.element.Modifier;

public class RelationWriter extends BaseWriter {

    private final SchemaDefinition schema;

    private final ConditionQueryHelpers conditionQueryHelpers;

    public RelationWriter(ProcessingContext context, SchemaDefinition schema) {
        super(context);
        this.schema = schema;
        this.conditionQueryHelpers = new ConditionQueryHelpers(context, schema, getTargetClassName());
    }

    ClassName getTargetClassName() {
        return schema.getRelationClassName();
    }

    @Override
    public String getPackageName() {
        return schema.getPackageName();
    }

    @Override
    public TypeSpec buildTypeSpec() {
        TypeSpec.Builder classBuilder = TypeSpec.classBuilder(getTargetClassName());
        classBuilder.addModifiers(Modifier.PUBLIC);
        classBuilder.superclass(Types.getRelation(schema.getModelClassName(), getTargetClassName()));

        classBuilder.addMethods(buildMethodSpecs());

        return classBuilder.build();
    }

    public List<MethodSpec> buildMethodSpecs() {
        List<MethodSpec> methodSpecs = new ArrayList<>();

        methodSpecs.add(MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addParameter(Types.OrmaConnection, "conn")
                .addParameter(Types.getSchema(schema.getModelClassName()), "schema")
                .addCode("super(conn, schema);\n")
                .build());

        methodSpecs.add(MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addParameter(getTargetClassName(), "relation")
                .addCode("super(relation);\n")
                .build());

        methodSpecs.add(MethodSpec.methodBuilder("clone")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Annotations.override())
                .returns(getTargetClassName())
                .addStatement("return new $T(this)", getTargetClassName())
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

        methodSpecs.addAll(conditionQueryHelpers.buildConditionHelpers());

        schema.getColumns()
                .stream()
                .filter(this::needsOrderByHelpers)
                .flatMap(this::buildOrderByHelpers)
                .forEach(methodSpecs::add);

        return methodSpecs;
    }

    boolean needsOrderByHelpers(ColumnDefinition column) {
        return (column.indexed || (column.primaryKey && (column.autoincrement || !column.autoId)));
    }

    Stream<MethodSpec> buildOrderByHelpers(ColumnDefinition column) {
        return Stream.of(
                MethodSpec.methodBuilder("orderBy" + Strings.toUpperFirst(column.name) + "Asc")
                        .addModifiers(Modifier.PUBLIC)
                        .returns(getTargetClassName())
                        .addStatement("return orderBy($T.$L.orderInAscending())", schema.getSchemaClassName(),
                                column.name)
                        .build(),
                MethodSpec.methodBuilder("orderBy" + Strings.toUpperFirst(column.name) + "Desc")
                        .addModifiers(Modifier.PUBLIC)
                        .returns(getTargetClassName())
                        .addStatement("return orderBy($T.$L.orderInDescending())", schema.getSchemaClassName(),
                                column.name)
                        .build()
        );
    }

}
