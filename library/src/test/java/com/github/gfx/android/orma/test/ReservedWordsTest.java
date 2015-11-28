package com.github.gfx.android.orma.test;

import com.github.gfx.android.orma.BuildConfig;
import com.github.gfx.android.orma.test.model.OrmaDatabase;
import com.github.gfx.android.orma.test.model.Where;

import org.junit.Before;
import org.junit.Ignore;
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
public class ReservedWordsTest {

    OrmaDatabase db;

    Context getContext() {
        return RuntimeEnvironment.application;
    }

    @Before
    public void setUp() throws Exception {
        db = OrmaDatabase.builder(getContext()).name(null).build();
    }

    @Test
    public void useReservedWordsInInsert() throws Exception {
        Where where = new Where();
        where.where = "a";
        where.table = "b";
        where.on = "c";
        long rowId = db.insertIntoWhere(where);
        assertThat(rowId, is(1L));
    }

    @Ignore("the table name must be escaped.")
    @Test
    public void useReservedWordsInSelect() throws Exception {
        assertThat(db.selectFromWhere().toList(), hasSize(0));
    }
}
