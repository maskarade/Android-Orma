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

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;

/**
 * Utilities for {@link AnnotationMirror}
 */
public class Mirrors {

    @Nullable
    public static AnnotationMirror findAnnotationMirror(Element element, Class<? extends Annotation> annotationClass) {
        for (AnnotationMirror annotationMirror : element.getAnnotationMirrors()) {
            DeclaredType declaredType = annotationMirror.getAnnotationType();
            TypeElement typeElement = (TypeElement) declaredType.asElement();
            if (typeElement.getQualifiedName().contentEquals(annotationClass.getName())) {
                return annotationMirror;
            }
        }
        return null;
    }

    @Nullable
    public static AnnotationValue findAnnotationValue(AnnotationMirror annotation, String name) {
        for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry
                : annotation.getElementValues().entrySet()) {
            if (entry.getKey().getSimpleName().contentEquals(name)) {
                return entry.getValue();
            }
        }
        return null;
    }

    @NonNull
    public static TypeMirror findAnnotationValueAsTypeMirror(AnnotationMirror annotation, String name) {
        AnnotationValue value = findAnnotationValue(annotation, name);
        if (value != null) {
            return (TypeMirror) value.getValue();
        }
        throw new AssertionError("No annotation value for " + annotation.getAnnotationType() + "#" + name + "()");
    }

    @NonNull
    public static TypeName findAnnotationValueAsType(AnnotationMirror annotation, String name) {
        return TypeName.get(findAnnotationValueAsTypeMirror(annotation, name));
    }

    @SuppressWarnings("unchecked")
    @NonNull
    public static List<AnnotationValue> findAnnotationValueAsAnnotationValues(AnnotationMirror annotation, String name) {
        AnnotationValue value = findAnnotationValue(annotation, name);
        if (value != null) {
            return (List<AnnotationValue>) value.getValue();
        }
        return Collections.emptyList();
    }

    @NonNull
    public static List<TypeName> findAnnotationValueAsTypes(AnnotationMirror annotation, String name) {
        List<TypeName> typeNames = new ArrayList<>();
        for (AnnotationValue value : findAnnotationValueAsAnnotationValues(annotation, name)) {
            typeNames.add(TypeName.get((TypeMirror)value.getValue()));
        }
        return typeNames;
    }

    @Nullable
    public static String findAnnotationValueAsStringOrNull(AnnotationMirror annotation, String name) {
        AnnotationValue value = findAnnotationValue(annotation, name);
        if (value != null) {
            return (String) value.getValue();
        }
        return null;
    }

    @NonNull
    public static String findAnnotationValueAsString(AnnotationMirror annotation, String name) {
        String value = findAnnotationValueAsStringOrNull(annotation, name);
        if (value != null) {
            return value;
        }
        throw new AssertionError("No annotation value for " + annotation.getAnnotationType() + "#" + name + "()");
    }
}
