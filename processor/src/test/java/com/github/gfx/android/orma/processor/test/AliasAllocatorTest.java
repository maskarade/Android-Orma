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

package com.github.gfx.android.orma.processor.test;

import com.github.gfx.android.orma.processor.generator.SqlGenerator;
import com.github.gfx.android.orma.processor.tool.AliasAllocator;

import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;


public class AliasAllocatorTest {

    @Test
    public void columnPathWithoutParent() throws Exception {
        AliasAllocator.ColumnPath path = AliasAllocator.ColumnPath.builder()
                .add("foo", "bar")
                .build();

        assertThat(path.toString()).isEqualTo("foo");
    }

    @Test
    public void columnPathWithParent() throws Exception {
        AliasAllocator.ColumnPath path = AliasAllocator.ColumnPath.builder()
                .add("t1", "c1")
                .add("t2", "c2")
                .add("t3", "c3")
                .build();

        assertThat(path.toString()).isEqualTo("t1.c1.t2.c2.t3");
    }

    @Test
    public void getAlias() throws Exception {
        AliasAllocator allocator = new AliasAllocator();

        assertThat(allocator.getAlias(AliasAllocator.ColumnPath.builder()
                .add("a", "x")
                .add("b", "x")
                .add("c", "x")
                .build()
        )).isEqualTo("c1");

        assertThat(allocator.getAlias(AliasAllocator.ColumnPath.builder()
                .add("a", "x")
                .add("b", "x")
                .add("c")
                .build()
        )).isEqualTo("c1");

        assertThat(allocator.getAlias(AliasAllocator.ColumnPath.builder()
                .add("a", "x")
                .add("b", "x")
                .build()
        )).isEqualTo("b2");

        assertThat(allocator.getAlias(AliasAllocator.ColumnPath.builder()
                .add("a", "x")
                .build()
        )).isEqualTo("a3");
    }

    @Test
    public void getQualifiedName() throws Exception {
        AliasAllocator allocator = new AliasAllocator();
        SqlGenerator sqlg = new SqlGenerator();

        assertThat(allocator.getQualifiedName(AliasAllocator.ColumnPath.builder()
                        .add("a", "x")
                        .add("b", "x")
                        .add("c", "x")
                        .build()
                , sqlg)).isEqualTo("`c1`.`x`");

        assertThat(allocator.getQualifiedName(AliasAllocator.ColumnPath.builder()
                        .add("a", "x")
                        .add("b", "x")
                        .build()
                , sqlg)).isEqualTo("`b2`.`x`");

        assertThat(allocator.getQualifiedName(AliasAllocator.ColumnPath.builder()
                        .add("a", "x")
                        .build()
                , sqlg)).isEqualTo("`a3`.`x`");

        assertThat(allocator.getQualifiedName(AliasAllocator.ColumnPath.builder()
                        .add("a", "y")
                        .build()
                , sqlg)).isEqualTo("`a3`.`y`");
    }

    @Test
    public void fromTableName() throws Exception {
        AliasAllocator allocator = new AliasAllocator();

        assertThat(allocator.getAlias(AliasAllocator.ColumnPath.builder()
                .add("Test")
                .build()
        )).isEqualTo("t1");
    }

}
