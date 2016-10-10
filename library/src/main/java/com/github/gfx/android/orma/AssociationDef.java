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

import java.lang.reflect.Type;

public abstract class AssociationDef<Model, T, S extends Schema<T>> extends ColumnDef<Model, T> {

    public final S associationSchema;

    public AssociationDef(Schema<Model> schema, String name, Type type, String storageType, int flags, S associationSchema) {
        super(schema, name, type, storageType, flags);
        this.associationSchema = associationSchema;
    }
}
