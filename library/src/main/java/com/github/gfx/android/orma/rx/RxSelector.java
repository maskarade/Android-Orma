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

import com.github.gfx.android.orma.ColumnDef;
import com.github.gfx.android.orma.OrmaConnection;
import com.github.gfx.android.orma.Relation;
import com.github.gfx.android.orma.Selector;

import android.database.Cursor;
import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;

import java.util.concurrent.Callable;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Single;

public abstract class RxSelector<Model, S extends RxSelector<Model, ?>> extends Selector<Model, S> {

    public RxSelector(@NonNull OrmaConnection conn) {
        super(conn);
    }

    public RxSelector(@NonNull Relation<Model, ?> relation) {
        super(relation);
    }

    public RxSelector(@NonNull Selector<Model, ?> selector) {
        super(selector);
    }

    @CheckResult
    @NonNull
    public Single<Integer> countAsSingle() {
        return Single.fromCallable(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                return count();
            }
        });
    }

    @NonNull
    public <T> Observable<T> pluckAsObservable(final ColumnDef<Model, T> column) {
        return Observable.create(new ObservableOnSubscribe<T>() {
            @Override
            public void subscribe(ObservableEmitter<T> emitter) throws Exception {
                Cursor cursor = executeWithColumns(column.getQualifiedName());
                try {
                    for (int pos = 0; !emitter.isDisposed() && cursor.moveToPosition(pos); pos++) {
                        emitter.onNext(column.getFromCursor(conn, cursor, 0));
                    }
                } finally {
                    cursor.close();
                }
                emitter.onComplete();
            }
        });
    }

    @NonNull
    public Observable<Model> executeAsObservable() {
        return Observable.create(new ObservableOnSubscribe<Model>() {
            @Override
            public void subscribe(ObservableEmitter<Model> emitter) throws Exception {
                final Cursor cursor = execute();
                try {
                    for (int pos = 0; !emitter.isDisposed() && cursor.moveToPosition(pos); pos++) {
                        emitter.onNext(newModelFromCursor(cursor));
                    }
                } finally {
                    cursor.close();
                }
                emitter.onComplete();
            }
        });
    }
}
