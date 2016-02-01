/*
 * Copyright (c) 2015 FUJI Goro (gfx).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.gfx.android.orma.adapter;

import com.github.gfx.android.orma.internal.EquatableTypeWrapper;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * Deprecated; use {@link com.github.gfx.android.orma.annotation.StaticTypeAdapter} instead.
 *
 * @param <SourceType> the target type
 */
@Deprecated
public abstract class AbstractTypeAdapter<SourceType> implements TypeAdapter<SourceType> {

    @NonNull
    @Override
    public Type getSourceType() {
        ParameterizedType superClass = (ParameterizedType) getClass().getGenericSuperclass();
        return EquatableTypeWrapper.wrap(superClass.getActualTypeArguments()[0]);
    }

    @Nullable
    @Override
    public String serializeNullable(@Nullable SourceType source) {
        return source == null ? null : serialize(source);
    }

    @Nullable
    @Override
    public SourceType deserializeNullable(@Nullable String serialized) {
        return serialized == null ? null : deserialize(serialized);
    }
}
