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
package com.github.gfx.android.orma.processor.util;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Pre-defined JavaPoet specs.
 */
public class Annotations {

    private static final AnnotationSpec override = AnnotationSpec.builder(Override.class).build();

    private static final AnnotationSpec nonNull = AnnotationSpec.builder(Types.NonNull).build();

    private static final AnnotationSpec nullable = AnnotationSpec.builder(Types.Nullable).build();

    private static final AnnotationSpec checkResult = AnnotationSpec.builder(Types.CheckResult).build();

    private static final AnnotationSpec deprecated = AnnotationSpec.builder(Deprecated.class).build();

    private static final List<AnnotationSpec> safeVarArgsAnnotations = Arrays.asList(
            AnnotationSpec.builder(SafeVarargs.class).build(),
            suppressWarnings("varargs")
    );

    private static List<AnnotationSpec> overrideAndNonNull = Arrays.asList(
            Annotations.nonNull(),
            Annotations.override()
    );

    private static List<AnnotationSpec> overrideAndNullable = Arrays.asList(
            Annotations.nullable(),
            Annotations.override()
    );

    public static AnnotationSpec override() {
        return override;
    }

    public static AnnotationSpec nonNull() {
        return nonNull;
    }

    public static AnnotationSpec nullable() {
        return nullable;
    }

    public static List<AnnotationSpec> overrideAndNonNull() {
        return overrideAndNonNull;
    }

    public static List<AnnotationSpec> overrideAndNullable() {
        return overrideAndNullable;
    }

    public static AnnotationSpec workerThread() {
        return AnnotationSpec.builder(Types.WorkerThread)
                .build();
    }

    public static List<AnnotationSpec> safeVarargsIfNeeded(TypeName type) {
        if (type instanceof ParameterizedTypeName) {
            return safeVarArgsAnnotations;
        } else {
            return Collections.emptyList();
        }
    }

    public static AnnotationSpec suppressWarnings(String... warnings) {
        AnnotationSpec.Builder builder = AnnotationSpec.builder(SuppressWarnings.class);
        CodeBlock.Builder names = CodeBlock.builder();
        boolean first = true;
        for (String warning : warnings) {
            if (first) {
                names.add("$S", warning);
                first = false;
            } else {
                names.add(", $S", warning);
            }
        }
        if (warnings.length == 1) {
            builder.addMember("value", names.build());
        } else {
            builder.addMember("value", "{$L}", names.build());
        }
        return builder.build();
    }

    public static AnnotationSpec deprecated() {
        return deprecated;
    }

    public static AnnotationSpec checkResult() {
        return checkResult;
    }
}
