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
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.appbuilder.sdk.android.AppBuilderModuleMain;
import com.ibuildapp.romanblack.CataloguePlugin.adapter.BaseImageAdapter;
import com.ibuildapp.romanblack.CataloguePlugin.adapter.CategoryProductGridAdapter;
import com.ibuildapp.romanblack.CataloguePlugin.adapter.CategoryProductListAdapter;
import com.ibuildapp.romanblack.CataloguePlugin.database.SqlAdapter;
import com.ibuildapp.romanblack.CataloguePlugin.model.CategoryEntity;
import com.ibuildapp.romanblack.CataloguePlugin.model.CategoryProduct;
import com.ibuildapp.romanblack.CataloguePlugin.model.OnShoppingCartItemAddedListener;
import com.ibuildapp.romanblack.CataloguePlugin.model.ProductEntity;
import com.ibuildapp.romanblack.CataloguePlugin.model.ShoppingCart;
import com.ibuildapp.romanblack.CataloguePlugin.view.SearchView;
import com.jess.ui.TwoWayGridView;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class CatalogueCategoryProductActivity extends AppBuilderModuleMain implements OnShoppingCartItemAddedListener {

    private LinearLayout root;
    private ListView list;
    private TwoWayGridView grid;
    private BaseAdapter adapter;
    private List<CategoryProduct> categoryProductList = new ArrayList<>();
    private float density;
    private TextView title;
    private ImageView searchViewBtn;
    private LinearLayout backBtn;
    private TextView cancel;
    private LinearLayout searchHolder;
    private RelativeLayout titleHolder;
    private SearchView search;
    private boolean searchActivity = false;
    private int screenWidth;
    private LinearLayout content;
    private LinearLayout noResultHolder;
    private int shopingCartIndex;
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
        categoryProductList = (List<CategoryProduct>) getIntent().getSerializableExtra("products");

        if (categoryProductList == null) {
            categoryProductList = new ArrayList<>();

            // select categories for rendering
            int parentCategory = getIntent().getIntExtra("categoryId", -1);
            CategoryEntity categoryEntity = SqlAdapter.selectCategoryById(parentCategory);
            if (categoryEntity != null)
                title.setText(categoryEntity.name);

            // prepare categories
            List<CategoryEntity> categoryEntityList = SqlAdapter.selectCategoryWithParendId(parentCategory);
            if (Statics.uiConfig.mainpagestyle.compareToIgnoreCase("grid") == 0) {
                if (categoryEntityList.size() % 2 != 0)
                    categoryEntityList.add(new CategoryEntity());
            }
            for (CategoryEntity cat : categoryEntityList)
                categoryProductList.add(new CategoryProduct(cat, null));

            // prepare products
            List<ProductEntity> productEntityList = SqlAdapter.selectProductsForCategory(parentCategory);
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
        } else {
            searchActivity = true;
            title.setText(getString(R.string.search_result));
            if (categoryProductList.size() == 0) {
                noResultHolder.setVisibility(View.VISIBLE);
                content.setVisibility(View.GONE);
            }
        }

        // draw
        if (Statics.uiConfig.mainpagestyle.compareToIgnoreCase(Statics.GRID_STYLE) == 0) {
            adapter = new CategoryProductGridAdapter(CatalogueCategoryProductActivity.this, categoryProductList, grid);
            grid.setAdapter(adapter);
        } else {
            adapter = new CategoryProductListAdapter(CatalogueCategoryProductActivity.this, categoryProductList, list);
            list.setAdapter(adapter);
        }
    }
    public void updateIndexSideBar(int count){
        if (showSideBar && Statics.isBasket) {
            StringBuilder resString = new StringBuilder( getResources().getString(R.string.shopping_cart));
            if (count > 0)
                resString.append(" (").append(String.valueOf(count)).append(")");
            updateWidgetInActualList(shopingCartIndex, resString.toString());
        }
    }
    /**
     * Initializing user interface
     */
    private void initializeUI() {
        setContentView(R.layout.category_product_layot);
        hideTopBar();

        root = (LinearLayout) findViewById(R.id.root);
        root.setBackgroundColor(Statics.uiConfig.colorSkin.color1);

        noResultHolder = (LinearLayout) findViewById(R.id.no_result);
        content = (LinearLayout) findViewById(R.id.content);

        TextView noResultText = (TextView) findViewById(R.id.no_result_text);
        noResultText.setTextColor(Statics.uiConfig.colorSkin.color3);

        title = (TextView) findViewById(R.id.title_text);

        title.setMaxWidth((int) (screenWidth * 0.55));
        title.setText(getString(R.string.catalogue));

        FrameLayout navbarHolder = (FrameLayout) findViewById(R.id.navbar_holder);
        if (Statics.uiConfig.colorSkin.color1 == Color.WHITE)
            navbarHolder.setBackgroundColor(ContextCompat.getColor(this, R.color.black_trans_20));
        else
            navbarHolder.setBackgroundColor(ContextCompat.getColor(this, R.color.white_trans_50));

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
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        View basketBtn = findViewById(R.id.basket_view_btn);
        basketBtn.setVisibility(Statics.isBasket ? View.VISIBLE : View.GONE);
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CatalogueCategoryProductActivity.this, ShoppingCartPage.class);
                startActivity(intent);
            }
        };
        basketBtn.setOnClickListener(listener);

        // final boolean showSideBar = ((Boolean) getIntent().getExtras().getSerializable("showSideBar")).booleanValue();
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
        }
        else {
            hamburgerView.setVisibility(View.VISIBLE);
            basketBtn.setVisibility(View.GONE);
            if(Statics.isBasket)
                shopingCartIndex = setTopBarRightButton(basketBtn, getResources().getString(R.string.shopping_cart), listener);
        }

        if (!Statics.isBasket) {
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) searchViewBtn.getLayoutParams();
            if (!showSideBar)
                params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            findViewById(R.id.cart_items).setVisibility(View.GONE);
        }
        onShoppingCartItemAdded();

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


        if (Statics.uiConfig.mainpagestyle.compareToIgnoreCase(Statics.GRID_STYLE) == 0) {
            grid = new TwoWayGridView(CatalogueCategoryProductActivity.this);
            grid.setVerticalSpacing((int) (density * Statics.GRID_SPASING));
            grid.setHorizontalSpacing((int) (density * Statics.GRID_SPASING));
            grid.setScrollDirectionPortrait(0);
            grid.setScrollDirectionLandscape(0);
            grid.setNumColumns(Statics.GRID_NUM_COLUMNS);
            grid.setVerticalScrollBarEnabled(false);
            grid.setCacheColorHint(Color.TRANSPARENT);
            grid.setSelector(R.drawable.adapter_selector);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            params.setMargins(
                    (int) (density * Statics.GRID_MARGINS),
                    (int) (density * Statics.GRID_SPASING),
                    (int) (density * Statics.GRID_MARGINS),
                    (int) (density * Statics.GRID_SPASING));
            content.addView(grid, params);

        } else {
            list = new ListView(CatalogueCategoryProductActivity.this);
            list.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            list.setDivider(null);
            list.setCacheColorHint(Color.TRANSPARENT);
            list.setSelector(R.drawable.adapter_selector);
            content.addView(list);
        }

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

                            if (i == searchList.size() - 1) {
                                prod.marginBottom = true;
                            }
                            resList.add(new CategoryProduct(null, prod));
                        }
                    }

                    if (!searchActivity) {
                        Intent bridge = new Intent(CatalogueCategoryProductActivity.this, CatalogueCategoryProductActivity.class);
                        bridge.putExtra("products", (Serializable) resList);
                        startActivity(bridge);
                    } else {
                        if (resList.size() == 0) {
                            noResultHolder.setVisibility(View.VISIBLE);
                            content.setVisibility(View.GONE);
                        } else {
                            noResultHolder.setVisibility(View.GONE);
                            content.setVisibility(View.VISIBLE);

                            categoryProductList.clear();
                            categoryProductList.addAll(resList);
                            adapter.notifyDataSetChanged();
                        }
                    }

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
     * Set some values
     */
    private void initializeBackend() {
        density = getResources().getDisplayMetrics().density;

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        screenWidth = metrics.widthPixels;
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
        onShoppingCartItemAdded();
    }

    /**
     * The same as onDestroy()
     */
    @Override
    public void destroy() {
        if (adapter != null)
            ((BaseImageAdapter) adapter).clearBitmaps();
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
        updateIndexSideBar(count);
    }
}
