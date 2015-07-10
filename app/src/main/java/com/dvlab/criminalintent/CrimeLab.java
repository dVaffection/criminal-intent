package com.dvlab.criminalintent;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CrimeLab {

    private static CrimeLab instance;
    private Context appContext;
    private List<Crime> crimes;

    private CrimeLab(Context appContext) {
        this.appContext = appContext;
        crimes = new ArrayList<>();

        for (int i = 0; i < 100; i++) {
            int n = i + 1;
            Crime crime = new Crime();
            crime.setTitle("Crime #" + n);
            crime.setIsSolved(i % 2 == 0);
            crimes.add(crime);
        }
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

}
