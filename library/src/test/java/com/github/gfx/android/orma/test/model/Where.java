package com.github.gfx.android.orma.test.model;

import com.github.gfx.android.orma.annotation.Column;
import com.github.gfx.android.orma.annotation.Table;

/**
 * To test if SQL reserved words are available
 */
@Table
public class Where {

    @Column(indexed = true)
    public String table;

    @Column(indexed = true)
    public String on;

    @Column(indexed = true)
    public String where;

}
