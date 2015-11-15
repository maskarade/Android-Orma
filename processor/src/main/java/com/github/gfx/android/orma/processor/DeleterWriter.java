package com.github.gfx.android.orma.processor;

import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Modifier;

public class DeleterWriter {

    private final SchemaDefinition schema;

    private final ProcessingEnvironment processingEnv;

    public DeleterWriter(SchemaDefinition schema, ProcessingEnvironment processingEnv) {
        this.schema = schema;
        this.processingEnv = processingEnv;
    }

    public TypeSpec buildTypeSpec() {
        TypeSpec.Builder classBuilder = TypeSpec.classBuilder(schema.getDeleterClassName().simpleName());
        classBuilder.addAnnotation(Specs.buildGeneratedAnnotationSpec());
        classBuilder.addModifiers(Modifier.PUBLIC);
        classBuilder.superclass(Types.getDeleter(schema.getModelClassName(), schema.getDeleterClassName()));

        classBuilder.addFields(buildFieldSpecs());
        classBuilder.addMethods(buildMethodSpecs());

        return classBuilder.build();
    }

    public List<FieldSpec> buildFieldSpecs() {
        List<FieldSpec> fieldSpecs = new ArrayList<>();
        return fieldSpecs;
    }

    public List<MethodSpec> buildMethodSpecs() {
        List<MethodSpec> methodSpecs = new ArrayList<>();

        methodSpecs.add(
                MethodSpec.constructorBuilder()
                        .addParameter(Types.OrmaConnection, "connection")
                        .addParameter(schema.getSchemaClassName(), "schema")
                        .addStatement("super(connection, schema)")
                        .build()
        );

        return methodSpecs;
    }
}
