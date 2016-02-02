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
import com.github.gfx.android.orma.test.toolbox.IntTuple2;

import android.support.annotation.NonNull;

@StaticTypeAdapter(
        targetType = IntTuple2.class,
        serializedType = long.class
)
public class IntTuple2Adapter {

    public static long serialize(@NonNull IntTuple2 tuple) {
        return ((long) tuple.first << 32) | (long) tuple.second;
    }

    @NonNull
    public static IntTuple2 deserialize(long serialized) {
        return new IntTuple2((int) (serialized >> 32), (int) serialized);
    }
}
