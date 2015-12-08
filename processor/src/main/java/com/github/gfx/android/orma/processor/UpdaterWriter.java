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

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeSpec;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Modifier;

public class UpdaterWriter extends BaseWriter {

    private final SchemaDefinition schema;

    private final ConditionQueryHelpers conditionQueryHelpers;

    private final SqlGenerator sql = new SqlGenerator();

    public UpdaterWriter(SchemaDefinition schema, ProcessingEnvironment processingEnv) {
        super(processingEnv);
        this.schema = schema;
        conditionQueryHelpers = new ConditionQueryHelpers(schema, schema.getUpdaterClassName());
    }

    @Override
    public TypeSpec buildTypeSpec() {
        TypeSpec.Builder classBuilder = TypeSpec.classBuilder(schema.getUpdaterClassName().simpleName());
        classBuilder.addModifiers(Modifier.PUBLIC);
        classBuilder.superclass(Types.getUpdater(schema.getModelClassName(), schema.getUpdaterClassName()));

        classBuilder.addMethods(buildMethodSpecs());

        return classBuilder.build();
    }

    public List<MethodSpec> buildMethodSpecs() {
        List<MethodSpec> methodSpecs = new ArrayList<>();

        methodSpecs.add(
                MethodSpec.constructorBuilder()
                        .addParameter(Types.OrmaConnection, "conn")
                        .addParameter(schema.getSchemaClassName(), "schema")
                        .addStatement("super(conn, schema)")
                        .build()
        );

        schema.getColumnsWithoutAutoId().forEach(column -> {
            RelationDefinition r = column.getRelation();
            if (r == null) {
                CodeBlock.Builder valueExpr = CodeBlock.builder();
                if (Types.needsTypeAdapter(column.getUnboxType())) {
                    valueExpr.add("conn.getTypeAdapterRegistry().serialize($T.$L.type, value)",
                            schema.getSchemaClassName(), column.name);
                } else {
                    valueExpr.add("value");
                }

                methodSpecs.add(
                        MethodSpec.methodBuilder(column.name)
                                .addModifiers(Modifier.PUBLIC)
                                .returns(schema.getUpdaterClassName())
                                .addParameter(
                                        ParameterSpec.builder(column.getType(), "value")
                                                .build()
                                )
                                .addStatement("contents.put($S, $L)", sql.quoteIdentifier(column.columnName),
                                        valueExpr.build())
                                .addStatement("return this")
                                .build()
                );
            } else {
                // FIXME: in case the column represents a relationship
            }
        });

        methodSpecs.addAll(conditionQueryHelpers.buildConditionHelpers());

        return methodSpecs;
    }
}
