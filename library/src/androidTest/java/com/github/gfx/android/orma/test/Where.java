package com.github.gfx.android.orma.test;

import com.github.gfx.android.orma.annotation.Column;
import com.github.gfx.android.orma.annotation.Table;

/**
 * To test if SQL reserved words are available
 */
@Table
public class Where {

    @Column String table;

    @Column String on;

    @Column String where;

}
