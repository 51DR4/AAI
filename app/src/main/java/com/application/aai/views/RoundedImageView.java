/*
 * Copyright (c) 2024 ABB-03
 * Licensed under the MIT License. See the LICENSE file for details.
 *
 */

package com.application.aai.views;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import androidx.appcompat.widget.AppCompatImageView;

public class RoundedImageView extends AppCompatImageView {

    private Path path;

    public RoundedImageView(Context context) {
        super(context);
        init();
    }

    public RoundedImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public RoundedImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(Color.WHITE);
        path = new Path();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (getDrawable() == null) {
            super.onDraw(canvas);
            return;
        }

        // Set the path for rounded corners
        path.reset();
        @SuppressLint("DrawAllocation") RectF rectF = new RectF(0, 0, getWidth(), getHeight());
        // Radius of corners
        float radius = 17f;
        path.addRoundRect(rectF, radius, radius, Path.Direction.CW);

        canvas.clipPath(path);
        super.onDraw(canvas);
    }
}
