package com.github.gfx.android.orma.processor;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

public class SchemaValidator {

    public static class ValidationException extends RuntimeException {

        final Element element;

        public ValidationException(Element element, Throwable cause) {
            super("Invalid type element", cause);
            this.element = element;
        }

        public Element getElement() {
            return element;
        }
    }

    public SchemaValidator() {
    }

    public TypeElement validate(Element element) {
        TypeElement typeElement;
        try {
            typeElement = (TypeElement) element;

            // TODO
        } catch (Exception e) {
            throw new ValidationException(element, e);
        }
        return typeElement;
    }
}
