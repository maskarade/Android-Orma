package com.github.gfx.android.orma.processor;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Modifier;

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

        List<FieldSpec> columns = new ArrayList<>();

        schema.getColumns().forEach(columnDef -> {
            columns.add(buildColumnFieldSpec(columnDef));
        });

        fieldSpecs.addAll(columns);

        fieldSpecs.add(
                FieldSpec.builder(Types.String, TABLE_NAME)
                        .addModifiers(publicStaticFinal)
                        .initializer("$S", schema.tableName)
                        .build()
        );

        fieldSpecs.add(
                FieldSpec.builder(Types.ColumnList, COLUMNS)
                        .addModifiers(publicStaticFinal)
                        .initializer(buildColumnsInitializer(columns))
                        .build()
        );

        fieldSpecs.add(
                FieldSpec.builder(Types.StringArray, COLUMN_NAMES)
                        .addModifiers(publicStaticFinal)
                        .initializer(buildColumnNamesInitializer(columns))
                        .build()
        );

        return fieldSpecs;
    }

    public FieldSpec buildColumnFieldSpec(ColumnDefinition c) {
        CodeBlock initializer = CodeBlock.builder()
                .add("new $T($S, $T.class, $L, $L, $L, $L)",
                        c.getColumnDefType(), c.columnName, c.getType(),
                        c.nullable, c.primaryKey, c.indexed, c.unique)
                .build();
        return FieldSpec.builder(c.getColumnDefType(), c.name)
                .addModifiers(publicStaticFinal)
                .initializer(initializer)
                .build();
    }

    public CodeBlock buildColumnsInitializer(List<FieldSpec> columns) {
        CodeBlock.Builder builder = CodeBlock.builder();

        builder.add("$T.<$T>asList(\n", Types.Arrays, Types.WildcardColumnDef).indent();

        for (int i = 0; i < columns.size(); i++) {
            builder.add("$N", columns.get(i));
            if ((i + 1) != columns.size()) {
                builder.add(",\n");
            } else {
                builder.add("\n");
            }
        }

        builder.unindent().add(")");

        return builder.build();
    }

    public CodeBlock buildColumnNamesInitializer(List<FieldSpec> columns) {
        CodeBlock.Builder builder = CodeBlock.builder();

        builder.add("{\n").indent();

        for (int i = 0; i < columns.size(); i++) {
            builder.add("$N.name", columns.get(i));
            if ((i + 1) != columns.size()) {
                builder.add(",\n");
            } else {
                builder.add("\n");
            }
        }

        builder.unindent().add("}");

        return builder.build();
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
                        .addCode(buildSerializeModelToContentValues())
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
                        .addCode(buildCreateModelFromCursor())
                        .build()
        );

        return methodSpecs;
    }

    private CodeBlock buildSerializeModelToContentValues() {
        CodeBlock.Builder builder = CodeBlock.builder();

        builder.addStatement("$T contents = new $T()", Types.ContentValues, Types.ContentValues);

        schema.getColumns().forEach(c -> {
            if (c.primaryKey
                    && (c.getType().equals(TypeName.INT) || c.getType().equals(TypeName.LONG))) {
                builder.beginControlFlow("if (model.$L != 0)", c.name);
                builder.addStatement("contents.put($S, model.$L)", c.columnName, c.name);
                builder.endControlFlow();
            } else {
                builder.addStatement("contents.put($S, model.$L)", c.columnName, c.name);
            }
        });

        builder.addStatement("return contents");

        return builder.build();
    }

    private CodeBlock buildCreateModelFromCursor() {
        CodeBlock.Builder builder = CodeBlock.builder();

        builder.addStatement("$T model = new $T()", schema.getModelClassName(), schema.getModelClassName());

        List<ColumnDefinition> columns = schema.getColumns();
        for (int i = 0; i < columns.size(); i++) {
            ColumnDefinition c = columns.get(i);
            String getter = "get" + capitalize(c.getType());
            builder.addStatement("model.$L = cursor.$L($L)", c.name, getter, i);
        }

        builder.addStatement("return model");

        return builder.build();
    }

    private String capitalize(TypeName type) {
        if (type.isPrimitive()) {
            String s = type.toString();
            return s.substring(0, 1).toUpperCase() + s.substring(1);
        } else if (type instanceof ClassName) {
            return ((ClassName) type).simpleName();
        } else {
            throw new UnsupportedOperationException("TODO: " + type);
        }
    }
}
