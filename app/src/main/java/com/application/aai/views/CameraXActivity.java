/*
 * Copyright (c) 2024 ABB-03
 * Licensed under the MIT License. See the LICENSE file for details.
 *
 */

package com.application.aai.views;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.application.aai.processing.camera.CameraManager;
import com.example.aai.R;

public class CameraXActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Initialize layout of the activity: parse XML layout, convert into View objects in memory and arrange them based on the layout
        setContentView(R.layout.activity_camerax);
        EdgeToEdge.enable(this);

        CameraManager cameraManager = new CameraManager();
        cameraManager.setContext(this);
        cameraManager.startCamera();

    }
}
