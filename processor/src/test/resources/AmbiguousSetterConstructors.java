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

import com.github.gfx.android.orma.annotation.Column;
import com.github.gfx.android.orma.annotation.PrimaryKey;
import com.github.gfx.android.orma.annotation.Setter;
import com.github.gfx.android.orma.annotation.Table;

@Table
public class AmbiguousSetterConstructors {

    @PrimaryKey
    public final String foo;

    @Column
    public final int bar;

    @Setter
    public AmbiguousSetterConstructors(String foo, int bar) {
        this.foo = foo;
        this.bar = bar;
    }

    public AmbiguousSetterConstructors(@Setter int bar, int baz) {
        foo = String.valueOf(System.currentTimeMillis());
        this.bar = bar;
    }
}
