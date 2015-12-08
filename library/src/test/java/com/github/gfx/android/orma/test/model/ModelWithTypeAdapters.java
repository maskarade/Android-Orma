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

import android.net.Uri;
import android.support.annotation.Nullable;

import java.util.Date;
import java.util.List;
import java.util.Set;

@Table
public class ModelWithTypeAdapters {

    @PrimaryKey
    public long id;

    @Column
    public List<String> list;

    @Column
    public Set<String> set;

    @Column
    public Uri uri;

    @Column
    public Date date;

    @Nullable
    @Column
    public List<String> nullableList;

    @Nullable
    @Column
    public Set<String> nullableSet;

    @Nullable
    @Column
    public Uri nullableUri;

    @Nullable
    @Column
    public Date nullableDate;

}
