package com.github.gfx.android.orma.processor;

import com.squareup.javapoet.TypeName;

import java.util.Map;

import javax.annotation.processing.ProcessingEnvironment;

public class ProcessingContext {
    public final ProcessingEnvironment processingEnv;

    public final Map<TypeName, SchemaDefinition> schemaMap;

    public ProcessingContext(ProcessingEnvironment processingEnv, Map<TypeName, SchemaDefinition> schemaMap) {
        this.processingEnv = processingEnv;
        this.schemaMap = schemaMap;
    }

    public SchemaDefinition getSchemaDef(TypeName modelClassName) {
        return schemaMap.get(modelClassName);
    }

    public String getPackageName() {
        for (SchemaDefinition schema : schemaMap.values()) {
            return schema.getPackageName();
        }
        throw new RuntimeException("No schema defined");
    }
}
