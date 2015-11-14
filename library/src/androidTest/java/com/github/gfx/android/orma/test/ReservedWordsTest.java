package com.github.gfx.android.orma.test;

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

    @Ignore // FIXME: SQLiteDatabase#insert() does not quote identifiers
    @Test
    public void useReservedWords() throws Exception {
        db = new OrmaDatabase(getContext(), "test.db");
        db.getConnection().resetDatabase();

        {
            Where where = new Where();
            where.where = "a";
            where.table = "b";
            where.on = "c";
            db.insert(where);
        }

        assertThat(db.fromWhere().toList(), hasSize(1));
    }

}
