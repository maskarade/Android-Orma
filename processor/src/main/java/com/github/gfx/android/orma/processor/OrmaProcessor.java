package com.github.gfx.android.orma.processor;

import com.github.gfx.android.orma.annotation.Table;
import com.github.gfx.android.orma.annotation.VirtualTable;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;

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
import javax.tools.Diagnostic;

@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedAnnotationTypes({
        "com.github.gfx.android.orma.annotation.*",
        "com.google.gson.annotations.SerializedName", // GSON
        "com.fasterxml.jackson.annotation.JsonProperty", // Jackson
})
public class OrmaProcessor extends AbstractProcessor {

    static final String TAG = OrmaProcessor.class.getSimpleName();

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (annotations.size() == 0) {
            return true;
        }

        DatabaseWriter databaseWriter = new DatabaseWriter(processingEnv);

        buildTableSchemas(roundEnv)
                .peek(this::writeSchema)
                .peek(this::writeRelation)
                .peek(this::writeUpdater)
                .peek(this::writeDeleter)
                .forEach(databaseWriter::add);

        buildVirtualTableSchemas(roundEnv)
                .peek(schema -> {
                    throw new RuntimeException("@VirtualTable is not yet implemented.");
                });

        if (databaseWriter.isRequired()) {
            writeToFiler(null,
                    JavaFile.builder(databaseWriter.getPackageName(),
                            databaseWriter.buildTypeSpec())
                            .build());
        }

        return false;
    }

    public Stream<SchemaDefinition> buildTableSchemas(RoundEnvironment roundEnv) {
        SchemaValidator validator = new SchemaValidator();
        return roundEnv
                .getElementsAnnotatedWith(Table.class)
                .stream()
                .map(element -> new SchemaDefinition(validator.validate(element)));
    }

    public Stream<SchemaDefinition> buildVirtualTableSchemas(RoundEnvironment roundEnv) {
        SchemaValidator validator = new SchemaValidator();
        return roundEnv
                .getElementsAnnotatedWith(VirtualTable.class)
                .stream()
                .map(element -> new SchemaDefinition(validator.validate(element)));
    }

    public void writeSchema(SchemaDefinition schema) {
        SchemaWriter writer = new SchemaWriter(schema, processingEnv);
        writeToFilerForEachModel(schema, writer.buildTypeSpec());
    }

    public void writeRelation(SchemaDefinition schema) {
        RelationWriter writer = new RelationWriter(schema, processingEnv);
        writeToFilerForEachModel(schema, writer.buildTypeSpec());
    }

    private void writeUpdater(SchemaDefinition schema) {
        UpdaterWriter writer = new UpdaterWriter(schema, processingEnv);
        writeToFilerForEachModel(schema, writer.buildTypeSpec());
    }

    private void writeDeleter(SchemaDefinition schema) {
        DeleterWriter writer = new DeleterWriter(schema, processingEnv);
        writeToFilerForEachModel(schema, writer.buildTypeSpec());
    }

    public void writeToFilerForEachModel(SchemaDefinition schema, TypeSpec typeSpec) {
        writeToFiler(schema.getElement(),
                JavaFile.builder(schema.getPackageName(), typeSpec)
                        .build());
    }

    public void writeToFiler(Element element, JavaFile javaFile) {
        try {
            javaFile.writeTo(processingEnv.getFiler());
        } catch (IOException e) {
            error("Failed to write " + javaFile.typeSpec.name + ": " + e, element);
        }
    }

    void note(CharSequence message, Element element) {
        printMessage(Diagnostic.Kind.NOTE, message, element);
    }

    void error(CharSequence message, Element element) {
        printMessage(Diagnostic.Kind.ERROR, message, element);
    }

    void printMessage(Diagnostic.Kind kind, CharSequence message, Element element) {
        processingEnv.getMessager().printMessage(kind, "[" + TAG + "] " + message, element);
    }
}
