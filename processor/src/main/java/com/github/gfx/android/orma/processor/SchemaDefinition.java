package com.github.gfx.android.orma.processor;

import com.github.gfx.android.orma.annotation.Column;
import com.github.gfx.android.orma.annotation.PrimaryKey;
import com.squareup.javapoet.ClassName;

import java.util.ArrayList;
import java.util.List;

import javax.lang.model.element.TypeElement;

public class SchemaDefinition {

    final TypeElement typeElement;

    final ClassName modelClassName;

    final ClassName schemaClassName;

    final ClassName relationClassName;

    final List<ColumnDefinition> columns = new ArrayList<>();

    public SchemaDefinition(TypeElement typeElement) {
        this.typeElement = typeElement;
        this.modelClassName = ClassName.get(typeElement);
        this.schemaClassName = helperClassName(modelClassName, "_Schema");
        this.relationClassName = helperClassName(modelClassName, "_Relation");

        typeElement.getEnclosedElements().forEach(element -> {
            if (element.getAnnotation(Column.class) != null || element.getAnnotation(PrimaryKey.class) != null) {
                columns.add(new ColumnDefinition(element));
            }
        });
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

    public List<ColumnDefinition> getColumns() {
        return columns;
    }

    private static ClassName helperClassName(ClassName modelClassName, String helperSuffix) {
        return ClassName.get(modelClassName.packageName(), modelClassName.simpleName() + helperSuffix);
    }

    @Override
    public String toString() {
        return getModelClassName().simpleName();
    }
}
