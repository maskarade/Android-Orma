package com.github.gfx.android.orma.test.model;

import com.github.gfx.android.orma.annotation.PrimaryKey;
import com.github.gfx.android.orma.annotation.Table;

@Table
public class Author {

    @PrimaryKey(auto = false)
    public String name;

}
