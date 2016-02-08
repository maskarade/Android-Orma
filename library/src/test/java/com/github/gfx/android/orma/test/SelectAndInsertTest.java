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
import com.github.gfx.android.orma.test.model.Author;
import com.github.gfx.android.orma.test.model.OrmaDatabase;

import org.junit.Test;
import org.junit.runner.RunWith;

import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

@RunWith(AndroidJUnit4.class)
public class SelectAndInsertTest {

    @Test
    public void testSelectAndInsert() throws Exception {
        InstrumentationRegistry.getContext().deleteDatabase("test.db");

        OrmaDatabase orma = OrmaBuilder.builder().name("test.db").build();

        assertThat(orma.selectFromAuthor().toList(), hasSize(0));

        orma.createAuthor(new ModelFactory<Author>() {
            @Override
            public Author call() {
                Author author = new Author();
                author.name = "foo";
                author.note = "bar";
                return author;
            }
        });

        orma = OrmaBuilder.builder().name("test.db").build();

        assertThat(orma.selectFromAuthor().toList(), hasSize(1));
    }
}
