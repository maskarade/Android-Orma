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
import com.github.gfx.android.orma.Selector;
import com.github.gfx.android.orma.annotation.Experimental;

import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.RestrictTo;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.subjects.PublishSubject;

/**
 * Helper class for query observables. This class is NOT thread-safe.
 */
@RestrictTo(RestrictTo.Scope.GROUP_ID)
@Experimental
public class DataSetChangedTrigger {

    final WeakHashMap<Observer<DataSetChangedEvent<?>>, Selector<?, ?>> observerMap = new WeakHashMap<>();

    Set<Schema<?>> changedDataSetInTransaction = null;

    public <S extends Selector<?, ?>> Observable<DataSetChangedEvent<S>> create(S selector) {
        PublishSubject<DataSetChangedEvent<S>> subject = PublishSubject.create();
        register(subject, selector);
        return subject;
    }

    @SuppressWarnings("unchecked")
    public <S extends Selector<?, ?>> void register(Observer<DataSetChangedEvent<S>> observer, Selector<?, ?> selector) {
        observerMap.put((Observer<DataSetChangedEvent<?>>)(Object)observer, selector);
    }

    public <Model> void fire(SQLiteDatabase db, DataSetChangedEvent.Type type, Schema<Model> schema) {
        if (observerMap.isEmpty()) {
            return;
        }
        if (db.inTransaction()) {
            addChangedDataSetInTransaction(schema);
            return;
        }

        for (Map.Entry<Observer<DataSetChangedEvent<?>>, Selector<?, ?>> entry : observerMap.entrySet()) {
            Selector<?, ?> selector = entry.getValue();
            if (schema == selector.getSchema()) {
                Observer<DataSetChangedEvent<?>> observer = entry.getKey();
                observer.onNext(new DataSetChangedEvent<>(type, selector));
            }
        }
    }

    private void addChangedDataSetInTransaction(Schema<?> schema) {
        if (changedDataSetInTransaction == null) {
            changedDataSetInTransaction = new HashSet<>();
        }

        changedDataSetInTransaction.add(schema);
    }

    public void fireForTransaction() {
        Set<Schema<?>> schemaSet = changedDataSetInTransaction;
        changedDataSetInTransaction = null;

        if (schemaSet == null) {
            return;
        }
        for (Map.Entry<Observer<DataSetChangedEvent<?>>, Selector<?, ?>> entry : observerMap.entrySet()) {
            Selector<?, ?> selector = entry.getValue();
            if (schemaSet.contains(selector.getSchema())) {
                Observer<DataSetChangedEvent<?>> observer = entry.getKey();
                observer.onNext(new DataSetChangedEvent<>(DataSetChangedEvent.Type.TRANSACTION, selector));
            }
        }
    }
}
