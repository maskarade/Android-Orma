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
import com.github.gfx.android.orma.test.model.ModelWithBlob;
import com.github.gfx.android.orma.test.model.ModelWithBoxTypes;
import com.github.gfx.android.orma.test.model.ModelWithCollation;
import com.github.gfx.android.orma.test.model.ModelWithConflictResolutions;
import com.github.gfx.android.orma.test.model.ModelWithConstraints;
import com.github.gfx.android.orma.test.model.ModelWithDefaults;
import com.github.gfx.android.orma.test.model.ModelWithPrimitives;
import com.github.gfx.android.orma.test.model.ModelWithTypeAdapters;
import com.github.gfx.android.orma.test.model.OrmaDatabase;
import com.github.gfx.android.orma.test.toolbox.IntTuple2;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.test.runner.AndroidJUnit4;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Currency;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

@RunWith(AndroidJUnit4.class)
public class ModelSpecTest {

    OrmaDatabase db;

    @Before
    public void setUp() throws Exception {
        db = OrmaFactory.create();
    }

    @Test
    public void testDefaultValue() throws Exception {
        ModelWithDefaults model = db.createModelWithDefaults(new ModelFactory<ModelWithDefaults>() {
            @NonNull
            @Override
            public ModelWithDefaults call() {
                return new ModelWithDefaults();
            }
        });

        assertThat(model.s, is("foo"));
        assertThat(model.i, is(10L));
    }

    @Test
    public void testCollation() throws Exception {
        ModelWithCollation one = new ModelWithCollation();
        one.binaryField = "foo";
        one.rtrimField = "foo";
        one.nocaseField = "foo";
        db.insertIntoModelWithCollation(one);

        ModelWithCollation two = new ModelWithCollation();
        two.binaryField = "foo  ";
        two.rtrimField = "foo  ";
        two.nocaseField = "foo  ";
        db.insertIntoModelWithCollation(two);

        ModelWithCollation three = new ModelWithCollation();
        three.binaryField = "FOO";
        three.rtrimField = "FOO";
        three.nocaseField = "FOO";
        db.insertIntoModelWithCollation(three);

        assertThat(db.selectFromModelWithCollation().rtrimFieldEq("foo ").count(), is(2));
        assertThat(db.selectFromModelWithCollation().nocaseFieldEq("foo").count(), is(2));
        assertThat(db.selectFromModelWithCollation().binaryFieldEq("foo").count(), is(1));
    }

    @Test
    public void testBlob() throws Exception {
        ModelWithBlob model = db.createModelWithBlob(new ModelFactory<ModelWithBlob>() {
            @NonNull
            @Override
            public ModelWithBlob call() {
                ModelWithBlob model = new ModelWithBlob();
                model.blob = new byte[]{0, 1, 2, 3};
                return model;
            }
        });

        assertThat(model.blob, is(new byte[]{0, 1, 2, 3}));
    }

    @Test
    public void testModelWithTypeAdapters() throws Exception {
        final long now = System.currentTimeMillis();
        final UUID uuid = UUID.randomUUID();
        final BigDecimal bd = BigDecimal.valueOf(Long.MAX_VALUE).add(BigDecimal.ONE);
        final BigInteger bi = BigInteger.valueOf(Long.MAX_VALUE).add(BigInteger.TEN);
        final List<String> collection = Arrays.asList("foo", "bar baz", "42", "true");

        ModelWithTypeAdapters model = db.createModelWithTypeAdapters(new ModelFactory<ModelWithTypeAdapters>() {
            @NonNull
            @Override
            public ModelWithTypeAdapters call() {
                ModelWithTypeAdapters model = new ModelWithTypeAdapters();
                model.list = collection;
                model.arrayList = new ArrayList<>(collection);
                model.set = new LinkedHashSet<>(collection);
                model.hashSet = new HashSet<>(collection);
                model.uri = Uri.parse("http://example.com");
                model.date = new Date(now);
                model.sqlDate = new java.sql.Date(now);
                model.sqlTime = new java.sql.Time(now);
                model.sqlTimestamp = new java.sql.Timestamp(now);
                model.uuid = uuid;
                model.bigDecimal = bd;
                model.bigInteger = bi;
                model.currency = Currency.getInstance("JPY");
                model.intTuple2 = new IntTuple2(-13, 17);
                model.byteBuffer = ByteBuffer.wrap(new byte[]{0, 1, 2, 3});
                return model;
            }
        });

        assertThat(model.list, contains(collection.toArray()));
        assertThat(model.arrayList, contains(collection.toArray()));
        assertThat(model.set, containsInAnyOrder(collection.toArray()));
        assertThat(model.hashSet, containsInAnyOrder(collection.toArray()));
        assertThat(model.uri, is(Uri.parse("http://example.com")));
        assertThat(model.date, is(new Date(now)));
        assertThat(model.sqlDate.toString(), is(new java.sql.Date(now).toString()));
        assertThat(model.sqlTime.toString(), is(new java.sql.Time(now).toString()));
        assertThat(model.sqlTimestamp.toString(), is(new java.sql.Timestamp(now).toString()));
        assertThat(model.uuid, is(uuid));
        assertThat(model.bigDecimal, is(bd));
        assertThat(model.bigInteger, is(bi));
        assertThat(model.currency, is(Currency.getInstance("JPY")));
        assertThat(model.intTuple2, is(new IntTuple2(-13, 17)));
        assertThat(model.byteBuffer, is(ByteBuffer.wrap(new byte[]{0, 1, 2, 3})));

        // nullable
        assertThat(model.nullableUri, is(nullValue())); // TEXT
        assertThat(model.nullableIntTuple2, is(nullValue())); // INTEGER
        assertThat(model.nullableByteBuffer, is(nullValue())); // BLOB
    }

    @Test
    public void testPrimitives() throws Exception {
        ModelWithPrimitives model = db.createModelWithPrimitives(new ModelFactory<ModelWithPrimitives>() {
            @NonNull
            @Override
            public ModelWithPrimitives call() {
                ModelWithPrimitives model = new ModelWithPrimitives();

                model.booleanValue = true;
                model.byteValue = 1;
                model.shortValue = 2;
                model.intValue = 3;
                model.longValue = 4L;
                model.floatValue = 1.14f;
                model.doubleValue = 3.14;

                return model;
            }
        });

        assertThat(model.booleanValue, is(true));
        assertThat(model.byteValue, is((byte) 1));
        assertThat(model.shortValue, is((short) 2));
        assertThat(model.intValue, is(3));
        assertThat(model.longValue, is(4L));
        assertThat(model.floatValue, is(1.14f));
        assertThat(model.doubleValue, is(3.14));
    }

    @Test
    public void testBoxTypes() throws Exception {
        ModelWithBoxTypes model = db.createModelWithBoxTypes(new ModelFactory<ModelWithBoxTypes>() {
            @NonNull
            @Override
            public ModelWithBoxTypes call() {
                ModelWithBoxTypes model = new ModelWithBoxTypes();

                model.booleanValue = true;
                model.byteValue = 1;
                model.shortValue = 2;
                model.intValue = 3;
                model.longValue = 4L;
                model.floatValue = 1.14f;
                model.doubleValue = 3.14;

                return model;
            }
        });

        assertThat(model.booleanValue, is(true));
        assertThat(model.byteValue, is((byte) 1));
        assertThat(model.shortValue, is((short) 2));
        assertThat(model.intValue, is(3));
        assertThat(model.longValue, is(4L));
        assertThat(model.floatValue, is(1.14f));
        assertThat(model.doubleValue, is(3.14));
    }

    @Test
    public void testTableConstraintsSuccess() throws Exception {
        ModelWithConstraints model = db.createModelWithConstraints(new ModelFactory<ModelWithConstraints>() {
            @NonNull
            @Override
            public ModelWithConstraints call() {
                ModelWithConstraints model = new ModelWithConstraints();
                model.foo = "foo";
                model.bar = "bar";
                return model;
            }
        });
        assertThat(model.foo, is("foo"));
        assertThat(model.bar, is("bar"));
    }

    @Test(expected = SQLiteException.class)
    public void testTableConstraintsViolateUniqueConstraint() throws Exception {
        db.createModelWithConstraints(new ModelFactory<ModelWithConstraints>() {
            @NonNull
            @Override
            public ModelWithConstraints call() {
                ModelWithConstraints model = new ModelWithConstraints();
                model.foo = "foo";
                model.bar = "bar";
                return model;
            }
        });

        db.createModelWithConstraints(new ModelFactory<ModelWithConstraints>() {
            @NonNull
            @Override
            public ModelWithConstraints call() {
                ModelWithConstraints model = new ModelWithConstraints();
                model.foo = "foo";
                model.bar = "bar";
                return model;
            }
        });
    }

    @Test(expected = SQLiteException.class)
    public void testTableConstraintsViolateCheckConstraint1() throws Exception {
        db.createModelWithConstraints(new ModelFactory<ModelWithConstraints>() {
            @NonNull
            @Override
            public ModelWithConstraints call() {
                ModelWithConstraints model = new ModelWithConstraints();
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < 100; i++) {
                    sb.append('.');
                }
                model.foo = "foo" + sb;
                model.bar = "bar";
                return model;
            }
        });
    }

    @Test(expected = SQLiteException.class)
    public void testTableConstraintsViolateCheckConstraint2() throws Exception {
        db.createModelWithConstraints(new ModelFactory<ModelWithConstraints>() {
            @NonNull
            @Override
            public ModelWithConstraints call() {
                ModelWithConstraints model = new ModelWithConstraints();
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < 100; i++) {
                    sb.append('.');
                }
                model.foo = "foo";
                model.bar = "bar" + sb;
                return model;
            }
        });
    }

    @Test
    public void testPrimaryKeyOnConflict() throws Exception {
        db.createModelWithConflictResolutions(new ModelFactory<ModelWithConflictResolutions>() {
            @Override
            public ModelWithConflictResolutions call() {
                ModelWithConflictResolutions model = new ModelWithConflictResolutions();
                model.primaryKeyOrIgnore = 10;
                model.uniqueOrRollback = 1;
                model.uniqueOrAbort = 1;
                model.uniqueOrFail = 1;
                model.uniqueOrIgnore = 1;
                model.uniqueOrReplace = 1;
                return model;
            }
        });

        db.createModelWithConflictResolutions(new ModelFactory<ModelWithConflictResolutions>() {
            @Override
            public ModelWithConflictResolutions call() {
                ModelWithConflictResolutions model = new ModelWithConflictResolutions();
                model.primaryKeyOrIgnore = 10;
                model.uniqueOrRollback = 2;
                model.uniqueOrAbort = 2;
                model.uniqueOrFail = 2;
                model.uniqueOrIgnore = 2;
                model.uniqueOrReplace = 2;
                return model;
            }
        });

        assertThat(db.selectFromModelWithConflictResolutions().count(), is(1));
    }

    @Test
    public void testUniqueOnConflict() throws Exception {
        db.createModelWithConflictResolutions(new ModelFactory<ModelWithConflictResolutions>() {
            @Override
            public ModelWithConflictResolutions call() {
                ModelWithConflictResolutions model = new ModelWithConflictResolutions();
                model.primaryKeyOrIgnore = 10;
                model.uniqueOrRollback = 1;
                model.uniqueOrAbort = 1;
                model.uniqueOrFail = 1;
                model.uniqueOrIgnore = 1;
                model.uniqueOrReplace = 1;
                return model;
            }
        });

        db.createModelWithConflictResolutions(new ModelFactory<ModelWithConflictResolutions>() {
            @Override
            public ModelWithConflictResolutions call() {
                ModelWithConflictResolutions model = new ModelWithConflictResolutions();
                model.primaryKeyOrIgnore = 11;
                model.uniqueOrRollback = 2;
                model.uniqueOrAbort = 2;
                model.uniqueOrFail = 2;
                model.uniqueOrIgnore = 2;
                model.uniqueOrReplace = 1;
                return model;
            }
        });

        assertThat(db.selectFromModelWithConflictResolutions().count(), is(1));
        assertThat(db.selectFromModelWithConflictResolutions().value().primaryKeyOrIgnore, is(11L));
    }
}
