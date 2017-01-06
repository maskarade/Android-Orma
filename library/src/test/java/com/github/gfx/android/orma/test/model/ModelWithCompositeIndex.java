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
import com.github.gfx.android.orma.annotation.Index;
import com.github.gfx.android.orma.annotation.Setter;
import com.github.gfx.android.orma.annotation.Table;

// keywords: compound index, multi-column index, multicolumn index, composite index
@Table(indexes = {
        @Index({"c1", "c2"}),
        @Index(
                value = {"c4", "c3"},
                unique = true,
                name = "custom_index_on_ModelWithCompositeIndex",
                helpers = Column.Helpers.NONE
        )
})
public class ModelWithCompositeIndex {

    @Column
    public long c1;

    @Column
    public String c2;

    @Column
    public long c3;

    @Column
    public String c4;

    @Setter
    public ModelWithCompositeIndex(long c1, String c2, long c3, String c4) {
        this.c1 = c1;
        this.c2 = c2;
        this.c3 = c3;
        this.c4 = c4;
    }
}
