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

import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;

import java.util.concurrent.Callable;

import io.reactivex.Single;

public abstract class Deleter<Model, D extends Deleter<Model, ?>> extends OrmaConditionBase<Model, D> {

    public Deleter(@NonNull OrmaConnection connection) {
        super(connection);
    }

    public Deleter(@NonNull Relation<Model, ?> relation) {
        super(relation);
    }

    /**
     * @return Number of rows deleted.
     */
    public int execute() {
        return conn.delete(getSchema(), getWhereClause(), getBindArgs());
    }

    @CheckResult
    @NonNull
    public Single<Integer> executeAsSingle() {
        return Single.fromCallable(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                return execute();
            }
        });
    }
}
