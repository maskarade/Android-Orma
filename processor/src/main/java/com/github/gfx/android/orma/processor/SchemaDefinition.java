package com.github.gfx.android.orma.processor;

import com.github.gfx.android.orma.annotation.Column;
import com.github.gfx.android.orma.annotation.PrimaryKey;
import com.github.gfx.android.orma.annotation.Table;
import com.squareup.javapoet.ClassName;

import java.util.ArrayList;
import java.util.List;

import javax.lang.model.element.TypeElement;

public class SchemaDefinition {

    final TypeElement typeElement;

    final ClassName modelClassName;

    final ClassName schemaClassName;

    final ClassName relationClassName;

    final String tableName;

    final List<ColumnDefinition> columns = new ArrayList<>();

    public SchemaDefinition(TypeElement typeElement) {
        this.typeElement = typeElement;
        this.modelClassName = ClassName.get(typeElement);

        Table table = typeElement.getAnnotation(Table.class);
        this.schemaClassName = helperClassName(table.schemaClassName(), modelClassName, "_Schema");
        this.relationClassName = helperClassName(table.relationClassName(), modelClassName, "_Relation");
        this.tableName = firstNonEmptyName(table.value(), modelClassName.simpleName());

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

    public List<ColumnDefinition> getColumnsWithoutAutoId() {
        List<ColumnDefinition> list = new ArrayList<>(columns.size());
        for (ColumnDefinition c : columns) {
            if (!c.autoId) {
                list.add(c);
            }
        }
        return list;
    }

    private static ClassName helperClassName(String specifiedName, ClassName modelClassName, String helperSuffix) {
        String simpleName = firstNonEmptyName(specifiedName, modelClassName.simpleName() + helperSuffix);
        return ClassName.get(modelClassName.packageName(), simpleName);
    }

    static String firstNonEmptyName(String... names) {
        for (String name : names) {
            if (name != null && !name.equals("")) {
                return name;
            }
        }
        throw new AssertionError("No non-empty string here");
    }

    @Override
    public String toString() {
        return getModelClassName().simpleName();
    }
}
