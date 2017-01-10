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

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.appbuilder.sdk.android.AppBuilderModuleMain;
import com.appbuilder.sdk.android.StartUpActivity;
import com.appbuilder.sdk.android.Widget;
import com.ibuildapp.romanblack.CataloguePlugin.adapter.BaseImageAdapter;
import com.ibuildapp.romanblack.CataloguePlugin.adapter.CategoryProductGridAdapter;
import com.ibuildapp.romanblack.CataloguePlugin.adapter.CategoryProductListAdapter;
import com.ibuildapp.romanblack.CataloguePlugin.database.SqlAdapter;
import com.ibuildapp.romanblack.CataloguePlugin.model.CategoryEntity;
import com.ibuildapp.romanblack.CataloguePlugin.model.CategoryProduct;
import com.ibuildapp.romanblack.CataloguePlugin.model.ProductEntity;
import com.ibuildapp.romanblack.CataloguePlugin.model.ShoppingCart;
import com.ibuildapp.romanblack.CataloguePlugin.utils.Utils;
import com.ibuildapp.romanblack.CataloguePlugin.view.SearchView;
import com.ibuildapp.romanblack.CataloguePlugin.xml.XmlParser;
import com.jess.ui.TwoWayGridView;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@StartUpActivity(moduleName = "Catalog")
public class CataloguePlugin extends AppBuilderModuleMain {

    private static final String BASE_ENDPOINT = "/endpoint/payment.php";

    private final String TAG = CataloguePlugin.class.getCanonicalName();
    private String widgetXml;
    private ListView list;
    private TwoWayGridView grid;
    private LinearLayout root;
    private List<CategoryProduct> categoryProductList = new ArrayList<>();
    private BaseImageAdapter adapter;
    private TextView title;
    private ImageView searchViewBtn;
    private LinearLayout backBtn;
    private LinearLayout searchHolder;
    private TextView cancel;
    private SearchView search;
    private RelativeLayout titleHolder;
    private float density;
    private String pageTitle;
    private int shopingCartIndex;
    private Thread loader;
    private boolean destroyed;
    private String url = "http://ibuilder.solovathost.com/test/data.catalog.xml";
    /**
     * The same as onCreate()
     */
    @Override
    public void create() {
        initializeBackend();
        initializeUI();
        loadContent();
    }

    /**
     * Loading content
     */
    private void loadContent() {
        loader = new Thread(new Runnable() {
            @Override
            public void run() {
                XmlParser parser = new XmlParser(widgetXml);
                parser.parser();

                url = parser.getUiConfig().url;
                if (!url.equals("")) {
                    widgetXml = Utils.downloadFileAsString(url);
                    parser = new XmlParser(widgetXml);
                    parser.parser();
                }
                Statics.uiConfig = parser.getUiConfig();
                Statics.PAYPAL_CLIENT_ID = parser.getPaymentData() == null ? "" : parser.getPaymentData().getClientId();
                Statics.ENDPOINT = com.appbuilder.sdk.android.Statics.BASE_DOMEN + BASE_ENDPOINT;

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        root.setBackgroundColor(Statics.uiConfig.colorSkin.color1);

                        View hamburgerView = findViewById(R.id.hamburger_view_btn);
                        hamburgerView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                animateRootContainer();
                            }
                        });
                        View basketBtn = findViewById(R.id.basket_view_btn);
                        View.OnClickListener listener = new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent intent = new Intent(CataloguePlugin.this, ShoppingCartPage.class);
                                startActivity(intent);
                            }
                        };
                        basketBtn.setOnClickListener(listener);
                        if (!showSideBar) {
                            hamburgerView.setVisibility(View.GONE);
                            basketBtn.setVisibility(View.VISIBLE);
                            basketBtn.setVisibility(Statics.isBasket ? View.VISIBLE : View.GONE);
                            findViewById(R.id.cart_items).setVisibility(View.VISIBLE);
                        }
                        else {
                            hamburgerView.setVisibility(View.VISIBLE);
                            findViewById(R.id.cart_items).setVisibility(View.INVISIBLE);
                            basketBtn.setVisibility(View.GONE);
                            if(Statics.isBasket)
                                shopingCartIndex = setTopBarRightButton(basketBtn, getResources().getString(R.string.shopping_cart), listener);
                        }
                        resume();

                        if (!Statics.isBasket) {
                            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) searchViewBtn.getLayoutParams();
                            if (!showSideBar)
                                params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                        } else {
                            List<ShoppingCart.Product> products = ShoppingCart.getProducts();
                            int count = 0;

                            for (ShoppingCart.Product product : products)
                                count += product.getQuantity();

                            TextView cart_items = (TextView) findViewById(R.id.cart_items);
                            cart_items.setText(String.valueOf(count));
                            if (showSideBar) {
                                StringBuilder resString = new StringBuilder( getResources().getString(R.string.shopping_cart));
                                if (count > 0)
                                    resString.append(" (").append(String.valueOf(count)).append(")");
                                updateWidgetInActualList(shopingCartIndex, resString.toString() );
                            }
                        }
                    }
                });

                SqlAdapter.deleteDataFromTables();
                List<CategoryEntity> categoryEntityList = parser.getCategoryList();
                List<CategoryEntity> categoryListToDelete = new ArrayList<>();
                List<CategoryEntity> categoryListToInsert = new ArrayList<>();
                for (CategoryEntity cat : categoryEntityList) {
                    if (cat.valid) {
                        categoryListToInsert.add(cat);
                    } else
                        categoryListToDelete.add(cat);
                }
                SqlAdapter.deleteCategoryRow(categoryListToDelete);
                SqlAdapter.insertCategoryRows(categoryListToInsert);

                List<ProductEntity> productEntityList = parser.getProductList();
                List<ProductEntity> productListToDelete = new ArrayList<>();
                List<ProductEntity> productListToInsert = new ArrayList<>();
                for (ProductEntity prod : productEntityList) {
                    if (prod.valid)
                        productListToInsert.add(prod);
                    else productListToDelete.add(prod);
                }
                SqlAdapter.deleteProductRow(productListToDelete);
                SqlAdapter.insertProductRows(productListToInsert);


                // select categories for rendering
                categoryEntityList.clear();
                categoryEntityList = SqlAdapter.selectCategoryWithParendId(0);
                if (Statics.uiConfig.mainpagestyle.compareToIgnoreCase("grid") == 0) {
                    if (categoryEntityList.size() % 2 != 0)
                        categoryEntityList.add(new CategoryEntity());
                } else if (categoryEntityList.size() == 1)
                    categoryEntityList.add(new CategoryEntity());

                productEntityList.clear();
                productEntityList = SqlAdapter.selectProductsForCategory(0);
                for (CategoryEntity cat : categoryEntityList)
                    categoryProductList.add(new CategoryProduct(cat, null));

                for (int i = 0; i < productEntityList.size(); i++) {
                    ProductEntity prod = productEntityList.get(i);

                    if (i == 0) {
                        prod.marginTop = true;
                        if (productEntityList.size() == 1)
                            prod.marginBottom = true;
                    }

                    if (i == productEntityList.size() - 1) {
                        prod.marginBottom = true;
                    }

                    categoryProductList.add(new CategoryProduct(null, prod));
                }

                if ((categoryProductList.size() == 2) && (categoryProductList.get(0).category != null) && (categoryProductList.get(1).category != null) && (categoryProductList.get(1).category.parentId == 0) && (categoryProductList.get(1).category.name.equals(""))) {

                    // select categories for rendering
                    if (SqlAdapter.selectCategoryWithParendId(categoryProductList.get(0).category.id).size() != 0) {

                    } else {
                        categoryEntityList.clear();
                        int parentCategoryId = categoryProductList.get(0).category.id;
                        categoryProductList.clear();

                        categoryEntityList = SqlAdapter.selectCategoryWithParendId(parentCategoryId);
                        if (Statics.uiConfig.mainpagestyle.compareToIgnoreCase("grid") == 0) {
                            if (categoryEntityList.size() % 2 != 0)
                                categoryEntityList.add(new CategoryEntity());
                        }

                        productEntityList.clear();
                        productEntityList = SqlAdapter.selectProductsForCategory(parentCategoryId);
                        for (CategoryEntity cat : categoryEntityList)
                            categoryProductList.add(new CategoryProduct(cat, null));

                        for (int i = 0; i < productEntityList.size(); i++) {
                            ProductEntity prod = productEntityList.get(i);

                            if (i == 0) {
                                prod.marginTop = true;
                                if (productEntityList.size() == 1)
                                    prod.marginBottom = true;
                            }

                            if (i == productEntityList.size() - 1)
                                prod.marginBottom = true;

                            categoryProductList.add(new CategoryProduct(null, prod));
                        }
                    }
                }

                Log.e(TAG, "");

                if(!destroyed)
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            FrameLayout navbarHolder = (FrameLayout) findViewById(R.id.navbar_holder);
                            if (Statics.uiConfig.colorSkin.color1 == Color.WHITE)
                                navbarHolder.setBackgroundColor(getResources().getColor(R.color.black_trans_20));
                            else
                                navbarHolder.setBackgroundColor(getResources().getColor(R.color.white_trans_50));

                            if (Statics.uiConfig.mainpagestyle.compareToIgnoreCase(Statics.GRID_STYLE) == 0) {
                                grid = new TwoWayGridView(CataloguePlugin.this);
                                grid.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                                grid.setNumColumns(Statics.GRID_NUM_COLUMNS);
                                grid.setVerticalSpacing((int) (density * Statics.GRID_SPASING));
                                grid.setHorizontalSpacing((int) (density * Statics.GRID_SPASING));
                                grid.setVerticalScrollBarEnabled(false);
                                grid.setScrollDirectionPortrait(0);
                                grid.setScrollDirectionLandscape(0);
                                grid.setCacheColorHint(Color.TRANSPARENT);
                                grid.setSelector(R.drawable.adapter_selector);
                                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                                params.setMargins(
                                        (int) (density * Statics.GRID_MARGINS),
                                        (int) (density * Statics.GRID_SPASING),
                                        (int) (density * Statics.GRID_MARGINS),
                                        (int) (density * Statics.GRID_SPASING));
                                root.addView(grid, params);
                                adapter = new CategoryProductGridAdapter(CataloguePlugin.this, categoryProductList, grid);
                                grid.setAdapter(adapter);
                            } else {
                                list = new ListView(CataloguePlugin.this);
                                list.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                                list.setDivider(null);
                                list.setCacheColorHint(Color.TRANSPARENT);
                                list.setSelector(R.drawable.adapter_selector);
                                root.addView(list);
                                adapter = new CategoryProductListAdapter(CataloguePlugin.this, categoryProductList, list);
                                list.setAdapter(adapter);
                            }
                        }
                    });
            }
        });
        loader.start();
    }

    /**
     * Set some values
     */
    private void initializeBackend() {
        density = getResources().getDisplayMetrics().density;

        widgetXml = null;
        Widget widget;
        Intent currentIntent = getIntent();
        Bundle store = currentIntent.getExtras();
        if (store != null) {
            widget = (Widget) store.getSerializable("Widget");
            if (widget == null) {
                Toast.makeText(this, getResources().getString(R.string.cannot_init_plugin), Toast.LENGTH_LONG).show();
                return;
            }

            try {
                if (widget.getPluginXmlData().length() == 0) {
                    if (widget.getPathToXmlFile().length() == 0) {
                        Toast.makeText(this, getResources().getString(R.string.cannot_init_plugin), Toast.LENGTH_LONG).show();
                        return;
                    }
                }
            } catch (Exception e) {
                Toast.makeText(this, getResources().getString(R.string.cannot_init_plugin), Toast.LENGTH_LONG).show();
                return;
            }

            pageTitle = widget.getTitle();

            // load from data file
            if (widget.getPluginXmlData().length() > 0) {
                widgetXml = widget.getPluginXmlData();
            } else {
                widgetXml = readXmlFromFile(widget.getPathToXmlFile());
            }


            Statics.hasAd = widget.isHaveAdvertisement();
            Statics.appId = com.appbuilder.sdk.android.Statics.appId;
            Statics.appName = widget.getAppName();
            Statics.widgetName = widget.getTitle();
            Statics.widgetOrder = widget.getOrder();
            Statics.widgetId = widget.getWidgetId();
            Statics.moduleCachePath = getExternalCacheDir().getAbsolutePath() + Statics.CACHE_FILE_PATH
                    + File.separator + Statics.appId + File.separator + Integer.toString(Statics.widgetId)
                    + File.separator;
            new File(Statics.moduleCachePath).mkdirs();

            SqlAdapter.init(CataloguePlugin.this, Statics.appId, Statics.widgetOrder);
        } else {
            Toast.makeText(this, getResources().getString(R.string.cannot_init_plugin), Toast.LENGTH_LONG).show();
            finish();
        }
    }

    /**
     * Initializing user interface
     */
    private void initializeUI() {
        setContentView(R.layout.catalogue_main);
        hideTopBar();

        root = (LinearLayout) findViewById(R.id.root);
        title = (TextView) findViewById(R.id.title_text);
        if (!TextUtils.isEmpty(pageTitle))
            title.setText(pageTitle);
        else
            title.setText(getString(R.string.catalogue));

        searchViewBtn = (ImageView) findViewById(R.id.search_view_btn);
        searchViewBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (searchHolder.getVisibility() != View.VISIBLE) {
                    titleHolder.setVisibility(View.GONE);
                    searchHolder.setVisibility(View.VISIBLE);
                    showKeyboard();
                    search.myRequestFocus();
                }
            }
        });

        backBtn = (LinearLayout) findViewById(R.id.back_btn);
        TextView textView = (TextView) findViewById(R.id.back_btn_text);
        textView.setText(R.string.home);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideKeyboard();
                finish();
            }
        });

        cancel = (TextView) findViewById(R.id.cancel_btn);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (searchHolder.getVisibility() == View.VISIBLE) {
                    titleHolder.setVisibility(View.VISIBLE);
                    searchHolder.setVisibility(View.GONE);
                    search.clearText();
                    hideKeyboard();
                }
            }
        });

        searchHolder = (LinearLayout) findViewById(R.id.search_holder);
        titleHolder = (RelativeLayout) findViewById(R.id.title_holder);
        search = (SearchView) findViewById(R.id.search_view);

        search.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int position, KeyEvent keyEvent) {
                if (keyEvent != null && keyEvent.getAction() == KeyEvent.ACTION_UP)
                    return false;

                if (!TextUtils.isEmpty(textView.getText().toString())) {
                    List<ProductEntity> searchList = SqlAdapter.selectProductsLike(textView.getText().toString());
                    List<CategoryProduct> resList = new ArrayList<>();
                    if (searchList.size() > 0) {
                        for (int i = 0; i < searchList.size(); i++) {
                            ProductEntity prod = searchList.get(i);
                            if (i == 0) {
                                prod.marginTop = true;
                                if (searchList.size() == 1)
                                    prod.marginBottom = true;
                            }

                            if (i == searchList.size() - 1)
                                prod.marginBottom = true;

                            resList.add(new CategoryProduct(null, prod));
                        }
                    }

                    Intent bridge = new Intent(CataloguePlugin.this, CatalogueCategoryProductActivity.class);
                    bridge.putExtra("products", (Serializable) resList);
                    startActivity(bridge);
                    search.clearText();

                    titleHolder.setVisibility(View.VISIBLE);
                    searchHolder.setVisibility(View.GONE);
                    hideKeyboard();
                }
                Log.e("", "");
                return true;
            }
        });
    }

    /**
     * Force hide keyboard
     */
    private void hideKeyboard() {
        ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE))
                .hideSoftInputFromWindow(search.getWindowToken(), 0);
    }

    /**
     * Force show keyboard
     */
    private void showKeyboard() {
        ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE)).
                toggleSoftInputFromWindow(search.getApplicationWindowToken(), InputMethodManager.SHOW_FORCED, 0);
    }

    /**
     * The same as onResume()
     */
    @Override
    public void resume() {
        List<ShoppingCart.Product> products = ShoppingCart.getProducts();
        int count = 0;

        for (ShoppingCart.Product product : products)
            count += product.getQuantity();

        TextView cart_items = (TextView) findViewById(R.id.cart_items);
        cart_items.setText(String.valueOf(count));
        cart_items.setVisibility(count > 0 && Statics.isBasket && !showSideBar ? View.VISIBLE : View.GONE);
        if (showSideBar) {
            StringBuilder resString = new StringBuilder( getResources().getString(R.string.shopping_cart));
            if (count > 0)
                resString.append(" (").append(String.valueOf(count)).append(")");
            //updateWidgetInActualList(shopingCartIndex, resString.toString() );
        }
    }

    /**
     * The same as onDestroy()
     */
    @Override
    public void destroy() {
        if (adapter != null)
            adapter.clearBitmaps();

        destroyed = true;
        loader.interrupt();
    }
}
