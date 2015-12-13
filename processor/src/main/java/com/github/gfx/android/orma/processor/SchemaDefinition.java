/*
 * Copyright (c) 2015 FUJI Goro (gfx).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.gfx.android.orma.processor;

import com.github.gfx.android.orma.annotation.Column;
import com.github.gfx.android.orma.annotation.Getter;
import com.github.gfx.android.orma.annotation.PrimaryKey;
import com.github.gfx.android.orma.annotation.Setter;
import com.github.gfx.android.orma.annotation.Table;
import com.squareup.javapoet.ClassName;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

public class SchemaDefinition {

    final TypeElement typeElement;

    final ClassName modelClassName;

    final ClassName schemaClassName;

    final ClassName relationClassName;

    final ClassName updaterClassName;

    final ClassName deleterClassName;

    final String tableName;

    final String[] constraints;

    final List<ColumnDefinition> columns;


    public SchemaDefinition(TypeElement typeElement) {
        this.typeElement = typeElement;
        this.modelClassName = ClassName.get(typeElement);

        Table table = typeElement.getAnnotation(Table.class);
        this.constraints = table.constraints();
        this.schemaClassName = helperClassName(table.schemaClassName(), modelClassName, "_Schema");
        this.relationClassName = helperClassName(table.relationClassName(), modelClassName, "_Relation");
        this.updaterClassName = helperClassName(table.updaterClassName(), modelClassName, "_Updater");
        this.deleterClassName = helperClassName(table.deleterClassName(), modelClassName, "_Deleter");
        this.tableName = firstNonEmptyName(table.value(), modelClassName.simpleName());

        this.columns = collectColumns(typeElement);
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
        throw new AssertionError("No non-empty string found");
    }

    static List<ColumnDefinition> collectColumns(TypeElement typeElement) {
        Map<String, Element> getters = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        Map<String, Element> setters = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

        typeElement.getEnclosedElements()
                .stream()
                .forEach(element -> {
                    Getter getter = element.getAnnotation(Getter.class);
                    Setter setter = element.getAnnotation(Setter.class);

                    if (getter != null) {
                        getters.put( extractNameFromGetter(getter, element), element);
                    } else if (setter != null) {
                        setters.put(extractNameFromSetter(setter, element), element);
                    }

                });

        return typeElement.getEnclosedElements()
                .stream()
                .filter(element -> element.getAnnotation(Column.class) != null
                        || element.getAnnotation(PrimaryKey.class) != null)
                .map((element) -> {
                    ColumnDefinition column = new ColumnDefinition(element);
                    column.getter = getters.get(column.columnName);
                    column.setter = setters.get(column.columnName);
                    return column;
                })
                .collect(Collectors.toList());
    }

    private static String extractNameFromGetter(Getter getter, Element element) {
        if (!Strings.isEmpty(getter.value())) {
            return getter.value();
        } else {
            String name = element.getSimpleName().toString();
            if (name.startsWith("get")) {
                return name.substring("get".length());
            } else {
                return name;
            }
        }
    }

    private static String extractNameFromSetter(Setter getter, Element element) {
        if (!Strings.isEmpty(getter.value())) {
            return getter.value();
        } else {
            String name = element.getSimpleName().toString();
            if (name.startsWith("set")) {
                return name.substring("set".length());
            } else {
                return name;
            }
        }
    }

    public TypeElement getElement() {
        return typeElement;
    }

    public String getPackageName() {
        return schemaClassName.packageName();
    }

    public String getTableName() {
        return tableName;
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

    public ClassName getUpdaterClassName() {
        return updaterClassName;
    }

    public ClassName getDeleterClassName() {
        return deleterClassName;
    }

    public List<ColumnDefinition> getColumns() {
        return columns;
    }

    public List<ColumnDefinition> getColumnsWithoutAutoId() {
        return columns.stream().filter(c -> !c.autoId).collect(Collectors.toList());
    }

    public ColumnDefinition getPrimaryKey() {
        for (ColumnDefinition c : columns) {
            if (c.primaryKey) {
                return c;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return getModelClassName().simpleName();
    }
}
