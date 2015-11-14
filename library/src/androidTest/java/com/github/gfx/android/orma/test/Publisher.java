package com.github.gfx.android.orma.test;

import com.google.gson.annotations.SerializedName;

import com.github.gfx.android.orma.annotation.Column;
import com.github.gfx.android.orma.annotation.PrimaryKey;
import com.github.gfx.android.orma.annotation.Table;

@Table(value = "publishers", schemaClassName = "PublisherSchema", relationClassName = "PublisherRelation")
public class Publisher {

    @PrimaryKey
    long id;

    @Column(unique = true)
    String name;

    @Column
    @SerializedName("started_year")
    int startedYear;

    @Column("started_month")
    int startedMonth;

    Book_Relation books(OrmaDatabase orma) {
        return orma.fromBook().where("publisher = ?", id);
    }
}
