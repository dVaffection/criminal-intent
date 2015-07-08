package com.dvlab.criminalintent;

import java.util.UUID;

public class Crime {

    private UUID id;
    private String title;

    public Crime() {
        id = UUID.randomUUID();
    }

    public UUID getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
