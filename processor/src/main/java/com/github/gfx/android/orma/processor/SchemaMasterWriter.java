package com.github.gfx.android.orma.processor;

import com.squareup.javapoet.TypeSpec;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.processing.ProcessingEnvironment;

public class SchemaMasterWriter {
    static final String kClassName = "Schemas";

    final ProcessingEnvironment processingEnv;

    List<Schema> schemas = new ArrayList<>();

    public SchemaMasterWriter(ProcessingEnvironment processingEnv) {
        this.processingEnv = processingEnv;
    }

    public void add(Schema schema) {
        schemas.add(schema);
    }

    public boolean isRequired() {
        return schemas.size() > 0;
    }

    public String getPackageName() {
        assert isRequired();

        return schemas.get(0).getPackageName();
    }

    public TypeSpec buildTypeSpec() {
        assert isRequired();

        return TypeSpec.classBuilder(kClassName)
                .addAnnotation(Specs.buildGeneratedAnnotationSpec())
                .build();
    }
}
