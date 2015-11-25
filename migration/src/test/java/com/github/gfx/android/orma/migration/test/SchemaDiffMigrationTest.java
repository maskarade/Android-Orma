package com.github.gfx.android.orma.migration.test;

import com.github.gfx.android.orma.migration.BuildConfig;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, manifest = Config.NONE)
public class SchemaDiffMigrationTest {

    @Test
    public void testBuildConfig() throws Exception {
        assertThat(BuildConfig.VERSION_NAME, not(isEmptyString()));

    }
}
