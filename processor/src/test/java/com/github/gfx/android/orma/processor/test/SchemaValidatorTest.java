package com.github.gfx.android.orma.processor.test;

import com.google.testing.compile.JavaFileObjects;

import com.github.gfx.android.orma.processor.OrmaProcessor;

import org.junit.Test;

import javax.tools.JavaFileObject;

import static com.google.common.truth.Truth.assert_;
import static com.google.testing.compile.JavaSourceSubjectFactory.javaSource;

public class SchemaValidatorTest {

    @Test
    public void testDuplicateColumnNames() throws Exception {
        JavaFileObject classModelWithDuplicateColumnNames = JavaFileObjects
                .forSourceString("ModelWithDuplicateColumnNames",
                        "import com.github.gfx.android.orma.annotation.*;\n"
                                + "@Table\n"
                                + "public class ModelWithDuplicateColumnNames {\n"
                                + "@Column(\"foo\") String foo;\n"
                                + "@Column(\"foo\") String bar;\n"
                                + "}\n");

        assert_().about(javaSource())
                .that(classModelWithDuplicateColumnNames)
                .processedWith(new OrmaProcessor())
                .failsToCompile()
                .withErrorCount(2)
                .withErrorContaining("Duplicate column names \"foo\" found");
    }

    @Test
    public void testDuplicateColumnNamesCaseInsensitive() throws Exception {
        JavaFileObject classModelWithDuplicateColumnNames = JavaFileObjects
                .forSourceString("ModelWithDuplicateColumnNames",
                        "import com.github.gfx.android.orma.annotation.*;\n"
                                + "@Table\n"
                                + "public class ModelWithDuplicateColumnNames {\n"
                                + "@Column String foo;\n"
                                + "@Column String FOO;\n"
                                + "}\n");

        assert_().about(javaSource())
                .that(classModelWithDuplicateColumnNames)
                .processedWith(new OrmaProcessor())
                .failsToCompile()
                .withErrorCount(2)
                .withErrorContaining("Duplicate column names \"foo\" found");
    }

    @Test
    public void testDuplicateColumnNamesIncludingPrimaryKey() throws Exception {
        JavaFileObject classModelWithDuplicateColumnNames = JavaFileObjects
                .forSourceString("ModelWithDuplicateColumnNames",
                        "import com.github.gfx.android.orma.annotation.*;\n"
                                + "@Table\n"
                                + "public class ModelWithDuplicateColumnNames {\n"
                                + "@PrimaryKey(\"foo\") String foo;\n"
                                + "@Column(\"foo\") String bar;\n"
                                + "}\n");

        assert_().about(javaSource())
                .that(classModelWithDuplicateColumnNames)
                .processedWith(new OrmaProcessor())
                .failsToCompile()
                .withErrorCount(2)
                .withErrorContaining("Duplicate column names \"foo\" found");
    }

}
