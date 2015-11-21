package com.github.gfx.android.orma.processor;

import com.github.gfx.android.orma.annotation.PrimaryKey;

import java.util.List;
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
    }

    private void validatePrimaryKey(TypeElement typeElement) {
        List<Element> elements = typeElement.getEnclosedElements().stream()
                .filter(element -> element.getAnnotation(PrimaryKey.class) != null)
                .collect(Collectors.toList());

        if (elements.size() > 1) {
            elements.forEach(element -> {
                processingEnv.getMessager()
                        .printMessage(Diagnostic.Kind.ERROR,
                                "[" + OrmaProcessor.TAG + "] Multiple @PrimaryKey found",
                                element);
            });
        }
    }
}
