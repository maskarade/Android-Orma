package com.github.gfx.android.orma.test.model;

import com.github.gfx.android.orma.annotation.Column;
import com.github.gfx.android.orma.annotation.PrimaryKey;
import com.github.gfx.android.orma.annotation.Table;

@Table
public class ModelWithDefaults {

    @PrimaryKey
    public long id;

    @Column
    public String s = "foo";

    @Column
    public long i = 10;

}
