package com.github.gfx.android.orma.processor;

import com.squareup.javapoet.TypeName;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.tools.Diagnostic;

public class ProcessingContext {
    public final ProcessingEnvironment processingEnv;

    public final List<ProcessingException> errors = new ArrayList<>();

    public final Map<TypeName, SchemaDefinition> schemaMap;

    public ProcessingContext(ProcessingEnvironment processingEnv, Map<TypeName, SchemaDefinition> schemaMap) {
        this.processingEnv = processingEnv;
        this.schemaMap = schemaMap;
    }

    public void addError(String message, Element element) {
        addError(new ProcessingException(message, element));
    }

    public void addError(ProcessingException e) {
        errors.add(e);
    }

    public void printErrors() {
        Messager messager = processingEnv.getMessager();

        errors.forEach(error -> messager.printMessage(
                Diagnostic.Kind.ERROR, error.getMessage(), error.element));
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
