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

package com.github.gfx.android.orma.processor.tool;

import com.github.gfx.android.orma.processor.generator.SqlGenerator;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Allocates table aliases from column paths and table names.
 *
 * Example:
 * <code>
 *     table B {
 *         long id;
 *     }
 *     table A {
 *         hasOne B foo;
 *         hasOne B bar;
 *     }

 *     allocator.getAlias(A) -> a1
 *     allocator.getAlias(A.foo) -> a1
 *     allocator.getQualifiedName(A.foo) -> a1.foo
 *     allocator.getQualifiedName(A.foo:B.id) -> b1.id
 *     allocator.getQualifiedName(A.bar:B.id) -> b2.id
 * </code>
 */
public class AliasAllocator {

    public final Map<ColumnPath, String> map = new HashMap<>();

    public String getAlias(ColumnPath path) {
        return map.computeIfAbsent(path, key -> key.tableName.substring(0, 1).toLowerCase() + (map.size() + 1));
    }

    public String getQualifiedName(ColumnPath path, SqlGenerator sqlg) {
        assert path.columnName != null;

        StringBuilder sb = new StringBuilder();
        sqlg.appendIdentifier(sb, getAlias(path));
        sb.append('.');
        sqlg.appendIdentifier(sb, path.columnName);
        return sb.toString();
    }

    public static final class ColumnPath {

        public final ColumnPath parent;

        public final String tableName;

        public final String columnName;

        private final String serialized;

        public ColumnPath(@Nullable ColumnPath parent, @NonNull String tableName, @Nullable String columnName) {
            this.parent = parent;
            this.tableName = tableName;
            this.columnName = columnName;
            this.serialized = serialize();
        }

        public ColumnPath(@Nullable ColumnPath parent, @NonNull String tableName) {
            this(parent, tableName, null);
        }

        public static Builder builder() {
            return new Builder(null);
        }

        public static Builder builder(@Nullable ColumnPath root) {
            return new Builder(root);
        }

        public ColumnPath withColumnName(@NonNull String columnName) {
            return new ColumnPath(parent, tableName, columnName);
        }

        public ColumnPath add(@NonNull String tableName, @NonNull String columnName) {
            return new ColumnPath(this, tableName, columnName);
        }

        public ColumnPath add(@NonNull String tableName) {
            return new ColumnPath(this, tableName);
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
            ColumnPath last = flatten.remove(flatten.size() - 1);
            flatten.forEach(path -> {
                s.append(path.tableName);
                s.append('.');

                if (path.columnName == null) {
                    throw new AssertionError("columnName == null found in " + s);
                }
                s.append(path.columnName);
                s.append('.');
            });
            s.append(last.tableName);
            // omit the columnName of the last component

            return s.toString();
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
            return serialized.equals(that.serialized);
        }

        @Override
        public int hashCode() {
            return serialized.hashCode();
        }

        @Override
        public String toString() {
            return serialized;
        }

        public static class Builder {

            ColumnPath path;

            public Builder(ColumnPath path) {
                this.path = path;
            }

            public Builder add(String tableName, String columnName) {
                path = new ColumnPath(path, tableName, columnName);
                return this;
            }

            public Builder add(String tableName) {
                path = new ColumnPath(path, tableName);
                return this;
            }

            public ColumnPath build() {
                return path;
            }
        }
    }
}
