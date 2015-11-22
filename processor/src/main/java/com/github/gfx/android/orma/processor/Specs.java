package com.github.gfx.android.orma.processor;

import com.squareup.javapoet.AnnotationSpec;

public class Specs {

    public static AnnotationSpec buildOverrideAnnotationSpec() {
        return AnnotationSpec.builder(Override.class)
                .build();
    }

    public static AnnotationSpec buildNonNullAnnotationSpec() {
        return AnnotationSpec.builder(Types.NonNull)
                .build();
    }

    public static AnnotationSpec buildNullableAnnotationSpec() {
        return AnnotationSpec.builder(Types.Nullable)
                .build();
    }

}
