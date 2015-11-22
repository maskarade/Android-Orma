package com.github.gfx.android.orma.test.model;

import com.github.gfx.android.orma.annotation.Column;
import com.github.gfx.android.orma.annotation.Table;

@Table
public class ModelWithDefaults {

    @Column
    public String s = "foo";

    @Column
    public long i = 10;

}
