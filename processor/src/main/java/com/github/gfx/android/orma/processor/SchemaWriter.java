package com.github.gfx.android.orma.processor;

import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Modifier;

public class SchemaWriter {

    private final Schema schema;

    private final ProcessingEnvironment processingEnv;

    public SchemaWriter(Schema schema, ProcessingEnvironment processingEnv) {

        this.schema = schema;
        this.processingEnv = processingEnv;
    }

    public TypeSpec buildTypeSpec() {
        TypeSpec.Builder classBuilder = TypeSpec.classBuilder(schema.getSchemaClassName());
        classBuilder.addModifiers(Modifier.PUBLIC);
        classBuilder.addAnnotation(Specs.buildGeneratedAnnotationSpec());

        classBuilder.addMethods(buildConstructorSpecs());
        classBuilder.addMethods(buildMethodSpecs());

        return classBuilder.build();
    }

    public List<MethodSpec> buildConstructorSpecs() {
        List<MethodSpec> constructors = new ArrayList<>();

        constructors.add(MethodSpec.constructorBuilder()
              //  .addParameter(Types.SQLiteDatabase, "db")
                .build());

        return constructors;
    }

    public List<MethodSpec> buildMethodSpecs() {
        return new ArrayList<>();
    }
}
