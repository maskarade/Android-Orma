package com.github.gfx.android.orma.processor;

import com.squareup.javapoet.ClassName;

import javax.lang.model.element.TypeElement;

public class SchemaDefinition {

    final TypeElement typeElement;

    final ClassName modelClassName;

    final ClassName schemaClassName;

    public SchemaDefinition(TypeElement typeElement) {
        this.typeElement = typeElement;
        this.modelClassName = ClassName.get(typeElement);
        this.schemaClassName = ClassName.get(modelClassName.packageName(), modelClassName.simpleName() + "_Schema");
    }

    public TypeElement getElement() {
        return typeElement;
    }

    public String getPackageName() {
        return schemaClassName.packageName();
    }

    public ClassName getSchemaClassName() {
        return schemaClassName;
    }

    public ClassName getModelClassName() {
        return modelClassName;
    }

    @Override
    public String toString() {
        return getModelClassName().simpleName();
    }
}
