package com.github.gfx.android.orma.adapter;

import android.support.annotation.NonNull;

import java.lang.reflect.Type;

public interface TypeAdapter<SourceType> {

    Type getSourceType();

    @NonNull
    String serialize(@NonNull SourceType source);

    @NonNull
    SourceType deserialize(@NonNull String serialized);
}
