package com.github.gfx.android.orma.processor;

import com.squareup.javapoet.TypeSpec;

import javax.annotation.processing.ProcessingEnvironment;

public abstract class BaseWriter {

    protected final ProcessingEnvironment processingEnv;

    public BaseWriter(ProcessingEnvironment processingEnv) {
        this.processingEnv = processingEnv;
    }

    public abstract TypeSpec buildTypeSpec();
}
