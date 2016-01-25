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
package com.ibuildapp.romanblack.CataloguePlugin.imageloader;

import android.content.Context;
import android.text.TextUtils;

import java.io.File;

public class StaticData {
    private static final String PLUGIN_CACHE_FOLDER = com.appbuilder.sdk.android.Utils.md5("CatalogPlugin");

    private static String cachePath;

    public static String getCachePath(Context context) {
        if(TextUtils.isEmpty(cachePath))
            cachePath = context.getExternalCacheDir() + File.separator + PLUGIN_CACHE_FOLDER + File.separator;

        return cachePath;
    }
}
