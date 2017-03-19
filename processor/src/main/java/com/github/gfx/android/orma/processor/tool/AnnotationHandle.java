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

package com.github.gfx.android.orma.processor.tool;

import com.squareup.javapoet.TypeName;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;

public class AnnotationHandle<T extends Annotation> {

    public final Element element;

    final Class<T> annotationClass;

    final AnnotationMirror mirror;

    public AnnotationHandle(Element element, Class<T> annotationClass, AnnotationMirror mirror) {
        this.element = element;
        this.annotationClass = annotationClass;
        this.mirror = mirror;
    }

    public static <A extends Annotation> Optional<AnnotationHandle<A>> find(Element element, Class<A> annotation) {
        return element.getAnnotationMirrors()
                .stream()
                .filter(annotationMirror -> {
                    TypeElement annotationTypeElement = (TypeElement) annotationMirror.getAnnotationType().asElement();
                    return annotationTypeElement.getQualifiedName().contentEquals(annotation.getName());
                })
                .map(annotationMirror -> new AnnotationHandle<>(element, annotation, annotationMirror))
                .findFirst();
    }

    public static <A extends Annotation, C extends Annotation> Stream<AnnotationHandle<A>> findRepeatable(Element element,
            Class<A> annotation, Class<C> container) {
        AnnotationHandle<C> c = AnnotationHandle.find(element, container).get();
        return c.getValues("value", AnnotationMirror.class)
                .map(m -> new AnnotationHandle<>(element, annotation, m));
    }

    public <V> Optional<V> get(String name, Class<V> t) {
        return mirror.getElementValues()
                .entrySet()
                .stream()
                .filter(entry -> entry.getKey().getSimpleName().contentEquals(name))
                .map(entry -> (AnnotationValue) entry.getValue())
                .map(annotationValue -> t.cast(annotationValue.getValue()))
                .findFirst();
    }

    public <V> V getOrDefault(String name, Class<V> t) {
        return get(name, t).orElseGet(() -> t.cast(getDefaultValue(name)));
    }

    public TypeName getValueAsTypeName(String name) {
        return TypeName.get(getOrDefault(name, TypeMirror.class));
    }

    public <V> Stream<V> getValues(String name, Class<V> valueType) {
        @SuppressWarnings("unchecked")
        List<AnnotationValue> values = get(name, List.class)
                .orElseGet(() -> Arrays.asList((Class[])getDefaultValue(name)));
        return values.stream().map(v -> valueType.cast(v.getValue()));
    }

    @SuppressWarnings("unchecked")
    private Object getDefaultValue(String name) {
        Method m;
        try {
            m = annotationClass.getMethod(name);
        } catch (NoSuchMethodException e) {
            throw new AssertionError(e);
        }
        return m.getDefaultValue();
    }
}
