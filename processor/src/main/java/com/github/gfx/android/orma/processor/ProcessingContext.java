package com.github.gfx.android.orma.processor;

import com.squareup.javapoet.ClassName;

import java.util.Map;

import javax.annotation.processing.ProcessingEnvironment;

public class ProcessingContext {
    public final ProcessingEnvironment processingEnv;

    public final Map<ClassName, SchemaDefinition> schemaMap;

    public ProcessingContext(ProcessingEnvironment processingEnv,
            Map<ClassName, SchemaDefinition> schemaMap) {
        this.processingEnv = processingEnv;
        this.schemaMap = schemaMap;
    }

    public String getPackageName() {
        for (SchemaDefinition schema : schemaMap.values()) {
            return schema.getPackageName();
        }
        throw new RuntimeException("No schema defined");
    }
}
