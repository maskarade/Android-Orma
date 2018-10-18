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
import androidx.annotation.NonNull;
import androidx.annotation.RestrictTo;

/**
 * Lazy has-one association. The {@code Model} is assumed to have a primary key with the `long` type.
 * This is typically created from factory methods.
 *
 * @param <Model> The type of a model
 */
@JsonAdapter(SingleAssociationTypeAdapterFactory.class)
public class SingleAssociation<Model> implements Parcelable {

    final long id;

    final protected ModelFactory<Model> factory;

    Model value = null;

    @RestrictTo(RestrictTo.Scope.LIBRARY)
    public SingleAssociation(long id, @NonNull final Model model) {
        this.id = id;
        this.factory = new ModelFactory<Model>() {
            @NonNull
            @Override
            public Model call() {
                return model;
            }
        };
    }

    @RestrictTo(RestrictTo.Scope.LIBRARY)
    public SingleAssociation(long id, @NonNull ModelFactory<Model> factory) {
        this.id = id;
        this.factory = factory;
    }

    // may be called from *_Schema
    public SingleAssociation(@NonNull final OrmaConnection conn, @NonNull final Schema<Model> schema, final long id) {
        this.id = id;
        this.factory = new ModelFactory<Model>() {
            @NonNull
            @Override
            public Model call() {
                return conn.findByRowId(schema, id);
            }
        };
    }

    /**
     * The most typical factory method to create a {@code SingleAssociation} instance,
     * just wrapping the model with it.
     *
     * @param model A model to wrap, which must have a valid primary key
     * @param <T>   The type of the model to wrap
     * @return An instance of {@code SingleAssociation}
     */
    @SuppressWarnings("unchecked")
    @NonNull
    public static <T> SingleAssociation<T> just(@NonNull T model) {
        Schema<T> schema = Schemas.get((Class<T>) model.getClass());
        return just(schema, model);
    }

    @NonNull
    public static <T> SingleAssociation<T> just(long id, @NonNull T model) {
        return new SingleAssociation<>(id, model);
    }

    @NonNull
    public static <T> SingleAssociation<T> just(@NonNull Schema<T> schema, @NonNull T model) {
        return new SingleAssociation<>((long) schema.getPrimaryKey().getSerialized(model), model);
    }

    @NonNull
    public static <T> SingleAssociation<T> just(final long id) {
        return new SingleAssociation<>(id, new ModelFactory<T>() {
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
    public static <T> SingleAssociation<T> id(final long id) {
        return just(id);
    }

    /**
     * @return The primary key of the associated model.
     */
    public long getId() {
        return id;
    }

    /**
     * Get the model referred from this instance with blocking.
     *
     * @return A model that the instance refers to.
     */
    @NonNull
    public Model get() throws NoValueException {
        if (value == null) {
            synchronized (this) {
                if (value == null) {
                    value = factory.call();
                }
            }
        }
        return value;
    }

    @Deprecated
    @NonNull
    public Model value() throws NoValueException {
        return get();
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
        Model model = get();
        if (!(model instanceof Parcelable)) {
            throw new InvalidModelException("Orma model " + model.getClass() + " is not a Parcelable");
        }
        dest.writeLong(id);
        dest.writeParcelable((Parcelable) model, flags);
    }
}
