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

package com.github.gfx.android.orma.example.orma;

import com.github.gfx.android.orma.ModelFactory;
import com.github.gfx.android.orma.SingleAssociation;
import com.github.gfx.android.orma.annotation.Column;
import com.github.gfx.android.orma.annotation.OnConflict;
import com.github.gfx.android.orma.annotation.PrimaryKey;
import com.github.gfx.android.orma.annotation.Setter;
import com.github.gfx.android.orma.annotation.Table;

import android.support.annotation.NonNull;

@Table
public class Category {

    @PrimaryKey
    public final long id;

    @Column(uniqueOnConflict = OnConflict.IGNORE)
    public final  String name;

    @Setter
    public Category(long id, @NonNull String name) {
        this.id = id;
        this.name = name;
    }

    public Category(@NonNull String name) {
        this(0, name);
    }

    public Item_Relation getItems(OrmaDatabase orma) {
        return orma.relationOfItem().categoryEq(this);
    }

    public Item createItem(OrmaDatabase orma, final String name) {
        return orma.createItem(new ModelFactory<Item>() {
            @NonNull
            @Override
            public Item call() {
                return new Item(name, SingleAssociation.just(id, Category.this));
            }
        });
    }

    @Override
    public String toString() {
        return "Category:" + name;
    }
}
