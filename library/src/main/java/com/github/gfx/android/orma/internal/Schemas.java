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

package com.github.gfx.android.orma.internal;

import com.github.gfx.android.orma.Schema;

import android.support.annotation.NonNull;

import java.util.HashMap;
import java.util.Map;

/**
 * The set of all the {@link Schema} instances.
 */
public class Schemas {

    static final Map<Class<?>, Schema<?>> SCHEMAS = new HashMap<>();

    public static <M, T extends Schema<M>> T register(@NonNull T schema) {
        SCHEMAS.put(schema.getModelClass(), schema);
        return schema;
    }

    @NonNull
    @SuppressWarnings("unchecked")
    public static <T> Schema<T> get(@NonNull Class<T> modelClass) {
        Schema<T> schema = (Schema<T>) SCHEMAS.get(modelClass);
        if (schema == null) {
            throw new RuntimeException("No schema found for " + modelClass);
        }
        return schema;
    }
}
