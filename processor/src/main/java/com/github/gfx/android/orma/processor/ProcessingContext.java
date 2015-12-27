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

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.TypeName;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;

public class ProcessingContext {

    public final ProcessingEnvironment processingEnv;

    public final List<ProcessingException> errors = new ArrayList<>();

    public final Map<TypeName, SchemaDefinition> schemaMap;

    public ClassName OrmaDatabase;

    public ProcessingContext(ProcessingEnvironment processingEnv) {
        this.processingEnv = processingEnv;
        this.schemaMap = new LinkedHashMap<>(); // the order matters
    }

    public void addError(String message, Element element) {
        addError(new ProcessingException(message, element));
    }

    public void addError(ProcessingException e) {
        errors.add(e);
    }

    public void printErrors() {
        Messager messager = processingEnv.getMessager();

        errors.forEach(error -> messager.printMessage(
                Diagnostic.Kind.ERROR, error.getMessage(), error.element));
    }

    public SchemaDefinition getSchemaDef(TypeName modelClassName) {
        return schemaMap.get(modelClassName);
    }

    public String getPackageName() {
        for (SchemaDefinition schema : schemaMap.values()) {
            return schema.getPackageName();
        }
        throw new RuntimeException("No schema defined");
    }

    public CodeBlock getSchemaInstanceExpr(ClassName modelClassName) {
        return CodeBlock.builder()
                .add("$T.schema$L", OrmaDatabase, modelClassName.simpleName())
                .build();
    }

    public TypeMirror getTypeMirrorOf(Type type) {
        return processingEnv.getElementUtils().getTypeElement(type.getTypeName()).asType();
    }

    public boolean isSameType(TypeMirror t1, TypeMirror t2) {
        return processingEnv.getTypeUtils().isSameType(t1, t2);
    }

    public void initializeOrmaDatabase() {
        if (!schemaMap.isEmpty()) {
            OrmaDatabase = ClassName.get(getPackageName(), DatabaseWriter.kClassName);
        }
    }
}
