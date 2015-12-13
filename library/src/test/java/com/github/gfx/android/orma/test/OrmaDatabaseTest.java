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

import com.github.gfx.android.orma.AccessThreadConstraint;
import com.github.gfx.android.orma.BuildConfig;
import com.github.gfx.android.orma.adapter.DateAdapter;
import com.github.gfx.android.orma.adapter.UriAdapter;
import com.github.gfx.android.orma.test.model.OrmaDatabase;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, manifest = Config.NONE)
public class OrmaDatabaseTest {

    static final String NAME = "main.db";

    Context getContext() {
        return RuntimeEnvironment.application;
    }

    @After
    public void tearDown() throws Exception {
        getContext().deleteDatabase(NAME);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Test
    public void testCreateInstance() throws Exception {
        OrmaDatabase db = OrmaDatabase.builder(getContext())
                .name(NAME)
                .typeAdapters(new UriAdapter(), new DateAdapter())
                .readOnMainThread(AccessThreadConstraint.NONE)
                .writeOnMainThread(AccessThreadConstraint.NONE)
                .trace(true)
                .build();

        assertThat(db.getConnection(), is(not(nullValue())));
        assertThat(db.getSchemas(), is(not(nullValue())));
        assertThat(db.getConnection().getDatabaseName(), is(NAME));
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Test
    public void testCreateInstanceWithWriteAheadLogging() throws Exception {
        OrmaDatabase db = OrmaDatabase.builder(getContext())
                .name(NAME)
                .writeAheadLogging(true)
                .build();

        assertThat(db.getConnection().getReadableDatabase().isWriteAheadLoggingEnabled(), is(true));
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Test
    public void testCreateInstanceWithoutWriteAheadLogging() throws Exception {
        OrmaDatabase db = OrmaDatabase.builder(getContext())
                .name(NAME)
                .writeAheadLogging(false)
                .build();

        assertThat(db.getConnection().getReadableDatabase().isWriteAheadLoggingEnabled(), is(false));
    }

}
