package com.github.gfx.android.orma.processor;

import javax.lang.model.element.TypeElement;

public class Schema {

    final TypeElement typeElement;

    public Schema(TypeElement typeElement) {
        this.typeElement = typeElement;
    }

    public TypeElement getElement() {
        return typeElement;
    }

    public String getPackageName() {
        String fqn = typeElement.getQualifiedName().toString();
        return fqn.substring(0, fqn.lastIndexOf('.'));
    }

    public String getSchemaClassName() {
        return typeElement.getSimpleName() + "_Schema";
    }

    public String getModelClassName() {
        return typeElement.getSimpleName().toString();
    }


    @Override
    public String toString() {
        return getModelClassName();
    }
}
