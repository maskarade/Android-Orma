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

import com.github.gfx.android.orma.annotation.StaticTypeAdapter;
import com.github.gfx.android.orma.annotation.Table;
import com.github.gfx.android.orma.annotation.VirtualTable;
import com.squareup.javapoet.JavaFile;

import java.io.IOException;
import java.util.Set;
import java.util.stream.Stream;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedAnnotationTypes({
        "com.github.gfx.android.orma.annotation.*",
})
public class OrmaProcessor extends AbstractProcessor {

    public static final String TAG = OrmaProcessor.class.getSimpleName();

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (annotations.size() == 0) {
            return true;
        }

        ProcessingContext context = new ProcessingContext(processingEnv);

        try {
            buildTypeAdapters(context, roundEnv)
                    .forEach(context::addTypeAdapterDefinition);

            buildTableSchemas(context, roundEnv)
                    .forEach(schema -> context.schemaMap.put(schema.getModelClassName(), schema));

            buildVirtualTableSchemas(context, roundEnv)
                    .peek(schema -> {
                        throw new ProcessingException("@VirtualTable is not yet implemented.", schema.getElement());
                    });

            context.initializeOrmaDatabase();

            context.schemaMap.values().forEach((schema) -> {
                writeCodeForEachModel(schema, new SchemaWriter(context, schema));
                writeCodeForEachModel(schema, new RelationWriter(context, schema));
                writeCodeForEachModel(schema, new SelectorWriter(context, schema));
                writeCodeForEachModel(schema, new UpdaterWriter(context, schema));
                writeCodeForEachModel(schema, new DeleterWriter(context, schema));

            });

            DatabaseWriter databaseWriter = new DatabaseWriter(context);
            if (databaseWriter.isRequired()) {
                writeToFiler(null,
                        JavaFile.builder(databaseWriter.getPackageName(),
                                databaseWriter.buildTypeSpec())
                                .build());
            }

        } catch (ProcessingException e) {
            context.addError(e);
        }

        context.printErrors();

        return false;
    }

    public Stream<TypeAdapterDefinition> buildTypeAdapters(ProcessingContext context, RoundEnvironment roundEnv) {
        return roundEnv
                .getElementsAnnotatedWith(StaticTypeAdapter.class)
                .stream()
                .map(TypeAdapterDefinition::new);
    }

    public Stream<SchemaDefinition> buildTableSchemas(ProcessingContext context, RoundEnvironment roundEnv) {
        SchemaValidator validator = new SchemaValidator(context);
        return roundEnv
                .getElementsAnnotatedWith(Table.class)
                .stream()
                .map(element -> new SchemaDefinition(context, validator.validate(element)));
    }

    public Stream<SchemaDefinition> buildVirtualTableSchemas(ProcessingContext context, RoundEnvironment roundEnv) {
        SchemaValidator validator = new SchemaValidator(context);
        return roundEnv
                .getElementsAnnotatedWith(VirtualTable.class)
                .stream()
                .map(element -> new SchemaDefinition(context, validator.validate(element)));
    }

    public void writeCodeForEachModel(SchemaDefinition schema, BaseWriter writer) {
        writeToFiler(schema.getElement(),
                JavaFile.builder(schema.getPackageName(), writer.buildTypeSpec())
                        .build());
    }

    public void writeToFiler(Element element, JavaFile javaFile) {
        try {
            javaFile.writeTo(processingEnv.getFiler());
        } catch (IOException e) {
            throw new ProcessingException("Failed to write " + javaFile.typeSpec.name + ": " + e, element);
        }
    }
}
