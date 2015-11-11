package com.github.gfx.android.orma.example;

import com.github.gfx.android.orma.annotation.Column;
import com.github.gfx.android.orma.annotation.Index;
import com.github.gfx.android.orma.annotation.PrimaryKey;
import com.github.gfx.android.orma.annotation.Table;

import android.support.annotation.Nullable;

@Table
public class Book {

    @PrimaryKey
    public long id;

    @Column
    @Index
    public String title;

    @Column
    @Nullable
    public String content;

    // TODO: has-many authors
    // @Columns
    // public List<Authors>
}
