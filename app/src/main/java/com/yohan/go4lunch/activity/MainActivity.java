package com.yohan.go4lunch.activity;

import android.os.Bundle;
import android.view.MenuItem;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.yohan.go4lunch.R;
import com.yohan.go4lunch.fragments.FragmentList;
import com.yohan.go4lunch.fragments.FragmentMap;
import com.yohan.go4lunch.fragments.FragmentWorkmates;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();

        BottomNavigationView bottomNavigationView = findViewById(R.id.navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment fragment;
                switch (item.getItemId()) {
                    case R.id.navigation_map:
                        fragment = new FragmentMap();
                        MainActivity.this.loadFragment(fragment);
                        return true;
                    case R.id.navigation_list:
                        fragment = new FragmentList();
                        MainActivity.this.loadFragment(fragment);
                        return true;
                    case R.id.navigation_workmates:
                        fragment = new FragmentWorkmates();
                        MainActivity.this.loadFragment(fragment);
                        return true;
                }
                return false;
            }
        });

        //Load map fragment by default
        loadFragment(new FragmentMap());
    }

    private void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.frame_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

}
