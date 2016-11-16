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

package com.github.gfx.android.orma.event;

import com.github.gfx.android.orma.Selector;
import com.github.gfx.android.orma.annotation.Experimental;

@Experimental
public class DataSetChangedEvent<S extends Selector<?, ?>> {
    public enum Type {
        INSERT,
        UPDATE,
        DELETE,
        TRANSACTION,
    }

    private final Type type;

    private final S selector;

    public DataSetChangedEvent(Type type, S selector) {
        this.type = type;
        this.selector = selector;
    }

    public Type getType() {
        return type;
    }

    public S getSelector() {
        return selector;
    }
}
