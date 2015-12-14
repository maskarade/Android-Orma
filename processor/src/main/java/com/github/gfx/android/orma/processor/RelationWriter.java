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

import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import javax.lang.model.element.Modifier;

public class RelationWriter extends BaseWriter {

    private final SchemaDefinition schema;

    private final ConditionQueryHelpers conditionQueryHelpers;

    private final SqlGenerator sql = new SqlGenerator();

    public RelationWriter(ProcessingContext context, SchemaDefinition schema) {
        super(context);
        this.schema = schema;
        this.conditionQueryHelpers = new ConditionQueryHelpers(schema, schema.getRelationClassName());
    }

    @Override
    public TypeSpec buildTypeSpec() {
        TypeSpec.Builder classBuilder = TypeSpec.classBuilder(schema.getRelationClassName().simpleName());
        classBuilder.addModifiers(Modifier.PUBLIC);
        classBuilder.superclass(Types.getRelation(schema.getModelClassName(), schema.getRelationClassName()));

        classBuilder.addMethods(buildMethodSpecs());

        return classBuilder.build();
    }

    public List<MethodSpec> buildMethodSpecs() {
        List<MethodSpec> methodSpecs = new ArrayList<>();

        methodSpecs.add(MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addParameter(Types.OrmaConnection, "orma")
                .addParameter(schema.getSchemaClassName(), "schema")
                .addCode("super(orma, schema);\n")
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
        return (column.indexed || (column.primaryKey && column.autoincrement));
    }

    Stream<MethodSpec> buildOrderByHelpers(ColumnDefinition column) {
        return Stream.of(
                MethodSpec.methodBuilder("orderBy" + Strings.toUpperFirst(column.name) + "Asc")
                        .addModifiers(Modifier.PUBLIC)
                        .returns(schema.getRelationClassName())
                        .addStatement("return orderBy($S)", sql.quoteIdentifier(column.columnName) + " ASC")
                        .build(),
                MethodSpec.methodBuilder("orderBy" + Strings.toUpperFirst(column.name) + "Desc")
                        .addModifiers(Modifier.PUBLIC)
                        .returns(schema.getRelationClassName())
                        .addStatement("return orderBy($S)", sql.quoteIdentifier(column.columnName) + " DESC")
                        .build()
        );
    }
}
