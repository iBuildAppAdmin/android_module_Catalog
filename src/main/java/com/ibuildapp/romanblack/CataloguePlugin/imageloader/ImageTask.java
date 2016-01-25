/****************************************************************************
*                                                                           *
*  Copyright (C) 2014-2015 iBuildApp, Inc. ( http://ibuildapp.com )         *
*                                                                           *
*  This file is part of iBuildApp.                                          *
*                                                                           *
*  This Source Code Form is subject to the terms of the iBuildApp License.  *
*  You can obtain one at http://ibuildapp.com/license/                      *
*                                                                           *
****************************************************************************/
package com.ibuildapp.romanblack.CataloguePlugin.imageloader;

import android.app.Activity;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.webkit.URLUtil;

import java.io.File;
import java.io.FileOutputStream;

public class ImageTask  extends Plugin.ComplexTask {
    public interface OnBitmapPreparedListener {
        void onBitmapPrepared(Bitmap bitmap);
    }

    private Activity context;
    private String url;
    private String md5;
    private String filePath;
    private OnBitmapPreparedListener onBitmapPreparedListener;
    private static int widthPixels;

    public ImageTask(Activity context, String url, OnBitmapPreparedListener onBitmapPreparedListener) {
        if(TextUtils.isEmpty(url) || !URLUtil.isValidUrl(url))
            throw new IllegalArgumentException("Url must have a valid value");

        this.context = context;
        if (widthPixels == 0)
            widthPixels = context.getResources().getDisplayMetrics().widthPixels;
        this.url = url;
        this.md5 = com.appbuilder.sdk.android.Utils.md5(url)+"full";
        this.filePath = StaticData.getCachePath(context)+md5;
        this.onBitmapPreparedListener = onBitmapPreparedListener;
    }

    private void callListener(final Bitmap bitmap, final OnBitmapPreparedListener onBitmapPreparedListener) {
        if (onBitmapPreparedListener != null && context != null)
            context.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    onBitmapPreparedListener.onBitmapPrepared(bitmap);
                }
            });
    }

    @Override
    public int hashCode() {
        return url.hashCode();
    }

    @Override
    public boolean equals(Object object) {
        return object instanceof ImageTask && url.equals(((ImageTask)object).url);
    }

    @Override
    protected DownloadResult download() {
        File file = new File(filePath);

        if(file.exists())
            return DownloadResult.NEEDLESS;

        if (!file.exists()) {
            String downloadResult = Utils.downloadFile(context, url, md5);

            if (TextUtils.isEmpty(downloadResult))
                return DownloadResult.FAILED;
            else
                return DownloadResult.COMPLETED;
        }

        return null;
    }

    @Override
    protected void work(DownloadResult downloadResult) {
        if(downloadResult == null || downloadResult == DownloadResult.FAILED)
            return;

        File  file = new File(filePath);
        Bitmap bitmap = Utils.processBitmap(
                file.getAbsolutePath(),
                Bitmap.Config.RGB_565, widthPixels);

        if(downloadResult == DownloadResult.COMPLETED) {
            FileOutputStream fileOutputStream = null;

            try {
                fileOutputStream = new FileOutputStream(file, false);
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream);
            } catch (Exception exception) {
                exception.printStackTrace();
            } finally {
                try {
                    if(fileOutputStream != null)
                        fileOutputStream.close();
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
            }
        }

        if(bitmap != null) {
            Plugin.INSTANCE.cacheBitmap(md5, bitmap);
            callListener(bitmap, onBitmapPreparedListener);
        }

    }
}
