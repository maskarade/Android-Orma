package com.github.gfx.android.orma.test;

import com.github.gfx.android.orma.BuildConfig;
import com.github.gfx.android.orma.OrmaMigration;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, manifest = Config.NONE)
public class OrmaMigrationTest {

    OrmaMigration migration;

    @Before
    public void setUp() throws Exception {
        migration = new OrmaMigration();
    }

    @Test
    public void buildDropIndexStatement() throws Exception {
        assertThat(migration.buildDropIndexStatement("CREATE INDEX index_foo ON foo (bar)"),
                is("DROP INDEX IF EXISTS \"index_foo\""));
        assertThat(migration.buildDropIndexStatement("create index if not exists index_foo on foo (bar)"),
                is("DROP INDEX IF EXISTS \"index_foo\""));
    }
}
