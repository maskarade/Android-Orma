package com.github.gfx.android.orma.test.model;

import com.github.gfx.android.orma.annotation.Column;
import com.github.gfx.android.orma.annotation.PrimaryKey;
import com.github.gfx.android.orma.annotation.Table;

@Table
public class Model_v2_addColumn {

    @PrimaryKey
    long id;

    @Column
    String field;

    @Column
    long field2;

}
