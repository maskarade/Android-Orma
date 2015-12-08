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
package com.github.gfx.android.orma;

import android.support.annotation.NonNull;

import java.lang.reflect.Type;

public class ColumnDef<T /* type param is not used */> {

    public final String name;

    public final Type type;

    public final boolean nullable;

    public final boolean primaryKey;

    public final boolean autoincrement;

    public final boolean autoId;

    public final boolean indexed;

    public final boolean unique;

    public ColumnDef(String name, Type type, boolean nullable, boolean primaryKey, boolean autoincrement, boolean autoId,
            boolean indexed, boolean unique) {
        this.name = name;
        this.type = type;
        this.nullable = nullable;
        this.primaryKey = primaryKey;
        this.autoincrement = autoincrement;
        this.autoId = autoId;
        this.indexed = indexed;
        this.unique = unique;
    }

    @NonNull
    @Override
    public String toString() {
        return name;
    }
}
