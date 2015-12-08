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

import com.github.gfx.android.orma.exception.TypeAdapterNotFoundException;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class TypeAdapterRegistry {

    final Map<Type, TypeAdapter<?>> adapters = new HashMap<>();

    public static TypeAdapter<?>[] defaultTypeAdapters() {
        return new TypeAdapter<?>[]{
                new StringListAdapter(),
                new StringSetAdapter(),
                new UriAdapter(),
                new DateAdapter(),
        };
    }

    public <SourceType> void add(@NonNull TypeAdapter<SourceType> adapter) {
        adapters.put(adapter.getSourceType(), adapter);
    }

    public void addAll(@NonNull TypeAdapter<?>... adapters) {
        for (TypeAdapter<?> typeAdapter : adapters) {
            add(typeAdapter);
        }
    }

    @NonNull
    @SuppressWarnings("unchecked")
    public <SourceType> String serialize(@NonNull Type sourceType, @NonNull SourceType source) {
        TypeAdapter<SourceType> adapter = (TypeAdapter<SourceType>) adapters.get(sourceType);
        if (adapter == null) {
            throw new TypeAdapterNotFoundException("No TypeAdapter found for " + sourceType);
        }
        return adapter.serialize(source);
    }

    @NonNull
    @SuppressWarnings("unchecked")
    public <SourceType> SourceType deserialize(@NonNull Type sourceType, @NonNull String serialized) {
        TypeAdapter<SourceType> adapter = (TypeAdapter<SourceType>) adapters.get(sourceType);
        if (adapter == null) {
            throw new TypeAdapterNotFoundException("No TypeAdapter found for " + sourceType);
        }
        return adapter.deserialize(serialized);
    }

    @Nullable
    @SuppressWarnings("unchecked")
    public <SourceType> String serializeNullable(@NonNull Type sourceType, @Nullable SourceType source) {
        return source == null ? null : serialize(sourceType, source);
    }

    @Nullable
    @SuppressWarnings("unchecked")
    public <SourceType> SourceType deserializeNullable(@NonNull Type sourceType, @Nullable String serialized) {
        return serialized == null ? null : (SourceType) deserialize(sourceType, serialized);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append("{");
        ArrayList<String> pairs = new ArrayList<>();
        for (Map.Entry<Type, TypeAdapter<?>> entry : adapters.entrySet()) {
            pairs.add(entry.getKey() + ": " + entry.getValue());
        }
        sb.append(TextUtils.join(", ", pairs));
        sb.append("}");
        return sb.toString();
    }
}
