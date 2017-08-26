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

import com.github.gfx.android.orma.Deleter;
import com.github.gfx.android.orma.OrmaConnection;
import com.github.gfx.android.orma.Relation;

import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;

import java.util.concurrent.Callable;

import io.reactivex.Single;

public abstract class RxDeleter<Model, D extends RxDeleter<Model, ?>> extends Deleter<Model, D> {

    public RxDeleter(@NonNull OrmaConnection connection) {
        super(connection);
    }

    public RxDeleter(@NonNull RxDeleter<Model, D> relation) {
        super(relation);
    }

    public RxDeleter(@NonNull Relation<Model, ?> relation) {
        super(relation);
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
