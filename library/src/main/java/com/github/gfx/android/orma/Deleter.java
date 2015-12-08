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

import android.support.annotation.NonNull;

import rx.Single;
import rx.SingleSubscriber;

public class Deleter<Model, D extends Deleter<?, ?>> extends OrmaConditionBase<Model, D> {

    public Deleter(OrmaConnection connection, Schema<Model> schema) {
        super(connection, schema);
    }

    /**
     * @return Number of rows deleted.
     */
    public int execute() {
        return conn.delete(schema, getWhereClause(), getWhereArgs());
    }

    @NonNull
    public Single<Integer> observable() {
        return Single.create(new Single.OnSubscribe<Integer>() {
            @Override
            public void call(SingleSubscriber<? super Integer> subscriber) {
                subscriber.onSuccess(execute());
            }
        });
    }
}
