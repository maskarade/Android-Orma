package com.github.gfx.android.orma.example;

import com.github.gfx.android.orma.annotation.Column;
import com.github.gfx.android.orma.annotation.PrimaryKey;
import com.github.gfx.android.orma.annotation.Table;

@Table
public class Author {
    @PrimaryKey
    String name;

    @Column
    int firstPublishedYear;
}
