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
package com.ibuildapp.romanblack.CataloguePlugin.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ibuildapp.romanblack.CataloguePlugin.CatalogueCategoryProductActivity;
import com.ibuildapp.romanblack.CataloguePlugin.ProductDetails;
import com.ibuildapp.romanblack.CataloguePlugin.R;
import com.ibuildapp.romanblack.CataloguePlugin.ShoppingCartPage;
import com.ibuildapp.romanblack.CataloguePlugin.Statics;
import com.ibuildapp.romanblack.CataloguePlugin.model.CategoryProduct;
import com.ibuildapp.romanblack.CataloguePlugin.model.OnShoppingCartItemAddedListener;
import com.ibuildapp.romanblack.CataloguePlugin.model.ShoppingCart;
import com.ibuildapp.romanblack.CataloguePlugin.utils.Utils;
import com.ibuildapp.romanblack.CataloguePlugin.view.RoundedListView;
import com.seppius.i18n.plurals.PluralResources;

import org.jsoup.Jsoup;

import java.io.File;
import java.util.List;
import java.util.Locale;

public class CategoryProductListAdapter extends BaseImageAdapter {

    private final int CATEGORY_HEIGHT = 100;
    private final int ROUND_K = 7;
    private final float density;
    private final int TYPE_CATEGORY = 0;
    private final int TYPE_PRODUCT = 1;
    private final int TYPE_MAX_COUNT = 2;
    boolean needlessPlaceHolder = true;
    private Bitmap placeHolder;
    private List<CategoryProduct> source;
    private LayoutInflater inflater;
    private Context context;

    public CategoryProductListAdapter(Context incontext, final List<CategoryProduct> source_ar, final AbsListView ui) {
        super(incontext, ui);
        this.context = incontext;
        this.inflater = LayoutInflater.from(context);
        this.source = source_ar;
        density = context.getResources().getDisplayMetrics().density;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 4;
        options.inDither = false;                     //Disable Dithering mode
        options.inPurgeable = true;
        placeHolder = BitmapFactory.decodeResource(context.getResources(), R.drawable.placeholder_black, options);

        setOnLoadedListener(new onLoadedListener() {
            @Override
            public void onImageLoaded(final int uid, String downloadedImagePath) {
                ((Activity) context).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        View v = uiView.findViewWithTag(uid);
                        if (v != null) {
                            LinearLayout gradienHolder = (LinearLayout) ((ViewGroup) v.getParent()).findViewById(R.id.category_gridadapter_gradient);
                            if (gradienHolder != null)
                                gradienHolder.setVisibility(View.VISIBLE);

                            TextView text = (TextView) ((ViewGroup) v.getParent()).findViewById(R.id.category_gridadapter_name);
                            if (text != null)
                                text.setTextColor(Color.WHITE);

                            View separator = ((ViewGroup) v.getParent()).findViewById(R.id.separator);
                            if (separator != null)
                                separator.setVisibility(View.GONE);
                        }
                    }
                });
            }
        });

        for (CategoryProduct categoryProduct : source_ar)
            needlessPlaceHolder = needlessPlaceHolder &&
                    categoryProduct.product != null &&
                    TextUtils.isEmpty(categoryProduct.product.imagePath) &&
                    TextUtils.isEmpty(categoryProduct.product.imageRes) &&
                    TextUtils.isEmpty(categoryProduct.product.imageURL);

    }

    @Override
    public int getCount() {
        return source.size();
    }

    @Override
    public Object getItem(int i) {
        return source.get(i);
    }

    @Override
    public long getItemId(int i) {
        return source.get(i).hashCode();
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        final CategoryProduct categoryProduct = (CategoryProduct) getItem(i);
        int type = getItemViewType(i);

//        if (view == null) {
        switch (type) {
            case TYPE_CATEGORY: {
                view = inflater.inflate(R.layout.category_item_list, null);
                // set view holder params
                AbsListView.LayoutParams holderParams = (AbsListView.LayoutParams) view.getLayoutParams();
                if (holderParams != null) {
                    holderParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
                    holderParams.height = (int) (CATEGORY_HEIGHT * density);
                } else {
                    holderParams = new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) (CATEGORY_HEIGHT * density));
                }
                view.setLayoutParams(holderParams);
            }
            break;

            case TYPE_PRODUCT:
                view = inflater.inflate(R.layout.product_item_list, null);
                break;
        }
//        }

        addClickListener(view, categoryProduct);

        switch (type) {
            case TYPE_CATEGORY: {
                TextView categoryName = ViewHolder.get(view, R.id.category_gridadapter_name);
                categoryName.setText(categoryProduct.category.name);

                if (Statics.uiConfig.colorSkin.color1 == Color.WHITE)
                    ViewHolder.get(view, R.id.category_gridadapter_root).setBackgroundColor(context.getResources().getColor(R.color.black_trans_20));
                else
                    ViewHolder.get(view, R.id.category_gridadapter_root).setBackgroundColor(Statics.uiConfig.colorSkin.color1);///Color.parseColor("#7fffffff"));

                ViewHolder.get(view, R.id.separator).setVisibility(View.VISIBLE);
                ImageView img = ViewHolder.get(view, R.id.category_gridadapter_image);

                if (TextUtils.isEmpty(categoryProduct.category.imageURL)) {
                    ViewHolder.get(view, R.id.category_gridadapter_image).setVisibility(View.GONE);
                    if (i == 0)
                        ViewHolder.get(view, R.id.header_separator).setVisibility(View.VISIBLE);
                } else {
                    ViewHolder.get(view, R.id.category_gridadapter_image).setVisibility(View.VISIBLE);
                    img.setTag(categoryProduct.category.id);
                    Bitmap btm = imageMap.get(categoryProduct.category.id);
                    if (btm == null || btm.getHeight() == 1) {
                        addTask(img,
                                categoryProduct.category.id,
                                categoryProduct.category.name,
                                categoryProduct.category.imageRes,
                                Statics.moduleCachePath + File.separator + com.appbuilder.sdk.android.Utils.md5(categoryProduct.category.imageURL),
                                categoryProduct.category.imageURL, -1, -1, OnImageDoneListener.REACTION_DEFAULT);

                        img.setImageResource(android.R.color.transparent);
                        categoryName.setTextColor(Statics.uiConfig.colorSkin.color1);

                    } else {
                        ViewHolder.get(view, R.id.category_gridadapter_gradient).setVisibility(View.VISIBLE);
                        categoryName.setTextColor(Color.WHITE);
                        img.setImageBitmap(btm);
                    }
                }
            }
            break;

            case TYPE_PRODUCT: {
                if (categoryProduct.product.marginBottom || categoryProduct.product.marginTop) {
                    FrameLayout rootFrame = ViewHolder.get(view, R.id.frame_root);
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) ((CATEGORY_HEIGHT + 25) * density));
                    params.setMargins(
                            (int) (density * 6),
                            (categoryProduct.product.marginTop) ? (int) (density * 6) : (int) (density * 3),
                            (int) (density * 6),
                            (categoryProduct.product.marginBottom) ? (int) (density * 6) : (int) (density * 3));
                    rootFrame.setLayoutParams(params);
                }

                TextView productName = ViewHolder.get(view, R.id.product_gridadapter_name);
                productName.setText(categoryProduct.product.name);
                TextView productDesc = ViewHolder.get(view, R.id.product_gridadapter_description);
                productDesc.setText(Jsoup.parse(categoryProduct.product.description).text());
                TextView productPrice = ViewHolder.get(view, R.id.product_gridadapter_price);

                if ("0.00".equals(String.format(Locale.US, "%.2f", categoryProduct.product.price)))
                    productPrice.setText("");
                else
                    productPrice.setText(Utils.currencyToPosition(Statics.uiConfig.currency, categoryProduct.product.price));

                ImageView productImage = (ImageView) view.findViewById(R.id.product_gridadapter_image);

                if (needlessPlaceHolder) {
                    productImage.setVisibility(View.GONE);
                } else {
                    ((RoundedListView) productImage).setCorners((int) (ROUND_K * density), 0, (int) (ROUND_K * density), 0);

                    productImage.setTag(categoryProduct.product.id);
                    Bitmap btm = imageMap.get(categoryProduct.product.id);
                    if (btm == null || btm.getHeight() == 1) {
                        addTask(productImage,
                                categoryProduct.product.id,
                                categoryProduct.product.name,
                                categoryProduct.product.thumbnailRes,
                                Statics.moduleCachePath + File.separator + com.appbuilder.sdk.android.Utils.md5(categoryProduct.product.thumbnailURL),
                                categoryProduct.product.thumbnailURL,
                                -1, -1, OnImageDoneListener.REACTION_DEFAULT);
                        productImage.setImageBitmap(placeHolder);
                    } else {
                        productImage.setImageBitmap(btm);
                    }
                }

                View basketBtn = ViewHolder.get(view, R.id.basket_view_btn);
                basketBtn.setVisibility(Statics.isBasket ? View.VISIBLE : View.GONE);
                basketBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String message = "";
                        List<ShoppingCart.Product> products = ShoppingCart.getProducts();
                        int count = 0;

                        for (ShoppingCart.Product product : products)
                            count += product.getQuantity();

                        try {
                            message = new PluralResources(context.getResources()).getQuantityString(R.plurals.items_to_cart, count + 1, count + 1);
                        } catch (NoSuchMethodException e) {
                            e.printStackTrace();
                        }

                        int index = products.indexOf(new ShoppingCart.Product.Builder()
                                .setId(categoryProduct.product.id)
                                .build());
                        ShoppingCart.insertProduct(new ShoppingCart.Product.Builder()
                                .setId(categoryProduct.product.id)
                                .setQuantity((index == -1 ? 0 : products.get(index).getQuantity()) + 1)
                                .build());

                        if (context instanceof OnShoppingCartItemAddedListener)
                            ((OnShoppingCartItemAddedListener) context).onShoppingCartItemAdded();

                        Utils.showDialog(context,
                                R.string.shopping_cart_dialog_title,
                                message,
                                R.string.shopping_cart_dialog_continue,
                                R.string.shopping_cart_dialog_view_cart,
                                new Utils.OnDialogButtonClickListener() {
                                    @Override
                                    public void onPositiveClick(DialogInterface dialog) {
                                        dialog.dismiss();
                                    }

                                    @Override
                                    public void onNegativeClick(DialogInterface dialog) {
                                        Intent intent = new Intent(context, ShoppingCartPage.class);
                                        context.startActivity(intent);
                                    }
                                });
                    }
                });
            }
            break;
        }

        return view;
    }

    /**
     * Adds click listener
     *
     * @param view            view that will be fire added click
     * @param categoryProduct category product for pass to activity
     */
    private void addClickListener(View view, final CategoryProduct categoryProduct) {
        if (view != null)
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (categoryProduct.product != null) {
                        Intent bridge = new Intent(context, ProductDetails.class);
                        bridge.putExtra("productId", categoryProduct.product.id);
                        context.startActivity(bridge);
                    } else {
                        Intent bridge = new Intent(context, CatalogueCategoryProductActivity.class);
                        bridge.putExtra("categoryId", categoryProduct.category.id);
                        context.startActivity(bridge);
                    }
                }
            });
    }

    @Override
    public int getItemViewType(int position) {
        return (source.get(position).category != null ? TYPE_CATEGORY : TYPE_PRODUCT);
    }

    @Override
    public int getViewTypeCount() {
        return TYPE_MAX_COUNT;
    }

    @Override
    public void clearBitmaps() {
        super.clearBitmaps();

        placeHolder.recycle();
        placeHolder = null;

        System.gc();
    }
}
