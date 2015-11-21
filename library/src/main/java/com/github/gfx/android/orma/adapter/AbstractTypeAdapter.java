package com.github.gfx.android.orma.adapter;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public abstract class AbstractTypeAdapter<SourceType> implements TypeAdapter<SourceType> {

    @Override
    public Type getSourceType() {
        ParameterizedType superClass = (ParameterizedType) getClass().getGenericSuperclass();
        return superClass.getActualTypeArguments()[0];
    }
}
