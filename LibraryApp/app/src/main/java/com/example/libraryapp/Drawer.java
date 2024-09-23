package com.example.libraryapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.room.Room;

import com.example.libraryapp.database.LibraryDatabase;
import com.example.libraryapp.databinding.ActivityDrawerBinding;
import com.example.libraryapp.notifications.DueDateCheckService;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.firestore.FirebaseFirestore;


public class Drawer extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private ActivityDrawerBinding binding;
    private SharedPreferences sharedPreferences;
    public static LibraryDatabase database;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityDrawerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.appBarDrawer.toolbar);

        sharedPreferences = getSharedPreferences("loginPrefs", Context.MODE_PRIVATE);

        // Check if the user is not logged in
        if (!sharedPreferences.getBoolean("loggedIn", false)) {
            // If not logged in, start the MainActivity
            startActivity(new Intent(Drawer.this, MainActivity.class));
            finish(); // Finish the DrawerActivity so the user can't go back to it until logged in
        }


        binding.appBarDrawer.mailbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String emailAddress = "librarysupport@gmail.com";

                Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
                emailIntent.setData(Uri.parse("mailto:" + emailAddress));
                startActivity(emailIntent);
            }
        });
        database = Room.databaseBuilder(getApplicationContext(), LibraryDatabase.class,
                "c").allowMainThreadQueries().build();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_search, R.id.nav_insert_book, R.id.nav_update_book, R.id.nav_delete_book, R.id.nav_insert_member, R.id.nav_update_member, R.id.nav_delete_member,R.id.nav_insert_section, R.id.nav_update_section, R.id.nav_delete_section, R.id.nav_insert_loan, R.id.nav_update_loan, R.id.nav_delete_loan)
                .setOpenableLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_drawer);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        Intent serviceIntent = new Intent(this, DueDateCheckService.class);
        startService(serviceIntent);
    }




    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_drawer);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }
}
