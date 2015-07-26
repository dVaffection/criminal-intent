package com.dvlab.criminalintent;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

public class CriminalIntentJSONSerializer {

    private Context context;
    private String filename;

    public CriminalIntentJSONSerializer(Context context, String filename) {
        this.context = context;
        this.filename = filename;
    }

    public void saveCrimes(List<Crime> crimes) throws JSONException, IOException {
        JSONArray jsonArray = new JSONArray();
        for (Crime crime : crimes) {
            jsonArray.put(crime.toJSON());
        }

        Writer writer = null;

        try {
            OutputStream stream = context.openFileOutput(filename, Context.MODE_PRIVATE);
            writer = new OutputStreamWriter(stream);
            writer.write(jsonArray.toString());
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
    }

    public List<Crime> loadCrimes() throws IOException, JSONException {
        List<Crime> crimes = new ArrayList<>();
        BufferedReader reader = null;
        try {
            // Open and read the file into a StringBuilder
            InputStream in = context.openFileInput(filename);
            reader = new BufferedReader(new InputStreamReader(in));
            StringBuilder jsonString = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                // Line breaks are omitted and irrelevant
                jsonString.append(line);
            }
            // Parse the JSON using JSONTokener
            JSONArray array = (JSONArray) new JSONTokener(jsonString.toString()).nextValue();
            // Build the array of crimes from JSONObjects
            for (int i = 0; i < array.length(); i++) {
                crimes.add(new Crime(array.getJSONObject(i)));
            }
        } catch (FileNotFoundException e) {
            // Ignore this one; it happens when starting fresh
        } finally {
            if (reader != null)
                reader.close();
        }

        return crimes;
    }

}
