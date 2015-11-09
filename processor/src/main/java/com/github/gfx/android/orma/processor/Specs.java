package com.github.gfx.android.orma.processor;

import com.squareup.javapoet.AnnotationSpec;

import javax.annotation.Generated;

public class Specs {
    public static AnnotationSpec buildGeneratedAnnotationSpec() {
        return AnnotationSpec.builder(Generated.class)
                .addMember("value", "$S", OrmaProcessor.class.getCanonicalName())
                .build();
    }

}
