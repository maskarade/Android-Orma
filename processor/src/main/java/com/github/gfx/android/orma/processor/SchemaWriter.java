package com.github.gfx.android.orma.processor;

import com.github.gfx.android.orma.annotation.Column;
import com.github.gfx.android.orma.annotation.Index;
import com.github.gfx.android.orma.annotation.PrimaryKey;
import com.github.gfx.android.orma.annotation.Unique;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeSpec;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;

public class SchemaWriter {

    static final String TABLE_NAME = "$TABLE_NAME";

    static final String COLUMNS = "$COLUMNS";

    static final String COLUMN_NAMES = "$COLUMN_NAMES";

    static final Modifier[] publicStaticFinal = {
            Modifier.PUBLIC,
            Modifier.STATIC,
            Modifier.FINAL,
    };

    private final SchemaDefinition schema;

    private final ProcessingEnvironment processingEnv;

    public SchemaWriter(SchemaDefinition schema, ProcessingEnvironment processingEnv) {
        this.schema = schema;
        this.processingEnv = processingEnv;
    }

    public TypeSpec buildTypeSpec() {
        TypeSpec.Builder classBuilder = TypeSpec.classBuilder(schema.getSchemaClassName().simpleName());
        classBuilder.addAnnotation(Specs.buildGeneratedAnnotationSpec());
        classBuilder.addModifiers(Modifier.PUBLIC);
        classBuilder.addSuperinterface(Types.getSchema(schema.getModelClassName()));

        classBuilder.addFields(buildFieldSpecs());
        classBuilder.addMethods(buildMethodSpecs());

        return classBuilder.build();
    }

    public List<FieldSpec> buildFieldSpecs() {
        List<FieldSpec> fieldSpecs = new ArrayList<>();

        schema.getElement().getEnclosedElements().forEach(element -> {
            if (element.getAnnotation(Column.class) != null && element instanceof VariableElement) {
                PrimaryKey primaryKey = element.getAnnotation(PrimaryKey.class);
                Index index = element.getAnnotation(Index.class);
                Unique unique = element.getAnnotation(Unique.class);

                boolean nullable = false;
                for (AnnotationMirror annotation : element.getAnnotationMirrors()) {
                    if (annotation.getAnnotationType().asElement().getSimpleName().contentEquals("Nullable")) {
                        nullable = true;
                    }
                }

                // new ColumnDef<>(name, type, nullable, primaryKey, indexed, unique)
                String columnName = element.getSimpleName().toString();
                fieldSpecs.add(
                        FieldSpec.builder(Types.getColumnDef(ClassName.get(element.asType())), columnName)
                                .addModifiers(publicStaticFinal)
                                .initializer("null")
                                .build()
                );
            }
        });

        fieldSpecs.add(
                FieldSpec.builder(Types.String, TABLE_NAME)
                        .addModifiers(publicStaticFinal)
                        .initializer("$S", schema.getModelClassName().simpleName())
                        .build()
        );

        fieldSpecs.add(
                FieldSpec.builder(Types.ColumnList, COLUMNS)
                        .addModifiers(publicStaticFinal)
                        .initializer("new $T<>()", Types.ArrayList)
                        .build()
        );

        fieldSpecs.add(
                FieldSpec.builder(Types.StringArray, COLUMN_NAMES)
                        .addModifiers(publicStaticFinal)
                        .initializer("$L", "{}")
                        .build()
        );

        return fieldSpecs;
    }

    public List<MethodSpec> buildMethodSpecs() {
        List<MethodSpec> methodSpecs = new ArrayList<>();

        List<AnnotationSpec> overrideAndNonNull = Arrays.asList(
                Specs.buildNonNullAnnotationSpec(),
                Specs.buildOverrideAnnotationSpec()
        );

        methodSpecs.add(
                MethodSpec.methodBuilder("getTableName")
                        .addAnnotations(overrideAndNonNull)
                        .addModifiers(Modifier.PUBLIC)
                        .returns(Types.String)
                        .addCode(CodeBlock.builder().addStatement("return $L", TABLE_NAME).build())
                        .build()
        );

        methodSpecs.add(
                MethodSpec.methodBuilder("getColumns")
                        .addAnnotations(overrideAndNonNull)
                        .addModifiers(Modifier.PUBLIC)
                        .returns(Types.ColumnList)
                        .addCode(CodeBlock.builder().addStatement("return $L", COLUMNS).build())
                        .build()
        );

        methodSpecs.add(
                MethodSpec.methodBuilder("getColumnNames")
                        .addAnnotations(overrideAndNonNull)
                        .addModifiers(Modifier.PUBLIC)
                        .returns(Types.StringArray)
                        .addCode(CodeBlock.builder().addStatement("return $L", COLUMN_NAMES).build())
                        .build()
        );

        methodSpecs.add(
                MethodSpec.methodBuilder("serializeModelToContentValues")
                        .addAnnotations(overrideAndNonNull)
                        .addModifiers(Modifier.PUBLIC)
                        .returns(Types.ContentValues)
                        .addParameter(
                                ParameterSpec.builder(schema.getModelClassName(), "model")
                                        .addAnnotation(Specs.buildNonNullAnnotationSpec())
                                        .build())
                        .addCode(CodeBlock.builder().addStatement("return null").build())
                        .build()
        );

        methodSpecs.add(
                MethodSpec.methodBuilder("createModelFromCursor")
                        .addAnnotations(overrideAndNonNull)
                        .addModifiers(Modifier.PUBLIC)
                        .returns(schema.getModelClassName())
                        .addParameter(
                                ParameterSpec.builder(Types.Cursor, "cursor")
                                        .addAnnotation(Specs.buildNonNullAnnotationSpec())
                                        .build())
                        .addCode(CodeBlock.builder().addStatement("return null").build())
                        .build()
        );

        return methodSpecs;
    }
}
