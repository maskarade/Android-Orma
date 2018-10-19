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

import androidx.annotation.Nullable;

/**
 * @see ModelWithBoxTypes_Schema
 * @see ModelWithPrimitives
 */
@Table
public class ModelWithBoxTypes {

    @Column
    public Boolean booleanValue;

    @Column
    public Byte byteValue;

    @Column
    public Short shortValue;

    @Column
    public Integer intValue;

    @Column
    public Long longValue;

    @Column
    public Float floatValue;

    @Column
    public Double doubleValue;

    @Column
    @Nullable
    public Boolean nullableBooleanValue;

    @Column
    @Nullable
    public Byte nullableByteValue;

    @Column
    @Nullable
    public Short nullableShortValue;

    @Column
    @Nullable
    public Integer nullableIntValue;

    @Column
    @Nullable
    public Long nullableLongValue;

    @Column
    @Nullable
    public Float nullableFloatValue;

    @Column
    @Nullable
    public Double nullableDoubleValue;

}
