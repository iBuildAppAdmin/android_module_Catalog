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
package com.ibuildapp.romanblack.CataloguePlugin.model;

import android.graphics.Color;

import java.io.Serializable;

/**
 * Class describes module colorschema
 */
public class ColorSkin implements Serializable {

    public boolean isLight = true;

    public int color1 = Color.parseColor("#974ab6"); // default colors
    public int color2 = Color.parseColor("#ffe8c5");
    public int color3 = Color.parseColor("#FFFFFF");
    public int color4 = Color.parseColor("#fed9fe");
    public int color5 = Color.parseColor("#ffe8c5");
    public int color6;
    public int color7;
    public int color8;
}
