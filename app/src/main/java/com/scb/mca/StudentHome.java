package com.scb.mca;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.os.Bundle;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.scb.mca.fragments.StudentAttendanceFragment;
import com.scb.mca.fragments.StudentHomeFragment;
import com.scb.mca.fragments.StudentNotesFragment;
import com.scb.mca.fragments.StudentPaymentsFragment;
import com.scb.mca.fragments.StudentSettingsFragment;

public class StudentHome extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_home);

        //loading default fragment
        loadFragment(new StudentHomeFragment());

        //getting bottom navigation view and attaching the listener
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(this);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Fragment fragment=null;

        switch (item.getItemId()){
            case R.id.navigation_home:
                fragment = new StudentHomeFragment();
                break;

            case R.id.navigation_calendar:
                fragment = new StudentAttendanceFragment();
                break;

            case R.id.navigation_notes:
                fragment = new StudentNotesFragment();
                break;

            case R.id.navigation_payment:
                fragment = new StudentPaymentsFragment();
                break;

            case R.id.navigation_settings:
                fragment = new StudentSettingsFragment();
                break;
        }
        return loadFragment(fragment);
    }

    private boolean loadFragment(Fragment fragment){
        if(fragment!=null){
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container,fragment)
                    .commit();
            return true;
        }
        return false;
    }

}