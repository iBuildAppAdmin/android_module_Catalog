/****************************************************************************
 * *
 * Copyright (C) 2014-2015 iBuildApp, Inc. ( http://ibuildapp.com )         *
 * *
 * This file is part of iBuildApp.                                          *
 * *
 * This Source Code Form is subject to the terms of the iBuildApp License.  *
 * You can obtain one at http://ibuildapp.com/license/                      *
 * *
 ****************************************************************************/
package com.ibuildapp.romanblack.CataloguePlugin.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;

public class RoundView extends ImageView {

    private final String TAG = RoundView.class.getCanonicalName();

    private int cornerTopLeft = 0;
    private int cornerTopRight = 0;
    private int cornerBottomLeft = 0;
    private int cornerBottomRight = 0;

    public RoundView(Context context) {
        super(context);
    }

    public RoundView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * Set bitmap for this imageView with alpha
     *
     * @param bm bitmap
     */
    public void setImageBitmapWithAlpha(Bitmap bm) {
        super.setImageBitmap(bm);
        Animation alpha = new AlphaAnimation(0.3f, 1.0f);
        alpha.setDuration(500);
        startAnimation(alpha);
    }

    /**
     * Set bitmap for this imageView with alpha
     *
     * @param cornerTopLeft     top left corner
     * @param cornerTopRight    top right corner
     * @param cornerBottomLeft  bottom left corner
     * @param cornerBottomRight bottom right corner
     */
    public void setCorners(
            int cornerTopLeft,
            int cornerTopRight,
            int cornerBottomLeft,
            int cornerBottomRight) {
        this.cornerTopLeft = cornerTopLeft;
        this.cornerTopRight = cornerTopRight;
        this.cornerBottomLeft = cornerBottomLeft;
        this.cornerBottomRight = cornerBottomRight;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        double res = MeasureSpec.getSize(widthMeasureSpec);
        res = res * 6;
        res = res / 5;
        int parentWidth = MeasureSpec.getSize(widthMeasureSpec);
        this.setMeasuredDimension(parentWidth, (int) res);
    }

    @Override
    protected void onDraw(Canvas canvas) {

        Path clipPath = new Path();
        RectF rect = new RectF(0, 0, this.getWidth(), this.getHeight());

        float[] rad = new float[]{
                cornerTopLeft, cornerTopLeft,
                cornerTopRight, cornerTopRight,
                cornerBottomRight, cornerBottomRight,
                cornerBottomLeft, cornerBottomLeft};

        clipPath.addRoundRect(rect, rad, Path.Direction.CW);
        canvas.clipPath(clipPath);
        super.onDraw(canvas);
    }

}
