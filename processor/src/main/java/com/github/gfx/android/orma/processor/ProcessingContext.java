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

import com.github.gfx.android.orma.annotation.Database;
import com.github.gfx.android.orma.processor.exception.ProcessingException;
import com.github.gfx.android.orma.processor.generator.SqlGenerator;
import com.github.gfx.android.orma.processor.model.DatabaseDefinition;
import com.github.gfx.android.orma.processor.model.SchemaDefinition;
import com.github.gfx.android.orma.processor.model.TypeAdapterDefinition;
import com.github.gfx.android.orma.processor.util.SqlTypes;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;

import android.support.annotation.Nullable;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.SimpleTypeVisitor8;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

public class ProcessingContext {

    public final ProcessingEnvironment processingEnv;

    public final Types typeUtils;

    public final List<ProcessingException> errors = new ArrayList<>();

    public final List<DatabaseDefinition> databases = new ArrayList<>();

    public final Map<TypeName, SchemaDefinition> schemaMap = new LinkedHashMap<>(); // the order matters

    public final SqlGenerator sqlg = new SqlGenerator();

    private final Map<TypeName, TypeAdapterDefinition> typeAdapterMap = new HashMap<>();

    public ProcessingContext(ProcessingEnvironment processingEnv) {
        this.processingEnv = processingEnv;
        typeUtils = processingEnv.getTypeUtils();
        for (TypeAdapterDefinition typeAdapterDefinition : TypeAdapterDefinition.BUILTINS) {
            addTypeAdapterDefinition(typeAdapterDefinition);
        }
    }

    public void addError(String message, Element element) {
        addError(new ProcessingException(message, element));
    }

    public void addError(ProcessingException e) {
        errors.add(e);
    }

    public void printErrors() {
        Messager messager = processingEnv.getMessager();

        errors.forEach(error -> {
            if (error.getCause() != null) {
                StringWriter s = new StringWriter();
                s.append(error.getMessage());
                s.append('\n');
                error.getCause().printStackTrace(new PrintWriter(s));
                messager.printMessage(Diagnostic.Kind.ERROR, s.toString(), error.element);
            } else {
                messager.printMessage(Diagnostic.Kind.ERROR, error.getMessage(), error.element);
            }
        });
    }

    public Elements getElements() {
        return processingEnv.getElementUtils();
    }

    public void addDatabaseDefinition(DatabaseDefinition databaseDefinition) {
        databases.add(databaseDefinition);
    }

    public void addTypeAdapterDefinition(TypeAdapterDefinition typeAdapterDefinition) {
        // warn if non-built-in type adapters are overridden
        TypeAdapterDefinition previous = typeAdapterMap.get(typeAdapterDefinition.targetType);
        if (previous != null && !previous.builtin) {
            warn("Duplicated @StaticTypeAdapter for " + typeAdapterDefinition.targetType, typeAdapterDefinition.element);
        }
        typeAdapterMap.put(typeAdapterDefinition.targetType, typeAdapterDefinition);
    }

    public SchemaDefinition getSchemaDef(TypeName modelClassName) {
        return schemaMap.get(modelClassName);
    }

    public TypeMirror getTypeMirrorOf(Type type) {
        return getTypeElement(type).asType();
    }

    public TypeElement getTypeElement(CharSequence fqName) {
        TypeElement typeElement = processingEnv.getElementUtils().getTypeElement(fqName);
        if (typeElement == null) {
            throw new RuntimeException("No such class: " + fqName);
        }
        return typeElement;
    }

    public TypeElement getTypeElement(TypeMirror typeMirror) {
        return typeMirror.accept(new SimpleTypeVisitor8<TypeElement, TypeMirror>() {
            @Override
            public TypeElement visitDeclared(DeclaredType declaredType, TypeMirror typeMirror) {
                return (TypeElement) declaredType.asElement();
            }
        }, typeMirror);
    }

    public TypeElement getTypeElement(Type name) {
        return getTypeElement(name.getTypeName());
    }

    public TypeAdapterDefinition getTypeAdapter(TypeName typeName) {
        return typeAdapterMap.get(typeName);
    }

    @Nullable
    public TypeAdapterDefinition findTypeAdapter(TypeMirror typeMirror) {
        if (typeMirror.getKind().isPrimitive()) {
            return null;
        }

        TypeName typeName = TypeName.get(typeMirror);
        if (SqlTypes.canHandle(typeName)) {
            return null;
        }

        TypeAdapterDefinition typeAdapter = typeAdapterMap.get(typeName);
        if (typeAdapter != null) {
            return typeAdapter;
        }

        // find the best matched one
        Map<TypeName, Integer> classHierarchyMap = buildClassHierarchyMap(typeMirror);
        if (classHierarchyMap == null) {
            return null; // no corresponding type element, e.g. byte[]
        }
        return typeAdapterMap.keySet()
                .stream()
                .filter(classHierarchyMap::containsKey)
                .sorted((a, b) -> classHierarchyMap.get(b) - classHierarchyMap.get(a))
                .findFirst()
                .map(typeAdapterMap::get)
                .orElse(null);
    }

    private Map<TypeName, Integer> buildClassHierarchyMap(TypeMirror typeMirror) {
        TypeElement element = (TypeElement) typeUtils.asElement(typeMirror);
        if (element == null) {
            return null;
        }
        Map<TypeName, Integer> map = new HashMap<>();
        int distance = 0;
        while (!element.toString().equals(Object.class.getName())) {
            map.put(ClassName.get(element), distance);
            collectInterfaces(map, element, ++distance);

            element = (TypeElement) typeUtils.asElement(element.getSuperclass());
        }
        return map;
    }

    private void collectInterfaces(Map<TypeName, Integer> map, TypeElement element, int distance) {
        element.getInterfaces().forEach((interfaceMirror) -> {
            TypeElement interfaceElement = (TypeElement) typeUtils.asElement(interfaceMirror);
            map.put(ClassName.get(interfaceElement), distance);

            collectInterfaces(map, interfaceElement, distance + 1);
        });

    }


    public boolean isSameType(TypeMirror t1, TypeMirror t2) {
        return typeUtils.isSameType(t1, t2);
    }

    public void setupDefaultDatabaseIfNeeded() {
        if (databases.isEmpty()) {
            SchemaDefinition schema = getFirstSchema();
            databases.add(new DatabaseDefinition(this,
                    schema.getPackageName(),
                    Database.DEFAULT_DATABASE_CLASS_NAME,
                    Database.DEFAULT_RX_JAVA_SUPPORT));
        }
    }

    public SchemaDefinition getFirstSchema() {
        return schemaMap.values().iterator().next();
    }

    public boolean isRxJavaSupport(SchemaDefinition schema) {
        if (databases.isEmpty()) {
            return Database.DEFAULT_RX_JAVA_SUPPORT;
        }
        return databases
                .stream()
                .anyMatch((database) -> database.isRxJavaSupport() && database.getSchemas().contains(schema));
    }

    public void note(String message) {
        processingEnv.getMessager()
                .printMessage(Diagnostic.Kind.NOTE, "[" + OrmaProcessor.TAG + "] " + message);
    }

    public void warn(String message, Element element) {
        processingEnv.getMessager()
                .printMessage(Diagnostic.Kind.WARNING, "[" + OrmaProcessor.TAG + "] " + message, element);
    }

}
