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

package com.github.gfx.android.orma.test.toolbox;

import com.github.gfx.android.orma.test.model.OrmaDatabase;
import com.github.gfx.android.orma.test.model.OrmaDatabaseToAvoidTryParsing;

import android.support.test.InstrumentationRegistry;

import java.io.File;
import java.io.IOException;

public class OrmaFactory {

    static OrmaDatabase db;

    static String createTempfileName() {
        try {
            File file = File.createTempFile("test", ".db", InstrumentationRegistry.getTargetContext().getCacheDir());
            file.deleteOnExit();
            return file.getName();
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }

    public static OrmaDatabase.Builder builder() {
        return OrmaDatabase.builder(InstrumentationRegistry.getTargetContext())
                .name(createTempfileName())
                .trace(true);
    }

    public static OrmaDatabase create() {
        return builder().build();
    }

    public static OrmaDatabaseToAvoidTryParsing create2() {
        return OrmaDatabaseToAvoidTryParsing.builder(InstrumentationRegistry.getTargetContext())
                .tryParsingSql(false)
                .name(createTempfileName())
                .trace(true)
                .build();
    }
}
