package com.github.gfx.android.orma.processor;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;

public class RelationDefinition {

    final ClassName relationType;

    final TypeName modelType;

    public RelationDefinition(ClassName relationType, TypeName modelType) {
        this.relationType = relationType;
        this.modelType = modelType;
    }
}
