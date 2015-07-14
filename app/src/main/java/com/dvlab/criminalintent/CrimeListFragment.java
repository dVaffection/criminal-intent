package com.dvlab.criminalintent;

import android.app.ListFragment;
import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

public class CrimeListFragment extends ListFragment {

    public static final String TAG = CrimeListFragment.class.getSimpleName();

    private List<Crime> crimes;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getActivity().setTitle(R.string.crime_list_title_label);
        crimes = CrimeLab.getInstance(getActivity()).getCrimes();

//        ListAdapter adapter = new ArrayAdapter<Crime>(getActivity(), android.R.layout.simple_list_item_1, crimes);
        CrimeAdapter adapter = new CrimeAdapter(crimes);
        setListAdapter(adapter);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        Crime crime = (Crime) getListAdapter().getItem(position);

        Intent intent = new Intent(getActivity(), CrimeActivity.class);
        intent.putExtra(CrimeFragment.EXTRA_CRIME_ID, crime.getId());
        startActivity(intent);
    }

    private class CrimeAdapter extends ArrayAdapter<Crime> {

        public CrimeAdapter(List<Crime> crimes) {
            super(getActivity(), 0, crimes);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // If we weren't given a view, inflate one
            if (convertView == null) {
                convertView = getActivity().getLayoutInflater().inflate(R.layout.list_item_crime, null);
            }

            // Configure the view for this Crime
            Crime crime = getItem(position);
            TextView titleTextView = (TextView) convertView.findViewById(R.id.crime_list_item_title_text_view);
            titleTextView.setText(crime.getTitle());

            TextView dateTextView = (TextView) convertView.findViewById(R.id.crime_list_item_date_text_view);
            String dateFormatted = DateFormat.format("EEEE, MMMM dd, yyyy", crime.getDate()).toString();
            dateTextView.setText(dateFormatted);

            CheckBox solvedCheckBox = (CheckBox) convertView.findViewById(R.id.crime_list_item_solved_checkBox);
            solvedCheckBox.setChecked(crime.isSolved());

            return convertView;
        }

    }

    @Override
    public void onResume() {
        super.onResume();

        ((CrimeAdapter)getListAdapter()).notifyDataSetChanged();
    }
}
