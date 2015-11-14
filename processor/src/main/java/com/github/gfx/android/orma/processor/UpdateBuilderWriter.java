package com.github.gfx.android.orma.processor;

import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeSpec;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Modifier;

public class UpdateBuilderWriter {

    private final SchemaDefinition schema;

    private final ProcessingEnvironment processingEnv;

    public UpdateBuilderWriter(SchemaDefinition schema, ProcessingEnvironment processingEnv) {
        this.schema = schema;
        this.processingEnv = processingEnv;
    }

    public TypeSpec buildTypeSpec() {
        TypeSpec.Builder classBuilder = TypeSpec.classBuilder(schema.getUpdateBuilderClassName().simpleName());
        classBuilder.addAnnotation(Specs.buildGeneratedAnnotationSpec());
        classBuilder.addModifiers(Modifier.PUBLIC);

        classBuilder.addFields(buildFieldSpecs());
        classBuilder.addMethods(buildMethodSpecs());

        return classBuilder.build();
    }

    public List<FieldSpec> buildFieldSpecs() {
        List<FieldSpec> fieldSpecs = new ArrayList<>();
        fieldSpecs.add(
                FieldSpec.builder(Types.ContentValues, "contents", Modifier.FINAL)
                        .initializer("new $T()", Types.ContentValues)
                        .build());
        return fieldSpecs;
    }

    public List<MethodSpec> buildMethodSpecs() {
        List<MethodSpec> methodSpecs = new ArrayList<>();

        schema.getColumnsWithoutAutoId().forEach(column -> {
            RelationDefinition r = column.getRelation();
            if (r == null) {
                methodSpecs.add(
                        MethodSpec.methodBuilder(column.name)
                                .addModifiers(Modifier.PUBLIC)
                                .returns(schema.getUpdateBuilderClassName())
                                .addParameter(
                                        ParameterSpec.builder(column.getType(), column.name)
                                                .build()
                                )
                                .addStatement("this.contents.put($S, $L)", column.columnName, column.name)
                                .addStatement("return this")
                                .build()
                );
            } else {
                // FIXME: in case the column represents a relationship
            }
        });

        methodSpecs.add(MethodSpec.methodBuilder("getContentValues")
                .addModifiers(Modifier.PUBLIC)
                .returns(Types.ContentValues)
                .addStatement("return contents")
                .build());

        return methodSpecs;
    }
}
