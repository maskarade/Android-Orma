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

package com.github.gfx.android.orma.test.toolbox;

import com.github.gfx.android.orma.annotation.StaticTypeAdapter;

import android.support.annotation.NonNull;

/**
 * A demo class to show how {@link com.github.gfx.android.orma.annotation.StaticTypeAdapter} works.
 */
@StaticTypeAdapter(
        targetType = IntTuple2.class,
        serializedType = long.class
)
public class IntTuple2 {

    public static long serialize(@NonNull IntTuple2 tuple) {
        return ((long) tuple.first << 32) | (long) tuple.second;
    }

    @NonNull
    public static IntTuple2 deserialize(long serialized) {
        return new IntTuple2((int) (serialized >> 32), (int) serialized);
    }


    public final int first;

    public final int second;

    public IntTuple2(int first, int second) {
        this.first = first;
        this.second = second;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof IntTuple2)) {
            return false;
        }

        IntTuple2 intTuple2 = (IntTuple2) o;

        if (first != intTuple2.first) {
            return false;
        }
        return second == intTuple2.second;

    }

    @Override
    public int hashCode() {
        int result = first;
        result = 31 * result + second;
        return result;
    }
}
