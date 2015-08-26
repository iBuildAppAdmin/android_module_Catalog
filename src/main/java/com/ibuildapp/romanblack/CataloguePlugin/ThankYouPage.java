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

import android.content.Intent;
import android.graphics.Color;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.appbuilder.sdk.android.AppBuilderModuleMain;
import com.ibuildapp.romanblack.CataloguePlugin.model.ShoppingCart;

public class ThankYouPage extends AppBuilderModuleMain {

    private int orderNumber;

    /**
     * Detect passed color (background) darkness and return actual font color. For dark background - white, for light - black.
     *
     * @param backColor background color
     * @return font color
     */
    public static int BackColorToFontColor(int backColor) {
        int r = (backColor >> 16) & 0xFF;
        int g = (backColor >> 8) & 0xFF;
        int b = backColor & 0xFF;

        return 0.299 * r + 0.587 * g + 0.114 * b > 127 ? Color.BLACK : Color.WHITE;
    }

    /**
     * The same as onCreate()
     */
    @Override
    public void create() {
        initializeBackend();
        initializeUI();
    }

    /**
     * Set some values
     */
    private void initializeBackend() {
        orderNumber = getIntent().getIntExtra(ShoppingCart.EXTRA_ORDER_NUMBER, -1);
    }

    /**
     * Initializing user interface
     */
    private void initializeUI() {
        hideTopBar();

        setContentView(R.layout.shopping_cart_thank_you_page);

        findViewById(R.id.navbar_holder).setBackgroundColor(getResources().getColor(Statics.uiConfig.colorSkin.color1 == Color.WHITE ? R.color.black_trans_20 : R.color.white_trans_50));
        findViewById(R.id.back_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeActivityOk();
            }
        });
        findViewById(R.id.layout).setBackgroundColor(Statics.uiConfig.colorSkin.color1);
        findViewById(R.id.orderconfirmation_scroll_root).setBackgroundColor(Statics.uiConfig.colorSkin.color1);

        TextView order_number = (TextView) findViewById(R.id.order_number);
        order_number.setText(Statics.shoppingCartFields.orderTitle + " " + orderNumber);
        order_number.setTextColor(Statics.uiConfig.colorSkin.color4);

        TextView text = (TextView) findViewById(R.id.text);
        text.setText(Statics.shoppingCartFields.orderText);
        text.setTextColor(Statics.uiConfig.colorSkin.color4);

        ((ImageView) findViewById(R.id.orderconfirmation_image)).setImageResource(
                BackColorToFontColor(Statics.uiConfig.colorSkin.color1) == Color.BLACK ?
                        R.drawable.sergeyb_shopingcard_cart_pic :
                        R.drawable.sergeyb_shopingcard_cart_pic_dark
        );
    }

    /**
     * Close activity with OK result
     */
    private void closeActivityOk() {
        setResult(RESULT_OK, new Intent());
        finish();
    }

    @Override
    public void onBackPressed() {
        closeActivityOk();
    }

}
