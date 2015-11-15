package com.github.gfx.android.orma.test;

import com.github.gfx.android.orma.test.model.OrmaDatabase;
import com.github.gfx.android.orma.test.model.Where;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import android.content.Context;
import android.support.test.InstrumentationRegistry;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

public class ReservedWordsTest {

    OrmaDatabase db;

    Context getContext() {
        return InstrumentationRegistry.getTargetContext();
    }

    @Before
    public void setUp() throws Exception {
        db = new OrmaDatabase(getContext(), "test.db");
        db.getConnection().resetDatabase();
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
