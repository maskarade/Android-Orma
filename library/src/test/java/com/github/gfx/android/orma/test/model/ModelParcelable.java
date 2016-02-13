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

package com.github.gfx.android.orma.test.model;

import com.github.gfx.android.orma.annotation.Column;
import com.github.gfx.android.orma.annotation.PrimaryKey;
import com.github.gfx.android.orma.annotation.Setter;
import com.github.gfx.android.orma.annotation.Table;

import android.os.Parcel;
import android.os.Parcelable;

@Table
public class ModelParcelable implements Parcelable {

    @PrimaryKey
    public long id;

    @Column
    public String text;

    @Setter
    public ModelParcelable(long id, String text) {
        this.id = id;
        this.text = text;
    }

    protected ModelParcelable(Parcel source) {
        id = source.readLong();
        text = source.readString();
    }

    public static final ClassLoaderCreator<ModelParcelable> CREATOR = new ClassLoaderCreator<ModelParcelable>() {
        @Override
        public ModelParcelable createFromParcel(Parcel source, ClassLoader loader) {
            return new ModelParcelable(source);
        }

        @Override
        public ModelParcelable createFromParcel(Parcel source) {
            return new ModelParcelable(source);
        }

        @Override
        public ModelParcelable[] newArray(int size) {
            return new ModelParcelable[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(text);
    }
}
