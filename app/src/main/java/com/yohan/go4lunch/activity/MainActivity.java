package com.yohan.go4lunch.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.login.LoginManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.yohan.go4lunch.R;
import com.yohan.go4lunch.fragment.FragmentList;
import com.yohan.go4lunch.fragment.FragmentWorkmates;
import com.yohan.go4lunch.fragment.FragmentMap;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private BottomNavigationView bottomNavigationView;
    private Toolbar toolbar;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private SimpleDraweeView profilePicture;
    private TextView drawerTvName, drawerTvEmail;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fresco.initialize(getApplicationContext());
        setContentView(R.layout.activity_main);

        //TOOLBAR
        toolbar = findViewById(R.id.activity_main_toolbar);
        setSupportActionBar(toolbar);

        //ToolBar Hamburger menu toggle
        drawerLayout = findViewById(R.id.drawer);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        //Drawer Navigation Listener
        navigationView = findViewById(R.id.activity_main_nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //Load data in drawer header
        loadDataInHeader();

        //Handle fragments in bottom navigation view
        bottomNavBarFragmentsManagement();

        //Display Map fragment by default
        displayFragment(new FragmentMap());
    }

    private void bottomNavBarFragmentsManagement() {
        bottomNavigationView = findViewById(R.id.navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.navigation_map:
                    toolbar.setTitle(R.string.toolbar_first_title);
                    MainActivity.this.displayFragment(new FragmentMap());
                    return true;
                case R.id.navigation_list:
                    toolbar.setTitle(R.string.toolbar_first_title);
                    MainActivity.this.displayFragment(new FragmentList());
                    return true;
                case R.id.navigation_workmates:
                    toolbar.setTitle(R.string.toolbar_second_title);
                    MainActivity.this.displayFragment(new FragmentWorkmates());
                    return true;
            }
            return false;
        });
    }

    private void loadDataInHeader() {
        View headerView = navigationView.getHeaderView(0);
        //Profile picture
        profilePicture = headerView.findViewById(R.id.drawerIvProfilePicture);
        profilePicture.setImageURI(FirebaseAuth.getInstance().getCurrentUser().getPhotoUrl());
        //Name
        drawerTvName = headerView.findViewById(R.id.drawerTvName);
        drawerTvName.setText(FirebaseAuth.getInstance().getCurrentUser().getDisplayName());
        //Email
        drawerTvEmail = headerView.findViewById(R.id.drawerTvEmail);
        drawerTvEmail.setText(FirebaseAuth.getInstance().getCurrentUser().getEmail());
    }

    private void displayFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.frame_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        //Handle Navigation Item Click
        switch (item.getItemId()){
            case R.id.drawer_lunch :
                break;
            case R.id.drawer_settings:
                //Start settings activity
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                break;
            case R.id.drawer_logout:
                //Logout user
                FirebaseAuth.getInstance().signOut();
                LoginManager.getInstance().logOut();
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
                break;
            default:
                break;
        }

        this.drawerLayout.closeDrawer(GravityCompat.START);

        return true;
    }

    @Override
    public void onBackPressed() {
        //Handle back click to close menu
        if (this.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            this.drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}
