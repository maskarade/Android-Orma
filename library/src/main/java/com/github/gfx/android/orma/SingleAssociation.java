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

import com.google.gson.annotations.JsonAdapter;

import com.github.gfx.android.orma.exception.InvalidModelException;
import com.github.gfx.android.orma.exception.NoValueException;
import com.github.gfx.android.orma.gson.SingleAssociationTypeAdapterFactory;
import com.github.gfx.android.orma.internal.Schemas;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import rx.Single;
import rx.SingleSubscriber;

/**
 * Represents a has-one relation with lazy loading.
 *
 * @param <Model> The type of a model to relate.
 */
@JsonAdapter(SingleAssociationTypeAdapterFactory.class)
public class SingleAssociation<Model> implements Parcelable {

    final long id;

    final Single<Model> single;

    public SingleAssociation(long id, @NonNull Model model) {
        this.id = id;
        this.single = Single.just(model);
    }

    public SingleAssociation(long id, @NonNull Single<Model> single) {
        this.id = id;
        this.single = single;
    }

    public SingleAssociation(@NonNull final OrmaConnection conn, @NonNull final Schema<Model> schema, final long id) {
        this.id = id;
        single = Single.create(new Single.OnSubscribe<Model>() {
            @Override
            public void call(SingleSubscriber<? super Model> subscriber) {
                ColumnDef<Model, ?> primaryKey = schema.getPrimaryKey();
                String whereClause = primaryKey.getEscapedName() + " = ?";
                String[] whereArgs = {String.valueOf(id)};
                Model model = conn.querySingle(schema, schema.getDefaultResultColumns(),
                        whereClause, whereArgs, null, null, null, 0);
                if (model != null) {
                    subscriber.onSuccess(model);
                } else {
                    subscriber.onError(new NoValueException("No value found for "
                            + schema.getTableName() + "." + primaryKey.name + " = " + id));
                }
            }
        });
    }

    @SuppressWarnings("unchecked")
    public static <T> SingleAssociation<T> just(@NonNull T model) {
        Schema<T> schema = Schemas.get((Class<T>) model.getClass());
        return just(schema, model);
    }

    public static <T> SingleAssociation<T> just(long id, @NonNull T model) {
        return new SingleAssociation<>(id, model);
    }

    public static <T> SingleAssociation<T> just(@NonNull Schema<T> schema, @NonNull T model) {
        return new SingleAssociation<>((long) schema.getPrimaryKey().getSerialized(model), model);
    }

    public static <T> SingleAssociation<T> id(final long id) {
        return new SingleAssociation<>(id, Single.create(new Single.OnSubscribe<T>() {
            @Override
            public void call(SingleSubscriber<? super T> singleSubscriber) {
                singleSubscriber.onError(new NoValueException("No value set for id=" + id));
            }
        }));
    }

    public long getId() {
        return id;
    }

    @NonNull
    public Single<Model> observable() {
        return single;
    }

    /**
     * A shortcut of {@code singleAssociation.observable().toBlocking().value()}.
     *
     * @return A model value the {@code SingleAssociation<T>} refers to.
     */
    @NonNull
    public Model value() throws NoValueException {
        return single.toBlocking().value();
    }

    @Override
    public String toString() {
        return "SingleAssociation{" +
                "id=" + id + '}';
    }

    // Parcelable

    public static Parcelable.ClassLoaderCreator<SingleAssociation<?>> CREATOR
            = new ClassLoaderCreator<SingleAssociation<?>>() {
        @Override
        public SingleAssociation<?> createFromParcel(Parcel source) {
            return createFromParcel(source, null);
        }

        @Override
        public SingleAssociation<?>[] newArray(int size) {
            return new SingleAssociation<?>[size];
        }

        @Override
        public SingleAssociation<?> createFromParcel(Parcel source, ClassLoader loader) {
            long id = source.readLong();
            Parcelable parcelable = source.readParcelable(loader);
            return new SingleAssociation<>(id, parcelable);
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        Model model = single.toBlocking().value();
        if (!(model instanceof Parcelable)) {
            throw new InvalidModelException("Orma model " + model.getClass() + " is not a Parcelable");
        }
        dest.writeLong(id);
        dest.writeParcelable((Parcelable) model, flags);
    }
}
