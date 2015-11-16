package com.github.gfx.android.orma.test;

import com.github.gfx.android.orma.BuildConfig;
import com.github.gfx.android.orma.test.model.OrmaDatabase;

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
public class OrmaDatabaseTest {

    Context getContext() {
        return RuntimeEnvironment.application;
    }

    @Test
    public void testCreateInstance() throws Exception {
        OrmaDatabase db = new OrmaDatabase(getContext(), "test.db");
        assertThat(db.getConnection(), is(not(nullValue())));
    }
}
