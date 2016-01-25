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
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.AsyncTask;
import android.text.Html;
import android.text.InputFilter;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.URLSpan;
import android.util.SparseArray;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.appbuilder.sdk.android.AppBuilderModuleMain;
import com.ibuildapp.PayPalAndroidUtil.Payer;
import com.ibuildapp.romanblack.CataloguePlugin.database.SqlAdapter;
import com.ibuildapp.romanblack.CataloguePlugin.model.InputFilterMinMax;
import com.ibuildapp.romanblack.CataloguePlugin.model.ProductEntity;
import com.ibuildapp.romanblack.CataloguePlugin.model.ShoppingCart;
import com.ibuildapp.romanblack.CataloguePlugin.model.UserProfile;
import com.ibuildapp.romanblack.CataloguePlugin.utils.Utils;

import java.io.File;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ShoppingCartPage extends AppBuilderModuleMain {
    private static final String pattern = "widget *= *\\d*";
    public static final int REQUEST_EXIT = 103012;
    private final boolean isLight = Statics.uiConfig.colorSkin.isLight;
    private final boolean isPayPalBased = Statics.isShoppingCartPayPalBased;
    private SparseArray<Pair> source;
    private float totalPrice = 0;
    private Payer payer;
    private int padding;
    private TextView total_price;
    private LinearLayout shopping_cart_content;
    private View footer;
    private LayoutInflater inflater;
    private SparseArray<String> editTextValues;
    private Bitmap placeHolder;
    private SparseArray<Bitmap> images;
    private SparseBooleanArray deleted;
    private LinearLayout priceLayout;

    /**
     * The same as onCreate()
     */
    @Override
    public void create() {
        initializeBackend();
        initializeUI();
    }

    /**
     * The same as onDestroy()
     */
    @Override
    public void destroy() {
        if (isPayPalBased)
            payer.stopPayPalService();

        for (int i = 0; i < images.size(); i++)
            images.get(i).recycle();

        placeHolder.recycle();
        placeHolder = null;

        System.gc();
    }

    @Override
    public void finish() {
        for (int i = 0; i < deleted.size(); i++)
            if (deleted.get(i))
                ShoppingCart.removeProduct(source.get(i).product.getId());

        super.finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (isPayPalBased && payer.handleResult(requestCode, resultCode, data) == Payer.ResultStates.SUCCESS) {
            ShoppingCart.clear();
            setContentView(R.layout.details_instead_thanks_page);
            findViewById(R.id.layout).setBackgroundColor(Statics.uiConfig.colorSkin.color1);
            findViewById(R.id.navbar_holder).setBackgroundColor(getResources().getColor(Statics.uiConfig.colorSkin.color1 == Color.WHITE ? R.color.black_trans_20 : R.color.white_trans_50));
            ((TextView) findViewById(R.id.title)).setText(Statics.widgetName);
            ((TextView) findViewById(R.id.text)).setTextColor(Statics.uiConfig.colorSkin.color4);
            findViewById(R.id.back_btn).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    finish();
                }
            });

            return;
        }

        if (REQUEST_EXIT == requestCode && resultCode == RESULT_OK) {
            ShoppingCart.clear();
            finish();
        }
    }

    /**
     * Set some values
     */
    private void initializeBackend() {
        List<ShoppingCart.Product> source = ShoppingCart.getProducts();
        this.source = new SparseArray<Pair>();
        editTextValues = new SparseArray<String>();
        images = new SparseArray<Bitmap>();
        deleted = new SparseBooleanArray();

        for (int i = 0; i < source.size(); i++) {
            ShoppingCart.Product product = source.get(i);
            this.source.put(i, new Pair(product, SqlAdapter.selectProductById(product.getId())));
            editTextValues.put(i, String.valueOf(product.getQuantity()));
            deleted.put(i, false);
        }

        placeHolder = BitmapFactory.decodeResource(getResources(), R.drawable.placeholder_black);
        padding = (int) (5 * getResources().getDisplayMetrics().density + 0.5);

        setTotalPrice();

        if (isPayPalBased) {
            payer = new Payer();
            payer.startPayPalService(this, Statics.PAYPAL_CLIENT_ID);
        }
    }

    /**
     * Initializing user interface
     */
    private void initializeUI() {
        hideTopBar();

        if (cartIsEmpty())
            return;

        setContentView(R.layout.shopping_cart_page);
        View hamburgerView = findViewById(R.id.hamburger_view_btn);
        hamburgerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                animateRootContainer();
            }
        });
        if (!showSideBar) {
            hamburgerView.setVisibility(View.GONE);
        }
        else {
            hamburgerView.setVisibility(View.VISIBLE);
        }

        findViewById(R.id.layout).setBackgroundColor(Statics.uiConfig.colorSkin.color1);

        shopping_cart_content = (LinearLayout) findViewById(R.id.shopping_cart_list_view_fake);

        inflater = LayoutInflater.from(this);

        footer = inflater.inflate(R.layout.shopping_cart_page_footer, shopping_cart_content, false);
        footer.findViewById(R.id.footer_layout).setBackgroundColor(Statics.uiConfig.colorSkin.color1);

        if (Statics.shoppingCartFields.description == null || "".equals(Statics.shoppingCartFields.description))
            footer.findViewById(R.id.shopping_cart_page_desc_layout).setVisibility(View.GONE);
        else {
            footer.findViewById(R.id.shopping_cart_page_desc_layout).setVisibility(View.VISIBLE);
            TextView view = (TextView) footer.findViewById(R.id.description_text);
            footer.findViewById(R.id.separator2).setBackgroundColor(getResources().getColor(isLight ? R.color.black_trans_20 : R.color.white_trans_20));
            setTextViewHTML(view, Statics.shoppingCartFields.description);
            view.setClickable(true);
            view.setMovementMethod(LinkMovementMethod.getInstance());
        }

        //webView.loadData(Statics.shoppingCartFields.description,"text/html; charset=UTF-8", null);
        priceLayout = (LinearLayout) footer.findViewById(R.id.shopping_cart_price_layout);
        if (totalPrice == 0.00f)
            priceLayout.setVisibility(View.GONE);
        else priceLayout.setVisibility(View.VISIBLE);

        TextView total_writing = (TextView) footer.findViewById(R.id.total_writing);
        total_writing.setTextColor(Statics.uiConfig.colorSkin.color4);

        TextView title = (TextView) footer.findViewById(R.id.title);
        title.setTextColor(Statics.uiConfig.colorSkin.color4);

        total_price = (TextView) footer.findViewById(R.id.total_price);
        total_price.setTextColor(Statics.uiConfig.colorSkin.color5);
        total_price.setText(com.ibuildapp.romanblack.CataloguePlugin.utils.Utils.currencyToPosition(Statics.uiConfig.currency, totalPrice));

        final EditText first_name = (EditText) footer.findViewById(R.id.first_name);
        final EditText last_name = (EditText) footer.findViewById(R.id.last_name);
        final EditText email_address = (EditText) footer.findViewById(R.id.email_address);
        final EditText phone = (EditText) footer.findViewById(R.id.phone);
        final EditText country = (EditText) footer.findViewById(R.id.country);
        final EditText street_address = (EditText) footer.findViewById(R.id.street_address);
        final EditText city = (EditText) footer.findViewById(R.id.city);
        final EditText state = (EditText) footer.findViewById(R.id.state);
        final EditText zip_code = (EditText) footer.findViewById(R.id.zip_code);
        final EditText note = (EditText) footer.findViewById(R.id.note);

        TextView apply = (TextView) footer.findViewById(R.id.apply);
        apply.getBackground().setColorFilter(Statics.uiConfig.colorSkin.color5, PorterDuff.Mode.SRC_ATOP);
        apply.setTextColor(isLight ? getResources().getColor(R.color.white) : Color.parseColor("#333333"));
        apply.setText(getResources().getString(isPayPalBased ? R.string.shopping_cart_footer_apply_check_out : R.string.shopping_cart_footer_apply_submit_order));
        apply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isPayPalBased) {
                    ShoppingCart.checkoutWithPayPal(payer);
                } else {
                    final boolean firstNameIsFine =
                            !Statics.shoppingCartFields.firstName.isVisible() ||
                                    !Statics.shoppingCartFields.firstName.isRequired() ||
                                    !TextUtils.isEmpty(first_name.getText().toString());
                    final boolean lastNameIsFine =
                            !Statics.shoppingCartFields.lastName.isVisible() ||
                                    !Statics.shoppingCartFields.lastName.isRequired() ||
                                    !TextUtils.isEmpty(last_name.getText().toString());
                    final boolean emailAddressIsFine =
                            !Statics.shoppingCartFields.emailAddress.isVisible() ||
                                    !Statics.shoppingCartFields.emailAddress.isRequired() ||
                                    Utils.isValidEmail(email_address.getText().toString());
                    final boolean phoneIsFine =
                            !Statics.shoppingCartFields.phone.isVisible() ||
                                    !Statics.shoppingCartFields.phone.isRequired() ||
                                    !TextUtils.isEmpty(phone.getText().toString());
                    final boolean countryIsFine =
                            !Statics.shoppingCartFields.country.isVisible() ||
                                    !Statics.shoppingCartFields.country.isRequired() ||
                                    !TextUtils.isEmpty(country.getText().toString());
                    final boolean streetAddressIsFine =
                            !Statics.shoppingCartFields.streetAddress.isVisible() ||
                                    !Statics.shoppingCartFields.streetAddress.isRequired() ||
                                    !TextUtils.isEmpty(street_address.getText().toString());
                    final boolean cityIsFine =
                            !Statics.shoppingCartFields.city.isVisible() ||
                                    !Statics.shoppingCartFields.city.isRequired() ||
                                    !TextUtils.isEmpty(city.getText().toString());
                    final boolean stateIsFine =
                            !Statics.shoppingCartFields.state.isVisible() ||
                                    !Statics.shoppingCartFields.state.isRequired() ||
                                    !TextUtils.isEmpty(state.getText().toString());
                    final boolean zipCodeIsFine =
                            !Statics.shoppingCartFields.zipCode.isVisible() ||
                                    !Statics.shoppingCartFields.zipCode.isRequired() ||
                                    !TextUtils.isEmpty(zip_code.getText().toString());
                    final boolean noteIsFine =
                            !Statics.shoppingCartFields.note.isVisible() ||
                                    !Statics.shoppingCartFields.note.isRequired() ||
                                    !TextUtils.isEmpty(note.getText().toString());

                    if (firstNameIsFine &&
                            lastNameIsFine &&
                            emailAddressIsFine &&
                            phoneIsFine &&
                            countryIsFine &&
                            streetAddressIsFine &&
                            cityIsFine &&
                            stateIsFine &&
                            zipCodeIsFine &&
                            noteIsFine) {

                        first_name.setBackgroundDrawable(getResources().getDrawable(R.drawable.shopping_cart_edittext_no_errors));
                        first_name.setPadding(padding, 0, padding, 0);
                        last_name.setBackgroundDrawable(getResources().getDrawable(R.drawable.shopping_cart_edittext_no_errors));
                        last_name.setPadding(padding, 0, padding, 0);
                        email_address.setBackgroundDrawable(getResources().getDrawable(R.drawable.shopping_cart_edittext_no_errors));
                        email_address.setPadding(padding, 0, padding, 0);
                        phone.setBackgroundDrawable(getResources().getDrawable(R.drawable.shopping_cart_edittext_no_errors));
                        phone.setPadding(padding, 0, padding, 0);
                        country.setBackgroundDrawable(getResources().getDrawable(R.drawable.shopping_cart_edittext_no_errors));
                        country.setPadding(padding, 0, padding, 0);
                        street_address.setBackgroundDrawable(getResources().getDrawable(R.drawable.shopping_cart_edittext_no_errors));
                        street_address.setPadding(padding, 0, padding, 0);
                        city.setBackgroundDrawable(getResources().getDrawable(R.drawable.shopping_cart_edittext_no_errors));
                        city.setPadding(padding, 0, padding, 0);
                        state.setBackgroundDrawable(getResources().getDrawable(R.drawable.shopping_cart_edittext_no_errors));
                        state.setPadding(padding, 0, padding, 0);
                        zip_code.setBackgroundDrawable(getResources().getDrawable(R.drawable.shopping_cart_edittext_no_errors));
                        zip_code.setPadding(padding, 0, padding, 0);
                        note.setBackgroundDrawable(getResources().getDrawable(R.drawable.shopping_cart_edittext_no_errors));
                        note.setPadding(padding, 0, padding, 0);

                        if (!isInternetConnected())
                            return;

                        ShoppingCart.sendOrder(ShoppingCartPage.this, new UserProfile.Builder()
                                .setFirstName(first_name.getText().toString())
                                .setLastName(last_name.getText().toString())
                                .setEmailAddress(email_address.getText().toString())
                                .setPhone(phone.getText().toString())
                                .setCountry(country.getText().toString())
                                .setStreetAddress(street_address.getText().toString())
                                .setCity(city.getText().toString())
                                .setState(state.getText().toString())
                                .setZipCode(zip_code.getText().toString())
                                .setNote(note.getText().toString())
                                .build());
                    } else {
                        if (!emailAddressIsFine) {
                            email_address.setBackgroundDrawable(getResources().getDrawable(R.drawable.shopping_cart_edittext_errors));
                            email_address.setPadding(padding, 0, padding, 0);
                            email_address.requestFocus();
                            email_address.setSelection(email_address.length());

                            Toast.makeText(ShoppingCartPage.this, R.string.shopping_cart_invalid_email, Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(ShoppingCartPage.this, R.string.shopping_cart_fill_required_fields, Toast.LENGTH_LONG).show();

                            if (!firstNameIsFine) {
                                first_name.setBackgroundDrawable(getResources().getDrawable(R.drawable.shopping_cart_edittext_errors));
                                first_name.setPadding(padding, 0, padding, 0);
                            }

                            if (!lastNameIsFine) {
                                last_name.setBackgroundDrawable(getResources().getDrawable(R.drawable.shopping_cart_edittext_errors));
                                last_name.setPadding(padding, 0, padding, 0);
                            }

                            if (!phoneIsFine) {
                                phone.setBackgroundDrawable(getResources().getDrawable(R.drawable.shopping_cart_edittext_errors));
                                phone.setPadding(padding, 0, padding, 0);
                            }

                            if (!countryIsFine) {
                                country.setBackgroundDrawable(getResources().getDrawable(R.drawable.shopping_cart_edittext_errors));
                                country.setPadding(padding, 0, padding, 0);
                            }

                            if (!streetAddressIsFine) {
                                street_address.setBackgroundDrawable(getResources().getDrawable(R.drawable.shopping_cart_edittext_errors));
                                street_address.setPadding(padding, 0, padding, 0);
                            }

                            if (!cityIsFine) {
                                city.setBackgroundDrawable(getResources().getDrawable(R.drawable.shopping_cart_edittext_errors));
                                city.setPadding(padding, 0, padding, 0);
                            }

                            if (!stateIsFine) {
                                state.setBackgroundDrawable(getResources().getDrawable(R.drawable.shopping_cart_edittext_errors));
                                state.setPadding(padding, 0, padding, 0);
                            }

                            if (!zipCodeIsFine) {
                                zip_code.setBackgroundDrawable(getResources().getDrawable(R.drawable.shopping_cart_edittext_errors));
                                zip_code.setPadding(padding, 0, padding, 0);
                            }

                            if (!noteIsFine) {
                                note.setBackgroundDrawable(getResources().getDrawable(R.drawable.shopping_cart_edittext_errors));
                                note.setPadding(padding, 0, padding, 0);
                            }
                        }
                    }
                }
            }
        });

        UserProfile userProfile = SqlAdapter.getUserProfile();

        if (isPayPalBased) {
            first_name.setVisibility(View.GONE);
            last_name.setVisibility(View.GONE);
            email_address.setVisibility(View.GONE);
            phone.setVisibility(View.GONE);
            country.setVisibility(View.GONE);
            street_address.setVisibility(View.GONE);
            city.setVisibility(View.GONE);
            state.setVisibility(View.GONE);
            zip_code.setVisibility(View.GONE);
            note.setVisibility(View.GONE);
            title.setVisibility(View.GONE);
            footer.findViewById(R.id.separator).setVisibility(View.GONE);
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) apply.getLayoutParams();
            params.setMargins(0, 0, 0, 0);
            apply.setLayoutParams(params);
        } else {
            first_name.setVisibility(Statics.shoppingCartFields.firstName.isVisible() ? View.VISIBLE : View.GONE);
            first_name.setMaxLines(Statics.shoppingCartFields.firstName.isMultiline() ? Integer.MAX_VALUE : 1);
            first_name.setHint(Statics.shoppingCartFields.firstName.getHint());
            first_name.setText(userProfile.getFirstName());
            last_name.setVisibility(Statics.shoppingCartFields.lastName.isVisible() ? View.VISIBLE : View.GONE);
            last_name.setMaxLines(Statics.shoppingCartFields.lastName.isMultiline() ? Integer.MAX_VALUE : 1);
            last_name.setHint(Statics.shoppingCartFields.lastName.getHint());
            last_name.setText(userProfile.getLastName());
            email_address.setVisibility(Statics.shoppingCartFields.emailAddress.isVisible() ? View.VISIBLE : View.GONE);
            email_address.setMaxLines(Statics.shoppingCartFields.emailAddress.isMultiline() ? Integer.MAX_VALUE : 1);
            email_address.setHint(Statics.shoppingCartFields.emailAddress.getHint());
            email_address.setText(userProfile.getEmailAddress());
            phone.setVisibility(Statics.shoppingCartFields.phone.isVisible() ? View.VISIBLE : View.GONE);
            phone.setMaxLines(Statics.shoppingCartFields.phone.isMultiline() ? Integer.MAX_VALUE : 1);
            phone.setHint(Statics.shoppingCartFields.phone.getHint());
            phone.setText(userProfile.getPhone());
            country.setVisibility(Statics.shoppingCartFields.country.isVisible() ? View.VISIBLE : View.GONE);
            country.setMaxLines(Statics.shoppingCartFields.country.isMultiline() ? Integer.MAX_VALUE : 1);
            country.setHint(Statics.shoppingCartFields.country.getHint());
            country.setText(userProfile.getCountry());
            street_address.setVisibility(Statics.shoppingCartFields.streetAddress.isVisible() ? View.VISIBLE : View.GONE);
            street_address.setMaxLines(Statics.shoppingCartFields.streetAddress.isMultiline() ? Integer.MAX_VALUE : 1);
            street_address.setHint(Statics.shoppingCartFields.streetAddress.getHint());
            street_address.setText(userProfile.getStreetAddress());
            city.setVisibility(Statics.shoppingCartFields.city.isVisible() ? View.VISIBLE : View.GONE);
            city.setMaxLines(Statics.shoppingCartFields.city.isMultiline() ? Integer.MAX_VALUE : 1);
            city.setHint(Statics.shoppingCartFields.city.getHint());
            city.setText(userProfile.getCity());
            state.setVisibility(Statics.shoppingCartFields.state.isVisible() ? View.VISIBLE : View.GONE);
            state.setMaxLines(Statics.shoppingCartFields.state.isMultiline() ? Integer.MAX_VALUE : 1);
            state.setHint(Statics.shoppingCartFields.state.getHint());
            state.setText(userProfile.getState());
            zip_code.setVisibility(Statics.shoppingCartFields.zipCode.isVisible() ? View.VISIBLE : View.GONE);
            zip_code.setMaxLines(Statics.shoppingCartFields.zipCode.isMultiline() ? Integer.MAX_VALUE : 1);
            zip_code.setHint(Statics.shoppingCartFields.zipCode.getHint());
            zip_code.setText(userProfile.getZipCode());
            note.setVisibility(Statics.shoppingCartFields.note.isVisible() ? View.VISIBLE : View.GONE);
            note.setMaxLines(Statics.shoppingCartFields.note.isMultiline() ? Integer.MAX_VALUE : 1);
            note.setHint(Statics.shoppingCartFields.note.getHint());
            note.setText(userProfile.getNote());

            footer.findViewById(R.id.separator).setBackgroundColor(getResources().getColor(isLight ? R.color.black_trans_20 : R.color.white_trans_20));
        }

        findViewById(R.id.navbar_holder).setBackgroundColor(getResources().getColor(Statics.uiConfig.colorSkin.color1 == Color.WHITE ? R.color.black_trans_20 : R.color.white_trans_50));
        findViewById(R.id.back_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    protected void makeLinkClickable(SpannableStringBuilder strBuilder, final URLSpan span)
    {
        int start = strBuilder.getSpanStart(span);
        int end = strBuilder.getSpanEnd(span);
        int flags = strBuilder.getSpanFlags(span);
        ClickableSpan clickable = new ClickableSpan() {
            public void onClick(View view) {
               String s = span.getURL();
                Pattern outerPattern = Pattern.compile(pattern);
                Matcher outerMatcher = outerPattern.matcher(s);
                if ( outerMatcher.find() )
                {
                    Pattern innerPattern = Pattern.compile("\\d+");
                    String res = outerMatcher.toMatchResult().group();
                    Matcher innerMatcher = innerPattern.matcher(res);
                    if ( innerMatcher.find() ){
                        Integer order = Integer.valueOf(innerMatcher.toMatchResult().group());
                        try {
                            com.appbuilder.sdk.android.Statics.linkWidgets.get(order).onClick(view);
                        }catch(Exception e){
                            e.printStackTrace();
                        }
                    }
                }
            }
        };
        strBuilder.setSpan(clickable, start, end, flags);
        strBuilder.removeSpan(span);
    }

    protected void setTextViewHTML(TextView text, String html)
    {
        CharSequence sequence = Html.fromHtml(html);
        SpannableStringBuilder strBuilder = new SpannableStringBuilder(sequence);
        URLSpan[] urls = strBuilder.getSpans(0, sequence.length(), URLSpan.class);
        for(URLSpan span : urls) {
            makeLinkClickable(strBuilder, span);
        }
        text.setText(strBuilder);
    }
    /**
     * The same as onResume()
     */
    @Override
    public void resume() {

        if (source == null || source.size() == 0) {
            setShoppingCartEmpty();

            return;
        }

        shopping_cart_content.removeAllViews();

        for (int i = 0; i < source.size(); i++) {
            Pair pair = source.get(i);
            View view = inflater.inflate(R.layout.shopping_cart_page_item, shopping_cart_content, false);

            ImageView preview = (ImageView) view.findViewById(R.id.preview);
            Bitmap btm = images.get(pair.product.getId());
            if (btm == null || btm.getHeight() == 1) {
                new ImageDownloadTask(i, preview).execute();
                preview.setImageBitmap(placeHolder);
            } else {
                preview.setImageBitmap(btm);
            }

            TextView name = (TextView) view.findViewById(R.id.name);
            name.setTextColor(Statics.uiConfig.colorSkin.color4);
            name.setText(pair.productEntity.name);

            TextView sku = (TextView) view.findViewById(R.id.sku);

            if(TextUtils.isEmpty(pair.productEntity.sku)) {
                sku.setVisibility(View.GONE);
                sku.setText("");
            } else {
                sku.setVisibility(View.VISIBLE);
                sku.setText(getString(R.string.item_sku) + " " + pair.productEntity.sku);
            }

            TextView price = (TextView) view.findViewById(R.id.price);
            price.setTextColor(Statics.uiConfig.colorSkin.color5);
            price.setText(Utils.currencyToPosition(Statics.uiConfig.currency, pair.productEntity.price));
            if (pair.productEntity.price == 0.00f)
                price.setVisibility(View.INVISIBLE);
            else price.setVisibility(View.VISIBLE);

            EditText quantity = (EditText) view.findViewById(R.id.quantity);
            quantity.setTag(i);
            quantity.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                public void onFocusChange(View view, boolean hasFocus) {
                    EditText editText = (EditText) view;
                    int index = (Integer) view.getTag();
                    if (!deleted.get(index)) {
                        String text = editText.getText().toString();
                        Pair pair = source.get(index);
                        int quantity;

                        try {
                            quantity = Integer.valueOf(text);
                        } catch (Exception exception) {
                            try {
                                quantity = Integer.valueOf(editTextValues.get(index));
                            } catch (Exception nestedException) {
                                quantity = 0;
                            }
                        }

                        editTextValues.put(index, String.valueOf(quantity));

                        if (!hasFocus && !TextUtils.isEmpty(text) && quantity > 0) {
                            pair.product = new ShoppingCart.Product.Builder()
                                    .setId(pair.product.getId())
                                    .setQuantity(quantity)
                                    .build();
                            source.put(index, pair);
                            ShoppingCart.insertProduct(pair.product);

                            setTotalPrice();
                        } else {
                            editText.setText(editTextValues.get(index));
                        }
                    }
                }
            });
            quantity.setFilters(new InputFilter[]{
                    new InputFilter() {
                        @Override
                        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                            return (dest.subSequence(0, dstart).toString() +
                                    source.subSequence(start, end) +
                                    dest.subSequence(dend, dest.length()).toString()).startsWith("0") ? "" : null;
                        }
                    }, new InputFilterMinMax("1", "999999")
            });
            quantity.setText(editTextValues.get(i));

            View delete = view.findViewById(R.id.delete);
            delete.setTag(i);
            ((ImageView) delete.findViewById(R.id.delete_cross)).setImageDrawable(
                    getResources().getDrawable(isLight ? R.drawable.shopping_cart_delete_cross_black : R.drawable.shopping_cart_delete_cross_white)
            );
            ((TextView) delete.findViewById(R.id.delete_writing)).setTextColor(getResources().getColor(isLight ? R.color.black : R.color.white));
            delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int index = (Integer) view.getTag();
                    deleted.put(index, true);
                    shopping_cart_content.getChildAt(index).setVisibility(View.GONE);
                    setTotalPrice();
                }
            });

            view.findViewById(R.id.separator).setBackgroundColor(getResources().getColor(isLight ? R.color.black_trans_20 : R.color.white_trans_20));

            shopping_cart_content.addView(view);
        }

        shopping_cart_content.addView(footer);
        shopping_cart_content.requestFocus();
    }

    /**
     * Checks if internet is connected
     */
    private boolean isInternetConnected() {
        boolean isInternetConnected = com.appbuilder.sdk.android.Utils.networkAvailable(this);

        if (!isInternetConnected)
            Toast.makeText(this, R.string.shopping_cart_need_internet_connection, Toast.LENGTH_LONG).show();

        return isInternetConnected;
    }

    /**
     * Checks if cart is empty
     *
     * @return true if cart is empty and false otherwise
     */
    private boolean cartIsEmpty() {
        boolean isEmpty = source == null || source.size() == 0;
        boolean tmp = true;

        for (int i = 0; i < deleted.size(); i++)
            tmp &= deleted.get(i);

        return isEmpty || tmp;
    }

    /**
     * Setting total price of cart
     */
    public void setTotalPrice() {
        totalPrice = 0;

        if (cartIsEmpty()) {
            setShoppingCartEmpty();

            return;
        }

        List<ShoppingCart.Product> source = ShoppingCart.getProducts();

        for (int i = 0; i < deleted.size(); i++)
            if (!deleted.get(i))
                totalPrice += SqlAdapter.selectProductById(source.get(i).getId()).price * source.get(i).getQuantity();

        if (total_price != null)
            total_price.setText(com.ibuildapp.romanblack.CataloguePlugin.utils.Utils.currencyToPosition(Statics.uiConfig.currency, totalPrice));

        if (priceLayout!=null)
        if (totalPrice == 0.00f)
            priceLayout.setVisibility(View.GONE);
        else priceLayout.setVisibility(View.VISIBLE);
    }

    /**
     * Set layout for situation when cart is empty
     */
    public void setShoppingCartEmpty() {
        setContentView(R.layout.shopping_cart_empty);

        findViewById(R.id.layout).setBackgroundColor(Statics.uiConfig.colorSkin.color1);

        ImageView empty_cart_image = (ImageView) findViewById(R.id.empty_cart_image);
        empty_cart_image.setImageResource(isLight ? R.drawable.sergeyb_shopingcard_cart_pic : R.drawable.sergeyb_shopingcard_cart_pic_dark);

        TextView empty_cart_text = (TextView) findViewById(R.id.empty_cart_text);
        empty_cart_text.setTextColor(Statics.uiConfig.colorSkin.color4);

        findViewById(R.id.navbar_holder).setBackgroundColor(getResources().getColor(Statics.uiConfig.colorSkin.color1 == Color.WHITE ? R.color.black_trans_20 : R.color.white_trans_50));
        findViewById(R.id.back_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        View hamburgerView = findViewById(R.id.hamburger_view_btn);
        hamburgerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                animateRootContainer();
            }
        });
        if (!showSideBar) {
            hamburgerView.setVisibility(View.GONE);
        }
        else {
            hamburgerView.setVisibility(View.VISIBLE);
        }

    }

    /**
     * Tie the values for save in one container
     */
    private class Pair {
        private ShoppingCart.Product product;
        private ProductEntity productEntity;

        private Pair(ShoppingCart.Product product, ProductEntity productEntity) {
            this.product = product;
            this.productEntity = productEntity;
        }

        @Override
        public boolean equals(Object object) {
            return object instanceof Pair && ((Pair) object).product.equals(product);
        }

        @Override
        public int hashCode() {
            return product.hashCode();
        }
    }

    /**
     * Async image loader
     */
    private class ImageDownloadTask extends AsyncTask<Void, String, Void> {

        private int index;
        private ImageView view;
        private Bitmap bitmap;

        private ImageDownloadTask(int index, ImageView view) {
            this.index = index;
            this.view = view;
        }

        @Override
        protected Void doInBackground(Void... items) {
            Pair pair = source.get(index);
            String imagePath = Statics.moduleCachePath + File.separator + com.appbuilder.sdk.android.Utils.md5(pair.productEntity.thumbnailURL);
            File file = new File(imagePath);

            if (!file.exists())
                imagePath = Utils.downloadFile(pair.productEntity.thumbnailURL);

            if (imagePath != null)
                bitmap = Utils.proccessBitmap(imagePath, Bitmap.Config.RGB_565, 300);

            return null;
        }

        @Override
        protected void onProgressUpdate(String... param) {
        }

        @Override
        protected void onPostExecute(Void unused) {
            if (bitmap != null && view != null)
                view.setImageBitmap(bitmap);

            view = null;
        }
    }
}
