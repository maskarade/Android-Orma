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

import com.github.gfx.android.orma.Schema;
import com.github.gfx.android.orma.annotation.Experimental;
import com.github.gfx.android.orma.core.Database;

import androidx.annotation.RestrictTo;

/**
 * Helper class for query observables. This class is NOT thread-safe.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
@Experimental
public interface DataSetChangedTrigger {

    <Model> void fire(Database db, DataSetChangedEvent.Type type, Schema<Model> schema);

    void fireForTransaction();

    class NoOp implements DataSetChangedTrigger {

        @Override
        public <Model> void fire(Database db, DataSetChangedEvent.Type type, Schema<Model> schema) {
            // Nothing to do
        }

        @Override
        public void fireForTransaction() {
            // Nothing to do
        }
    }
}
