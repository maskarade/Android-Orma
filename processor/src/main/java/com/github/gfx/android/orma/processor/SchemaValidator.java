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
import com.github.gfx.android.orma.annotation.PrimaryKey;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Collectors;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

public class SchemaValidator {

    final ProcessingContext context;

    public SchemaValidator(ProcessingContext context) {
        this.context = context;
    }

    public TypeElement validate(Element element) {
        TypeElement typeElement = (TypeElement) element;
        validateTypeElement(typeElement);
        return typeElement;
    }

    private void validateTypeElement(TypeElement typeElement) {
        validateAtLeastOneColumn(typeElement);
        validatePrimaryKey(typeElement);
        validateNames(typeElement);
    }

    private void validateAtLeastOneColumn(TypeElement typeElement) {
        Optional<? extends Element> any = typeElement.getEnclosedElements().stream()
                .filter(element -> element.getAnnotation(Column.class) != null
                        || element.getAnnotation(PrimaryKey.class) != null)
                .findAny();

        if (!any.isPresent()) {
            error("No @Column nor @PrimaryKey is defined", typeElement);
        }
    }

    private void validatePrimaryKey(TypeElement typeElement) {
        List<Element> elements = typeElement.getEnclosedElements().stream()
                .filter(element -> element.getAnnotation(PrimaryKey.class) != null)
                .collect(Collectors.toList());

        if (elements.size() > 1) {
            elements.forEach(element -> {
                error("Multiple @PrimaryKey found, but it must be once", element);
            });
        }
    }

    private void validateNames(TypeElement typeElement) {
        Map<String, List<Element>> unique = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

        typeElement.getEnclosedElements().stream()
                .filter(element -> element.getAnnotation(Column.class) != null
                        || element.getAnnotation(PrimaryKey.class) != null)
                .forEach(element -> {
                    String name = ColumnDefinition.getColumnName(element);

                    List<Element> elements = unique.get(name);
                    if (elements == null) {
                        elements = new ArrayList<>();
                    }
                    elements.add(element);
                    unique.put(name, elements);
                });

        unique.forEach((name, elements) -> {
            if (elements.size() > 1) {
                elements.forEach(element -> {
                    error("Duplicate column names \"" + name + "\" found", element);
                });
            }
        });
    }

    void error(String message, Element element) {
        context.addError(message, element);
    }
}
