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
import com.github.gfx.android.orma.test.toolbox.MutableInt;
import com.github.gfx.android.orma.test.toolbox.MutableLong;

@StaticTypeAdapters({
        @StaticTypeAdapter(
                targetType = MutableInt.class,
                serializedType = int.class,
                serializer = "serializeMutableInt",
                deserializer = "deserializeMutableInt"
        ),
        @StaticTypeAdapter(
                targetType = MutableLong.class,
                serializedType = long.class,
                serializer = "serializeMutableLong",
                deserializer = "deserializeMutableLong"
        )
})
public class TypeAdapters {

    public static int serializeMutableInt(MutableInt target) {
        return target.value;
    }

    public static MutableInt deserializeMutableInt(int deserialized) {
        return new MutableInt(deserialized);
    }

    public static long serializeMutableLong(MutableLong target) {
        return target.value;
    }

    public static MutableLong deserializeMutableLong(long deserialized) {
        return new MutableLong(deserialized);
    }
}
