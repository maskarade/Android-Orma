package com.github.gfx.android.orma.test;

import org.junit.Before;
import org.junit.Test;

import android.content.Context;
import android.support.test.InstrumentationRegistry;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

public class PublisherTest {

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
    public void initAndInsert() throws Exception {
        {
            Publisher publisher = new Publisher();
            publisher.name = "The Fire";
            publisher.startedYear = 1998;
            db.insert(publisher);
        }

        {
            Publisher publisher = new Publisher();
            publisher.name = "The Ice";
            publisher.startedYear = 2012;
            db.insert(publisher);
        }

        assertThat(db.fromPublisher().count(), is(2L));
    }

}
