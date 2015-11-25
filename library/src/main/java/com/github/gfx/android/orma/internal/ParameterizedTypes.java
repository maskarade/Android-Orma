package com.github.gfx.android.orma.internal;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * A utility class to get {@link Type} instance of a generic type.
 * e.g. {@code ParameterizedTypes.getType(new ParameterizedTypes.Holder<List<String>>(){}}
 */
public class ParameterizedTypes {

    public static Type getType(TypeHolder<?> typeHolder) {
        ParameterizedType t = (ParameterizedType) typeHolder.getClass().getGenericInterfaces()[0];
        return t.getActualTypeArguments()[0];
    }

    /**
     * A helper class to hold parameterized types.
     */
    @SuppressWarnings("unused")
    public interface TypeHolder<T> {

    }
}
