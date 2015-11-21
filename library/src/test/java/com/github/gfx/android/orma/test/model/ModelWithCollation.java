package com.github.gfx.android.orma.test.model;

import com.github.gfx.android.orma.annotation.Column;
import com.github.gfx.android.orma.annotation.Table;

@Table
public class ModelWithCollation {

    @Column
    public
    String noCollationField;

    @Column(collate = "RTRIM")
    public
    String rtrimField;

    @Column(collate = "NOCASE")
    public
    String nocaseField;
}
