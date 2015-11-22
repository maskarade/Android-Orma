package com.github.gfx.android.orma.test.model;

import com.github.gfx.android.orma.SingleRelation;
import com.github.gfx.android.orma.annotation.Column;
import com.github.gfx.android.orma.annotation.PrimaryKey;
import com.github.gfx.android.orma.annotation.Table;

import android.support.annotation.Nullable;

@Table
public class Book {

    @PrimaryKey
    public long id;

    @Column(indexed = true)
    public String title;

    @Column
    @Nullable
    public String content;

    @Column
    public boolean inPrint;

    @Column
    public SingleRelation<Publisher> publisher;
}
