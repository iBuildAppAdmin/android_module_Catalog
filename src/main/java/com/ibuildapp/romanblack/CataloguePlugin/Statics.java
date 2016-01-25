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
package com.ibuildapp.romanblack.CataloguePlugin;

import com.ibuildapp.romanblack.CataloguePlugin.model.ShoppingCartFields;
import com.ibuildapp.romanblack.CataloguePlugin.model.UIConfig;

public class Statics {
    public static final String CACHE_FILE_PATH = "catalogue_plugin";
    public static final String GRID_STYLE = "grid";
    public static final String LIST_STYLE = "list";
    public static final int GRID_NUM_COLUMNS = 2;
    public static final int GRID_MARGINS = 6;
    public static final int GRID_SPASING = 8;
    public static String ENDPOINT = "catalogue_plugin";
    public static String PAYPAL_CLIENT_ID = "";
    public static String FACEBOOK_APP_TOKEN;


    public static String appId;
    public static String widgetName;
    public static String orderUrl;
    public static boolean isBasket;
    public static int widgetOrder;
    public static int widgetId;
    public static String moduleCachePath;
    public static String appName;
    public static UIConfig uiConfig;
    public static boolean hasAd;
    public static boolean isShoppingCartPayPalBased = false;
    public static ShoppingCartFields shoppingCartFields = new ShoppingCartFields();
}
