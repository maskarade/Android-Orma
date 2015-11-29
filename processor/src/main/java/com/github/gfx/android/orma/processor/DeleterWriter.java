package com.github.gfx.android.orma.processor;

import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Modifier;

public class DeleterWriter extends BaseWriter {

    private final SchemaDefinition schema;

    public DeleterWriter(SchemaDefinition schema, ProcessingEnvironment processingEnv) {
        super(processingEnv);
        this.schema = schema;
    }

    @Override
    public TypeSpec buildTypeSpec() {
        TypeSpec.Builder classBuilder = TypeSpec.classBuilder(schema.getDeleterClassName().simpleName());
        classBuilder.addModifiers(Modifier.PUBLIC);
        classBuilder.superclass(Types.getDeleter(schema.getModelClassName(), schema.getDeleterClassName()));

        classBuilder.addMethods(buildMethodSpecs());

        return classBuilder.build();
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
