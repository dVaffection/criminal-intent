package com.dvlab.criminalintent;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.UUID;

public class Crime {

    private UUID id;
    private String title;
    private Date date;
    private boolean isSolved;

    public Crime() {
        id = UUID.randomUUID();
        date = new Date();
    }

    public Crime(JSONObject jsonObject) throws JSONException {
        id = UUID.fromString(jsonObject.getString("id"));
        if (jsonObject.has("title")) {
            title = jsonObject.getString("title");
        }
        date = new Date(jsonObject.getLong("date"));
        isSolved = jsonObject.getBoolean("is_solved");
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

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public boolean isSolved() {
        return isSolved;
    }

    public void setIsSolved(boolean isSolved) {
        this.isSolved = isSolved;
    }

    @Override
    public String toString() {
        return title;
    }

    public JSONObject toJSON() throws JSONException {
        JSONObject json = new JSONObject();

        json.put("id", id.toString());
        json.put("title", title);
        json.put("date", date.getTime());
        json.put("is_solved", isSolved);

        return json;
    }

}
