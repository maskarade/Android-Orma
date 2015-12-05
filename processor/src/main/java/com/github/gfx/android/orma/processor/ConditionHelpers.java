package com.github.gfx.android.orma.processor;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.lang.model.element.Modifier;

public class ConditionHelpers {

    private final SchemaDefinition schema;

    private final ClassName targetClassName;

    private final SqlGenerator sql = new SqlGenerator();

    public ConditionHelpers(SchemaDefinition schema, ClassName targetClassName) {
        this.schema = schema;
        this.targetClassName = targetClassName;
    }

    public List<MethodSpec> buildConditionHelpers() {
        List<MethodSpec> methodSpecs = new ArrayList<>();
        schema.getColumns()
                .stream()
                .filter(column -> column.indexed)
                .forEach(column -> buildConditionHelpersForEachColumn(methodSpecs, column));
        return methodSpecs;
    }

    void buildConditionHelpersForEachColumn(List<MethodSpec> methodSpecs, ColumnDefinition column) {

        ParameterSpec.Builder paramSpecBuilder = conditionParamSpecBuilder(column, "value");

        if (column.nullable) {
            methodSpecs.add(
                    MethodSpec.methodBuilder(column.name + "IsNull")
                            .addModifiers(Modifier.PUBLIC)
                            .returns(targetClassName)
                            .addStatement("return where($S)", sql.quoteIdentifier(column.columnName) + " IS NULL")
                            .build()
            );

            methodSpecs.add(
                    MethodSpec.methodBuilder(column.name + "IsNotNull")
                            .addModifiers(Modifier.PUBLIC)
                            .returns(targetClassName)
                            .addStatement("return where($S)", sql.quoteIdentifier(column.columnName) + " IS NOT NULL")
                            .build()
            );
        }

        methodSpecs.add(
                MethodSpec.methodBuilder(column.name + "Eq")
                        .addModifiers(Modifier.PUBLIC)
                        .addParameter(paramSpecBuilder.build())
                        .returns(targetClassName)
                        .addStatement("return where($S, value)", sql.quoteIdentifier(column.columnName) + " = ?")
                        .build()
        );

        methodSpecs.add(
                MethodSpec.methodBuilder(column.name + "NotEq")
                        .addModifiers(Modifier.PUBLIC)
                        .addParameter(paramSpecBuilder.build())
                        .returns(targetClassName)
                        .addStatement("return where($S, value)", sql.quoteIdentifier(column.columnName) + " <> ?")
                        .build()
        );

        methodSpecs.add(
                MethodSpec.methodBuilder(column.name + "In")
                        .addModifiers(Modifier.PUBLIC)
                        .addParameter(ParameterSpec.builder(Types.getCollection(column.getBoxType()), "values")
                                .addAnnotation(Specs.buildNonNullAnnotationSpec())
                                .build())
                        .returns(targetClassName)
                        .addStatement("return in(false, $S, values)", sql.quoteIdentifier(column.columnName))
                        .build()
        );

        methodSpecs.add(
                MethodSpec.methodBuilder(column.name + "NotIn")
                        .addModifiers(Modifier.PUBLIC)
                        .addParameter(ParameterSpec.builder(Types.getCollection(column.getBoxType()), "values")
                                .addAnnotation(Specs.buildNonNullAnnotationSpec())
                                .build())
                        .returns(targetClassName)
                        .addStatement("return in(true, $S, values)", sql.quoteIdentifier(column.columnName))
                        .build()
        );

        if (isNumberType(column.getUnboxType())) {
            methodSpecs.add(
                    MethodSpec.methodBuilder(column.name + "Lt")
                            .addModifiers(Modifier.PUBLIC)
                            .addParameter(paramSpecBuilder.build())
                            .returns(targetClassName)
                            .addStatement("return where($S, value)", sql.quoteIdentifier(column.columnName) + " < ?")
                            .build()
            );
            methodSpecs.add(
                    MethodSpec.methodBuilder(column.name + "Le")
                            .addModifiers(Modifier.PUBLIC)
                            .addParameter(paramSpecBuilder.build())
                            .returns(targetClassName)
                            .addStatement("return where($S, value)", sql.quoteIdentifier(column.columnName) + " <= ?")
                            .build()
            );
            methodSpecs.add(
                    MethodSpec.methodBuilder(column.name + "Gt")
                            .addModifiers(Modifier.PUBLIC)
                            .addParameter(paramSpecBuilder.build())
                            .returns(targetClassName)
                            .addStatement("return where($S, value)", sql.quoteIdentifier(column.columnName) + " > ?")
                            .build()
            );
            methodSpecs.add(
                    MethodSpec.methodBuilder(column.name + "Ge")
                            .addModifiers(Modifier.PUBLIC)
                            .addParameter(paramSpecBuilder.build())
                            .returns(targetClassName)
                            .addStatement("return where($S, value)", sql.quoteIdentifier(column.columnName) + " >= ?")
                            .build()
            );
        }
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
