package com.github.gfx.android.orma.test;

import com.github.gfx.android.orma.BuildConfig;
import com.github.gfx.android.orma.ModelBuilder;
import com.github.gfx.android.orma.test.model.ModelWithCollation;
import com.github.gfx.android.orma.test.model.ModelWithDefaults;
import com.github.gfx.android.orma.test.model.OrmaDatabase;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import android.content.Context;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, manifest = Config.NONE)
public class ColumnConstraintsTest {

    OrmaDatabase db;

    Context getContext() {
        return RuntimeEnvironment.application;
    }

    @Before
    public void setUp() throws Exception {
        db = new OrmaDatabase(getContext(), "test.db");
        db.getConnection().resetDatabase();
    }

    @Test
    public void testDefaultValue() throws Exception {
        ModelWithDefaults model = db.createModelWithDefaults(new ModelBuilder<ModelWithDefaults>() {
            @Override
            public ModelWithDefaults build() {
                return new ModelWithDefaults();
            }
        });

        assertThat(model.s, is("foo"));
        assertThat(model.i, is(10L));
    }

    @Test
    public void testCollation() throws Exception {
        ModelWithCollation one = new ModelWithCollation();
        one.noCollationField = "foo";
        one.rtrimField = "foo";
        one.nocaseField = "foo";
        db.insertIntoModelWithCollation(one);

        ModelWithCollation two = new ModelWithCollation();
        two.noCollationField = "foo  ";
        two.rtrimField = "foo  ";
        two.nocaseField = "foo  ";
        db.insertIntoModelWithCollation(two);

        ModelWithCollation three = new ModelWithCollation();
        three.noCollationField = "FOO";
        three.rtrimField = "FOO";
        three.nocaseField = "FOO";
        db.insertIntoModelWithCollation(three);

        assertThat(db.selectFromModelWithCollation().where("rtrimField = ?", "foo ").count(), is(2L));
        assertThat(db.selectFromModelWithCollation().where("nocaseField = ?", "foo").count(), is(2L));
        assertThat(db.selectFromModelWithCollation().where("noCollationField = ?", "foo").count(), is(1L));
    }

}
