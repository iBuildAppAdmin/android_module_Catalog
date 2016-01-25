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
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class AlphaGradiendView extends FrameLayout {

    private ImageView image;
    private LinearLayout gradientHolder;

    public AlphaGradiendView(Context context) {
        super(context);
        init();
    }

    public AlphaGradiendView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    /**
     * Init view
     */
    private void init() {
        image = new ImageView(getContext());

        GradientDrawable gradient = new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP, new int[]{Color.parseColor("#b6000000"),
                Color.parseColor("#00000000")});
        gradient.setGradientType(GradientDrawable.LINEAR_GRADIENT);

        gradientHolder = new LinearLayout(getContext());
        gradientHolder.setOrientation(LinearLayout.VERTICAL);
        gradientHolder.setVisibility(View.GONE);

        LinearLayout topLayout = new LinearLayout(getContext());
        LinearLayout bottomLayout = new LinearLayout(getContext());
        bottomLayout.setBackgroundDrawable(gradient);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        params.weight = 1;
        gradientHolder.addView(topLayout, params);
        gradientHolder.addView(bottomLayout, params);


        addView(image, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        addView(gradientHolder, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
    }

    /**
     * Set bitmap for image
     *
     * @param btm bitmap
     */
    public void setImageBitmap(Bitmap btm) {
        Animation alpha = new AlphaAnimation(0.3f, 1.0f);
        alpha.setDuration(500);
        image.startAnimation(alpha);
        image.setImageBitmap(btm);

        gradientHolder.setVisibility(VISIBLE);
    }

    /**
     * Set color for image
     *
     * @param color color
     */
    public void setColor(int color) {
        image.setBackgroundColor(color);
        gradientHolder.setVisibility(GONE);
    }
}
