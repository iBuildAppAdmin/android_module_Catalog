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
package com.ibuildapp.romanblack.CataloguePlugin.task;

/**
 * Created with IntelliJ IDEA.
 * User: SimpleIce
 * Date: 10.09.14
 * Time: 13:17
 * To change this template use File | Settings | File Templates.
 */

import android.app.Activity;
import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.ImageView;

import com.ibuildapp.romanblack.CataloguePlugin.Statics;
import com.ibuildapp.romanblack.CataloguePlugin.adapter.OnImageDoneListener;
import com.ibuildapp.romanblack.CataloguePlugin.utils.Utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * Support thread for baseimageadapter
 * Each copy of such class checks image in assets, cache or download it
 * As a result it calls the callback function
 */
public class GetBitmapTask extends Thread {

    private final String TAG = "com.ibuildapp.romanblack.ShopingCartPlugin.tasks";
    private int WIDTH_LIMITER = 300;
    private boolean isInterrupted = false;
    private int uid;
    private ImageView id;
    private String url;
    private String cachePath;
    private String resPath;
    private String name;
    private OnImageDoneListener listener;
    private AssetManager assetMgr;
    private int roundedCorners;
    private int reactionType;

    /**
     * @param context
     * @param uid       - task uid
     * @param name      - taks name
     * @param id        - uid of image
     * @param resPath   - path of image in assets
     * @param cachePath - path of image in cache
     * @param url       - - path of image at http
     * @param width     - not use now
     * @param height    - not use now
     */

    public GetBitmapTask(Context context, int uid, String name, ImageView id, String resPath, String cachePath, String url, int width, int height, int roundedCorners, int reactionType) {

        this.uid = uid;
        this.name = name;
        this.id = id;
        this.resPath = resPath;
        this.cachePath = cachePath;
        this.url = url;
        this.roundedCorners = roundedCorners;
        this.reactionType = reactionType;

        assetMgr = context.getAssets();

        float density = context.getResources().getDisplayMetrics().density;
        DisplayMetrics metrics = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int screenWidth = metrics.widthPixels;
        WIDTH_LIMITER = (screenWidth - (2 * (int) (density * Statics.GRID_MARGINS)) - (int) (density * Statics.GRID_SPASING)) / 2;
    }

    @Override
    public void interrupt() {
        super.interrupt();
        isInterrupted = true;
    }

    @Override
    public void run() {
        super.run();

        Bitmap result = null;

        // 1. check bitmap in assets
        if (!TextUtils.isEmpty(resPath)) {
            InputStream stream = null;
            try {
                stream = assetMgr.open(resPath);
                result = Utils.proccessBitmap(stream, Bitmap.Config.RGB_565);

                if (listener != null) {
                    Log.e(TAG, "RESOURSES");
                    listener.onImageLoaded(uid, id, name, result, null, reactionType);
                    return;
                }
            } catch (IOException e) {
                stream = null;
            }
        }

        if (isInterrupted) {
            return;
        }
        // 2. check bitmap in cache
        if (!TextUtils.isEmpty(cachePath)) {
            File imageFile = new File(cachePath);
            if (imageFile.exists()) {
                result = Utils.proccessBitmap(cachePath, Bitmap.Config.RGB_565, WIDTH_LIMITER);

                if (listener != null) {
                    Log.e(TAG, "CACHE");
                    listener.onImageLoaded(uid, id, name, result, cachePath, reactionType);
                    return;
                }
            }
        }

        if (isInterrupted) {
            return;
        }

        // 3. download bitmap from www
        if (!TextUtils.isEmpty(url)) {

            String downloadedImg =
                    Utils.downloadFile(url);


            if (downloadedImg != null) {

                result = Utils.proccessBitmap(downloadedImg, Bitmap.Config.RGB_565, WIDTH_LIMITER);

                if (result == null) {
                    Log.e(TAG, "btm = null");
                }

                if (listener != null) {
                    Log.e(TAG, "HTTP");
                    listener.onImageLoaded(uid, id, name, result, downloadedImg, reactionType);
                    return;
                }
            }
        }

        if (listener != null) {
            Log.e(TAG, "NULL");
            listener.onImageLoaded(uid, id, name, null, null, reactionType);
            return;
        }
    }

    /**
     * Set listener witch calls when image prepared
     *
     * @param listener listener
     */
    public void setListener(OnImageDoneListener listener) {
        this.listener = listener;
    }
}