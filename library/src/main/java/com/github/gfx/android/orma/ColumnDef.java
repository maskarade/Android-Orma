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

public abstract class ColumnDef<Model, T> {

    public static int PRIMARY_KEY = 0x01;

    public static int AUTOINCREMENT = 0x02;

    public static int AUTO_VALUE = 0x04;

    public static int NULLABLE = 0x08;

    public static int INDEXED = 0x10;

    public static int UNIQUE = 0x20;

    public final Schema<Model> schema;

    public final String name;

    public final Type type;

    public final String storageType;

    public final int flags;

    public ColumnDef(Schema<Model> schema, String name, Type type, String storageType, int flags) {
        this.schema = schema;
        this.name = name;
        this.type = type;
        this.storageType = storageType;
        this.flags = flags;
    }

    public String getEscapedName() {
        return "\"" + name + '"';
    }

    public String getFullyQualifiedName() {
        return "\"" + schema.getTableName() + '"' +
                '.' +
                '"' + name + '"';
    }

    private boolean checkFlags(int flags) {
        return (this.flags & flags) == flags;
    }

    public boolean isPrimaryKey() {
        return checkFlags(PRIMARY_KEY);
    }

    public boolean isAutoincremnt() {
        return checkFlags(AUTOINCREMENT);
    }

    public boolean isAutoValue() {
        return checkFlags(AUTO_VALUE);
    }

    public boolean isNullable() {
        return checkFlags(NULLABLE);
    }

    public boolean isIndexed() {
        return checkFlags(INDEXED);
    }

    public boolean isUnique() {
        return checkFlags(UNIQUE);
    }

    public abstract T get(@NonNull Model model);

    public abstract Object getSerialized(@NonNull Model model);

    public OrderSpec<Model> orderInAscending() {
        return new OrderSpec<>(this, OrderSpec.ASC);
    }

    public OrderSpec<Model> orderInDescending() {
        return new OrderSpec<>(this, OrderSpec.DESC);
    }

    @NonNull
    @Override
    public String toString() {
        return schema.getModelClass().getSimpleName() + '#' + name;
    }
}
