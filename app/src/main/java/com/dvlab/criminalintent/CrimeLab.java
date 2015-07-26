package com.dvlab.criminalintent;

import android.content.Context;
import android.util.Log;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CrimeLab {

    public final static String TAG = CrimeLab.class.getSimpleName();
    public final static String FILENAME = "crimes.json";

    private static CrimeLab instance;
    private Context appContext;

    private List<Crime> crimes;
    private CriminalIntentJSONSerializer jsonSerializer;

    private CrimeLab(Context appContext) {
        this.appContext = appContext;

        jsonSerializer = new CriminalIntentJSONSerializer(appContext, FILENAME);

        try {
            crimes = jsonSerializer.loadCrimes();
        } catch (IOException | JSONException e) {
            crimes = new ArrayList<>();
            Log.e(TAG, "Error loading crimes", e);
        }

//        for (int i = 0; i < 100; i++) {
//            int n = i + 1;
//            Crime crime = new Crime();
//            crime.setTitle("Crime #" + n);
//            crime.setIsSolved(i % 2 == 0);
//            crimes.add(crime);
//        }
    }

    public static CrimeLab getInstance(Context context) {
        if (instance == null) {
            instance = new CrimeLab(context.getApplicationContext());
        }

        return instance;
    }

    public List<Crime> getCrimes() {
        return crimes;
    }

    public Crime getCrime(UUID id) {
        for (Crime crime : crimes) {
            if (crime.getId().equals(id)) {
                return crime;
            }
        }

        return null;
    }

    public void addCrime(Crime crime) {
        crimes.add(crime);
    }

    public boolean saveCrimes() {
        try {
            jsonSerializer.saveCrimes(crimes);

            Log.d(TAG, "crimes saved successfully");
            return true;
        } catch (JSONException | IOException e) {
            Log.e(TAG, "Error while saving crimes", e);
            return false;
        }
    }

}
