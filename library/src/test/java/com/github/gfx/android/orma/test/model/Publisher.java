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

import com.google.gson.annotations.SerializedName;

import com.github.gfx.android.orma.annotation.Column;
import com.github.gfx.android.orma.annotation.PrimaryKey;
import com.github.gfx.android.orma.annotation.Table;

import android.support.annotation.NonNull;

@Table(value = "publishers",
        schemaClassName = "PublisherSchema",
        relationClassName = "PublisherRelation",
        selectorClassName = "PublisherSelector",
        updaterClassName = "PublisherUpdater",
        deleterClassName = "PublisherDeleter"
)
public class Publisher {

    @PrimaryKey(autoincrement = true)
    public long id;

    @Column(unique = true)
    public String name;

    @Column
    @SerializedName("started_year")
    public int startedYear;

    @Column("started_month")
    public int startedMonth;

    public Book_Selector books(OrmaDatabase orma) {
        return orma.selectFromBook().publisherEq(this);
    }

    public static Publisher create(@NonNull String name, int startedYear, int startedMonth) {
        Publisher publisher = new Publisher();
        publisher.name = name;
        publisher.startedYear = startedYear;
        publisher.startedMonth = startedMonth;
        return publisher;
    }
}
