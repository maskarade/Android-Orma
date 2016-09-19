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
package com.github.gfx.android.orma;

import com.github.gfx.android.orma.internal.OrmaConditionBase;

import android.content.ContentValues;
import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;

import rx.Single;
import rx.SingleSubscriber;

public abstract class Updater<Model, U extends Updater<Model, ?>> extends OrmaConditionBase<Model, U> {

    final protected ContentValues contents = new ContentValues();

    public Updater(@NonNull OrmaConnection conn) {
        super(conn);
    }

    public Updater(@NonNull Relation<Model, ?> relation) {
        super(relation);
    }

    @NonNull
    public ContentValues getContentValues() {
        return contents;
    }

    /**
     * @return The number of rows updated.
     */
    public int execute() {
        return conn.update(getSchema(), contents, getWhereClause(), getBindArgs());
    }

    @CheckResult
    @NonNull
    public Single<Integer> executeAsObservable() {
        return Single.create(new Single.OnSubscribe<Integer>() {
            @Override
            public void call(SingleSubscriber<? super Integer> subscriber) {
                subscriber.onSuccess(execute());
            }
        });
    }
}
