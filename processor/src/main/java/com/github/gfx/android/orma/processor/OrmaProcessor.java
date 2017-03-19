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
import com.github.gfx.android.orma.annotation.StaticTypeAdapter;
import com.github.gfx.android.orma.annotation.StaticTypeAdapters;
import com.github.gfx.android.orma.annotation.Table;
import com.github.gfx.android.orma.annotation.VirtualTable;
import com.github.gfx.android.orma.processor.exception.ProcessingException;
import com.github.gfx.android.orma.processor.generator.BaseWriter;
import com.github.gfx.android.orma.processor.generator.DatabaseWriter;
import com.github.gfx.android.orma.processor.generator.DeleterWriter;
import com.github.gfx.android.orma.processor.generator.RelationWriter;
import com.github.gfx.android.orma.processor.generator.SchemaWriter;
import com.github.gfx.android.orma.processor.generator.SelectorWriter;
import com.github.gfx.android.orma.processor.generator.UpdaterWriter;
import com.github.gfx.android.orma.processor.model.DatabaseDefinition;
import com.github.gfx.android.orma.processor.model.SchemaDefinition;
import com.github.gfx.android.orma.processor.model.TypeAdapterDefinition;
import com.github.gfx.android.orma.processor.tool.AnnotationHandle;
import com.github.gfx.android.orma.processor.tool.SynchronizedFiler;
import com.squareup.javapoet.JavaFile;

import java.util.Set;
import java.util.concurrent.TimeUnit;
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
        "com.github.gfx.android.orma.annotation.Table",
        "com.github.gfx.android.orma.annotation.VirtualTable",
        "com.github.gfx.android.orma.annotation.StaticTypeAdapter",
        "com.github.gfx.android.orma.annotation.StaticTypeAdapters",
})
public class OrmaProcessor extends AbstractProcessor {

    public static final String TAG = OrmaProcessor.class.getSimpleName();

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (annotations.size() == 0) {
            return true;
        }
        long t0 = System.nanoTime();

        ProcessingContext context = new ProcessingContext(processingEnv);
        try {
            buildDatabase(context, roundEnv)
                    .forEach(context::addDatabaseDefinition);

            buildTypeAdapters(context, roundEnv)
                    .forEach(context::addTypeAdapterDefinition);

            buildTableSchemas(context, roundEnv)
                    .forEach(schema -> context.schemaMap.put(schema.getModelClassName(), schema));

            buildVirtualTableSchemas(context, roundEnv)
                    .forEach(schema -> {
                        throw new ProcessingException("@VirtualTable is not yet implemented.", schema.getElement());
                    });

            context.note("built " + context.schemaMap.size() + " of schema models in " + TimeUnit.NANOSECONDS
                    .toMillis(System.nanoTime() - t0) + "ms");
            t0 = System.nanoTime();

            context.schemaMap.values()
                    .stream()
                    .flatMap((schema) -> Stream.of(
                            new SchemaWriter(context, schema),
                            new RelationWriter(context, schema),
                            new SelectorWriter(context, schema),
                            new UpdaterWriter(context, schema),
                            new DeleterWriter(context, schema)))
                    .parallel()
                    .forEach(this::writeCodeForEachModel);

            if (!context.schemaMap.isEmpty()) {
                context.setupDefaultDatabaseIfNeeded();
                for (DatabaseDefinition database : context.databases) {
                    DatabaseWriter databaseWriter = new DatabaseWriter(context, database);
                    writeToFiler(database.getElement().orElse(null), databaseWriter.buildJavaFile());
                }
            }
        } catch (ProcessingException e) {
            context.addError(e);
        } catch (Exception e) {
            context.addError(new ProcessingException("Unexpected exception while processing annotations", null, e));
        }

        context.printErrors();
        context.note("process classes in " + TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - t0) + "ms");

        return false;
    }

    private Stream<DatabaseDefinition> buildDatabase(ProcessingContext context, RoundEnvironment roundEnv) {
        return roundEnv
                .getElementsAnnotatedWith(Database.class)
                .stream()
                .map(element -> new DatabaseDefinition(context, (TypeElement) element));
    }

    public Stream<TypeAdapterDefinition> buildTypeAdapters(ProcessingContext context, RoundEnvironment roundEnv) {
        return Stream.concat(
                roundEnv.getElementsAnnotatedWith(StaticTypeAdapters.class).stream()
                        .flatMap(element -> AnnotationHandle
                                .findRepeatable(element, StaticTypeAdapter.class, StaticTypeAdapters.class)),
                roundEnv.getElementsAnnotatedWith(StaticTypeAdapter.class).stream()
                        .map(element -> AnnotationHandle.find(element, StaticTypeAdapter.class).get())
        )
                .map(annotation -> new TypeAdapterDefinition(context, annotation.element, annotation));
    }

    public Stream<SchemaDefinition> buildTableSchemas(ProcessingContext context, RoundEnvironment roundEnv) {
        return roundEnv
                .getElementsAnnotatedWith(Table.class)
                .stream()
                .map(element -> new SchemaDefinition(context, (TypeElement) element));
    }

    public Stream<SchemaDefinition> buildVirtualTableSchemas(ProcessingContext context, RoundEnvironment roundEnv) {
        return roundEnv
                .getElementsAnnotatedWith(VirtualTable.class)
                .stream()
                .map(element -> new SchemaDefinition(context, (TypeElement) element));
    }

    public void writeCodeForEachModel(BaseWriter writer) {
        writeToFiler(writer.getElement().orElse(null), writer.buildJavaFile());
    }

    private void writeToFiler(Element element, JavaFile javaFile) {
        try {
            javaFile.writeTo(new SynchronizedFiler(processingEnv.getFiler()));
        } catch (Exception e) {
            throw new ProcessingException("Failed to write " + javaFile.typeSpec.name, element, e);
        }
    }
}
