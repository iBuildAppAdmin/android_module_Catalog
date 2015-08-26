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

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.ibuildapp.PayPalAndroidUtil.Payer;
import com.ibuildapp.romanblack.CataloguePlugin.R;
import com.ibuildapp.romanblack.CataloguePlugin.ShoppingCartPage;
import com.ibuildapp.romanblack.CataloguePlugin.Statics;
import com.ibuildapp.romanblack.CataloguePlugin.ThankYouPage;
import com.ibuildapp.romanblack.CataloguePlugin.database.SqlAdapter;
import com.ibuildapp.romanblack.CataloguePlugin.utils.HTTPQuery;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ShoppingCart {

    public static final String EXTRA_ORDER_NUMBER = "EXTRA_ORDER_NUMBER";
    private static ProgressDialog progressDialog;

    /**
     * Insert passed product to cart
     *
     * @param product product
     */
    public static void insertProduct(final Product product) {
        List<Product> products = getProducts();
        int index = products.indexOf(product);

        if (index == -1) {
            products.add(product);
        } else {
            products.set(index, new Product.Builder()
                            .setId(product.getId())
                            .setQuantity(product.getQuantity())
                            .build()
            );
        }

        SqlAdapter.insertShoppingCartContent(products);
    }

    /**
     * Remove product from cart by id
     *
     * @param id id of product
     */
    public static void removeProduct(final int id) {
        SqlAdapter.deleteItemFromShoppingCart(id);
    }

    /**
     * Get list of products in cart
     *
     * @return products list
     */
    public static List<Product> getProducts() {
        return SqlAdapter.selectShoppingCartProducts();
    }

    /**
     * Clear cart
     */
    public static void clear() {
        SqlAdapter.deleteShoppingCartRow(getProducts());
    }

    /**
     * Convert current user profile to shipping for endpoint request
     *
     * @return converted value
     */
    private static String toShippingForm() {
        JSONObject root = new JSONObject();
        UserProfile user = SqlAdapter.getUserProfile();

        try {
            // user profile
            JSONObject shippingForm = new JSONObject();
            shippingForm.put("firstname", user.getFirstName());
            shippingForm.put("lastname", user.getLastName());
            shippingForm.put("email", user.getEmailAddress());
            shippingForm.put("phone", user.getPhone());
            shippingForm.put("country", user.getCountry());
            shippingForm.put("street", user.getStreetAddress());
            shippingForm.put("city", user.getCity());
            shippingForm.put("state", user.getState());
            shippingForm.put("zip", user.getZipCode());
            shippingForm.put("note", user.getNote());

            root.put("shipping_form", shippingForm);

            return root.toString();

        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Convert current product list to string array for endpoint request
     *
     * @return converted value
     */
    private static String toItems() {
        JSONObject root = new JSONObject();
        List<Product> productList = getProducts();

        try {
            // goods
            for (Product product : productList)
                root.put(String.valueOf(SqlAdapter.selectProductById(product.getId()).item_id), product.getQuantity());

            return root.toString();

        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Send order to endpoint
     *
     * @param context     context
     * @param userProfile userProfile
     */
    public static void sendOrder(final Context context, final UserProfile userProfile) {
        SqlAdapter.insertUserProfile(userProfile);

        try {
            final Thread worker = new Thread(new Runnable() {
                @Override
                public void run() {
                    final JsonResponse response = HTTPQuery.sendRequest(toShippingForm(), toItems());

                    if ("complete".equals(response.status)) {
                        if (context instanceof Activity) {
                            Intent intent = new Intent(context, ThankYouPage.class);
                            intent.putExtra(EXTRA_ORDER_NUMBER, response.orderNumber);
                            ((Activity) context).startActivityForResult(intent, ShoppingCartPage.REQUEST_EXIT);
                        }
                    } else {
                        if (context instanceof Activity)
                            ((Activity) context).runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(context, context.getString(R.string.shopping_cart_send_error) + " " + response.description, Toast.LENGTH_SHORT).show();
                                }
                            });
                    }

                    if (context instanceof Activity)
                        ((Activity) context).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                progressDialog.dismiss();
                                progressDialog = null;
                            }
                        });
                }
            });

            progressDialog = ProgressDialog.show(context, null, context.getResources().getString(R.string.shopping_cart_loading), true, true, new DialogInterface.OnCancelListener() {
                public void onCancel(DialogInterface arg0) {
                    try {
                        if (worker.isAlive())
                            worker.interrupt();
                        arg0.dismiss();
                    } catch (Exception e) {
                        Log.d("", "");
                    }
                }
            });

            worker.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Checkout with paypal
     *
     * @param payer instance of payer that will be used for pay
     */
    public static void checkoutWithPayPal(final Payer payer) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                List<Payer.Item> items = new ArrayList<Payer.Item>();
                List<Product> products = getProducts();

                for (Product product : products) {
                    ProductEntity entity = SqlAdapter.selectProductById(product.getId());
                    items.add(new Payer.Item.Builder()
                            .setPrice(entity.price)
                            .setCurrencyCode(Payer.CurrencyCode.valueOf(Statics.uiConfig.currency))
                            .setQuantity(product.getQuantity())
                            .setName(entity.name)
                            .setEndpoint(Statics.ENDPOINT)
                            .setAppId(Statics.appId)
                            .setWidgetId(Statics.widgetId)
                            .setItemId(entity.item_id)
                            .build());
                }

                payer.multiplePayment(items);
            }
        }).start();
    }

    public static class Product {

        private final int id;
        private final int quantity;

        private Product(Builder builder) {
            id = builder.id;
            quantity = builder.quantity;
        }

        /**
         * Gets quantity.
         *
         * @return Value of quantity.
         */
        public int getQuantity() {
            return quantity;
        }

        /**
         * Gets id.
         *
         * @return Value of id.
         */
        public int getId() {
            return id;
        }

        @Override
        public boolean equals(Object object) {
            return object instanceof Product && ((Product) object).id == id;
        }

        @Override
        public int hashCode() {
            return Integer.valueOf(id).hashCode();
        }

        public static class Builder {

            private int id;
            private int quantity;

            /**
             * Sets new id.
             *
             * @param id New value of id.
             */
            public Builder setId(int id) {
                this.id = id;

                return this;
            }

            /**
             * Sets new quantity.
             *
             * @param quantity New value of quantity.
             */
            public Builder setQuantity(int quantity) {
                this.quantity = quantity;

                return this;
            }

            /**
             * Build Product with set params.
             *
             * @return new instance of Product
             */
            public Product build() {
                return new Product(this);
            }

        }

    }

}
