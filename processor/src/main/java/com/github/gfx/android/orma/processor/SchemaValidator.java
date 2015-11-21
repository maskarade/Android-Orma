package com.github.gfx.android.orma.processor;

import com.github.gfx.android.orma.annotation.Column;
import com.github.gfx.android.orma.annotation.PrimaryKey;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;

public class SchemaValidator {

    final ProcessingEnvironment processingEnv;

    public SchemaValidator(ProcessingEnvironment processingEnv) {
        this.processingEnv = processingEnv;
    }

    public TypeElement validate(Element element) {
        TypeElement typeElement = (TypeElement) element;
        validateTypeElement(typeElement);
        return typeElement;
    }

    private void validateTypeElement(TypeElement typeElement) {
        validatePrimaryKey(typeElement);
        validateNames(typeElement);
    }

    private void validatePrimaryKey(TypeElement typeElement) {
        List<Element> elements = typeElement.getEnclosedElements().stream()
                .filter(element -> element.getAnnotation(PrimaryKey.class) != null)
                .collect(Collectors.toList());

        if (elements.size() > 1) {
            elements.forEach(element -> {
                error("Multiple @PrimaryKey found", element);
            });
        }
    }


    private void validateNames(TypeElement typeElement) {
        Map<String, List<Element>> unique = new HashMap<>();

        typeElement.getEnclosedElements().stream()
                .filter(element -> element.getAnnotation(Column.class) != null
                        || element.getAnnotation(PrimaryKey.class) != null)
                .forEach(element -> {
                    Column column = element.getAnnotation(Column.class);
                    String name = null;
                    if (column != null) {
                        if (!Strings.isEmpty(column.value())) {
                            name = column.value();
                        }
                    }
                    if (name == null) {
                        name = element.getSimpleName().toString();
                    }

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
                    error("Duplicated column name \"" + name + "\" found", element);
                });
            }
        });
    }

    void error(String message, Element element) {
        processingEnv.getMessager()
                .printMessage(Diagnostic.Kind.ERROR,
                        "[" + OrmaProcessor.TAG + "] " + message,
                        element);

    }
}
