package com.github.gfx.android.orma.test.model;

import com.github.gfx.android.orma.annotation.Column;
import com.github.gfx.android.orma.annotation.Table;

/**
 * To test if SQL reserved words are available
 */
@Table
public class Where {

    @Column
    public String table;

    @Column
    public String on;

    @Column
    public String where;

}
