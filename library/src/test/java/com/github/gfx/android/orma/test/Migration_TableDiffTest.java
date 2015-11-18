package com.github.gfx.android.orma.test;

import com.github.gfx.android.orma.BuildConfig;
import com.github.gfx.android.orma.migration.OrmaMigration;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, manifest = Config.NONE)
public class Migration_TableDiffTest {

    OrmaMigration migration;

    @Before
    public void setUp() throws Exception {
        migration = new OrmaMigration();
    }

    @Test
    public void tableDiff_addColumn() throws Exception {
        String from = "CREATE TABLE todo (title TEXT, content TEXT)";
        String to = "CREATE TABLE todo (id TEXT, title TEXT, content TEXT)";
        List<String> statements = migration.tableDiff(from, to);
        assertThat(statements, contains("ALTER TABLE \"todo\" ADD COLUMN \"id\" TEXT"));
    }

    @Test
    public void tableDiff_addUniqueConstraint() throws Exception {
        String from = "CREATE TABLE todo (title TEXT, content TEXT)";
        String to = "CREATE TABLE todo (title TEXT UNIQUE, content TEXT)";
        List<String> statements = migration.tableDiff(from, to);
        assertThat(statements, contains("ALTER TABLE \"todo\" ADD COLUMN \"title\" TEXT UNIQUE"));
    }
}