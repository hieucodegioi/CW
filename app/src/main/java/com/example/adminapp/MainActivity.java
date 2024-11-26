package com.example.adminapp;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNavigationView = findViewById(R.id.bottom_navigation);

        // Only load default fragment if this is the first creation
        if (savedInstanceState == null) {
            loadFragment(new HomeFragment());
        }

        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId();
            
            // Using if-else instead of multiple if statements for better readability
            if (itemId == R.id.action_add) {
                selectedFragment = new AddCourseFragment();
            } else if (itemId == R.id.action_home) {
                selectedFragment = new HomeFragment();
            } else if (itemId == R.id.action_search) {
                selectedFragment = new SearchFragment();
            }
            
            return selectedFragment != null && loadFragment(selectedFragment);
        });
    }

    private boolean loadFragment(Fragment fragment) {
        if (fragment != null) {
            getSupportFragmentManager()
                .beginTransaction()
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)  // Add smooth transition
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
            return true;
        }
        return false;
    }

    @Override
    public void onBackPressed() {
        // If there's only one fragment left in the back stack, finish the activity
        if (getSupportFragmentManager().getBackStackEntryCount() <= 1) {
            finish();
        } else {
            super.onBackPressed();
        }
    }
}
