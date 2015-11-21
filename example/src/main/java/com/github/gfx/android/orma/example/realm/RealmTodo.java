package com.github.gfx.android.orma.example.realm;

import io.realm.RealmObject;

public class RealmTodo extends RealmObject {

    private long id;

    private String title;

    private String content;

    private long createdTimeMillis;

    public RealmTodo() {
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }


    public long getCreatedTimeMillis() {
        return createdTimeMillis;
    }

    public void setCreatedTimeMillis(long createdTimeMillis) {
        this.createdTimeMillis = createdTimeMillis;
    }
}
