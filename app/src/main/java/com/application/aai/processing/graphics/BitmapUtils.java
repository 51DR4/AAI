/*
 * Copyright (c) 2024 ABB-03
 * Licensed under the MIT License. See the LICENSE file for details.
 *
 */

package com.application.aai.processing.graphics;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.TypedValue;

import androidx.appcompat.app.AppCompatActivity;
import androidx.exifinterface.media.ExifInterface;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class BitmapUtils {
    static AppCompatActivity context;
    /**
     * Singleton Pattern
     */
    private static BitmapUtils instance;
    private BitmapUtils() { }
    public static BitmapUtils getInstance(AppCompatActivity context) {
        if (instance == null) instance = new BitmapUtils();
        BitmapUtils.context = context;
        return instance;
    }

    public String saveImageToInternalStorage(Bitmap bitmap, String fileName) {
        try {
            // Create an output stream to the internal storage
            FileOutputStream fos = context.openFileOutput(fileName, Context.MODE_PRIVATE);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.close();
            // Get the file path from the internal storage
            return context.getFileStreamPath(fileName).getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private Bitmap getRoundedCornerBitmap(Bitmap bitmap, int dpRadius, int dpMargin) {
        int pxRadius = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpRadius, context.getResources().getDisplayMetrics());
        int pxMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpMargin, context.getResources().getDisplayMetrics());

        Bitmap output = Bitmap.createBitmap(bitmap.getWidth() + 2 * pxMargin, bitmap.getHeight() + 2 * pxMargin, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setShader(new BitmapShader(bitmap, BitmapShader.TileMode.CLAMP, BitmapShader.TileMode.CLAMP));

        RectF rect = new RectF(pxMargin, pxMargin, bitmap.getWidth() + pxMargin, bitmap.getHeight() + pxMargin);
        canvas.drawRoundRect(rect, pxRadius, pxRadius, paint);

        return output;
    }

    public Bitmap drawableToBitmap(Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    public Bitmap getBitmapFromUri(Uri uri) {
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            if (inputStream != null) {
                inputStream.close();
            }
            return bitmap;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public Bitmap rotateBitmap(Bitmap bitmap, float degrees) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degrees);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    private Bitmap resizeBitmap(Bitmap bitmap, int desiredWidth) {
        int originalWidth = bitmap.getWidth();
        int originalHeight = bitmap.getHeight();

        // Calculate the aspect ratio
        float aspectRatio = (float) originalHeight / originalWidth;

        // Calculate the desired height to maintain the aspect ratio
        int desiredHeight = Math.round(desiredWidth * aspectRatio);

        // Create a scaled bitmap with the desired dimensions
        return Bitmap.createScaledBitmap(bitmap, desiredWidth, desiredHeight, true);
    }

    private int getRotationFromExif(Uri imageUri) {
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(imageUri);
            assert inputStream != null;
            ExifInterface exif = new ExifInterface(inputStream);
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    return 90;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    return 180;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    return 270;
                default:
                    return 0;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return 0;
        }
    }
}
