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
import android.view.View;
import android.webkit.WebView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.appbuilder.sdk.android.AppBuilderModuleMain;
import com.ibuildapp.romanblack.CataloguePlugin.model.OnShoppingCartItemAddedListener;
import com.ibuildapp.romanblack.CataloguePlugin.model.ShoppingCart;

import java.util.List;

public class ExternalProductDetailsActivity extends AppBuilderModuleMain implements OnShoppingCartItemAddedListener {
    private WebView mainWebView;
    private String itemUrl;
    private LinearLayout backBtn;
    private int shopingCartIndex;

    @Override
    public void create() {
        content();
        UI();
    }

    private void UI() {
        setContentView(R.layout.catalog_external_details_layout);
        hideTopBar();

        backBtn = (LinearLayout) findViewById(R.id.back_btn);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        View basketBtn = findViewById(R.id.basket_view_btn);
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ExternalProductDetailsActivity.this, ShoppingCartPage.class);
                startActivity(intent);
            }
        };
        basketBtn.setOnClickListener(listener);

        View hamburgerView = findViewById(R.id.hamburger_view_btn);
        hamburgerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                animateRootContainer();
            }
        });
        if (!showSideBar) {
            hamburgerView.setVisibility(View.GONE);
            basketBtn.setVisibility(View.VISIBLE);
            basketBtn.setVisibility(Statics.isBasket ? View.VISIBLE : View.GONE);
            findViewById(R.id.cart_items).setVisibility(View.VISIBLE);

            if(Statics.isBasket) {
                onShoppingCartItemAdded();
                shopingCartIndex = setTopBarRightButton(basketBtn, getResources().getString(R.string.shopping_cart), listener);
            } else
                findViewById(R.id.cart_items).setVisibility(View.GONE);
        }
        else {
            hamburgerView.setVisibility(View.VISIBLE);
            findViewById(R.id.cart_items).setVisibility(View.INVISIBLE);
            basketBtn.setVisibility(View.GONE);
        }

        mainWebView = (WebView) findViewById(R.id.catalog_external_web_view);

        mainWebView.loadUrl(itemUrl);
    }

    private void content() {
        Intent intent = getIntent();
        itemUrl = intent.getStringExtra("itemUrl");
    }

    @Override
    public void destroy() {
    }

    @Override
    public void onShoppingCartItemAdded() {
        List<ShoppingCart.Product> products = ShoppingCart.getProducts();
        int count = 0;

        for (ShoppingCart.Product product : products)
            count += product.getQuantity();

        TextView cart_items = (TextView) findViewById(R.id.cart_items);
        cart_items.setText(String.valueOf(count));
        cart_items.setVisibility(count > 0 && Statics.isBasket && !showSideBar ? View.VISIBLE : View.GONE);

        if (showSideBar && Statics.isBasket) {
            StringBuilder resString = new StringBuilder( getResources().getString(R.string.shopping_cart));
            if (count > 0)
                resString.append(" (" + String.valueOf(count) + ")");
            updateWidgetInActualList(shopingCartIndex, resString.toString() );
        }
    }
}
