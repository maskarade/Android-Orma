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

package com.github.gfx.android.orma.processor.model;

import com.github.gfx.android.orma.annotation.Column;

import java.util.Arrays;
import java.util.List;

public class IndexDefinition {

    public final String name;

    public final boolean unique;

    @Column.Helpers
    public final long helperFlags;

    public final List<ColumnDefinition> columns;

    public IndexDefinition(String name, boolean unique, @Column.Helpers long helperFlags, List<ColumnDefinition> columns) {
        this.name = name;
        this.unique = unique;
        this.helperFlags = helperFlags == Column.Helpers.AUTO ? Column.Helpers.ALL : helperFlags;
        this.columns = columns;
    }

    public IndexDefinition(String name, boolean unique, @Column.Helpers long helperFlags, ColumnDefinition... columns) {
        this(name, unique, helperFlags, Arrays.asList(columns));
    }

    public boolean hasHelper(@Column.Helpers long f) {
        assert f != Column.Helpers.NONE && f != Column.Helpers.AUTO;
        return (helperFlags & f) == f;
    }
}
