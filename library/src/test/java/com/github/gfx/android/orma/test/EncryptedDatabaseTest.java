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
import com.github.gfx.android.orma.test.model.Publisher;
import com.github.gfx.android.orma.test.toolbox.OrmaFactory;
import com.github.gfx.android.orma.test.toolbox.TestUtils;

import org.junit.Test;
import org.junit.runner.RunWith;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;
import static org.junit.Assume.assumeTrue;

@RunWith(AndroidJUnit4.class)
public class EncryptedDatabaseTest {

    Context getContext() {
        return ApplicationProvider.getApplicationContext();
    }

    @Test
    public void simpleExecution() {
        assumeTrue(TestUtils.runOnAndroid());

        Publisher publisher = OrmaFactory.createEncrypted()
                .createPublisher(new ModelFactory<Publisher>() {
                    @NonNull
                    @Override
                    public Publisher call() {
                        Publisher publisher = new Publisher();
                        publisher.name = "foo bar";
                        publisher.startedYear = 2015;
                        publisher.startedMonth = 12;
                        return publisher;
                    }
                });

        assertThat(publisher.id, is(not(0L)));
    }
}
