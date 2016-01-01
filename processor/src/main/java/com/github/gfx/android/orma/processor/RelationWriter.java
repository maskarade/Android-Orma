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

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.util.ArrayList;
import java.util.List;

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
    public TypeSpec buildTypeSpec() {
        TypeSpec.Builder classBuilder = TypeSpec.classBuilder(getTargetClassName().simpleName());
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
                .addAnnotation(Override.class)
                .returns(getTargetClassName())
                .addStatement("return new $T(this)", getTargetClassName())
                .build());

        methodSpecs.add(MethodSpec.methodBuilder("selector")
                .addAnnotation(Specs.overrideAnnotationSpec())
                .addAnnotation(Specs.nonNullAnnotationSpec())
                .addModifiers(Modifier.PUBLIC)
                .returns(schema.getSelectorClassName())
                .addStatement("return new $T(this)", schema.getSelectorClassName())
                .build());

        methodSpecs.add(MethodSpec.methodBuilder("updater")
                .addAnnotation(Specs.overrideAnnotationSpec())
                .addAnnotation(Specs.nonNullAnnotationSpec())
                .addModifiers(Modifier.PUBLIC)
                .returns(schema.getUpdaterClassName())
                .addStatement("return new $T(this)", schema.getUpdaterClassName())
                .build());

        methodSpecs.add(MethodSpec.methodBuilder("deleter")
                .addAnnotation(Specs.overrideAnnotationSpec())
                .addAnnotation(Specs.nonNullAnnotationSpec())
                .addModifiers(Modifier.PUBLIC)
                .returns(schema.getDeleterClassName())
                .addStatement("return new $T(this)", schema.getDeleterClassName())
                .build());

        methodSpecs.add(MethodSpec.methodBuilder("groupBy")
                .addAnnotation(Specs.overrideAnnotationSpec())
                .addModifiers(Modifier.PUBLIC)
                .returns(schema.getSelectorClassName())
                .addParameter(ParameterSpec.builder(Types.String, "groupBy")
                        .addAnnotation(Specs.nonNullAnnotationSpec())
                        .build())
                .addStatement("return selector().groupBy(groupBy)")
                .build());

        methodSpecs.add(MethodSpec.methodBuilder("having")
                .addAnnotation(Specs.overrideAnnotationSpec())
                .addModifiers(Modifier.PUBLIC)
                .returns(schema.getSelectorClassName())
                .addParameter(ParameterSpec.builder(Types.String, "having")
                        .addAnnotation(Specs.nonNullAnnotationSpec())
                        .build())
                .varargs()
                .addParameter(ParameterSpec.builder(Types.ObjectArray, "args")
                        .addAnnotation(Specs.nonNullAnnotationSpec())
                        .build())
                .addStatement("return selector().having(having, args)")
                .build());

        methodSpecs.add(MethodSpec.methodBuilder("orderBy")
                .addAnnotation(Specs.overrideAnnotationSpec())
                .addAnnotation(Specs.suppressWarningsAnnotation("unchecked", "varargs"))
                .addModifiers(Modifier.PUBLIC)
                .returns(schema.getRelationClassName())
                .addParameter(ParameterSpec.builder(Types.getOrderSpecArray(schema.getModelClassName()), "orderSpecs")
                        .addAnnotation(Specs.nonNullAnnotationSpec())
                        .build())
                .varargs()
                .addStatement("$T.addAll(this.orderSpecs, orderSpecs)", Types.Collections)
                .addStatement("return this")
                .build());

        methodSpecs.add(MethodSpec.methodBuilder("limit")
                .addAnnotation(Specs.overrideAnnotationSpec())
                .addModifiers(Modifier.PUBLIC)
                .returns(schema.getSelectorClassName())
                .addParameter(ParameterSpec.builder(TypeName.LONG, "limit")
                        .build())
                .addStatement("return selector().limit(limit)")
                .build());

        methodSpecs.add(MethodSpec.methodBuilder("offset")
                .addAnnotation(Specs.overrideAnnotationSpec())
                .addModifiers(Modifier.PUBLIC)
                .returns(schema.getSelectorClassName())
                .addParameter(ParameterSpec.builder(TypeName.LONG, "offset")
                        .build())
                .addStatement("return selector().offset(offset)")
                .build());

        methodSpecs.add(MethodSpec.methodBuilder("page")
                .addAnnotation(Specs.overrideAnnotationSpec())
                .addModifiers(Modifier.PUBLIC)
                .returns(schema.getSelectorClassName())
                .addParameter(ParameterSpec.builder(TypeName.LONG, "page")
                        .build())
                .addStatement("return selector().page(page)")
                .build());

        methodSpecs.add(MethodSpec.methodBuilder("per")
                .addAnnotation(Specs.overrideAnnotationSpec())
                .addModifiers(Modifier.PUBLIC)
                .returns(schema.getSelectorClassName())
                .addParameter(ParameterSpec.builder(TypeName.LONG, "per")
                        .addAnnotation(Specs.nonNullAnnotationSpec())
                        .build())
                .addStatement("return selector().per(per)")
                .build());

        methodSpecs.addAll(conditionQueryHelpers.buildConditionHelpers());

        return methodSpecs;
    }
}
