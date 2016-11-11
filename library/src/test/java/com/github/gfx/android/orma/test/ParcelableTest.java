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

package com.github.gfx.android.orma.test;

import com.github.gfx.android.orma.ModelFactory;
import com.github.gfx.android.orma.SingleAssociation;
import com.github.gfx.android.orma.test.model.ModelParcelable;
import com.github.gfx.android.orma.test.model.OrmaDatabase;
import com.github.gfx.android.orma.test.toolbox.OrmaFactory;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@RunWith(AndroidJUnit4.class)
public class ParcelableTest {

    OrmaDatabase orma;

    Context getContext() {
        return InstrumentationRegistry.getTargetContext();
    }

    <T extends Parcelable> T reload(T parcelable) {
        Parcel parcel = Parcel.obtain();

        parcel.writeParcelable(parcelable, 0);
        parcel.setDataPosition(0);

        T reloadedParcelable = parcel.readParcelable(getContext().getClassLoader());

        parcel.recycle();

        return reloadedParcelable;
    }

    @Before
    public void setUp() throws Exception {
        orma = OrmaFactory.create();
    }

    @Test
    public void testSingleAssociation() throws Exception {
        ModelParcelable model = orma.createModelParcelable(new ModelFactory<ModelParcelable>() {
            @NonNull
            @Override
            public ModelParcelable call() {
                return new ModelParcelable(0, "foo");
            }
        });

        SingleAssociation<ModelParcelable> ref = reload(SingleAssociation.just(model));

        assertThat(ref.getId(), is(model.id));
        assertThat(ref.get().text, is(model.text));
    }
}
