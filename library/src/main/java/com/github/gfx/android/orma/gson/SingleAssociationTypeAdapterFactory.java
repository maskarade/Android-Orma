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

package com.github.gfx.android.orma.gson;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import com.github.gfx.android.orma.DatabaseHandle;
import com.github.gfx.android.orma.Schema;
import com.github.gfx.android.orma.SingleAssociation;

import android.support.annotation.NonNull;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;

/**
 * A Gson {@link TypeAdapterFactory} to handle Orma {@link SingleAssociation}.
 */
public class SingleAssociationTypeAdapterFactory implements TypeAdapterFactory {

    final DatabaseHandle orma;

    public SingleAssociationTypeAdapterFactory(@NonNull DatabaseHandle orma) {
        this.orma = orma;
    }

    @NonNull
    Schema<?> findSchema(Class<?> modelClass) {
        for (Schema<?> schema : orma.getSchemas()) {
            if (schema.getModelClass().equals(modelClass)) {
                return schema;
            }
        }
        throw new RuntimeException("No schema found for " + modelClass);
    }

    @SuppressWarnings("raw")
    @Override
    public <T> TypeAdapter<T> create(final Gson gson, TypeToken<T> typeToken) {
        if (!typeToken.getRawType().isAssignableFrom(SingleAssociation.class)) {
            return null;
        }
        if (!(typeToken.getType() instanceof ParameterizedType)) {
            throw new ClassCastException("Expected a parameterized SingleAssociation<T>, but got " + typeToken.getType());
        }

        final Class<?> modelType = (Class<?>) ((ParameterizedType) typeToken.getType())
                .getActualTypeArguments()[0];

        final Schema schema = findSchema(modelType);

        return new TypeAdapter<T>() {

            @Override
            public void write(JsonWriter out, T value) throws IOException {
                SingleAssociation<?> association = (SingleAssociation<?>) value;
                gson.toJson(association.value(), modelType, out);
            }

            @Override
            @SuppressWarnings("unchecked")
            public T read(JsonReader in) throws IOException {
                Object model = gson.fromJson(in, modelType);
                return (T) SingleAssociation.just(schema, model);
            }
        };
    }
}
