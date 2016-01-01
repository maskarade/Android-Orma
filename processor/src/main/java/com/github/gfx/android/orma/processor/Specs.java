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

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.CodeBlock;

public class Specs {

    private static final AnnotationSpec overrideAnnotationSpec = AnnotationSpec.builder(Override.class).build();

    private static final AnnotationSpec nonNullAnnotationSpec = AnnotationSpec.builder(Types.NonNull).build();

    private static final AnnotationSpec nullableAnnotationSpec = AnnotationSpec.builder(Types.Nullable).build();

    public static AnnotationSpec overrideAnnotationSpec() {
        return overrideAnnotationSpec;
    }

    public static AnnotationSpec nonNullAnnotationSpec() {
        return nonNullAnnotationSpec;
    }

    public static AnnotationSpec nullableAnnotation() {
        return nullableAnnotationSpec;
    }

    public static AnnotationSpec workerThreadAnnotation() {
        return AnnotationSpec.builder(Types.WorkerThread)
                .build();
    }

    public static AnnotationSpec suppressWarningsAnnotation(String... warnings) {
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
        builder.addMember("value", "{$L}", names.build());
        return builder.build();
    }
}
