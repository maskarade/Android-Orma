package com.github.gfx.android.orma.adapter;

import java.lang.reflect.Type;

public interface TypeAdapter<SourceType> {

    Type getSourceType();

    String serialize(SourceType source);

    SourceType deserialize(String serialized);

}
