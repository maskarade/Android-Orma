package com.github.gfx.android.orma.processor;

import com.squareup.javapoet.ClassName;

import javax.lang.model.element.TypeElement;

public class SchemaDefinition {

    final TypeElement typeElement;

    final ClassName modelClassName;

    final ClassName schemaClassName;

    final ClassName relationClassName;

    public SchemaDefinition(TypeElement typeElement) {
        this.typeElement = typeElement;
        this.modelClassName = ClassName.get(typeElement);
        this.schemaClassName = helperClassName(modelClassName, "_Schema");
        this.relationClassName = helperClassName(modelClassName, "_Relation");
    }

    public TypeElement getElement() {
        return typeElement;
    }

    public String getPackageName() {
        return schemaClassName.packageName();
    }

    public ClassName getModelClassName() {
        return modelClassName;
    }

    public ClassName getSchemaClassName() {
        return schemaClassName;
    }

    public ClassName getRelationClassName() {
        return relationClassName;
    }

    private static ClassName helperClassName(ClassName modelClassName, String helperSuffix) {
        return ClassName.get(modelClassName.packageName(), modelClassName.simpleName() + helperSuffix);
    }

    @Override
    public String toString() {
        return getModelClassName().simpleName();
    }
}
