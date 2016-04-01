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
import com.squareup.javapoet.TypeName;

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
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;

public class ProcessingContext {

    public final ProcessingEnvironment processingEnv;

    public final List<ProcessingException> errors = new ArrayList<>();

    public final List<DatabaseDefinition> databases = new ArrayList<>();

    public final Map<TypeName, SchemaDefinition> schemaMap;

    public final Map<TypeName, TypeAdapterDefinition> typeAdapterMap;

    public final SqlGenerator sqlg;

    public ProcessingContext(ProcessingEnvironment processingEnv) {
        this.processingEnv = processingEnv;
        this.schemaMap = new LinkedHashMap<>(); // the order matters
        this.typeAdapterMap = new HashMap<>();
        this.sqlg = new SqlGenerator();
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
        typeAdapterMap.put(typeAdapterDefinition.targetType, typeAdapterDefinition);
    }

    public SchemaDefinition getSchemaDef(TypeName modelClassName) {
        return schemaMap.get(modelClassName);
    }

    public TypeMirror getTypeMirrorOf(Type type) {
        return getTypeElement(type).asType();
    }

    public TypeElement getTypeElement(CharSequence name) {
        return processingEnv.getElementUtils().getTypeElement(name);
    }

    public TypeElement getTypeElement(TypeMirror name) {
        return getTypeElement(name.toString());
    }

    public TypeElement getTypeElement(Type name) {
        return getTypeElement(name.getTypeName());
    }

    public boolean isSameType(TypeMirror t1, TypeMirror t2) {
        return processingEnv.getTypeUtils().isSameType(t1, t2);
    }

    public void setupDefaultDatabaseIfNeeded() {
        if (databases.isEmpty()) {
            SchemaDefinition schema = getFirstSchema();
            databases.add(new DatabaseDefinition(this, schema.getPackageName(), Database.DEFAULT_DATABASE_CLASS_NAME));
        }
    }

    SchemaDefinition getFirstSchema() {
        return schemaMap.values().iterator().next();
    }

    public void note(String message) {
        processingEnv.getMessager()
                .printMessage(Diagnostic.Kind.NOTE, "[" + OrmaProcessor.TAG + "] " + message);
    }
}
