/*
 * Copyright (c) 2024 ABB-03
 * Licensed under the MIT License. See the LICENSE file for details.
 *
 */

package com.application.aai.processing.camera;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.net.Uri;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.AspectRatio;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraControl;
import androidx.camera.core.CameraInfo;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;

import com.application.aai.views.ImagePreviewActivity;
import com.example.aai.R;
import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

/**
 * CameraManager handles the camera functionality within the app. It's responsible for the camera preview, image capture, zoom, and flash control.
 */

public class CameraManager {
    private GestureDetector gestureDetector;
    private AppCompatActivity context;
    private PreviewView previewView;
    private ImageButton toggleFlashButton;
    private ImageButton flipCameraButton;
    private ImageCapture imageCapture;
    private int cameraFacing = CameraSelector.LENS_FACING_BACK;
    private ActivityResultLauncher<String> activityResultLauncher;
    private ScaleGestureDetector scaleGestureDetector;
    private CameraControl cameraControl;
    private CameraInfo cameraInfo;

    /**
     * Sets the context and initializes camera-related views and gesture detectors.
     *
     * @param context The context of the calling activity.
     */
    public void setContext(AppCompatActivity context) {
        this.context = context;
        previewView = context.findViewById(R.id.cameraPreview);
        toggleFlashButton = context.findViewById(R.id.toggleFlash);
        flipCameraButton = context.findViewById(R.id.flipCamera);

        // Initialize ScaleGestureDetector
        scaleGestureDetector = new ScaleGestureDetector(context,
                new ScaleGestureDetector.SimpleOnScaleGestureListener() {
                    @Override
                    public boolean onScale(@NonNull ScaleGestureDetector detector) {
                        if (cameraControl != null && cameraInfo != null) {
                            float currentZoomRatio = Objects.requireNonNull(cameraInfo.getZoomState().getValue()).getZoomRatio();
                            float delta = detector.getScaleFactor();
                            float newZoomRatio = currentZoomRatio * delta;
                            cameraControl.setZoomRatio(newZoomRatio);
                        }
                        return true;
                    }
                });


        // initialize gestureDetector
        gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener(){
            @Override
            public boolean onDoubleTap(@NonNull MotionEvent e) {
                return CameraManager.this.onDoubleTap();
            }

        });

        // Set touch listener for preview view
        previewView.setOnTouchListener((v, event) -> {
            scaleGestureDetector.onTouchEvent(event);
            gestureDetector.onTouchEvent(event);
            return true;
        });

    }

    /**
     * Starts the camera and handles permissions, preview, and camera controls.
     */
    public void startCamera() {
//        activityResultLauncher = context.registerForActivityResult(new ActivityResultContracts.RequestPermission(), result -> {});
//
//            if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
//                activityResultLauncher.launch(Manifest.permission.CAMERA);
//            }

            int aspectRatio = aspectRatio(previewView.getWidth(), previewView.getHeight());

            ListenableFuture<ProcessCameraProvider> listenableFuture = ProcessCameraProvider.getInstance(context);

            listenableFuture.addListener(() -> {
                try {
                    // Camera configuration
                    ProcessCameraProvider cameraProvider = listenableFuture.get();

                    Preview preview = new Preview.Builder().setTargetAspectRatio(aspectRatio).build();

                    imageCapture = new ImageCapture.Builder().setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                            .setTargetRotation(context.getWindowManager().getDefaultDisplay().getRotation()).build();

                    CameraSelector cameraSelector = new CameraSelector.Builder()
                            .requireLensFacing(cameraFacing).build();

                    cameraProvider.unbindAll();

                Camera camera = cameraProvider.bindToLifecycle(context, cameraSelector, preview, imageCapture);
                cameraControl = camera.getCameraControl();
                cameraInfo = camera.getCameraInfo();

                flipCameraButton.setOnClickListener(v -> {
                    if (cameraFacing == CameraSelector.LENS_FACING_BACK) {
                        cameraFacing = CameraSelector.LENS_FACING_FRONT;
                    } else {
                        cameraFacing = CameraSelector.LENS_FACING_BACK;
                    }
                    startCamera();
                });

                    toggleFlashButton.setOnClickListener(view -> setFlashIcon(camera));
                    // Set where the camera preview will be shown
                    preview.setSurfaceProvider(previewView.createSurfaceProvider());

                } catch (ExecutionException | InterruptedException e) {
                    handleCameraInitializationError(e);
                } catch (Exception e) {
                    handleCameraInitializationError(new RuntimeException("Unexpected error initializing camera.", e));
                }
            }, ContextCompat.getMainExecutor(context));
        }

        /**
         * Toggles the camera flash on or off and updates the flash button icon accordingly.
         *
         * @param camera The camera instance used to control the flash.
         */
        private void setFlashIcon(Camera camera) {
            if (camera.getCameraInfo().hasFlashUnit()) {
                if (camera.getCameraInfo().getTorchState().getValue() == 0) {
                    camera.getCameraControl().enableTorch(true);
                    toggleFlashButton.setImageResource(R.drawable.flash_off);
                } else {
                    camera.getCameraControl().enableTorch(false);
                    toggleFlashButton.setImageResource(R.drawable.flash);
                }
            } else {
                context.runOnUiThread(() -> Toast.makeText(context, "Flash is not available currently", Toast.LENGTH_SHORT).show());
            }
        }

        /**
         * Captures an image and saves it to the device's external storage.
         * Also opens a preview activity to display the captured image.
         */
        public void takePicture() {
//            if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
//                activityResultLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
//            }
            // Create a file to save the captured image
            final File file = new File(context.getExternalFilesDir(null), System.currentTimeMillis() + ".jpg");
            ImageCapture.OutputFileOptions outputFileOptions = new ImageCapture.OutputFileOptions.Builder(file).build();
            imageCapture.takePicture(outputFileOptions, Executors.newCachedThreadPool(), new ImageCapture.OnImageSavedCallback() {
                @Override
                public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                    context.runOnUiThread(() -> {
                        Intent intent = new Intent(context, ImagePreviewActivity.class);
                        Uri fileUri = Uri.fromFile(file);
                        intent.putExtra("FILE_URI", fileUri);
                        context.startActivity(intent);
                    });
                }

                @Override
                public void onError(@NonNull ImageCaptureException exception) {
                    context.runOnUiThread(() -> Toast.makeText(context, "Failed to save image. Please try again.", Toast.LENGTH_SHORT).show());
                }
            });
        }

        public Bitmap rotateBitmap(Bitmap bitmap, float degrees) {
            Matrix matrix = new Matrix();
            matrix.postRotate(degrees);
            return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        }

        /**
         * Determines the optimal aspect ratio for the camera preview based on the given width and height.
         *
         * @param width  The width of the preview.
         * @param height The height of the preview.
         * @return The aspect ratio, either 4:3 or 16:9.
         */
        private int aspectRatio(int width, int height) {
            double previewRatio = (double) Math.max(width, height) / Math.min(width, height);
            if (Math.abs(previewRatio - 4.0 / 3.0) <= Math.abs(previewRatio - 16.0 / 9.0)) {
                return AspectRatio.RATIO_4_3;
            }
            return AspectRatio.RATIO_16_9;
        }

        public boolean onDoubleTap() {
            takePicture();
            return true;
        }

        /**
         * Handles errors that occur during camera initialization.
         *
         * @param e The exception that was thrown.
         */
        private void handleCameraInitializationError(Exception e) {
            context.runOnUiThread(() -> Toast.makeText(context, "Error initializing camera: " + e.getMessage(), Toast.LENGTH_LONG).show());
        }
    }

