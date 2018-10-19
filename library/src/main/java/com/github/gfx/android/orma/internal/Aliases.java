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

package com.github.gfx.android.orma.internal;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.collection.SimpleArrayMap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * Manages table aliases from column paths / table names.
 *
 * Example:
 * <pre>
 * table B {
 *   long id;
 * }
 * table A {
 *   hasOne B foo;
 *   hasOne B bar;
 * }
 *
 * aliases.getAlias(A) -&gt; a1
 * aliases.getAlias(A.foo:B) -&gt; b1
 * aliases.getAlias(A.bar:B) -&gt; b2
 * </pre>
 */
public class Aliases {

    public final SimpleArrayMap<ColumnPath, String> map = new SimpleArrayMap<>();

    public ColumnPath createPath(@NonNull String tableName) {
        return new ColumnPath(null, null, tableName);
    }

    public class ColumnPath implements Comparable<ColumnPath> {

        @Nullable
        public final ColumnPath parent;

        @Nullable
        public final String columnName;

        @NonNull
        public final String tableName;

        @NonNull
        private final String serializedCache;

        public ColumnPath(@Nullable ColumnPath parent, @Nullable String columnName, @NonNull String tableName) {
            this.parent = parent;
            this.columnName = columnName;
            this.tableName = tableName;
            this.serializedCache = serialize();
        }

        @NonNull
        public synchronized String getAlias() {
            String alias = map.get(this);
            if (alias == null) {
                alias = tableName.substring(0, 1).toLowerCase(Locale.getDefault()) + (map.size() + 1);
                map.put(this, alias);
            }
            return alias;
        }

        public ColumnPath add(@NonNull String columnName, @NonNull String tableName) {
            return new ColumnPath(this, columnName, tableName);
        }

        private List<ColumnPath> flatten() {
            List<ColumnPath> flatten = new ArrayList<>();
            ColumnPath path = this;
            while (path.parent != null) {
                flatten.add(path);
                path = path.parent;
            }
            flatten.add(path);
            Collections.reverse(flatten);
            return flatten;
        }

        private String serialize() {
            StringBuilder s = new StringBuilder();

            List<ColumnPath> flatten = flatten();
            ColumnPath first = flatten.remove(0);
            s.append(first.tableName);
            // first component has no columnName

            for (ColumnPath path : flatten) {
                s.append('.');
                s.append(path.columnName);
                s.append(':');
                s.append(path.tableName);
            }
            return s.toString();
        }

        @Override
        public int compareTo(@NonNull ColumnPath other) {
            return serializedCache.compareTo(other.serializedCache);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            ColumnPath that = (ColumnPath) o;
            return serializedCache.equals(that.serializedCache);
        }

        @Override
        public int hashCode() {
            return serializedCache.hashCode();
        }

        @Override
        public String toString() {
            return serializedCache;
        }
    }
}
