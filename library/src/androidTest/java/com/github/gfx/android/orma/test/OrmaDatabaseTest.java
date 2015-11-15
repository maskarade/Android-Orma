package com.github.gfx.android.orma.test;

import com.github.gfx.android.orma.test.model.OrmaDatabase;

import org.junit.Test;

import android.content.Context;
import android.support.test.InstrumentationRegistry;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

public class OrmaDatabaseTest {

    Context getContext() {
        return InstrumentationRegistry.getTargetContext();
    }

    @Test
    public void testCreateInstance() throws Exception {
        OrmaDatabase db = new OrmaDatabase(getContext(), "test.db");
        assertThat(db.getConnection(), is(not(nullValue())));
    }
}
