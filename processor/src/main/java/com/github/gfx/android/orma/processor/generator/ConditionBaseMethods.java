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
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;

import java.util.ArrayList;
import java.util.List;

import javax.lang.model.element.Modifier;

public class ConditionBaseMethods {

    @SuppressWarnings("unused")
    private final ProcessingContext context;

    private final SchemaDefinition schema;

    private final ClassName targetClassName;

    private final boolean isRxJavaSupport;

    public ConditionBaseMethods(ProcessingContext context, SchemaDefinition schema, ClassName targetClassName) {
        this.context = context;
        this.schema = schema;
        this.targetClassName = targetClassName;
        this.isRxJavaSupport = context.isRxJavaSupport(schema);
    }

    public List<MethodSpec> buildMethodSpecs() {
        return buildMethodSpecs(false);
    }

    public List<MethodSpec> buildMethodSpecs(boolean useRawConnectionType) {
        List<MethodSpec> methodSpecs = new ArrayList<>();

        ClassName ormaConnectionType = (!useRawConnectionType && isRxJavaSupport)
                ? Types.RxOrmaConnection : Types.OrmaConnection;
        methodSpecs.add(MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addParameter(ormaConnectionType, "conn")
                .addParameter(schema.getSchemaClassName(), "schema")
                .addStatement("super(conn)")
                .addStatement("this.schema = schema")
                .build());

        methodSpecs.add(MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addParameter(targetClassName, "that")
                .addStatement("super(that)")
                .addStatement("this.schema = that.getSchema()", schema.getSchemaClassName())
                .build());

        if (!targetClassName.equals(schema.getRelationClassName())) {
            methodSpecs.add(MethodSpec.constructorBuilder()
                    .addModifiers(Modifier.PUBLIC)
                    .addParameter(schema.getRelationClassName(), "relation")
                    .addStatement("super(relation)")
                    .addStatement("this.schema = relation.getSchema()", schema.getSchemaClassName())
                    .build());
        }

        methodSpecs.add(MethodSpec.methodBuilder("clone")
                .addAnnotation(Annotations.override())
                .addModifiers(Modifier.PUBLIC)
                .returns(targetClassName)
                .addStatement("return new $T(this)", targetClassName)
                .build());

        methodSpecs.add(MethodSpec.methodBuilder("getSchema")
                .addAnnotation(Annotations.nonNull())
                .addAnnotation(Annotations.override())
                .addModifiers(Modifier.PUBLIC)
                .returns(schema.getSchemaClassName())
                .addStatement("return schema")
                .build());

        return methodSpecs;
    }
}
