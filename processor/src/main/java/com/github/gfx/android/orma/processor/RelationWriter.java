package com.github.gfx.android.orma.processor;

import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Modifier;

public class RelationWriter {

    private final SchemaDefinition schema;

    private final ProcessingEnvironment processingEnv;

    public RelationWriter(SchemaDefinition schema, ProcessingEnvironment processingEnv) {
        this.schema = schema;
        this.processingEnv = processingEnv;
    }

    public TypeSpec buildTypeSpec() {
        TypeSpec.Builder classBuilder = TypeSpec.classBuilder(schema.getRelationClassName().simpleName());
        classBuilder.addModifiers(Modifier.PUBLIC);
        classBuilder.superclass(Types.getRelation(schema.getModelClassName(), schema.getRelationClassName()));

        classBuilder.addFields(buildFieldSpecs());
        classBuilder.addMethods(buildMethodSpecs());

        return classBuilder.build();
    }

    public List<FieldSpec> buildFieldSpecs() {
        List<FieldSpec> fieldSpecs = new ArrayList<>();
        // TODO
        return fieldSpecs;
    }

    public List<MethodSpec> buildMethodSpecs() {
        List<MethodSpec> methodSpecs = new ArrayList<>();

        methodSpecs.add(MethodSpec.constructorBuilder()
                .addParameter(Types.OrmaConnection, "orma")
                .addParameter(schema.getSchemaClassName(), "schema")
                .addCode("super(orma, schema);\n")
                .build());

        return methodSpecs;
    }
}
