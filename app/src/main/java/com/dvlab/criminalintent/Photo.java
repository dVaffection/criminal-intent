package com.dvlab.criminalintent;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

public class Photo {

    private static final String JSON_FILENAME = "filename";
    private String filename;

    /**
     * Create a Photo representing an existing file on disk
     */
    public Photo(String filename) {
        this.filename = filename;
    }

    public Photo(JSONObject json) throws JSONException {
        filename = json.getString(JSON_FILENAME);
    }

    public JSONObject toJSON() throws JSONException {
        JSONObject json = new JSONObject();
        json.put(JSON_FILENAME, filename);
        return json;
    }

    public String getFilename() {
        return filename;
    }

    public boolean delete() {
        if (filename != null) {
            File file = new File(filename);
            return file.delete();
        } else {
            return false;
        }
    }

}
