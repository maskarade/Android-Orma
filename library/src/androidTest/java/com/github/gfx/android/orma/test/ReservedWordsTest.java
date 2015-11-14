package com.github.gfx.android.orma.test;

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
        long rowId = db.insert(where);
        assertThat(rowId, is(1L));
    }

    @Ignore // FIXME: SQLiteDatabase#query() does not quote names
    @Test
    public void useReservedWordsInSelect() throws Exception {
        assertThat(db.fromWhere().toList(), hasSize(0));
    }

}
