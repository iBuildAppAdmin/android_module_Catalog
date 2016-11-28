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


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.appbuilder.sdk.android.AppBuilderModuleMainAppCompat;
import com.appbuilder.sdk.android.DialogSharing;
import com.appbuilder.sdk.android.Utils;
import com.appbuilder.sdk.android.authorization.Authorization;
import com.appbuilder.sdk.android.authorization.FacebookAuthorizationActivity;
import com.ibuildapp.PayPalAndroidUtil.Payer;
import com.ibuildapp.romanblack.CataloguePlugin.adapter.DetailsViewPagerAdapter;
import com.ibuildapp.romanblack.CataloguePlugin.adapter.RoundAdapter;
import com.ibuildapp.romanblack.CataloguePlugin.database.SqlAdapter;
import com.ibuildapp.romanblack.CataloguePlugin.model.CategoryEntity;
import com.ibuildapp.romanblack.CataloguePlugin.model.OnShoppingCartItemAddedListener;
import com.ibuildapp.romanblack.CataloguePlugin.model.ProductEntity;
import com.ibuildapp.romanblack.CataloguePlugin.model.ProductItemType;
import com.ibuildapp.romanblack.CataloguePlugin.model.ShoppingCart;
import com.ibuildapp.romanblack.CataloguePlugin.view.AlphaImageView;
import com.restfb.util.StringUtils;
import com.seppius.i18n.plurals.PluralResources;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;


public class ProductDetails extends AppBuilderModuleMainAppCompat implements OnShoppingCartItemAddedListener {
    private static final Integer TOP_BAR_HEIGHT = 50;
    private final String EMAIL_IMAGE_NAME = "image.jpg";
    private final int FACEBOOK_AUTHORIZATION_ACTIVITY = 10000;
    private final int TWITTER_AUTHORIZATION_ACTIVITY = 10001;
    private final int FACEBOOK_PUBLISH_ACTIVITY = 10002;
    private final int TWITTER_PUBLISH_ACTIVITY = 10003;
    private final int AUTHORIZATION_FB = 10004;

    private LinearLayout backBtn;
    private TextView title;
    private TextView productTitle;
    private TextView product_sku;
    private WebView productDescription;
    private TextView productPrice;
    private TextView likeCount;
    private LinearLayout shareBtn;
    private LinearLayout likeBtn;
    private ImageView likeImage;
    private EditText quantity;
    private RelativeLayout buyLayout;

    private ViewPager pager;
    private DetailsViewPagerAdapter adapter;
    private RecyclerView roundsList;
    private int oldCurrentItem = -1;
    private int newCurrentItem = -1;

    private int productId;
    private ProductEntity product;
    private CategoryEntity category;
    private AlphaImageView image;
    private AssetManager manager;
    private int screenWidth;
    private boolean likedbyMe = false;
    private boolean alreadyLoaded = false;
    private int shopingCartIndex;
    private Payer payer = new Payer();

    private boolean thanksPage = false;
    private View basket;
    private TextView apply_button;

    /**
     * The same as onCreate()
     */
    @Override
    public void create() {
        initializeBackend();
        initializeUI();

        payer.startPayPalService(this, Statics.PAYPAL_CLIENT_ID);
    }

    /**
     * The same as onDestroy()
     */
    @Override
    public void destroy() {
        payer.stopPayPalService();
    }

    /**
     * Set some values
     */
    private void initializeBackend() {
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        screenWidth = metrics.widthPixels;

        manager = getAssets();

        productId = getIntent().getIntExtra("productId", -1);
        product = SqlAdapter.selectProductById(productId);

        category = SqlAdapter.selectCategoryById(product.categoryId);
    }

    /**
     * Initializing user interface
     */
    private void initializeUI() {
        setContentView(R.layout.details_layout);
        hideTopBar();

        List<String> imageUrls = new ArrayList<>();
        imageUrls.add(product.imageURL);
        imageUrls.addAll(product.imageUrls);

        final float density = getResources().getDisplayMetrics().density;
        int topBarHeight = (int) (TOP_BAR_HEIGHT*density);
        apply_button = (TextView) findViewById(R.id.apply_button);
        basket = findViewById(R.id.basket);
        View bottomSeparator = findViewById(R.id.bottom_separator);
        quantity = (EditText) findViewById(R.id.quantity);

        roundsList = (RecyclerView) findViewById(R.id.details_recyclerview);
        if (imageUrls.size() <= 1)
            roundsList.setVisibility(View.GONE);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);

        roundsList.setLayoutManager(layoutManager);
        pager = (ViewPager) findViewById(R.id.viewpager);

        if (!"".equals(imageUrls.get(0))) {

            final RoundAdapter rAdapter = new RoundAdapter(this, imageUrls);
            roundsList.setAdapter(rAdapter);


            float width = getResources().getDisplayMetrics().widthPixels;
            float height = getResources().getDisplayMetrics().heightPixels;
            height -= 2 * topBarHeight;
            height -= com.ibuildapp.romanblack.CataloguePlugin.utils.Utils.getStatusBarHeight(this);
            pager.getLayoutParams().width = (int) width;
            pager.getLayoutParams().height = (int) height;
            newCurrentItem = 0;
            pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                }

                @Override
                public void onPageSelected(int position) {
                    oldCurrentItem = newCurrentItem;
                    newCurrentItem = position;

                    rAdapter.setCurrentItem(newCurrentItem);
                    rAdapter.notifyItemChanged(newCurrentItem);

                    if (oldCurrentItem != -1)
                        rAdapter.notifyItemChanged(oldCurrentItem);
                }

                @Override
                public void onPageScrollStateChanged(int state) {

                }
            });
            pager.setPageTransformer(true, new InnerPageTransformer());
            adapter = new DetailsViewPagerAdapter(getSupportFragmentManager(), imageUrls);
            roundsList.addItemDecoration(new RecyclerView.ItemDecoration() {
                @Override
                public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                    int totalWidth = (int) (adapter.getCount() * 21 * density);
                    int screenWidth = getResources().getDisplayMetrics().widthPixels;
                    int position = parent.getChildAdapterPosition(view);

                    if ((totalWidth < screenWidth)
                            && position == 0)
                        outRect.left = (screenWidth - totalWidth) / 2;
                }
            });

            pager.setAdapter(adapter);
        }
        else{
            roundsList.setVisibility(View.GONE);
            pager.setVisibility(View.GONE);
        }
        buyLayout = (RelativeLayout) findViewById(R.id.details_buy_layout);

        if (product.itemType.equals(ProductItemType.EXTERNAL))
            quantity.setVisibility(View.GONE);
        else quantity.setVisibility(View.VISIBLE);

        if (Statics.isBasket) {
            buyLayout.setVisibility(View.VISIBLE);
            onShoppingCartItemAdded();
                apply_button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        hideKeyboard();
                        quantity.setText(StringUtils.isBlank(quantity.getText().toString())?"1":
                                quantity.getText().toString());
                        quantity.clearFocus();

                        String message = "";
                        int quant = Integer.valueOf(quantity.getText().toString());
                        List<ShoppingCart.Product> products = ShoppingCart.getProducts();
                        int count = 0;

                        for (ShoppingCart.Product product : products)
                            count += product.getQuantity();

                        try {
                            message = new PluralResources(getResources()).getQuantityString(R.plurals.items_to_cart, count + quant, count + quant);
                        } catch (NoSuchMethodException e) {
                            e.printStackTrace();
                        }

                        int index = products.indexOf(new ShoppingCart.Product.Builder()
                                .setId(product.id)
                                .build());
                        ShoppingCart.insertProduct(new ShoppingCart.Product.Builder()
                                .setId(product.id)
                                .setQuantity((index == -1 ? 0 : products.get(index).getQuantity()) + quant)
                                .build());
                        onShoppingCartItemAdded();
                        com.ibuildapp.romanblack.CataloguePlugin.utils.Utils.showDialog(ProductDetails.this,
                                R.string.shopping_cart_dialog_title,
                                message,
                                R.string.shopping_cart_dialog_continue,
                                R.string.shopping_cart_dialog_view_cart,
                                new com.ibuildapp.romanblack.CataloguePlugin.utils.Utils.OnDialogButtonClickListener() {
                                    @Override
                                    public void onPositiveClick(DialogInterface dialog) {
                                        dialog.dismiss();
                                    }

                                    @Override
                                    public void onNegativeClick(DialogInterface dialog) {
                                        Intent intent = new Intent(ProductDetails.this, ShoppingCartPage.class);
                                        ProductDetails.this.startActivity(intent);
                                    }
                                });
                    }
                });
            if (product.itemType.equals(ProductItemType.EXTERNAL)) {
                basket.setVisibility(View.GONE);
                apply_button.setText(product.itemButtonText);
                apply_button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(ProductDetails.this, ExternalProductDetailsActivity.class);
                        intent.putExtra("itemUrl", product.itemUrl);
                        startActivity(intent);
                    }
                });
            }else {
                basket.setVisibility(View.VISIBLE);
                apply_button.setText(R.string.shopping_cart_add_to_cart);
            }
        } else {
            if (product.itemType.equals(ProductItemType.EXTERNAL))
                buyLayout.setVisibility(View.VISIBLE);
                else buyLayout.setVisibility(View.GONE);

            apply_button.setText(R.string.buy_now);
            basket.setVisibility(View.GONE);
            findViewById(R.id.cart_items).setVisibility(View.GONE);

            if (TextUtils.isEmpty(Statics.PAYPAL_CLIENT_ID) || product.price == 0) {
                bottomSeparator.setVisibility(View.GONE);
                apply_button.setVisibility(View.GONE);
                basket.setVisibility(View.GONE);

            } else {
                apply_button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        payer.singlePayment(
                                new Payer.Item.Builder()
                                        .setPrice(product.price)
                                        .setCurrencyCode(Payer.CurrencyCode.valueOf(Statics.uiConfig.currency))
                                        .setName(product.name)
                                        .setEndpoint(Statics.ENDPOINT)
                                        .setAppId(Statics.appId)
                                        .setWidgetId(Statics.widgetId)
                                        .setItemId(product.item_id)
                                        .build()
                        );
                    }
                });
            }

            if (product.itemType.equals(ProductItemType.EXTERNAL)) {
                basket.setVisibility(View.GONE);
                apply_button.setText(product.itemButtonText);
                apply_button.setVisibility(View.VISIBLE);
                apply_button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(ProductDetails.this, ExternalProductDetailsActivity.class);
                        intent.putExtra("itemUrl", product.itemUrl);
                        startActivity(intent);
                    }
                });
            }
        }

        backBtn = (LinearLayout) findViewById(R.id.back_btn);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        title = (TextView) findViewById(R.id.title_text);
        title.setMaxWidth((int) (screenWidth * 0.55));
        if (category != null && !TextUtils.isEmpty(category.name))
            title.setText(category.name);

        View basketBtn = findViewById(R.id.basket_view_btn);
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ProductDetails.this, ShoppingCartPage.class);
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
        }
        else {
            hamburgerView.setVisibility(View.VISIBLE);
            findViewById(R.id.cart_items).setVisibility(View.INVISIBLE);
            basketBtn.setVisibility(View.GONE);
            if(Statics.isBasket)
                shopingCartIndex = setTopBarRightButton(basketBtn, getResources().getString(R.string.shopping_cart), listener);
        }

        productTitle = (TextView) findViewById(R.id.product_title);
        productTitle.setText(product.name);

        product_sku = (TextView) findViewById(R.id.product_sku);

        if(TextUtils.isEmpty(product.sku)) {
            product_sku.setVisibility(View.GONE);
            product_sku.setText("");
        } else {
            product_sku.setVisibility(View.VISIBLE);
            product_sku.setText(getString(R.string.item_sku) + " " + product.sku);
        }

        productDescription = (WebView) findViewById(R.id.product_description);
        productDescription.getSettings().setJavaScriptEnabled(true);
        productDescription.getSettings().setDomStorageEnabled(true);
        productDescription.setWebChromeClient(new WebChromeClient());
        productDescription.setWebViewClient(new WebViewClient() {
            @Override
            public void onLoadResource(WebView view, String url) {
                if (!alreadyLoaded && (url.startsWith("http://www.youtube.com/get_video_info?") || url.startsWith("https://www.youtube.com/get_video_info?")) && Build.VERSION.SDK_INT <= 11) {
                    try {
                        String path = url.contains("https://www.youtube.com/get_video_info?") ?
                                url.replace("https://www.youtube.com/get_video_info?", "") :
                                url.replace("http://www.youtube.com/get_video_info?", "");

                        String[] parqamValuePairs = path.split("&");

                        String videoId = null;

                        for (String pair : parqamValuePairs) {
                            if (pair.startsWith("video_id")) {
                                videoId = pair.split("=")[1];
                                break;
                            }
                        }

                        if (videoId != null) {
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.youtube.com"))
                                    .setData(Uri.parse("http://www.youtube.com/watch?v=" + videoId)));

                            alreadyLoaded = !alreadyLoaded;
                        }
                    } catch (Exception ex) {
                        Log.e(TAG, ex.getMessage());
                        ex.printStackTrace();
                    }
                } else {
                    super.onLoadResource(view, url);
                }
            }

            @Override
            public void onReceivedSslError(WebView view, final SslErrorHandler handler, SslError error) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(ProductDetails.this);
                builder.setMessage(R.string.catalog_notification_error_ssl_cert_invalid);
                builder.setPositiveButton(ProductDetails.this.getResources().getString(R.string.catalog_continue), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        handler.proceed();
                    }
                });
                builder.setNegativeButton(ProductDetails.this.getResources().getString(R.string.catalog_cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        handler.cancel();
                    }
                });
                final AlertDialog dialog = builder.create();
                dialog.show();
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                if (url.contains("youtube.com/embed")) {
                    startActivity(new Intent(Intent.ACTION_VIEW,
                            Uri.parse("http://www.youtube.com")).
                            setData(Uri.parse(url)));
                }
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url.startsWith("tel:")) {
                    Intent intent = new Intent(Intent.ACTION_DIAL,
                            Uri.parse(url));
                    startActivity(intent);

                    return true;
                } else if (url.startsWith("mailto:")) {
                    Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse(url));
                    startActivity(intent);

                    return true;
                } else if (url.contains("youtube.com")) {
                    try {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.youtube.com")).setData(Uri.parse(url)));

                        return true;
                    } catch (Exception ex) {
                        return false;
                    }
                }else if (url.contains("goo.gl") || url.contains("maps") || url.contains("maps.yandex") || url.contains("livegpstracks")) {
                    startActivity(new Intent(Intent.ACTION_VIEW,
                            Uri.parse(url)).
                            setData(Uri.parse(url)));
                    return true;
                }else {
                    startActivity(new Intent(Intent.ACTION_VIEW,
                            Uri.parse(url)).
                            setData(Uri.parse(url)));
                    return true;
                }
            }
        });
        productDescription.loadDataWithBaseURL(null, product.description, "text/html", "UTF-8", null);

        productPrice = (TextView) findViewById(R.id.product_price);
        productPrice.setVisibility("0.00".equals(String.format(Locale.US, "%.2f", product.price)) ? View.GONE : View.VISIBLE);
        String result = com.ibuildapp.romanblack.CataloguePlugin.utils.Utils.currencyToPosition(Statics.uiConfig.currency, product.price);
        if (result.contains(getResources().getString(R.string.rest_number_pattern)))
            result = result.replace(getResources().getString(R.string.rest_number_pattern),"");
        productPrice.setText(result);

        likeCount = (TextView) findViewById(R.id.like_count);
        likeImage = (ImageView) findViewById(R.id.like_image);

        if (!TextUtils.isEmpty(product.imageURL)) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    String token = FacebookAuthorizationActivity.getFbToken(com.appbuilder.sdk.android.Statics.FACEBOOK_APP_ID, com.appbuilder.sdk.android.Statics.FACEBOOK_APP_SECRET);
                    if (TextUtils.isEmpty(token))
                        return;

                    List<String> urls = new ArrayList<>();
                    urls.add(product.imageURL);
                    final Map<String, String> res = FacebookAuthorizationActivity.getLikesForUrls(urls, token);
                    if (res != null) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (res.containsKey(product.imageURL))
                                    likeCount.setText(res.get(product.imageURL));
                            }
                        });
                    }
                    Log.e("", "");
                }
            }).start();
        }

        shareBtn = (LinearLayout) findViewById(R.id.share_button);
        if (Statics.uiConfig.showShareButton)
            shareBtn.setVisibility(View.VISIBLE);
        else shareBtn.setVisibility(View.GONE);

        shareBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                showDialogSharing(new DialogSharing.Configuration.Builder()
                                .setFacebookSharingClickListener(new DialogSharing.Item.OnClickListener() {
                                    @Override
                                    public void onClick() {
                                        // checking Internet connection
                                        if (!Utils.networkAvailable(ProductDetails.this))
                                            Toast.makeText(ProductDetails.this, getResources().getString(R.string.alert_no_internet), Toast.LENGTH_SHORT).show();
                                        else {
                                            if (Authorization.getAuthorizedUser(Authorization.AUTHORIZATION_TYPE_FACEBOOK) != null) {
                                                shareFacebook();
                                            } else {
                                                Authorization.authorize(ProductDetails.this, FACEBOOK_AUTHORIZATION_ACTIVITY, Authorization.AUTHORIZATION_TYPE_FACEBOOK);
                                            }
                                        }
                                    }
                                })
                                .setTwitterSharingClickListener(new DialogSharing.Item.OnClickListener() {
                                    @Override
                                    public void onClick() {
                                        // checking Internet connection
                                        if (!Utils.networkAvailable(ProductDetails.this))
                                            Toast.makeText(ProductDetails.this, getResources().getString(R.string.alert_no_internet), Toast.LENGTH_SHORT).show();
                                        else {
                                            if (Authorization.getAuthorizedUser(Authorization.AUTHORIZATION_TYPE_TWITTER) != null) {
                                                shareTwitter();
                                            } else {
                                                Authorization.authorize(ProductDetails.this, TWITTER_AUTHORIZATION_ACTIVITY, Authorization.AUTHORIZATION_TYPE_TWITTER);
                                            }
                                        }
                                    }
                                })
                                .setEmailSharingClickListener(new DialogSharing.Item.OnClickListener() {
                                    @Override
                                    public void onClick() {
                                        Intent intent = chooseEmailClient();
                                        intent.setType("text/html");

                                        // *************************************************************************************************
                                        // preparing sharing message
                                        String downloadThe = getString(R.string.directoryplugin_email_download_this);
                                        String androidIphoneApp = getString(R.string.directoryplugin_email_android_iphone_app);
                                        String postedVia = getString(R.string.directoryplugin_email_posted_via);
                                        String foundThis = getString(R.string.directoryplugin_email_found_this);

                                        // prepare content
                                        String downloadAppUrl = String.format("http://%s/projects.php?action=info&projectid=%s",
                                                com.appbuilder.sdk.android.Statics.BASE_DOMEN,
                                                Statics.appId);

                                        String adPart = String.format(downloadThe + " %s " + androidIphoneApp + ": <a href=\"%s\">%s</a><br>%s",
                                                Statics.appName,
                                                downloadAppUrl,
                                                downloadAppUrl,
                                                postedVia + " <a href=\"http://ibuildapp.com\">www.ibuildapp.com</a>");

                                        // content part
                                        String contentPath = String.format("<!DOCTYPE html><html><body><b>%s</b><br><br>%s<br><br>%s</body></html>",
                                                product.name,
                                                product.description,
                                                com.ibuildapp.romanblack.CataloguePlugin.Statics.hasAd ? adPart : "");


                                        contentPath = contentPath.replaceAll("\\<img.*?>", "");

                                        // prepare image to attach
                                        // FROM ASSETS
                                        InputStream stream;
                                        try {
                                            if (!TextUtils.isEmpty(product.imageRes)) {
                                                stream = manager.open(product.imageRes);

                                                String fileName = inputStreamToFile(stream);
                                                File copyTo = new File(fileName);
                                                intent.putExtra(Intent.EXTRA_STREAM,
                                                        Uri.fromFile(copyTo));
                                            }
                                        } catch (IOException e) {
                                            // from cache
                                            File copyTo = new File(product.imagePath);
                                            if (copyTo.exists()) {
                                                intent.putExtra(Intent.EXTRA_STREAM,
                                                        Uri.fromFile(copyTo));
                                            }
                                        }

                                        intent.putExtra(Intent.EXTRA_SUBJECT, product.name);
                                        intent.putExtra(Intent.EXTRA_TEXT, Html.fromHtml(contentPath));
                                        startActivity(intent);
                                    }
                                })
                                .build()
                );
            }
        });

        likeBtn = (LinearLayout) findViewById(R.id.like_button);
        if (Statics.uiConfig.showLikeButton)
            likeBtn.setVisibility(View.VISIBLE);
        else likeBtn.setVisibility(View.GONE);

        likeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (Utils.networkAvailable(ProductDetails.this)) {
                    if (!TextUtils.isEmpty(product.imageURL)) {
                        if (Authorization.isAuthorized(Authorization.AUTHORIZATION_TYPE_FACEBOOK)) {
                            new Thread(new Runnable() {
                                @Override
                                public void run() {

                                    List<String> userLikes;
                                    try {
                                        userLikes = FacebookAuthorizationActivity.getUserOgLikes();
                                        for (String likeUrl : userLikes) {
                                            if (likeUrl.compareToIgnoreCase(product.imageURL) == 0) {
                                                likedbyMe = true;
                                                break;
                                            }
                                        }

                                        if (!likedbyMe) {
                                            if (FacebookAuthorizationActivity.like(product.imageURL)) {
                                                String likeCountStr = likeCount.getText().toString();
                                                try {
                                                    final int res = Integer.parseInt(likeCountStr);

                                                    runOnUiThread(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            likeCount.setText(String.valueOf(res + 1));
                                                            enableLikeButton(false);
                                                            Toast.makeText(ProductDetails.this, getString(R.string.like_success), Toast.LENGTH_SHORT).show();
                                                        }
                                                    });
                                                } catch (NumberFormatException e) {
                                                    runOnUiThread(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            Toast.makeText(ProductDetails.this, getString(R.string.like_error), Toast.LENGTH_SHORT).show();
                                                        }
                                                    });
                                                }
                                            }
                                        } else {
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    enableLikeButton(false);
                                                    Toast.makeText(ProductDetails.this, getString(R.string.already_liked), Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                        }
                                    } catch (FacebookAuthorizationActivity.FacebookNotAuthorizedException e) {
                                        if (!Utils.networkAvailable(ProductDetails.this)) {
                                            Toast.makeText(ProductDetails.this, getString(R.string.alert_no_internet), Toast.LENGTH_SHORT).show();
                                            return;
                                        }
                                        Authorization.authorize(ProductDetails.this, AUTHORIZATION_FB, Authorization.AUTHORIZATION_TYPE_FACEBOOK);
                                    } catch (FacebookAuthorizationActivity.FacebookAlreadyLiked facebookAlreadyLiked) {
                                        facebookAlreadyLiked.printStackTrace();
                                    }

                                }
                            }).start();
                        } else {
                            if (!Utils.networkAvailable(ProductDetails.this)) {
                                Toast.makeText(ProductDetails.this, getString(R.string.alert_no_internet), Toast.LENGTH_SHORT).show();
                                return;
                            }
                            Authorization.authorize(ProductDetails.this, AUTHORIZATION_FB, Authorization.AUTHORIZATION_TYPE_FACEBOOK);
                        }
                    } else
                        Toast.makeText(ProductDetails.this, getString(R.string.nothing_to_like), Toast.LENGTH_SHORT).show();
                } else
                    Toast.makeText(ProductDetails.this, getString(R.string.alert_no_internet), Toast.LENGTH_SHORT).show();
            }
        });

        if (TextUtils.isEmpty(product.imageURL)) {
            enableLikeButton(false);
        } else {
            if (Authorization.isAuthorized(Authorization.AUTHORIZATION_TYPE_FACEBOOK)) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {

                        List<String> userLikes;
                        try {
                            userLikes = FacebookAuthorizationActivity.getUserOgLikes();
                            for (String likeUrl : userLikes) {
                                if (likeUrl.compareToIgnoreCase(product.imageURL) == 0) {
                                    likedbyMe = true;
                                    break;
                                }
                            }
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    enableLikeButton(!likedbyMe);
                                }
                            });
                        } catch (FacebookAuthorizationActivity.FacebookNotAuthorizedException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
        }

        // product bitmap rendering
        image = (AlphaImageView) findViewById(R.id.product_image);
        image.setVisibility(View.GONE);
        if (!TextUtils.isEmpty(product.imageRes)) {
            try {
                InputStream input = manager.open(product.imageRes);
                Bitmap btm = BitmapFactory.decodeStream(input);
                if (btm != null) {
                    int ratio = btm.getWidth() / btm.getHeight();
                    image.setLayoutParams(new LinearLayout.LayoutParams(screenWidth, screenWidth / ratio));

                    image.setImageBitmapWithAlpha(btm);
                    return;
                }
            } catch (IOException e) {
                Log.e(TAG, e.getMessage());
                e.printStackTrace();
            }
        }

        if (!TextUtils.isEmpty(product.imagePath)) {
            Bitmap btm = BitmapFactory.decodeFile(product.imagePath);
            if (btm != null) {
                if (btm.getWidth() != 0 && btm.getHeight() != 0) {
                    float ratio = (float) btm.getWidth() / (float) btm.getHeight();
                    image.setLayoutParams(new LinearLayout.LayoutParams(screenWidth, (int) (screenWidth / ratio)));
                    image.setImageBitmapWithAlpha(btm);
                    image.setVisibility(View.GONE);
                    return;
                }
            }
        }

        if (!TextUtils.isEmpty(product.imageURL)) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    product.imagePath = com.ibuildapp.romanblack.CataloguePlugin.utils.Utils.downloadFile(product.imageURL);
                    if (!TextUtils.isEmpty(product.imagePath)) {
                        SqlAdapter.updateProduct(product);
                        final Bitmap btm = BitmapFactory.decodeFile(product.imagePath);

                        if (btm != null) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (btm.getWidth() != 0 && btm.getHeight() != 0) {
                                        float ratio = (float) btm.getWidth() / (float) btm.getHeight();
                                        image.setLayoutParams(new LinearLayout.LayoutParams(screenWidth, (int) (screenWidth / ratio)));
                                        image.setImageBitmapWithAlpha(btm);
                                        image.setVisibility(View.GONE);
                                    }
                                }
                            });
                        }
                    }
                }
            }).start();
        }

        image.setVisibility(View.GONE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Payer.ResultStates state = payer.handleResult(requestCode, resultCode, data);

        if (state == Payer.ResultStates.SUCCESS) {
            setContentView(R.layout.details_instead_thanks_page);
            ((TextView) findViewById(R.id.title)).setText(Statics.widgetName);
            findViewById(R.id.back_btn).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    finish();
                }
            });
            thanksPage = true;

            return;
        } else if (state == Payer.ResultStates.NOT_SUCCESS) {
            return;
        }

        switch (requestCode) {
            case FACEBOOK_AUTHORIZATION_ACTIVITY: {
                if (resultCode == RESULT_OK)
                    shareFacebook();
            }
            break;
            case TWITTER_AUTHORIZATION_ACTIVITY: {
                if (resultCode == RESULT_OK)
                    shareTwitter();
            }
            break;

            case TWITTER_PUBLISH_ACTIVITY: {
                if (resultCode == RESULT_OK) {
                    Toast.makeText(ProductDetails.this, getResources().getString(R.string.directoryplugin_twitter_posted_success), Toast.LENGTH_LONG).show();
                } else if (resultCode == RESULT_CANCELED) {
                    Toast.makeText(ProductDetails.this, getResources().getString(R.string.directoryplugin_twitter_posted_error), Toast.LENGTH_LONG).show();
                }
            }
            break;

            case FACEBOOK_PUBLISH_ACTIVITY: {
                if (resultCode == RESULT_OK) {
                    Toast.makeText(ProductDetails.this, getResources().getString(R.string.directoryplugin_facebook_posted_success), Toast.LENGTH_LONG).show();
                } else if (resultCode == RESULT_CANCELED) {
                    Toast.makeText(ProductDetails.this, getResources().getString(R.string.directoryplugin_facebook_posted_error), Toast.LENGTH_LONG).show();
                }
            }
            break;

            case AUTHORIZATION_FB: {
                if (resultCode == RESULT_OK) {

                    List<String> userLikes = null;
                    try {
                        userLikes = FacebookAuthorizationActivity.getUserOgLikes();
                        if (userLikes != null)
                        for (String likeUrl : userLikes) {
                            if (likeUrl.compareToIgnoreCase(product.imageURL) == 0) {
                                likedbyMe = true;
                                break;
                            }
                        }

                        if (!likedbyMe) {
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        if (FacebookAuthorizationActivity.like(product.imageURL)) {
                                            String likeCountStr = likeCount.getText().toString();
                                            try {
                                                final int res = Integer.parseInt(likeCountStr);
                                                runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        likeCount.setText(String.valueOf(res + 1));
                                                        Toast.makeText(ProductDetails.this, getString(R.string.like_success), Toast.LENGTH_SHORT).show();
                                                        enableLikeButton(false);
                                                    }
                                                });
                                            } catch (NumberFormatException e) {
                                                runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        Toast.makeText(ProductDetails.this, getString(R.string.like_error), Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                            }
                                        }
                                    }catch (FacebookAuthorizationActivity.FacebookNotAuthorizedException e) {

                                    }catch (FacebookAuthorizationActivity.FacebookAlreadyLiked facebookAlreadyLiked) {
                                            facebookAlreadyLiked.printStackTrace();
                                        }
                                }
                            }).start();
                        } else {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    enableLikeButton(false);
                                    Toast.makeText(ProductDetails.this, getString(R.string.already_liked), Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    } catch (FacebookAuthorizationActivity.FacebookNotAuthorizedException e) {

                    }
                }
            }
            break;
        }
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(
                Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(quantity.getWindowToken(), 0);
    }

    /**
     * Enable/disable like button
     */
    private void enableLikeButton(boolean enable) {
        if (enable) {
            likeBtn.setBackgroundResource(R.drawable.button_selector);
            likeImage.setImageResource(R.drawable.like_icon);
            likeCount.setTextColor(getResources().getColor(R.color.black_trans_90));
            likeBtn.setEnabled(true);
        } else {
            likeBtn.setBackgroundResource(R.drawable.gray_disable_rectangle);
            likeImage.setImageResource(R.drawable.like_disable);
            likeCount.setTextColor(getResources().getColor(R.color.black_trans_30));
            likeBtn.setEnabled(false);
        }
    }

    /**
     * This menu cantains share on Facebook, Twitter and Email buttons. Also it
     * contains "Cancel" button.
     *
     * @param menu
     * @return true
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return false;
    }

    /**
     * Allow or deny showing menu
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return !thanksPage;
    }

    /**
     * Starts SharingActivity to share on Twitter.
     */
    private void shareTwitter() {
        Intent it = new Intent(ProductDetails.this, SharingActivity.class);

        // pass the picture path and start the activity
        it.putExtra("type", "twitter");
        it.putExtra("image_url", product.imageURL);
        startActivityForResult(it, TWITTER_PUBLISH_ACTIVITY);
    }

    /**
     * Starts SharingActivity to share on Facebook.
     */
    private void shareFacebook() {
        Intent it = new Intent(ProductDetails.this, SharingActivity.class);

        // pass the picture path and start the activity
        it.putExtra("type", "facebook");
        it.putExtra("image_url", product.imageURL);
        startActivityForResult(it, FACEBOOK_PUBLISH_ACTIVITY);
    }

    /**
     * Finds the best email client to share via email.
     *
     * @return prepared intent
     */
    private Intent chooseEmailClient() {
        Intent intent = new Intent(android.content.Intent.ACTION_SEND);
        intent.setType("text/plain");
        final PackageManager pm = getPackageManager();
        final List<ResolveInfo> matches = pm.queryIntentActivities(intent, 0);
        ResolveInfo best = null;

        // trying to find gmail client
        for (final ResolveInfo info : matches) {
            if (info.activityInfo.packageName.endsWith(".gm")
                    || info.activityInfo.name.toLowerCase().contains("gmail")) {
                best = info;
            }
        }

        if (best == null) {
            // if there is no gmail client trying to fing internal email client
            for (final ResolveInfo info : matches) {
                if (info.activityInfo.name.toLowerCase().contains("mail")) {
                    best = info;
                }
            }
        }
        if (best != null) {
            intent.setClassName(best.activityInfo.packageName, best.activityInfo.name);
        }

        return intent;
    }

    /**
     * Writes input stream data to file.
     *
     * @param srteam input stream
     * @return file name string
     */
    private String inputStreamToFile(InputStream srteam) {
        OutputStream outputStream;
        File outPut;
        try {
            // prepare dirs
            File path = new File(Statics.moduleCachePath);
            if (!path.exists()) {
                path.mkdirs();
            }

            // prepare file
            outPut = new File(Statics.moduleCachePath + File.separator + EMAIL_IMAGE_NAME);
            if (outPut.exists()) {
                outPut.delete();
                outPut.createNewFile();
            } else {
                outPut.createNewFile();
            }

            // write the inputStream to a FileOutputStream
            outputStream =
                    new FileOutputStream(outPut);

            int read = 0;
            byte[] bytes = new byte[1024];

            while ((read = srteam.read(bytes)) != -1) {
                outputStream.write(bytes, 0, read);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        return outPut.getAbsolutePath();
    }

    /**
     * The same as onResume()
     */
    @Override
    public void resume() {
        onShoppingCartItemAdded();
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
                resString.append(" (").append(String.valueOf(count)).append(")");
            updateWidgetInActualList(shopingCartIndex, resString.toString());
        }
    }

    public static class InnerPageTransformer implements ViewPager.PageTransformer {
        private static final float MIN_SCALE = 0.75f;

        public void transformPage(View view, float position) {
            int pageWidth = view.getWidth();

            if (position < -1) { // [-Infinity,-1)
                // This page is way off-screen to the left.
                ViewCompat.setAlpha(view, 0);

            } else if (position <= 0) { // [-1,0]
                // Use the default slide transition when moving to the left page
                ViewCompat.setAlpha(view, 1);
                ViewCompat.setTranslationX(view, 0);
                ViewCompat.setScaleX(view, 1);
                ViewCompat.setScaleY(view, 1);

            } else if (position <= 1) { // (0,1]
                // Fade the page out.
                ViewCompat.setAlpha(view,1 - position);

                // Counteract the default slide transition
                ViewCompat.setTranslationX(view, pageWidth * -position);

                // Scale the page down (between MIN_SCALE and 1)
                float scaleFactor = MIN_SCALE
                        + (1 - MIN_SCALE) * (1 - Math.abs(position));
                ViewCompat.setScaleX(view, scaleFactor);
                ViewCompat.setScaleY(view, scaleFactor);

            } else { // (1,+Infinity]
                // This page is way off-screen to the right.
                ViewCompat.setAlpha(view, 0);
            }
        }
    }
}
