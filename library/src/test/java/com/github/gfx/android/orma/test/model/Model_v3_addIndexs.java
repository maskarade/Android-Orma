package com.github.gfx.android.orma.test.model;

import com.github.gfx.android.orma.annotation.Column;
import com.github.gfx.android.orma.annotation.PrimaryKey;

public class Model_v3_addIndexs {

    @PrimaryKey
    long id;

    @Column(indexed = true)
    String field;

    @Column(indexed = true)
    long field2;
}