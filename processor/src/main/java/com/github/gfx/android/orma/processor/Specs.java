package com.github.gfx.android.orma.processor;

import com.squareup.javapoet.AnnotationSpec;

import javax.annotation.Generated;

public class Specs {

    public static AnnotationSpec buildGeneratedAnnotationSpec() {
        return AnnotationSpec.builder(Generated.class)
                .addMember("value", "$S", OrmaProcessor.class.getCanonicalName())
                .build();
    }

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
