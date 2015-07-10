package com.dvlab.criminalintent;

import android.app.Fragment;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;

public class CrimeFragment extends Fragment {

    private Crime crime;
    private EditText crimeTitle;
    private Button dateButton;
    private CheckBox solvedCheckbox;

    public CrimeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        crime = new Crime();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_crime, container, false);

        crimeTitle = (EditText) view.findViewById(R.id.crime_title);
        crimeTitle.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // intentionally left blank
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                crime.setTitle(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
                // intentionally left blank
            }
        });

        String dateFormatted = DateFormat.format("EEEE, MMMM dd, yyyy", crime.getDate()).toString();
        dateButton = (Button) view.findViewById(R.id.crime_date);
        dateButton.setText(dateFormatted);
        dateButton.setEnabled(false);

        solvedCheckbox = (CheckBox) view.findViewById(R.id.crime_solved);
        solvedCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                crime.setIsSolved(isChecked);
            }
        });

        return view;
    }

}
