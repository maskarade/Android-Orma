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
                new SqlDateAdapter(),
                new SqlTimeAdapter(),
                new SqlTimestampAdapter(),
                new UUIDAdapter(),
                new BigDecimalAdapter(),
                new BigIntegerAdapter(),
                new CurrencyAdapter(),
        };
    }

    public <SourceType> void add(@NonNull TypeAdapter<SourceType> adapter) {
        //System.out.println("XXX " + adapter.getSourceType() + " " + adapter.getSourceType().getClass());
        adapters.put(adapter.getSourceType(), adapter);
    }

    public void addAll(@NonNull TypeAdapter<?>... adapters) {
        for (TypeAdapter<?> typeAdapter : adapters) {
            add(typeAdapter);
        }
    }

    @NonNull
    public <SourceType> TypeAdapter<SourceType> get(@NonNull Class<SourceType> sourceType) {
        return get((Type) sourceType);
    }

    @NonNull
    public <SourceType> TypeAdapter<SourceType> get(@NonNull Type sourceType) {
        @SuppressWarnings("unchecked")
        TypeAdapter<SourceType> adapter = (TypeAdapter<SourceType>) adapters.get(sourceType);
        if (adapter == null) {
            throw new TypeAdapterNotFoundException("No TypeAdapter found for " + sourceType);
        }
        return adapter;
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
