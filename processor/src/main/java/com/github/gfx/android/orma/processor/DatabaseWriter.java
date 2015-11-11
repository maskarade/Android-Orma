package com.github.gfx.android.orma.processor;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeSpec;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Modifier;

public class DatabaseWriter {

    static final String kClassName = "OrmaDatabase";

    final ProcessingEnvironment processingEnv;

    static final Modifier[] publicStaticFinal = {
            Modifier.PUBLIC,
            Modifier.STATIC,
            Modifier.FINAL,
    };

    static final String connection = "connection";

    List<SchemaDefinition> schemas = new ArrayList<>();

    public DatabaseWriter(ProcessingEnvironment processingEnv) {
        this.processingEnv = processingEnv;
    }

    public void add(SchemaDefinition schema) {
        schemas.add(schema);
    }

    public boolean isRequired() {
        return schemas.size() > 0;
    }

    public String getPackageName() {
        assert isRequired();

        return schemas.get(0).getPackageName();
    }

    public TypeSpec buildTypeSpec() {
        assert isRequired();

        TypeSpec.Builder classBuilder = TypeSpec.classBuilder(kClassName);
        classBuilder.addAnnotation(Specs.buildGeneratedAnnotationSpec());
        classBuilder.addModifiers(Modifier.PUBLIC);

        classBuilder.addFields(buildFieldSpecs());
        classBuilder.addMethods(buildMethodSpecs());

        return classBuilder.build();
    }

    public List<FieldSpec> buildFieldSpecs() {
        List<FieldSpec> fieldSpecs = new ArrayList<>();

        List<FieldSpec> schemaFields = new ArrayList<>();

        schemas.forEach(schema -> {
            schemaFields.add(
                    FieldSpec.builder(schema.getSchemaClassName(),
                            "schema" + schema.getModelClassName().simpleName())
                            .addModifiers(publicStaticFinal)
                            .initializer("new $T()", schema.getSchemaClassName())
                            .build());
        });

        fieldSpecs.addAll(schemaFields);

        fieldSpecs.add(
                FieldSpec.builder(Types.getList(Types.WildcardSchema), "schemas", publicStaticFinal)
                        .initializer(buildSchemasInitializer(schemaFields))
                        .build());

        fieldSpecs.add(
                FieldSpec.builder(Types.OrmaConnection, connection, Modifier.PRIVATE, Modifier.FINAL)
                        .build());

        return fieldSpecs;
    }

    private CodeBlock buildSchemasInitializer(List<FieldSpec> schemaFields) {
        CodeBlock.Builder builder = CodeBlock.builder();
        builder.add("$T.<$T>asList(\n", Types.Arrays, Types.WildcardSchema).indent();

        for (int i = 0; i < schemaFields.size(); i++) {
            builder.add("$N", schemaFields.get(i));

            if ((i + 1) != schemaFields.size()) {
                builder.add(",\n");
            } else {
                builder.add("\n");
            }
        }

        builder.unindent().add(")");
        return builder.build();
    }

    public List<MethodSpec> buildMethodSpecs() {
        List<MethodSpec> methodSpecs = new ArrayList<>();

        methodSpecs.addAll(buildConstructorSpecs());

        methodSpecs.add(
                MethodSpec.methodBuilder("getConnection")
                        .addAnnotation(Specs.buildNonNullAnnotationSpec())
                        .returns(Types.OrmaConnection)
                        .addStatement("return $L", connection)
                        .build()
        );

        methodSpecs.add(
                MethodSpec.methodBuilder("transaction")
                        .addParameter(
                                ParameterSpec.builder(Types.TransactionTask, "task")
                                        .addAnnotation(Specs.buildNonNullAnnotationSpec())
                                        .build())
                        .addStatement("$L.transaction(task)", connection)
                        .build()
        );

        schemas.forEach(schema -> {
            String schemaInstance = "schema" + schema.getModelClassName().simpleName();

            methodSpecs.add(
                    MethodSpec.methodBuilder("from" + schema.getModelClassName().simpleName())
                            .addJavadoc("Starts building query <code>SELECT * FROM $T ...</code>.", schema.getModelClassName())
                            .addAnnotation(Specs.buildNonNullAnnotationSpec())
                            .returns(schema.getRelationClassName())
                            .addStatement("return new $T($L, $L)",
                                    schema.getRelationClassName(),
                                    connection,
                                    schemaInstance)
                            .build());

            methodSpecs.add(
                    MethodSpec.methodBuilder("insert")
                            .addJavadoc("Inserts a model to the database.")
                            .returns(long.class)
                            .addParameter(
                                    ParameterSpec.builder(schema.getModelClassName(), "model")
                                            .addAnnotation(Specs.buildNonNullAnnotationSpec())
                                            .build()
                            )
                            .addStatement("return $L.insert($L, model)",
                                    connection,
                                    schemaInstance
                            )
                            .build());

        });

        return methodSpecs;
    }

    public List<MethodSpec> buildConstructorSpecs() {
        List<MethodSpec> methodSpecs = new ArrayList<>();

        methodSpecs.add(MethodSpec.constructorBuilder()
                .addParameter(
                        ParameterSpec.builder(Types.Context, "context")
                                .addAnnotation(Specs.buildNonNullAnnotationSpec())
                                .build())
                .addParameter(
                        ParameterSpec.builder(Types.String, "name")
                                .addAnnotation(Specs.buildNonNullAnnotationSpec())
                                .build())
                .addStatement("this(new $T(context, name, schemas))", Types.OrmaConnection)
                .addJavadoc("Create a database context that handles $L.\n", getListOfModelClassesForJavadoc())
                .build());

        methodSpecs.add(MethodSpec.constructorBuilder()
                .addParameter(
                        ParameterSpec.builder(Types.OrmaConnection, connection)
                                .addAnnotation(Specs.buildNonNullAnnotationSpec())
                                .build())
                .addStatement("this.$L = $L", connection, connection)
                .build());

        return methodSpecs;
    }

    private String getListOfModelClassesForJavadoc() {
        return schemas.stream()
                .map(schema -> schema.getModelClassName().simpleName())
                .collect(Collectors.joining(", "));
    }


}
