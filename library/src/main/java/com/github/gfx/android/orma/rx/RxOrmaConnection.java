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

package com.github.gfx.android.orma.rx;

import com.github.gfx.android.orma.OrmaConnection;
import com.github.gfx.android.orma.OrmaDatabaseBuilderBase;
import com.github.gfx.android.orma.Schema;
import com.github.gfx.android.orma.Selector;
import com.github.gfx.android.orma.annotation.Experimental;
import com.github.gfx.android.orma.event.DataSetChangedEvent;
import com.github.gfx.android.orma.event.DataSetChangedTrigger;

import androidx.annotation.NonNull;

import java.util.List;

import io.reactivex.Observable;

/**
 * Low-level interface to Orma database connection with RxJava support.
 */
public class RxOrmaConnection extends OrmaConnection {

    final RxDataSetChangedTrigger trigger = new RxDataSetChangedTrigger();

    public RxOrmaConnection(@NonNull OrmaDatabaseBuilderBase<?> builder, List<Schema<?>> schemas) {
        super(builder, schemas);
    }

    @Experimental
    public <S extends Selector<?, ?>> Observable<DataSetChangedEvent<S>> createEventObservable(S selector) {
        return trigger.create(selector);
    }

    @Override
    protected DataSetChangedTrigger getTrigger() {
        return trigger;
    }
}
