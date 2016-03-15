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

import com.github.gfx.android.orma.annotation.Table;
import com.github.gfx.android.orma.processor.model.ColumnDefinition;
import com.github.gfx.android.orma.processor.model.SchemaDefinition;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;

public class SchemaValidator {

    final ProcessingContext context;

    final SchemaDefinition schema;

    public static void validate(ProcessingContext context, SchemaDefinition schema) {
        new SchemaValidator(context, schema).run();
    }

    public SchemaValidator(ProcessingContext context, SchemaDefinition schema) {
        this.context = context;
        this.schema = schema;
    }

    public void run() {
        validateAtLeastOneColumn();
        validatePrimaryKey();
        validateNames();
        validateNoOrmaModelInInheritance(schema.getTypeElement().getSuperclass());
    }

    private void validateNoOrmaModelInInheritance(TypeMirror type) {
        TypeElement t = context.processingEnv.getElementUtils().getTypeElement(type.toString());
        if (t.getAnnotation(Table.class) != null) {
            error("The superclasses of Orma models are not allowed to have @Table annotation", t);
        }

        if (!t.toString().equals(Object.class.getCanonicalName())) {
            validateNoOrmaModelInInheritance(t.getSuperclass());
        }
    }

    private void validateAtLeastOneColumn() {
        if (schema.getColumns().isEmpty()) {
            error("No @Column nor @PrimaryKey is defined", schema.getTypeElement());
        }
    }

    private void validatePrimaryKey() {
        List<ColumnDefinition> primaryKeys = schema.getColumns()
                .stream().filter(column -> column.primaryKey)
                .collect(Collectors.toList());

        if (primaryKeys.size() > 1) {
            primaryKeys.forEach(column -> {
                error("Multiple @PrimaryKey found, but it must be once", column.element);
            });
        }
    }

    private void validateNames() {
        Map<String, List<ColumnDefinition>> unique = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

        for (ColumnDefinition column : schema.getColumns()) {
            String name = column.columnName;

            List<ColumnDefinition> columns = unique.get(name);
            if (columns == null) {
                columns = new ArrayList<>();
            }
            columns.add(column);
            unique.put(name, columns);
        }

        unique.forEach((name, elements) -> {
            if (elements.size() > 1) {
                elements.forEach(column -> {
                    error("Duplicate column names \"" + name + "\" found", column.element);
                });
            }
        });
    }

    void error(String message, Element element) {
        context.addError(message, element);
    }
}
