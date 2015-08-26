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

import java.io.Serializable;

/**
 * Describes settings for currency, mainpage style in handset mode and colorschema
 */
public class UIConfig implements Serializable {

    public String currency = "$";                       // price currency
    public String currencyPosition = "left";            // currency position
    public String mainpagestyle = "list";               // categiry view style grid\list
    public ColorSkin colorSkin = new ColorSkin();       // colorskin
}