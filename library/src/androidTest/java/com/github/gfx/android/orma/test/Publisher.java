package com.github.gfx.android.orma.test;

import com.github.gfx.android.orma.annotation.Column;
import com.github.gfx.android.orma.annotation.Table;

@Table
public class Publisher {

    @Column
    String name;

    @Column
    int startedYear;

    // TODO: has-many relations for Book
    // @Column HasMany<Book> books;
}
