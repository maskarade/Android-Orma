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

package com.github.gfx.android.orma.processor.model;

import com.github.gfx.android.orma.annotation.Database;
import com.github.gfx.android.orma.processor.ProcessingContext;
import com.github.gfx.android.orma.processor.tool.AnnotationHandle;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;

public class DatabaseDefinition {

    final ProcessingContext context;

    final TypeElement element;

    final String packageName;

    final ClassName className;

    final Set<TypeName> includes;

    final Set<TypeName> excludes;

    public DatabaseDefinition(ProcessingContext context, TypeElement element) {
        this.context = context;
        this.element = element;
        AnnotationHandle<Database> database = AnnotationHandle.find(element, Database.class).get();

        packageName = context.getElements().getPackageOf(element).toString();
        String name = database.getOrDefault("databaseClassName", String.class);
        className = ClassName.get(packageName, name);

        includes = database.getValues("includes", TypeMirror.class)
                .map(ClassName::get)
                .collect(Collectors.toSet());

        excludes = database.getValues("excludes", TypeMirror.class)
                .map(ClassName::get)
                .collect(Collectors.toSet());
    }

    public DatabaseDefinition(ProcessingContext context, String packageName, String className) {
        this.context = context;
        this.element = null;
        this.packageName = packageName;
        this.className = ClassName.get(packageName, className);
        this.includes = Collections.emptySet();
        this.excludes = Collections.emptySet();
    }

    public Optional<TypeElement> getElement() {
        return Optional.ofNullable(element);
    }

    public List<SchemaDefinition> getSchemas() {
        Stream<SchemaDefinition> stream = context.schemaMap.values().stream();

        if (!includes.isEmpty()) {
            stream = stream.filter(schema -> includes.contains(schema.getModelClassName()));
        }
        if (!excludes.isEmpty()) {
            stream = stream.filter(schema -> !excludes.contains(schema.getModelClassName()));
        }
        return stream.collect(Collectors.toList());
    }

    public String getPackageName() {
        return packageName;
    }

    public ClassName getClassName() {
        return className;
    }
}
