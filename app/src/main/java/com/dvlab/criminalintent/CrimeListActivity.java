package com.dvlab.criminalintent;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

public class CrimeListActivity extends SingleFragmentActivity implements CrimeListFragment.Callbacks, CrimeFragment.Callbacks {

    @Override
    protected Fragment createFragment() {
        return new CrimeListFragment();
    }

    @Override
    public void onCrimeUpdated(Crime crime) {
        FragmentManager fm = getSupportFragmentManager();
        CrimeListFragment listFragment = (CrimeListFragment) fm.findFragmentById(R.id.fragment_container);
        listFragment.updateUI();
    }

    @Override
    public void onCrimeDeleted() {
        FragmentManager fm = getSupportFragmentManager();
        CrimeListFragment listFragment = (CrimeListFragment) fm.findFragmentById(R.id.fragment_container);
        listFragment.updateUI();
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_masterdetail;
    }

    @Override
    public void onCrimeSelected(Crime crime) {
        // this is the way to check if we have "mast-detail" layout

        if (findViewById(R.id.detail_fragment_container) == null) {
            Intent intent = new Intent(this, CrimePagerActivity.class);
            intent.putExtra(CrimeFragment.EXTRA_CRIME_ID, crime.getId());
            startActivity(intent);
        } else {
            FragmentManager fm = getSupportFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            Fragment oldDetail = fm.findFragmentById(R.id.detail_fragment_container);

            CrimeFragment newDetail = new CrimeFragment();
            Bundle args = newDetail.createArgs(crime.getId());
            newDetail.setArguments(args);

            if (oldDetail != null) {
                ft.remove(oldDetail);
            }

            ft.add(R.id.detail_fragment_container, newDetail);
            ft.commit();
        }


    }
}
