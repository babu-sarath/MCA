package com.scb.mca;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.scb.mca.fragments.StudentAttendanceFragment;
import com.scb.mca.fragments.TeacherAttendanceFragment;
import com.scb.mca.fragments.TeacherHomeFragment;
import com.scb.mca.fragments.TeacherNotesFragment;
import com.scb.mca.fragments.TeacherPaymentsFragment;
import com.scb.mca.fragments.TeacherSettingsFragment;

public class TeacherHome extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_home);

        //loading default fragment
        loadFragment(new TeacherHomeFragment());

        //getting bottom navigation view and attaching the listener
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(this);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Fragment fragment=null;

        switch (item.getItemId()){
            case R.id.navigation_home:
                fragment = new TeacherHomeFragment();
                break;

            case R.id.navigation_calendar:
                fragment = new TeacherAttendanceFragment();
                break;

            case R.id.navigation_notes:
                fragment = new TeacherNotesFragment();
                break;

            case R.id.navigation_payment:
                fragment = new TeacherPaymentsFragment();
                break;

            case R.id.navigation_settings:
                fragment = new TeacherSettingsFragment();
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