/*
 * Copyright (c) 2024 ABB-03
 * Licensed under the MIT License. See the LICENSE file for details.
 *
 */

package com.application.aai.views;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.application.aai.processing.graphics.BitmapUtils;
import com.application.aai.processing.requests.RequestManager;
import com.example.aai.R;
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ImagePreviewActivity extends AppCompatActivity {
    private Uri imageUri;
    private Uri originalImageUri;  // Variable to store the original image URI
    private ExecutorService executorService;
    private BitmapUtils bitmapUtils;
    private GestureDetector gestureDetector;
    private ViewSwitcher viewSwitcher;
    private ImageView imageView;
    private TextView textView;
    private TextView explanation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_preview);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.preview), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize the ExecutorService
        executorService = Executors.newSingleThreadExecutor();

        TextView imageName = findViewById(R.id.image_name);
        explanation = findViewById(R.id.explanation);
        viewSwitcher = findViewById(R.id.viewSwitcher);
        imageView = findViewById(R.id.savedImageView);
        textView = findViewById(R.id.imageTextView);

        gestureDetector = new GestureDetector(this, new SwipeGestureDetector());

        // Add touch listener to the ViewSwitcher to detect swipes
        viewSwitcher.setOnTouchListener((v, event) -> gestureDetector.onTouchEvent(event));

        // Get the file URI from the intent
        Intent intent = getIntent();
        imageUri = intent.getParcelableExtra("FILE_URI");
        if (imageUri != null) {
            // get the image name
            String fileName = getFileNameFromPath(getFilePathFromUri(this, imageUri));
            imageName.setText(fileName);
            // Store the original image URI
            originalImageUri = imageUri;
            // Execute the image loading and processing in the background
            updateImage();

            OnBackPressedCallback callback = new OnBackPressedCallback(true) {
                @Override
                public void handleOnBackPressed() {
                    Intent intent = new Intent(ImagePreviewActivity.this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                }
            };
            getOnBackPressedDispatcher().addCallback(this, callback);
        }

        ImageButton cropButton = findViewById(R.id.button_crop_image);
        cropButton.setOnClickListener(v -> startCropActivity(originalImageUri));

        bitmapUtils = BitmapUtils.getInstance(this);
    }

    public static String getFilePathFromUri(Context context, Uri uri) {
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null);
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                    return cursor.getString(columnIndex);
                }
            } finally {
                cursor.close();
            }
        }
        return null;
    }

    public static String getFileNameFromPath(String filePath) {
        if (filePath != null) {
            return filePath.substring(filePath.lastIndexOf('/') + 1);
        }
        return null;
    }

    private void updateImage() {
        executorService.execute(() -> {
            try {
                ContentResolver resolver = getContentResolver();
                InputStream inputStream = resolver.openInputStream(imageUri);
                Drawable drawable = Drawable.createFromStream(inputStream, "image");
                if (drawable != null) {
                    // Ensures the view is properly laid out before getting its dimensions
                    imageView.post(() -> runOnUiThread(() -> imageView.setImageDrawable(drawable)));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public void startCropActivity(@Nullable Uri uri) {
        String destinationFileName = "croppedImage.jpg";
        Uri destinationUri = Uri.fromFile(new File(getCacheDir(), destinationFileName));

        UCrop.Options options = new UCrop.Options();
        options.setFreeStyleCropEnabled(true); // Enable free ratio cropping

        assert uri != null;
        UCrop.of(uri, destinationUri)
                .withOptions(options)
                .start(this);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return gestureDetector.onTouchEvent(event) || super.onTouchEvent(event);
    }

    private class SwipeGestureDetector extends GestureDetector.SimpleOnGestureListener {
        private static final int SWIPE_THRESHOLD = 40;  // Decreased threshold for more sensitivity
        private static final int SWIPE_VELOCITY_THRESHOLD = 50;  // Decreased velocity threshold for faster response

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            if (Math.abs(e1.getX() - e2.getX()) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                if (e1.getX() > e2.getX()) {
                    // Swipe left - Show ImageView
                    showImageView();
                    toggleExplanationVisibility();
                } else {
                    // Swipe right - Show TextView
                    showTextView();
                    toggleExplanationVisibility();
                }
                return true;
            }
            return false;
        }

        private void toggleExplanationVisibility() {
            if (explanation.getVisibility() == View.VISIBLE) explanation.setVisibility(View.GONE);
            else explanation.setVisibility(View.VISIBLE);
        }
    }

    private void showImageView() {
        Animation inAnimation = AnimationUtils.loadAnimation(this, android.R.anim.slide_in_left);
        Animation outAnimation = AnimationUtils.loadAnimation(this, android.R.anim.slide_out_right);

        viewSwitcher.setInAnimation(inAnimation);
        viewSwitcher.setOutAnimation(outAnimation);

        viewSwitcher.showNext(); // Show next view (ImageView)
    }

    private void showTextView() {
        Animation inAnimation = AnimationUtils.loadAnimation(this, android.R.anim.slide_in_left);
        Animation outAnimation = AnimationUtils.loadAnimation(this, android.R.anim.slide_out_right);

        viewSwitcher.setInAnimation(inAnimation);
        viewSwitcher.setOutAnimation(outAnimation);

        viewSwitcher.showPrevious(); // Show previous view (TextView)
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == UCrop.REQUEST_CROP) {
            assert data != null;
            Uri resultUri = UCrop.getOutput(data);
            if (resultUri != null) {
                imageUri = resultUri;  // Update the imageUri to the cropped image
                updateImage();
            }
        } else if (resultCode == UCrop.RESULT_ERROR) {
            assert data != null;
            final Throwable cropError = UCrop.getError(data);
            if (cropError != null) {
                Toast.makeText(this, cropError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void onOKClick(View v) {
        // Convert the URI to a Bitmap
        Bitmap bitmap = bitmapUtils.getBitmapFromUri(imageUri);

        // Save the Bitmap to internal storage and get the file path
        assert bitmap != null;
        String uniqueFileName = "image_" + System.currentTimeMillis() + ".png";
        String filePath = bitmapUtils.saveImageToInternalStorage(bitmap, uniqueFileName);

        if (filePath != null) {
            // Pass the file path to the ImagePreview activity
            RequestManager.getInstance().setInput(bitmap, filePath);
            RequestManager.getInstance().setInput(textView.getText().toString());
        } else {
            Toast.makeText(this, "Could not save image. Please try again.", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        }
        // Start the AnalysisActivity to show a loading screen
        Intent analysisIntent = new Intent(this, AnalysisActivity.class);
        startActivity(analysisIntent);
    }

    public void onRetryClick(View v) {
        Intent homeIntent = new Intent(this, MainActivity.class);
        startActivity(homeIntent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Shutdown the ExecutorService to avoid memory leaks
        executorService.shutdown();
    }

    public void clearClick(View v) {
        textView.setText("");
    }
}
