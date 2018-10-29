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

import com.github.gfx.android.orma.BuildConfig;
import com.github.gfx.android.orma.test.model.OrmaDatabase;

import org.junit.Test;
import org.junit.runner.RunWith;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

@RunWith(AndroidJUnit4.class)
public class Issue463_ForOrmaDatabaseBuilder {

    Context getContext() {
        return ApplicationProvider.getApplicationContext();
    }

    @Test
    public void dbVersionIsUpToDate() {
        OrmaDatabase orma = OrmaDatabase
                .builder(getContext())
                .name(null)
                .build();

        assertThat(orma.getConnection().getReadableDatabase().getVersion(), is(not(0)));
        assertThat(orma.getConnection().getReadableDatabase().getVersion(), is(BuildConfig.VERSION_CODE));
    }
}
