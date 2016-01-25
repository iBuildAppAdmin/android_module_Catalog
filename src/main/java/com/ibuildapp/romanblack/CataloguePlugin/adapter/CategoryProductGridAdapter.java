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
import android.graphics.Paint;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ibuildapp.romanblack.CataloguePlugin.CatalogueCategoryProductActivity;
import com.ibuildapp.romanblack.CataloguePlugin.ProductDetails;
import com.ibuildapp.romanblack.CataloguePlugin.R;
import com.ibuildapp.romanblack.CataloguePlugin.ShoppingCartPage;
import com.ibuildapp.romanblack.CataloguePlugin.Statics;
import com.ibuildapp.romanblack.CataloguePlugin.database.SqlAdapter;
import com.ibuildapp.romanblack.CataloguePlugin.model.CategoryProduct;
import com.ibuildapp.romanblack.CataloguePlugin.model.OnShoppingCartItemAddedListener;
import com.ibuildapp.romanblack.CataloguePlugin.model.ProductEntity;
import com.ibuildapp.romanblack.CataloguePlugin.model.ProductItemType;
import com.ibuildapp.romanblack.CataloguePlugin.model.ShoppingCart;
import com.ibuildapp.romanblack.CataloguePlugin.utils.Utils;
import com.ibuildapp.romanblack.CataloguePlugin.view.RoundView;
import com.jess.ui.TwoWayAbsListView;
import com.seppius.i18n.plurals.PluralResources;

import org.jsoup.Jsoup;

import java.io.File;
import java.util.List;
import java.util.Locale;

public class CategoryProductGridAdapter extends BaseImageAdapter {

    private final int ROUND_K = 7;
    private final int TYPE_CATEGORY = 0;
    private final int TYPE_PRODUCT = 1;
    private final int TYPE_MAX_COUNT = 2;
    private final int DESCRIPTION_SIZE = 50;
    private final float CATEGORY_RATIO = 1.2f;
    private final float ITEM_RATIO = 0.80f;
    private final float density;
    private final Context context;
    private List<CategoryProduct> source;
    private LayoutInflater inflater;
    private Bitmap placeHolder;
    private int screenWidth;
    private int categoryWidth;
    private int categoryHeight;
    private int itemWidth;
    private int itemHeight;
    private boolean needlessPlaceHolder = true;

    public CategoryProductGridAdapter(final Context context, List<CategoryProduct> source, ViewGroup ui) {
        super(context, ui);
        this.inflater = LayoutInflater.from(context);
        this.source = source;
        this.context = context;

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 4;
        options.inDither = false;                     //Disable Dithering mode
        options.inPurgeable = true;
        placeHolder = BitmapFactory.decodeResource(context.getResources(), R.drawable.placeholder_black, options);

        density = context.getResources().getDisplayMetrics().density;
        DisplayMetrics metrics = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(metrics);
        screenWidth = metrics.widthPixels;

        categoryWidth = (screenWidth - (2 * (int) (density * Statics.GRID_MARGINS)) - (int) (density * Statics.GRID_SPASING)) / 2;
        categoryHeight = (int) (categoryWidth / CATEGORY_RATIO);

        itemWidth = categoryWidth;
        itemHeight = 250;//(int) (itemWidth / ITEM_RATIO) + (int) (DESCRIPTION_SIZE * density);

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
                        }
                    }
                });
            }
        });

        for (CategoryProduct categoryProduct : source) {
            needlessPlaceHolder = needlessPlaceHolder &&
                    categoryProduct.product != null &&
                    TextUtils.isEmpty(categoryProduct.product.imagePath) &&
                    TextUtils.isEmpty(categoryProduct.product.imageRes) &&
                    TextUtils.isEmpty(categoryProduct.product.imageURL);

            if (!needlessPlaceHolder)
                break;
        }
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
                view = inflater.inflate(R.layout.category_item_grid, null);

                // set view holder params
                TwoWayAbsListView.LayoutParams holderParams = (TwoWayAbsListView.LayoutParams) view.getLayoutParams();
                if (holderParams != null) {
                    holderParams.width = TwoWayAbsListView.LayoutParams.MATCH_PARENT;
                    holderParams.height = categoryHeight;
                } else {
                    holderParams = new TwoWayAbsListView.LayoutParams(TwoWayAbsListView.LayoutParams.MATCH_PARENT, categoryHeight);
                }
                view.setLayoutParams(holderParams);
            }
            break;

            case TYPE_PRODUCT: {
               // int height1 = TwoWayAbsListView.LayoutParams.MATCH_PARENT;
               // height1 = height1 * 9;
                //height1 = height1 / 5;
                view = inflater.inflate(R.layout.product_item_grid, null);

                // set view holder params
                /*TwoWayAbsListView.LayoutParams holderParams = (TwoWayAbsListView.LayoutParams) view.getLayoutParams();
                if (holderParams != null) {
                    holderParams.width = TwoWayAbsListView.LayoutParams.MATCH_PARENT;
                    holderParams.height = height1;//(int)(270*context.getResources().getDisplayMetrics().density);
                } else {
                    holderParams = new TwoWayAbsListView.LayoutParams(TwoWayAbsListView.LayoutParams.MATCH_PARENT, height1);//(int)(270*context.getResources().getDisplayMetrics().density));
                }
                view.setLayoutParams(holderParams);*/
            }
            break;
        }
//        }

        switch (type) {
            case TYPE_CATEGORY: {
                // категория пустышка
                if (categoryProduct.category.id == 0) {
                    ViewHolder.get(view, R.id.category_gridadapter_root).setVisibility(View.INVISIBLE);
                } else {
                    ViewHolder.get(view, R.id.category_gridadapter_root).setVisibility(View.VISIBLE);

                    // цвет фона
                    //ViewHolder.get(view, R.id.category_gridadapter_root).setBackgroundColor(Statics.uiConfig.colorSkin.color5);
                    if (Statics.uiConfig.colorSkin.color1 == Color.WHITE)
                        ViewHolder.get(view, R.id.category_gridadapter_root).setBackgroundColor(context.getResources().getColor(R.color.black_trans_20));
                    else
                        ViewHolder.get(view, R.id.category_gridadapter_root).setBackgroundColor(Color.parseColor("#7fffffff"));

                    // текст
                    TextView categoryName = ViewHolder.get(view, R.id.category_gridadapter_name);
                    categoryName.setText(categoryProduct.category.name);

                    // картинка

                    if (TextUtils.isEmpty(categoryProduct.category.imageURL)) {
                        ProductEntity entity = SqlAdapter.selectFirstProductForCategory(categoryProduct.category.id);
                        if (entity != null) {
                            ImageView img = ViewHolder.get(view, R.id.category_gridadapter_image);
                            img.setTag(categoryProduct.category.id);
                            Bitmap btm = imageMap.get(categoryProduct.category.id);
                            if (btm == null || btm.getHeight() == 1) {
                                addTask(img,
                                        categoryProduct.category.id,
                                        categoryProduct.category.name,
                                        entity.imageRes,
                                        Statics.moduleCachePath + File.separator + com.appbuilder.sdk.android.Utils.md5(entity.imageURL),
                                        entity.imageURL, -1, -1, OnImageDoneListener.REACTION_DEFAULT);

                                img.setImageResource(android.R.color.transparent);
                                //img.setBackgroundColor(Color.parseColor("#7fffffff"));
                                ViewHolder.get(view, R.id.category_gridadapter_gradient).setVisibility(View.GONE);
                                categoryName.setTextColor(Statics.uiConfig.colorSkin.color1);
                            } else {
                                ViewHolder.get(view, R.id.category_gridadapter_gradient).setVisibility(View.VISIBLE);
                                categoryName.setTextColor(Color.WHITE);
                                img.setImageBitmap(btm);
                            }
                        }
                    } else {
                        ImageView img = ViewHolder.get(view, R.id.category_gridadapter_image);
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
                            //img.setBackgroundColor(Color.parseColor("#7fffffff"));
                            ViewHolder.get(view, R.id.category_gridadapter_gradient).setVisibility(View.GONE);
                            categoryName.setTextColor(Statics.uiConfig.colorSkin.color1);
                        } else {
                            ViewHolder.get(view, R.id.category_gridadapter_gradient).setVisibility(View.VISIBLE);
                            categoryName.setTextColor(Color.WHITE);
                            img.setImageBitmap(btm);
                        }
                    }
                }
            }
            break;

            case TYPE_PRODUCT: {
                TextView productName = ViewHolder.get(view, R.id.product_gridadapter_name);
                productName.setText(categoryProduct.product.name);
                TextView productDesc = ViewHolder.get(view, R.id.product_gridadapter_description);
                TextView productSku = ViewHolder.get(view, R.id.product_gridadapter_sku);
                String descriptionText = Jsoup.parse(categoryProduct.product.description).text();
                if (descriptionText == null || "".equals(descriptionText))
                   calculateInvisibleState(i,productDesc);
                else productDesc.setVisibility(View.VISIBLE);
                productDesc.setText(descriptionText);

                if (categoryProduct.product.sku == null || "".equals(categoryProduct.product.sku))
                    calculateInvisibleStateSku(i, productSku);
                else productSku.setVisibility(View.VISIBLE);
                productSku.setText(context.getString(R.string.item_sku)+" "+categoryProduct.product.sku);

                TextView productPrice = ViewHolder.get(view, R.id.product_gridadapter_price);
                LinearLayout rootFrame = ViewHolder.get(view, R.id.frame_root);
                TextView oldPrice = (TextView) view.findViewById(R.id.product_gridadapter_oldprice);

                if (categoryProduct.product.oldprice != -1 && categoryProduct.product.oldprice!= 0.0 ) {
                    oldPrice.setVisibility(View.VISIBLE);
                    String result = Utils.currencyToPosition(Statics.uiConfig.currency, categoryProduct.product.oldprice);
                        if (result.contains(context.getResources().getString(R.string.rest_number_pattern)))
                            result = result.replace(context.getResources().getString(R.string.rest_number_pattern), "");
                    oldPrice.setText(result);

                    oldPrice.setPaintFlags(oldPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                    productPrice.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);

                    if ("0.00".equals(String.format(Locale.US, "%.2f", categoryProduct.product.oldprice)))
                        oldPrice.setVisibility(View.INVISIBLE);
                    else
                        productPrice.setText(Utils.currencyToPosition(Statics.uiConfig.currency, categoryProduct.product.price));
                }
                else {
                    oldPrice.setVisibility(View.INVISIBLE);
                    productPrice.setTextSize(TypedValue.COMPLEX_UNIT_SP, 17);
                }

                if ("0.00".equals(String.format(Locale.US, "%.2f", categoryProduct.product.price)))
                    productPrice.setText("");
                else {
                        String result = Utils.currencyToPosition(Statics.uiConfig.currency, categoryProduct.product.price);
                    if (result.contains(context.getResources().getString(R.string.rest_number_pattern)))
                        result = result.replace(context.getResources().getString(R.string.rest_number_pattern), "");
                        productPrice.setText(result);
                }

                ImageView productImage = (ImageView) view.findViewById(R.id.product_gridadapter_image);
                if ((Statics.uiConfig.colorSkin.color1 == Color.parseColor("#000000")))
                    rootFrame.setBackgroundResource(R.drawable.rounded_corners_bottom_white);

                if (needlessPlaceHolder) {
                    productImage.setVisibility(View.GONE);
                    ViewHolder.get(view, R.id.separator).setVisibility(View.GONE);
                    ViewHolder.get(view, R.id.product_gridadapter_image_layout).setVisibility(View.GONE);
                    view.setLayoutParams(new TwoWayAbsListView.LayoutParams(TwoWayAbsListView.LayoutParams.MATCH_PARENT, (int) (90 * density)));
                } else {
                    ((RoundView) productImage).setCorners((int) (ROUND_K * density), (int) (ROUND_K * density), 0, 0);
                    productImage.setTag(categoryProduct.product.id);
                    Bitmap btm = imageMap.get(categoryProduct.product.id);
                    if (btm == null || btm.getHeight() == 1) {
                        addTask(productImage,
                                categoryProduct.product.id,
                                categoryProduct.product.name,
                                categoryProduct.product.imageRes,
                                Statics.moduleCachePath + File.separator + com.appbuilder.sdk.android.Utils.md5(categoryProduct.product.imageURL),
                                categoryProduct.product.imageURL,
                                -1, -1, OnImageDoneListener.REACTION_JUST_DOWNLOAD);

                        //productImage.setImageResource(android.R.color.transparent);
                        productImage.setImageBitmap(placeHolder);
                    } else prepareProductImage(productImage, btm, rootFrame);
                }

                View basketBtn = ViewHolder.get(view, R.id.basket_view_btn);
                if (Statics.isBasket){
                    if(categoryProduct.product.itemType.equals(ProductItemType.EXTERNAL))
                        basketBtn.setVisibility( View.GONE );
                    else basketBtn.setVisibility(View.VISIBLE);
                }
                else
                basketBtn.setVisibility(View.GONE);

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

        addClickListener(view, categoryProduct);

        return view;
    }

    private void calculateInvisibleStateSku(int position, TextView productSku) {
        CategoryProduct currentItem = (CategoryProduct) getItem(position);
        if (position % 2 == 0 && position ==  getCount()-1){
            productSku.setVisibility(View.GONE);
            return;
        }

        if (position%2 == 0 && position !=  getCount()-1 ){
            CategoryProduct categoryProduct = (CategoryProduct) getItem(position+1);
            if (categoryProduct.product.sku == null || "".equals(categoryProduct.product.sku)) {
                productSku.setVisibility(View.GONE);
            }
            else {
                if (!isNull(currentItem.product.description) && isNull(categoryProduct.product.description) )
                    productSku.setVisibility(View.GONE);
                else productSku.setVisibility(View.INVISIBLE);
            }
        }
        if (position%2 == 1 ){
            CategoryProduct categoryProduct = (CategoryProduct) getItem(position-1);
            if (categoryProduct.product.sku == null || "".equals(categoryProduct.product.sku))
                productSku.setVisibility(View.GONE);
            else{
                if (!isNull(currentItem.product.description) && isNull(categoryProduct.product.description) )
                    productSku.setVisibility(View.GONE);
                else productSku.setVisibility(View.INVISIBLE);
            }
        }
    }
    private boolean isNull(String s){
        return  s == null || "".equals(s);
    }
    private void calculateInvisibleState(int position, TextView textDesc) {
        CategoryProduct currentItem = (CategoryProduct) getItem(position);
        if (position % 2 == 0 && position ==  getCount()-1){
            textDesc.setVisibility(View.GONE);
            return;
        }

        if (position%2 == 0 && position !=  getCount()-1 ){
            CategoryProduct categoryProduct = (CategoryProduct) getItem(position+1);
            if (categoryProduct.product.description == null || "".equals(categoryProduct.product.description))
                textDesc.setVisibility(View.GONE);
            else {
                if (!isNull(currentItem.product.sku) && isNull(categoryProduct.product.sku) )
                    textDesc.setVisibility(View.GONE);
                else textDesc.setVisibility(View.INVISIBLE);
            }
        }
        if (position%2 == 1 ){
            CategoryProduct categoryProduct = (CategoryProduct) getItem(position-1);
            if (categoryProduct.product.description == null || "".equals(categoryProduct.product.description))
                textDesc.setVisibility(View.GONE);
            else {
                if (!isNull(currentItem.product.sku) && isNull(categoryProduct.product.sku) )
                    textDesc.setVisibility(View.GONE);
                else textDesc.setVisibility(View.INVISIBLE);
            }
        }
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

    /**
     * Prepare product image and set it to view
     *
     * @param productImage view
     * @param bitmap       bitmap
     * @param rootFrame
     */
    private void prepareProductImage(ImageView productImage, Bitmap bitmap, LinearLayout rootFrame) {
        final double convertedRatio = 3 / 2;
        final double width = bitmap.getWidth();
        final double height = bitmap.getHeight();
        final double actualRatio = width / height;
        final double error = 0.0000001;

       /* if (Math.abs(convertedRatio - actualRatio) <= error) {
            productImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
            productImage.setImageBitmap(bitmap);
            ((RoundView) productImage).setCorners(0, 0, 0, 0);
        } else {*/
        productImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
        productImage.setImageBitmap(bitmap);
        ((RoundView) productImage).setCorners((int) (ROUND_K * density), (int) (ROUND_K * density), 0, 0);
        if (rootFrame != null)
            if ((Statics.uiConfig.colorSkin.color1 == Color.parseColor("#000000")))
                rootFrame.setBackgroundResource(R.drawable.rounded_corners_bottom_white);

        /*}*/

        notifyDataSetChanged();
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
