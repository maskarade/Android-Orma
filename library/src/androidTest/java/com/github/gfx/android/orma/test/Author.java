package com.github.gfx.android.orma.test;

import com.github.gfx.android.orma.annotation.Column;
import com.github.gfx.android.orma.annotation.PrimaryKey;
import com.github.gfx.android.orma.annotation.Table;

@Table
public class Author {

    @PrimaryKey
    long id;

    @Column
    String name;

}
