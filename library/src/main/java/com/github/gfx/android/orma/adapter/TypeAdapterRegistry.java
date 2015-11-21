package com.github.gfx.android.orma.adapter;

import com.github.gfx.android.orma.exception.TypeAdapterNotFoundException;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class TypeAdapterRegistry {

    public static TypeAdapter<?>[] defaultTypeAdapters() {
        return new TypeAdapter[]{
                new StringListAdapter(),
                new StringSetAdapter(),
                new UriAdapter(),
        };
    }

    final Map<Type, TypeAdapter<?>> adapters = new HashMap<>();

    public <SourceType> void add(TypeAdapter<SourceType> adapter) {
        adapters.put(adapter.getSourceType(), adapter);
    }

    @SuppressWarnings("unchecked")
    public <SourceType> String serialize(Type sourceType, SourceType source) {
        TypeAdapter<SourceType> adapter = (TypeAdapter<SourceType>) adapters.get(sourceType);
        if (adapter == null) {
            throw new TypeAdapterNotFoundException("No TypeAdapter found for " + sourceType);
        }
        return adapter.serialize(source);
    }

    @SuppressWarnings("unchecked")
    public <SourceType> SourceType deserialize(Type sourceType, String serialized) {
        TypeAdapter<SourceType> adapter = (TypeAdapter<SourceType>) adapters.get(sourceType);
        if (adapter == null) {
            throw new TypeAdapterNotFoundException("No TypeAdapter found for " + sourceType);
        }
        return adapter.deserialize(serialized);
    }
}
