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

package com.github.gfx.android.orma.test.type_adapter;

import com.github.gfx.android.orma.annotation.StaticTypeAdapter;
import com.github.gfx.android.orma.annotation.StaticTypeAdapters;
import com.github.gfx.android.orma.test.toolbox.EnumA;
import com.github.gfx.android.orma.test.toolbox.EnumB;
import com.github.gfx.android.orma.test.toolbox.EnumDescription;

import android.support.annotation.NonNull;

/**
 * An example for generic type adapters; its {@code deserialize ()} methods takes a {@code Class<T> type} parameter.
 */
@StaticTypeAdapter(
        targetType = EnumDescription.class,
        serializedType = String.class
)
public class EnumTypeAdapter {

    public static <T extends Enum<T> & EnumDescription> String serialize(@NonNull T value) {
        return value.name();
    }

    @SuppressWarnings("unchecked")
    @NonNull
    public static <T extends Enum<T> & EnumDescription> T deserialize(@NonNull String serialized, @NonNull Class<T> type) {
        return Enum.valueOf(type, serialized);
    }
}
