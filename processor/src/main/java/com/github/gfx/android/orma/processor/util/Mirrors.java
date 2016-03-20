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

package com.github.gfx.android.orma.processor.util;

import com.squareup.javapoet.TypeName;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;

/**
 * Utilities for {@link AnnotationMirror}
 */
public class Mirrors {

    public static Optional<? extends AnnotationMirror> findAnnotationMirror(Element element, Class<? extends Annotation> annotation) {
        return element.getAnnotationMirrors()
                .stream()
                .filter(annotationMirror -> {
                    TypeElement annotationTypeElement = (TypeElement) annotationMirror.getAnnotationType().asElement();
                    return annotationTypeElement.getQualifiedName().contentEquals(annotation.getName());
                })
                .findFirst();
    }

    public static Optional<? extends AnnotationValue> findAnnotationValue(AnnotationMirror annotation, String name) {
        return annotation.getElementValues()
                .entrySet()
                .stream()
                .filter(entry -> entry.getKey().getSimpleName().contentEquals(name))
                .map(Map.Entry::getValue)
                .findFirst();
    }

    public static Optional<TypeMirror> findAnnotationValueAsTypeMirror(AnnotationMirror annotation, String name) {
        return findAnnotationValue(annotation, name)
                .map(annotationValue -> (TypeMirror) annotationValue.getValue());
    }

    public static Optional<TypeName> findAnnotationValueAsType(AnnotationMirror annotation, String name) {
        return findAnnotationValueAsTypeMirror(annotation, name)
                .map(TypeName::get);
    }

    @SuppressWarnings("unchecked")
    public static List<AnnotationValue> findAnnotationValueAsAnnotationValues(AnnotationMirror annotation, String name) {
        return findAnnotationValue(annotation, name)
                .map(annotationValue -> (List<AnnotationValue>) annotationValue.getValue())
                .orElse(Collections.emptyList());
    }

    public static Stream<TypeMirror> findAnnotationValueAsTypeMirrors(AnnotationMirror annotation, String name) {
        return findAnnotationValueAsAnnotationValues(annotation, name)
                .stream()
                .map(value -> (TypeMirror) value.getValue());
    }

    public static Stream<TypeName> findAnnotationValueAsTypes(AnnotationMirror annotation, String name) {
        return findAnnotationValueAsTypeMirrors(annotation, name)
                .map(TypeName::get);
    }

    public static Optional<String> findAnnotationValueAsString(AnnotationMirror annotation, String name) {
        return findAnnotationValue(annotation, name)
                .map(annotationValue -> (String) annotationValue.getValue());
    }
}
