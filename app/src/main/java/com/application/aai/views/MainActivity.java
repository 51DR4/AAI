/*
 * Copyright (c) 2024 ABB-03
 * Licensed under the MIT License. See the LICENSE file for details.
 *
 */

package com.application.aai.views;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.Manifest;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.aai.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private ActivityResultLauncher<PickVisualMediaRequest> pickMedia;
    // Variables for toggling day/night mode
    private static final String PREFS_NAME = "prefs";
    private static final String PREF_DARK_MODE = "dark_mode";
    private ActivityResultLauncher<String[]> requestPermissionLauncher;
    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom-100);
            return insets;
        });

        // Load the user's preference for dark mode
        preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean isDarkMode = preferences.getBoolean(PREF_DARK_MODE, false);
        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

        ImageButton toggleButton = findViewById(R.id.color_mode_toggle);
        toggleButton.setOnClickListener(this::toggleDarkModeClick);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.navigation_home);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.navigation_home) {
                return true;
            } else if (itemId == R.id.navigation_history) {

                Intent intent = new Intent(MainActivity.this, HistoryActivity.class);
                startActivity(intent);
                return true;
            }

            return false;

        });

        // Registers a photo picker activity launcher in single-select mode
        registerPickMedia();

        ImageButton info = findViewById(R.id.information);
        info.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, HelpActivity.class);
            startActivity(intent);
        });

    }
    private void registerPickMedia(){
        pickMedia = registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
            if(uri != null){
                // Pass the URI to the ImagePreview activity
                Intent intent = new Intent(MainActivity.this, ImagePreviewActivity.class);
                intent.putExtra("FILE_URI", uri);
                startActivity(intent);
            }
            else {
                Toast.makeText(MainActivity.this, "No image selected", Toast.LENGTH_SHORT).show();
            }
        });

        requestPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(),
                result -> {
                    boolean allGranted = true;
                    for (Map.Entry<String, Boolean> entry : result.entrySet()) {
                        if (!entry.getValue()) {
                            allGranted = false;
                            break;
                        }
                    }
                }
        );
        requestPermissions();
    }

    private void requestPermissions() {
        String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.MANAGE_EXTERNAL_STORAGE, Manifest.permission.ACCESS_MEDIA_LOCATION,
                Manifest.permission.INTERNET,
                Manifest.permission.READ_MEDIA_IMAGES};
        requestPermissionLauncher.launch(permissions);
    }

    public void addMenuClick(View v) {
        PopupMenu popupMenu = new PopupMenu(this, v);
        popupMenu.setOnMenuItemClickListener(item -> true);
        popupMenu.inflate(R.menu.add_input_pop_up);
        popupMenu.show();
    }

    public void audioClick(MenuItem menuItem) {
        Intent textIntent = new Intent(this, DescriptionActivity.class);
        startActivity(textIntent);
    }

    public void cameraClick(MenuItem menuItem) {
        Intent cameraIntent = new Intent(this, CameraXActivity.class);
        startActivity(cameraIntent);
    }

    public void galleryClick(MenuItem menuItem) {
        // launch the photo picker and let the user choose only images
        pickMedia.launch(new PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                .build());
    }

    public void toggleDarkModeClick(View v) {
        boolean isDarkMode = (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES);
        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }

        // Save the user's preference
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(PREF_DARK_MODE, !isDarkMode);
        editor.apply();

        // Restart the activity to apply the theme change immediately
        recreate();
    }
}