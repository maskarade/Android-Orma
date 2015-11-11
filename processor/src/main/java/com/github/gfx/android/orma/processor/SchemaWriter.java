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
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
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
        schema.getElement().getEnclosedElements().forEach(element -> {
            if (element.getAnnotation(Column.class) != null || element.getAnnotation(PrimaryKey.class) != null) {

                columns.add(buildColumnFieldSpec(element));
            }
        });

        fieldSpecs.addAll(columns);

        fieldSpecs.add(
                FieldSpec.builder(Types.String, TABLE_NAME)
                        .addModifiers(publicStaticFinal)
                        .initializer("$S", schema.getModelClassName().simpleName())
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

    public FieldSpec buildColumnFieldSpec(Element element) {
        // TODO: autoincrement, conflict clause, default value, etc...
        // See https://www.sqlite.org/lang_createtable.html for full specification
        PrimaryKey primaryKeyAnnotation = element.getAnnotation(PrimaryKey.class);
        Index indexAnnotation = element.getAnnotation(Index.class);
        Unique uniqueAnnotation = element.getAnnotation(Unique.class);

        boolean primaryKey = primaryKeyAnnotation != null;
        boolean index = indexAnnotation != null || primaryKey;
        boolean unique = uniqueAnnotation != null || primaryKey;

        boolean nullable = false;
        for (AnnotationMirror annotation : element.getAnnotationMirrors()) {
            // allow anything named "Nullable"
            if (annotation.getAnnotationType().asElement().getSimpleName().contentEquals("Nullable")) {
                nullable = true;
            }
        }

        String columnName = element.getSimpleName().toString();
        TypeName columnType = ClassName.get(element.asType());
        TypeName columnDefType = Types.getColumnDef(columnType.box());
        CodeBlock initializer = CodeBlock.builder()
                .add("new $T($S, $T.class, $L, $L, $L, $L)",
                        columnDefType, columnName, element.asType(), nullable, primaryKey, index, unique)
                .build();
        return FieldSpec.builder(columnDefType, columnName)
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
            builder.add("$S", columns.get(i).name);
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
