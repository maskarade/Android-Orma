package com.github.gfx.android.orma.processor;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Modifier;

public class RelationWriter extends BaseWriter {

    private final SchemaDefinition schema;

    private final SqlGenerator sql = new SqlGenerator();

    public RelationWriter(SchemaDefinition schema, ProcessingEnvironment processingEnv) {
        super(processingEnv);
        this.schema = schema;
    }

    @Override
    public TypeSpec buildTypeSpec() {
        TypeSpec.Builder classBuilder = TypeSpec.classBuilder(schema.getRelationClassName().simpleName());
        classBuilder.addModifiers(Modifier.PUBLIC);
        classBuilder.superclass(Types.getRelation(schema.getModelClassName(), schema.getRelationClassName()));

        classBuilder.addFields(buildFieldSpecs());
        classBuilder.addMethods(buildMethodSpecs());

        return classBuilder.build();
    }

    public List<FieldSpec> buildFieldSpecs() {
        List<FieldSpec> fieldSpecs = new ArrayList<>();
        // TODO
        return fieldSpecs;
    }

    public List<MethodSpec> buildMethodSpecs() {
        List<MethodSpec> methodSpecs = new ArrayList<>();

        methodSpecs.add(MethodSpec.constructorBuilder()
                .addParameter(Types.OrmaConnection, "orma")
                .addParameter(schema.getSchemaClassName(), "schema")
                .addCode("super(orma, schema);\n")
                .build());

        // per-column condition helpers

        schema.getColumns()
                .stream()
                .filter(column -> column.indexed)
                .forEach(column -> methodSpecs.addAll(buildConditionHelpers(column)));

        return methodSpecs;
    }

    List<MethodSpec> buildConditionHelpers(ColumnDefinition column) {
        List<MethodSpec> methodSpecs = new ArrayList<>();

        ParameterSpec.Builder paramSpecBuilder = conditionParamSpecBuilder(column, "value");

        methodSpecs.add(
                MethodSpec.methodBuilder(column.name + "Eq")
                        .addModifiers(Modifier.PUBLIC)
                        .addParameter(paramSpecBuilder.build())
                        .returns(schema.getRelationClassName())
                        .addStatement("return where($S, value)", sql.quoteIdentifier(column.columnName) + " = ?")
                        .build()
        );

        methodSpecs.add(
                MethodSpec.methodBuilder(column.name + "NotEq")
                        .addModifiers(Modifier.PUBLIC)
                        .addParameter(paramSpecBuilder.build())
                        .returns(schema.getRelationClassName())
                        .addStatement("return where($S, value)", sql.quoteIdentifier(column.columnName) + " <> ?")
                        .build()
        );

        methodSpecs.add(
                MethodSpec.methodBuilder(column.name + "In")
                        .addModifiers(Modifier.PUBLIC)
                        .addParameter(ParameterSpec.builder(Types.getCollection(column.getBoxType()), "values")
                                .addAnnotation(Specs.buildNonNullAnnotationSpec())
                                .build())
                        .returns(schema.getRelationClassName())
                        .addStatement("return in(false, $S, values)", sql.quoteIdentifier(column.columnName))
                        .build()
        );

        methodSpecs.add(
                MethodSpec.methodBuilder(column.name + "NotIn")
                        .addModifiers(Modifier.PUBLIC)
                        .addParameter(ParameterSpec.builder(Types.getCollection(column.getBoxType()), "values")
                                .addAnnotation(Specs.buildNonNullAnnotationSpec())
                                .build())
                        .returns(schema.getRelationClassName())
                        .addStatement("return in(true, $S, values)", sql.quoteIdentifier(column.columnName))
                        .build()
        );

        if (isNumberType(column.getUnboxType())) {
            methodSpecs.add(
                    MethodSpec.methodBuilder(column.name + "Lt")
                            .addModifiers(Modifier.PUBLIC)
                            .addParameter(paramSpecBuilder.build())
                            .returns(schema.getRelationClassName())
                            .addStatement("return where($S, value)", sql.quoteIdentifier(column.columnName) + " < ?")
                            .build()
            );
            methodSpecs.add(
                    MethodSpec.methodBuilder(column.name + "Le")
                            .addModifiers(Modifier.PUBLIC)
                            .addParameter(paramSpecBuilder.build())
                            .returns(schema.getRelationClassName())
                            .addStatement("return where($S, value)", sql.quoteIdentifier(column.columnName) + " <= ?")
                            .build()
            );
            methodSpecs.add(
                    MethodSpec.methodBuilder(column.name + "Gt")
                            .addModifiers(Modifier.PUBLIC)
                            .addParameter(paramSpecBuilder.build())
                            .returns(schema.getRelationClassName())
                            .addStatement("return where($S, value)", sql.quoteIdentifier(column.columnName) + " > ?")
                            .build()
            );
            methodSpecs.add(
                    MethodSpec.methodBuilder(column.name + "Ge")
                            .addModifiers(Modifier.PUBLIC)
                            .addParameter(paramSpecBuilder.build())
                            .returns(schema.getRelationClassName())
                            .addStatement("return where($S, value)", sql.quoteIdentifier(column.columnName) + " >= ?")
                            .build()
            );
        }

        return methodSpecs;
    }

    boolean isNumberType(TypeName typeName) {
        return typeName.equals(TypeName.BYTE)
                || typeName.equals(TypeName.SHORT)
                || typeName.equals(TypeName.INT)
                || typeName.equals(TypeName.LONG)
                || typeName.equals(TypeName.FLOAT)
                || typeName.equals(TypeName.DOUBLE);
    }

    ParameterSpec.Builder conditionParamSpecBuilder(ColumnDefinition column, String name) {
        return ParameterSpec.builder(column.getType(), name)
                .addAnnotations(nullabilityAnnotations(column));
    }

    List<AnnotationSpec> nullabilityAnnotations(ColumnDefinition column) {
        if (column.getType().isPrimitive()) {
            return Collections.emptyList();
        }

        if (column.nullable) {
            return Collections.singletonList(Specs.buildNullableAnnotationSpec());
        } else {
            return Collections.singletonList(Specs.buildNonNullAnnotationSpec());
        }
    }
}
