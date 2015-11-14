package com.github.gfx.android.orma.test.model;

import com.google.gson.annotations.SerializedName;

import com.github.gfx.android.orma.annotation.Column;
import com.github.gfx.android.orma.annotation.PrimaryKey;
import com.github.gfx.android.orma.annotation.Table;

@Table(value = "publishers", schemaClassName = "PublisherSchema", relationClassName = "PublisherRelation")
public class Publisher {

    @PrimaryKey
    public long id;

    @Column(unique = true)
    public String name;

    @Column
    @SerializedName("started_year")
    public int startedYear;

    @Column("started_month")
    public int startedMonth;

    public Book_Relation books(OrmaDatabase orma) {
        return orma.fromBook().where("publisher = ?", id);
    }
}
