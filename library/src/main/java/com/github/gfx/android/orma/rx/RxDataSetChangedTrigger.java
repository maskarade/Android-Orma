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

import com.github.gfx.android.orma.Schema;
import com.github.gfx.android.orma.Selector;
import com.github.gfx.android.orma.annotation.Experimental;
import com.github.gfx.android.orma.core.Database;
import com.github.gfx.android.orma.event.DataSetChangedEvent;
import com.github.gfx.android.orma.event.DataSetChangedTrigger;

import android.support.annotation.RestrictTo;

import java.lang.ref.WeakReference;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.subjects.PublishSubject;

/**
 * Helper class for query observables. This class is NOT thread-safe.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
@Experimental
public class RxDataSetChangedTrigger implements DataSetChangedTrigger {

    // TODO: use well-tested concurrent weak maps
    final Map<WeakReference<Observer<DataSetChangedEvent<?>>>, Selector<?, ?>> observerMap = new ConcurrentHashMap<>();

    Set<Schema<?>> changedDataSetInTransaction = null;

    public <S extends Selector<?, ?>> Observable<DataSetChangedEvent<S>> create(S selector) {
        PublishSubject<DataSetChangedEvent<S>> subject = PublishSubject.create();
        register(subject, selector);
        return subject;
    }

    @SuppressWarnings("unchecked")
    public <S extends Selector<?, ?>> void register(Observer<DataSetChangedEvent<S>> observer, Selector<?, ?> selector) {
        observerMap.put(new WeakReference<>((Observer<DataSetChangedEvent<?>>) (Object) observer), selector);
    }

    @Override
    public <Model> void fire(Database db, DataSetChangedEvent.Type type, Schema<Model> schema) {
        if (observerMap.isEmpty()) {
            return;
        }
        if (db.inTransaction()) {
            addChangedDataSetInTransaction(schema);
            return;
        }

        for (Map.Entry<WeakReference<Observer<DataSetChangedEvent<?>>>, Selector<?, ?>> entry : observerMap.entrySet()) {
            Selector<?, ?> selector = entry.getValue();
            if (schema == selector.getSchema()) {
                WeakReference<Observer<DataSetChangedEvent<?>>> observerRef = entry.getKey();
                if (observerRef.get() != null) {
                    observerRef.get().onNext(new DataSetChangedEvent<>(type, selector));
                } else {
                    observerMap.remove(observerRef);
                }
            }
        }
    }

    private void addChangedDataSetInTransaction(Schema<?> schema) {
        if (changedDataSetInTransaction == null) {
            changedDataSetInTransaction = new HashSet<>();
        }

        changedDataSetInTransaction.add(schema);
    }

    @Override
    public void fireForTransaction() {
        Set<Schema<?>> schemaSet = changedDataSetInTransaction;
        changedDataSetInTransaction = null;

        if (schemaSet == null) {
            return;
        }
        for (Map.Entry<WeakReference<Observer<DataSetChangedEvent<?>>>, Selector<?, ?>> entry : observerMap.entrySet()) {
            Selector<?, ?> selector = entry.getValue();
            if (schemaSet.contains(selector.getSchema())) {
                WeakReference<Observer<DataSetChangedEvent<?>>> observerRef = entry.getKey();
                if (observerRef.get() != null) {
                    observerRef.get().onNext(new DataSetChangedEvent<>(DataSetChangedEvent.Type.TRANSACTION, selector));
                } else {
                    observerMap.remove(observerRef);
                }
            }
        }
    }
}
