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
package com.ibuildapp.romanblack.CataloguePlugin.fragments;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.ibuildapp.romanblack.CataloguePlugin.R;
import com.ibuildapp.romanblack.CataloguePlugin.imageloader.ImageTask;
import com.ibuildapp.romanblack.CataloguePlugin.imageloader.Plugin;


public class DetailsItemFragment extends Fragment {
    private ImageView view;
    private ProgressBar progress;
    private String entity;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(
                R.layout.details_viewer_item_layout, container, false);

        view = (ImageView) rootView.findViewById(R.id.details_viewer_item_image);
        progress = (ProgressBar) rootView.findViewById(R.id.details_viewer_item_progress);
        progress.setVisibility(View.VISIBLE);
        getBitmap(entity, new ImageTask.OnBitmapPreparedListener() {
            @Override
            public void onBitmapPrepared(final Bitmap bitmap) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        view.setImageBitmap(bitmap);
                    }
                });
                progress.setVisibility(View.GONE);
            }
        });

        return rootView;
    }

    public String getEntity() {
        return entity;
    }

    public void setEntity(String entity) {
        this.entity = entity;
    }

    protected Runnable getBitmap(String url, ImageTask.OnBitmapPreparedListener onBitmapPreparedListener) {
        ImageTask task = new ImageTask(getActivity(), url, onBitmapPreparedListener);
        Plugin.INSTANCE.addComplexTask(task);

        return task;
    }
}
