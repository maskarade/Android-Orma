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

/**
 * Models which have multiple direct associations
 */
@Table
public class ModelWithDirectAssociation2 {

    @PrimaryKey
    public String name;

    @Column(indexed = true)
    public Author author;

    @Column(indexed = true)
    public Author author2;

    @Column(indexed = true)
    public Publisher publisher;

    @Column(indexed = true)
    public Publisher publisher2;

    @Column(indexed = true)
    public String note; // the same name as Author#note
}
