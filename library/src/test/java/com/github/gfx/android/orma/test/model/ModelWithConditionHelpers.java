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
package com.github.gfx.android.orma.test.model;

import com.github.gfx.android.orma.annotation.Column;
import com.github.gfx.android.orma.annotation.Table;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

@Table
public class ModelWithConditionHelpers {

    @Column(indexed = true)
    @Nullable
    public String nullableText;

    @Column(indexed = true)
    @NonNull
    public String nonNullText = "";

    @Column(indexed = true)
    public boolean booleanValue;

    @Column(indexed = true)
    public byte byteValue;

    @Column(indexed = true)
    public short shortValue;

    @Column(indexed = true)
    public int intValue;

    @Column(indexed = true)
    public long longValue;

    @Column(indexed = true)
    public float floatValue;

    @Column(indexed = true)
    public double doubleValue;

}
