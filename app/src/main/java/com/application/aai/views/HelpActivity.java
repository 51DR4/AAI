/*
 * Copyright (c) 2024 ABB-03
 * Licensed under the MIT License. See the LICENSE file for details.
 *
 */

package com.application.aai.views;


import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.aai.R;


public class HelpActivity extends AppCompatActivity{
    private VideoView videoView;
    private TextView aboutText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_help);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.information), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        SwitchCompat switchButton = findViewById(R.id.switch_button);
        videoView = findViewById(R.id.UI_video);
        aboutText = findViewById(R.id.ABB);

        // Set the path to the video file in the res/raw directory
        String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.tutorial;
        Uri uri = Uri.parse(videoPath);
        videoView.setVideoURI(uri);

        // Add media controls to the VideoView
        MediaController mediaController = new MediaController(this);
        videoView.setMediaController(mediaController);
        mediaController.setAnchorView(videoView);

        switchButton.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                videoView.setVisibility(View.VISIBLE);
                aboutText.setVisibility(View.GONE);
                videoView.start();  // Start the video when it becomes visible
            } else {
                videoView.setVisibility(View.GONE);
                aboutText.setVisibility(View.VISIBLE);
                videoView.pause();  // Pause the video when it is hidden
            }
        });
    }

}