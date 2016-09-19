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

import com.github.gfx.android.orma.processor.ProcessingContext;
import com.github.gfx.android.orma.processor.model.SchemaDefinition;
import com.github.gfx.android.orma.processor.util.Annotations;
import com.github.gfx.android.orma.processor.util.Types;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import java.util.ArrayList;
import java.util.List;

import javax.lang.model.element.Modifier;

public class DeleterWriter extends BaseWriter {

    private final SchemaDefinition schema;

    private final ConditionQueryHelpers queryHelpers;

    public DeleterWriter(ProcessingContext context, SchemaDefinition schema) {
        super(context);
        this.schema = schema;
        queryHelpers = new ConditionQueryHelpers(context, schema, schema.getDeleterClassName());
    }

    @Override
    public String getPackageName() {
        return schema.getPackageName();
    }

    @Override
    public TypeSpec buildTypeSpec() {
        TypeSpec.Builder classBuilder = TypeSpec.classBuilder(schema.getDeleterClassName());
        classBuilder.addModifiers(Modifier.PUBLIC);
        classBuilder.superclass(Types.getDeleter(schema.getModelClassName(), schema.getDeleterClassName()));

        classBuilder.addField(FieldSpec.builder(schema.getSchemaClassName(), "schema", Modifier.FINAL).build());

        classBuilder.addMethods(buildMethodSpecs());

        return classBuilder.build();
    }

    public List<MethodSpec> buildMethodSpecs() {
        List<MethodSpec> methodSpecs = new ArrayList<>();

        methodSpecs.add(
                MethodSpec.constructorBuilder()
                        .addModifiers(Modifier.PUBLIC)
                        .addParameter(Types.OrmaConnection, "conn")
                        .addParameter(schema.getSchemaClassName(), "schema")
                        .addStatement("super(conn)")
                        .addStatement("this.schema = schema")
                        .build()
        );

        methodSpecs.add(
                MethodSpec.constructorBuilder()
                        .addModifiers(Modifier.PUBLIC)
                        .addParameter(schema.getRelationClassName(), "relation")
                        .addStatement("super(relation)")
                        .addStatement("this.schema = ($T) relation.getSchema()", schema.getSchemaClassName())
                        .build()
        );

        methodSpecs.add(MethodSpec.methodBuilder("getSchema")
                .addAnnotation(Annotations.override())
                .addAnnotation(Annotations.nonNull())
                .addModifiers(Modifier.PUBLIC)
                .returns(schema.getSchemaClassName())
                .addStatement("return schema")
                .build());

        methodSpecs.addAll(queryHelpers.buildConditionHelpers(false));

        return methodSpecs;
    }
}
