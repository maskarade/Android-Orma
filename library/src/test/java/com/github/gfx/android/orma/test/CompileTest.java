package com.github.gfx.android.orma.test;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

public class CompileTest {

    @Test
    public void testCompile() throws Exception {
        assertThat(Class.forName("com.github.gfx.android.orma.test.OrmaDatabase"), is(instanceOf(Class.class)));
    }
}