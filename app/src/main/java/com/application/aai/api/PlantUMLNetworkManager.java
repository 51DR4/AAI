/*
 * Copyright (c) 2024 ABB-03
 * Licensed under the MIT License. See the LICENSE file for details.
 *
 */

package com.application.aai.api;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import androidx.appcompat.app.AppCompatActivity;

import com.application.aai.processing.requests.CustomCallback;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Provides methods for loading and editing PlantUML diagrams to prepare for display.
 */
public class PlantUMLNetworkManager {
    private static Bitmap drawableToBitmap(Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }
        int width = drawable.getIntrinsicWidth();
        int height = drawable.getIntrinsicHeight();
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }

    /**
     * Adds whitespace around an image to make it square, centering the original image in the middle. Used to keep the diagram sizes uniform.
     *
     * @param context  The context used to access resources.
     * @param drawable The drawable to be made square.
     * @return A new Drawable with a white square background and the original image centered.
     */
    public static Drawable addWhiteSpaceToMakeSquare(Context context, Drawable drawable) {
        // Convert the drawable to a bitmap
        Bitmap bitmap = drawableToBitmap(drawable);

        // Determine the new size (square) based on the largest dimension
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int newSize = Math.max(width, height);

        // Create a new bitmap with the new size and white background
        Bitmap newBitmap = Bitmap.createBitmap(newSize, newSize, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(newBitmap);
        canvas.drawColor(Color.WHITE);

        // Calculate the position to draw the original bitmap to center it
        int left = (newSize - width) / 2;
        int top = (newSize - height) / 2;

        // Draw the original bitmap onto the new canvas
        canvas.drawBitmap(bitmap, left, top, null);

        // Convert the new bitmap back to a drawable
        return new BitmapDrawable(context.getResources(), newBitmap);
    }

    /**
     * Loads an image from a URL and returns it as a square Drawable.
     *
     * @param context    The activity context, used to run UI-related code.
     * @param string     The URL string from which the image will be loaded.
     * @param callback   The callback to handle the result or any errors.
     * @return The loaded Drawable with whitespace added, or null if the image could not be loaded.
     */
    public static Drawable mLoad(AppCompatActivity context, String string, CustomCallback callback) {
        URL url = mStringToURL(string);
        HttpURLConnection connection = null;
        try {
            assert url != null;
            connection = (HttpURLConnection) url.openConnection();
            connection.connect();
            InputStream inputStream = connection.getInputStream();
            Drawable drawable = Drawable.createFromStream(inputStream, "image");
            if (drawable != null) {
                 drawable = addWhiteSpaceToMakeSquare(context, drawable);
                return drawable;
            }

        } catch (Exception e) {
            callback.handleEvent();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
        return null;
    }

    // Method for converting a string to URL
    private static URL mStringToURL(String string) {
        try {
            return new URL(string);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return null;
    }
}