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
import com.github.gfx.android.orma.annotation.PrimaryKey;
import com.github.gfx.android.orma.annotation.Table;
import com.github.gfx.android.orma.test.toolbox.EnumA;
import com.github.gfx.android.orma.test.toolbox.EnumB;
import com.github.gfx.android.orma.test.toolbox.IntTuple2;
import com.github.gfx.android.orma.test.toolbox.MutableInt;
import com.github.gfx.android.orma.test.toolbox.MutableLong;

import android.net.Uri;
import android.support.annotation.Nullable;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Currency;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Table
public class ModelWithTypeAdapters {

    @PrimaryKey
    public long id;

    @Column(indexed = true)
    public List<String> list;

    @Column(indexed = true)
    public Set<String> set;

    @Column(indexed = true)
    public ArrayList<String> arrayList;

    @Column(indexed = true)
    public HashSet<String> hashSet;

    @Column(indexed = true)
    public Uri uri;

    @Column(indexed = true)
    public Date date;

    @Column(indexed = true)
    public java.sql.Date sqlDate;

    @Column(indexed = true)
    public java.sql.Time sqlTime;

    @Column(indexed = true)
    public java.sql.Timestamp sqlTimestamp;

    @Column(indexed = true)
    public BigDecimal bigDecimal;

    @Column(indexed = true)
    public BigInteger bigInteger;

    @Column(indexed = true)
    public UUID uuid;

    @Column(indexed = true)
    public Currency currency;

    @Column(indexed = true)
    public ByteBuffer byteBuffer;

    @Column(indexed = true)
    public IntTuple2 intTuple2;

    @Column(indexed = true)
    public MutableInt mutableInt;

    @Column(indexed = true)
    public MutableLong mutableLong;

    @Nullable
    @Column(indexed = true)
    public List<String> nullableList;

    @Nullable
    @Column(indexed = true)
    public Set<String> nullableSet;

    @Nullable
    @Column(indexed = true)
    public LinkedList<String> nullableLinkedList;

    @Nullable
    @Column(indexed = true)
    public LinkedHashSet<String> nullableLinkedHashSet;

    @Nullable
    @Column(indexed = true)
    public Uri nullableUri;

    @Nullable
    @Column(indexed = true)
    public Date nullableDate;

    @Column(indexed = true)
    @Nullable
    public IntTuple2 nullableIntTuple2;

    @Column(indexed = true)
    @Nullable
    public ByteBuffer nullableByteBuffer;

    @Column(indexed = true)
    public EnumA enumA;

    @Column(indexed = true)
    public EnumB enumB;
}
