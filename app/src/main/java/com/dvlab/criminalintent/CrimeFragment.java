package com.dvlab.criminalintent;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.NavUtils;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.Date;
import java.util.List;
import java.util.UUID;

public class CrimeFragment extends Fragment {

    public static final String TAG = CrimeFragment.class.getSimpleName();

    public static final String EXTRA_CRIME_ID = "com.dvlab.criminalintent.crime_id";
    public static final String DIALOG_DATE = "date";
    private static final String DIALOG_IMAGE = "image";


    public static final int REQUEST_DATE = 0;
    public static final int REQUEST_PHOTO = 1;
    public static final int REQUEST_CONTACT = 2;


    private Callbacks callbacks;
    private Crime crime;
    private EditText crimeTitle;
    private Button dateButton;
    private CheckBox solvedCheckbox;
    private ImageButton photoButton;
    private ImageView photoView;
    private Button suspectButton;
    private Button callSuspectButton;

    public CrimeFragment() {
    }

    public Bundle createArgs(UUID crimeId) {
        Bundle args = new Bundle();
        args.putSerializable(EXTRA_CRIME_ID, crimeId);
        return args;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);

        UUID crimeId = (UUID) getArguments().getSerializable(EXTRA_CRIME_ID);
        crime = CrimeLab.getInstance(getActivity()).getCrime(crimeId);
    }

    @Override
    public void onStart() {
        super.onStart();

        showPhoto();
    }

    @Override
    public void onPause() {
        super.onPause();

        Log.i(TAG, "CrimeFragment.onPause");
        CrimeLab.getInstance(getActivity()).saveCrimes();
    }

    @Override
    public void onStop() {
        super.onStop();

        PictureUtils.cleanImageView(photoView);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        callbacks = (Callbacks) activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();

        callbacks = null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_crime, container, false);

        // technically our app level is "Jelly Bean" (16) but let's keep it for reference
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            if (NavUtils.getParentActivityName(getActivity()) != null) {
                ActionBar actionBar = getActivity().getActionBar();
                if (actionBar != null) {
                    actionBar.setDisplayHomeAsUpEnabled(true);
                }
            }
        }

        crimeTitle = (EditText) view.findViewById(R.id.crime_title);
        crimeTitle.setText(crime.getTitle());
        crimeTitle.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // intentionally left blank
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                crime.setTitle(s.toString());
                getActivity().setTitle(crime.getTitle());

                callbacks.onCrimeUpdated(crime);
            }

            @Override
            public void afterTextChanged(Editable s) {
                // intentionally left blank
            }
        });

        String dateFormatted = DateFormat.format("EEEE, MMMM dd, yyyy", crime.getDate()).toString();
        dateButton = (Button) view.findViewById(R.id.crime_date);
        dateButton.setText(dateFormatted);
        dateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fm = getActivity().getSupportFragmentManager();
                DatePickerFragment dialog = new DatePickerFragment();
                Bundle args = dialog.createArgs(crime.getDate());
                dialog.setArguments(args);
                dialog.setTargetFragment(CrimeFragment.this, REQUEST_DATE);
                dialog.show(fm, DIALOG_DATE);
            }
        });

        solvedCheckbox = (CheckBox) view.findViewById(R.id.crime_solved);
        solvedCheckbox.setChecked(crime.isSolved());
        solvedCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                crime.setIsSolved(isChecked);

                callbacks.onCrimeUpdated(crime);
            }
        });

        photoButton = (ImageButton) view.findViewById(R.id.crime_image_button);
        photoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), CrimeCameraActivity.class);
                startActivityForResult(intent, REQUEST_PHOTO);
            }
        });

        // If camera is not available, disable camera functionality
        PackageManager pm = getActivity().getPackageManager();
        boolean hasACamera = pm.hasSystemFeature(PackageManager.FEATURE_CAMERA) ||
                pm.hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT) ||
                (Camera.getNumberOfCameras() > 0);

        if (!hasACamera) {
            photoButton.setEnabled(false);
        }

        photoView = (ImageView) view.findViewById(R.id.crime_image_view);
        photoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Photo p = crime.getPhoto();
                if (p == null)
                    return;

                FragmentManager fm = getActivity().getSupportFragmentManager();
                String path = getActivity().getFileStreamPath(p.getFilename()).getAbsolutePath();
                ImageFragment.newInstance(path).show(fm, DIALOG_IMAGE);
            }
        });
        registerForContextMenu(photoView);

        Button reportButton = (Button) view.findViewById(R.id.crime_report_button);
        reportButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_TEXT, getCrimeReport());
                intent.putExtra(Intent.EXTRA_SUBJECT, R.string.crime_report_subject);
                intent = Intent.createChooser(intent, getString(R.string.send_report));

                if (isIntentSafe(intent)) {
                    startActivity(intent);
                } else {
                    Toast.makeText(getActivity(), "Unfortunately there is no app to share crime report", Toast.LENGTH_SHORT).show();
                }
            }
        });

        suspectButton = (Button) view.findViewById(R.id.crime_suspect_button);
        suspectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);

                if (isIntentSafe(intent)) {
                    startActivityForResult(intent, REQUEST_CONTACT);
                } else {
                    Toast.makeText(getActivity(), "Unfortunately there is no app to choose suspect from", Toast.LENGTH_SHORT).show();
                }
            }
        });
        if (crime.getSuspect() != null) {
            suspectButton.setText(crime.getSuspect());
        }

        callSuspectButton = (Button) view.findViewById(R.id.crime_call_suspect_button);
        if (crime.getSuspectNumber() != null) {
            callSuspectButton.setEnabled(true);
        }
        callSuspectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (crime.getSuspectNumber() != null) {
                    Uri number = Uri.parse("tel:" + crime.getSuspectNumber());
                    Intent intent = new Intent(Intent.ACTION_DIAL);
                    intent.setData(number);
                    startActivity(intent);
                } else {
                    Toast.makeText(getActivity(), "Suspect doesn't have a number", Toast.LENGTH_SHORT).show();
                }
            }
        });

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) return;

        if (requestCode == REQUEST_DATE) {
            Date date = (Date) data.getSerializableExtra(DatePickerFragment.EXTRA_DATE);
            crime.setDate(date);
            String dateFormatted = DateFormat.format("EEEE, MMMM dd, yyyy", date).toString();
            dateButton.setText(dateFormatted);

            callbacks.onCrimeUpdated(crime);
        } else if (requestCode == REQUEST_PHOTO) {
            // Create a new Photo object and attach it to the crime
            String filename = data.getStringExtra(CrimeCameraFragment.EXTRA_PHOTO_FILENAME);
            if (filename != null) {
                Photo p = new Photo(filename);
                crime.setPhoto(p);
                showPhoto();
            }

            callbacks.onCrimeUpdated(crime);
        } else if (requestCode == REQUEST_CONTACT) {
            String[] queryFields;
            Uri uri;
            Cursor cursor;

            uri = data.getData();
            queryFields = new String[]{ContactsContract.Contacts.DISPLAY_NAME};
            cursor = getActivity().getContentResolver().query(uri, queryFields, null, null, null);
            if (cursor.getCount() == 0) {
                cursor.close();
                return;
            }

            cursor.moveToFirst();
            String suspectName = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
            crime.setSuspect(suspectName);
            suspectButton.setText(suspectName);


            uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
            queryFields = new String[]{
                    ContactsContract.Contacts.DISPLAY_NAME,
                    ContactsContract.CommonDataKinds.Phone.NUMBER
            };

            // Perform your query - the contactUri is like a "where" clause here
            cursor = getActivity().getContentResolver().query(uri, queryFields, null, null, null);
            // Double-check that you actually got results
            if (cursor.getCount() == 0) {
                cursor.close();
                return;
            }
            // Pull out the first column of the first row of data -
            // that is your suspect's name.
            cursor.moveToFirst();

            int indexName = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
            int indexNumber = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);

            do {
                suspectName = cursor.getString(indexName);

                // if there is no phone number it doesn't work :(
                // gotta check for it first
                if (suspectName.equals(crime.getSuspect())) {
                    String suspectNumber = cursor.getString(indexNumber);

                    crime.setSuspectNumber(suspectNumber);
                    callSuspectButton.setEnabled(true);

                    break;
                }
            } while (cursor.moveToNext());

            cursor.close();


            callbacks.onCrimeUpdated(crime);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.fragment_crime, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (NavUtils.getParentActivityName(getActivity()) != null) {
                    NavUtils.navigateUpFromSameTask(getActivity());
                }
                return true;

            case R.id.menu_item_delete_crime:
                UUID crimeId = (UUID) getArguments().getSerializable(EXTRA_CRIME_ID);
                CrimeLab.getInstance(getActivity()).deleteCrime(crimeId);

                callbacks.onCrimeDeleted();

                if (NavUtils.getParentActivityName(getActivity()) != null) {
                    NavUtils.navigateUpFromSameTask(getActivity());
                }

                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        // inflate context menu and consider attaching it to the list `onCreateView`
        // but better use `MultiChoiceModeListener`
        getActivity().getMenuInflater().inflate(R.menu.crime_context, menu);

        super.onCreateContextMenu(menu, v, menuInfo);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_delete_photo:
                PictureUtils.cleanImageView(photoView);
                crime.setPhoto(null);

                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    private void showPhoto() {
        // (Re)set the image button's image based on our photo
        Photo p = crime.getPhoto();
        BitmapDrawable b = null;
        if (p != null) {
            String path = getActivity().getFileStreamPath(p.getFilename()).getAbsolutePath();
            b = PictureUtils.getScaledDrawable(getActivity(), path);
        }

        photoView.setImageDrawable(b);
    }

    private String getCrimeReport() {
        String solvedString = null;
        if (crime.isSolved()) {
            solvedString = getString(R.string.crime_report_solved);
        } else {
            solvedString = getString(R.string.crime_report_unsolved);
        }
        String dateFormat = "EEE, MMM dd";
        String dateString = DateFormat.format(dateFormat, crime.getDate()).toString();
        String suspect = crime.getSuspect();
        if (suspect == null) {
            suspect = getString(R.string.crime_report_no_suspect);
        } else {
            suspect = getString(R.string.crime_report_suspect, suspect);
        }

        String report = getString(R.string.crime_report, crime.getTitle(), dateString, solvedString, suspect);
        return report;
    }

    private boolean isIntentSafe(Intent intent) {
        PackageManager pm = getActivity().getPackageManager();
        List<ResolveInfo> activities = pm.queryIntentActivities(intent, 0);
        return activities.size() > 0;
    }

    public interface Callbacks {
        void onCrimeUpdated(Crime crime);
        void onCrimeDeleted();
    }


}
