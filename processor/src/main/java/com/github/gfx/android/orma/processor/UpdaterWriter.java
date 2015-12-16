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

import javax.lang.model.element.Modifier;

public class UpdaterWriter extends BaseWriter {

    private final SchemaDefinition schema;

    private final ConditionQueryHelpers conditionQueryHelpers;

    private final SqlGenerator sql = new SqlGenerator();

    public UpdaterWriter(ProcessingContext context, SchemaDefinition schema) {
        super(context);
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
                        .addModifiers(Modifier.PUBLIC)
                        .addParameter(Types.OrmaConnection, "conn")
                        .addParameter(schema.getSchemaClassName(), "schema")
                        .addStatement("super(conn, schema)")
                        .build()
        );

        schema.getColumnsWithoutAutoId().forEach(column -> {
            AssociationDefinition r = column.getRelation();

            if (r == null) {
                String paramName = column.name;
                CodeBlock.Builder valueExpr = CodeBlock.builder();

                if (Types.needsTypeAdapter(column.getUnboxType())) {
                    valueExpr.add("conn.getTypeAdapterRegistry().serialize($T.$L.type, $L)",
                            schema.getSchemaClassName(), column.name, paramName);
                } else {
                    valueExpr.add(paramName);
                }

                methodSpecs.add(
                        MethodSpec.methodBuilder(column.name)
                                .addModifiers(Modifier.PUBLIC)
                                .returns(schema.getUpdaterClassName())
                                .addParameter(
                                        ParameterSpec.builder(column.getType(), paramName)
                                                .build()
                                )
                                .addStatement("contents.put($S, $L)", sql.quoteIdentifier(column.columnName),
                                        valueExpr.build())
                                .addStatement("return this")
                                .build()
                );

            } else { // SingleAssociation<T>
                methodSpecs.add(
                        MethodSpec.methodBuilder(column.name)
                                .addModifiers(Modifier.PUBLIC)
                                .returns(schema.getUpdaterClassName())
                                .addParameter(
                                        ParameterSpec.builder(column.getType(), column.name + "Reference")
                                                .build()
                                )
                                .addStatement("contents.put($S, $L.getId())",
                                        sql.quoteIdentifier(column.columnName), column.name + "Reference")
                                .addStatement("return this")
                                .build()
                );

                SchemaDefinition modelSchema = context.getSchemaDef(r.modelType);
                if (modelSchema == null) {
                    // FIXME: just stack errors and return in order to continue processing
                    throw new ProcessingException(Types.SingleAssociation.simpleName() + "<T> can handle only Orma models",
                            column.element
                    );
                }

                methodSpecs.add(
                        MethodSpec.methodBuilder(column.name)
                                .addModifiers(Modifier.PUBLIC)
                                .returns(schema.getUpdaterClassName())
                                .addParameter(
                                        ParameterSpec.builder(r.modelType, column.name)
                                                .build()
                                )
                                .addStatement("contents.put($S, $L.$L)",
                                        sql.quoteIdentifier(column.columnName),
                                        column.name,
                                        modelSchema.getPrimaryKey().buildGetColumnExpr())
                                .addStatement("return this")
                                .build()
                );
            }
        });

        methodSpecs.addAll(conditionQueryHelpers.buildConditionHelpers());

        return methodSpecs;
    }
}
