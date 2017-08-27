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

import com.github.gfx.android.orma.ModelFactory;
import com.github.gfx.android.orma.OrmaConnection;
import com.github.gfx.android.orma.Schema;
import com.github.gfx.android.orma.SingleAssociation;
import com.github.gfx.android.orma.exception.NoValueException;
import com.github.gfx.android.orma.internal.Schemas;

import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;
import android.support.annotation.RestrictTo;

import java.util.concurrent.Callable;

import io.reactivex.Single;

/**
 * Lazy has-one association with RxJava support.
 * The {@code Model} is assumed to have a primary key with the `long` type.
 * This is typically created from factory methods.
 *
 * @param <Model> The type of a model
 */
public class RxSingleAssociation<Model> extends SingleAssociation<Model> {

    final Single<Model> single = Single.fromCallable(new Callable<Model>() {
        @Override
        public Model call() throws Exception {
            return get();
        }
    });

    @RestrictTo(RestrictTo.Scope.LIBRARY)
    public RxSingleAssociation(long id, @NonNull Model model) {
        super(id, model);
    }

    @RestrictTo(RestrictTo.Scope.LIBRARY)
    public RxSingleAssociation(long id, @NonNull ModelFactory<Model> factory) {
        super(id, factory);
    }

    // may be called from *_Schema
    public RxSingleAssociation(@NonNull final OrmaConnection conn, @NonNull final Schema<Model> schema, final long id) {
        super(conn, schema, id);
    }

    /**
     * The most typical factory method to create a {@code RxSingleAssociation} instance,
     * just wrapping the model with it.
     *
     * @param model A model to wrap, which must have a valid primary key
     * @param <T>   The type of the model to wrap
     * @return An instance of {@code RxSingleAssociation}
     */
    @SuppressWarnings("unchecked")
    @NonNull
    public static <T> RxSingleAssociation<T> just(@NonNull T model) {
        Schema<T> schema = Schemas.get((Class<T>) model.getClass());
        return just(schema, model);
    }

    @NonNull
    public static <T> RxSingleAssociation<T> just(long id, @NonNull T model) {
        return new RxSingleAssociation<>(id, model);
    }

    @NonNull
    public static <T> RxSingleAssociation<T> just(@NonNull Schema<T> schema, @NonNull T model) {
        return new RxSingleAssociation<>((long) schema.getPrimaryKey().getSerialized(model), model);
    }

    @NonNull
    public static <T> RxSingleAssociation<T> just(final long id) {
        return new RxSingleAssociation<>(id, new ModelFactory<T>() {
            @NonNull
            @Override
            public T call() {
                throw new NoValueException("No value set for id=" + id);
            }
        });
    }

    // use just(id) instead
    @Deprecated
    @NonNull
    public static <T> RxSingleAssociation<T> id(final long id) {
        return just(id);
    }

    @CheckResult
    @NonNull
    public Single<Model> single() {
        return single;
    }
}
