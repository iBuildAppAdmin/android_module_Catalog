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
package com.ibuildapp.romanblack.CataloguePlugin.utils;

import android.util.Log;

import com.ibuildapp.romanblack.CataloguePlugin.model.JsonResponse;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

public class HTTPQuery {

    static final int TIMEOUT_CONNECTION = 5000;
    static final int TIMEOUT_SOCKET = 7000;
    static final int STATUS_CODE_500 = 500;

    static final String PARAM_APP_ID = "app_id";
    static final String PARAM_WIDGET_ID = "widget_id";
    static final String PARAM_ORDER_INFO = "order_info";
    static final String PARAM_ITEMS = "items";

    /**
     * Send request to endpoint with passed params
     *
     * @param orderInfo information about order
     * @param items     list of items converted to string
     * @return response
     */
    public static JsonResponse sendRequest(final String orderInfo, final String items) {
        try {
            HttpParams httpParameters = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(httpParameters, TIMEOUT_CONNECTION);
            HttpConnectionParams.setSoTimeout(httpParameters, TIMEOUT_SOCKET);
            HttpClient client = new DefaultHttpClient(httpParameters);
            HttpPost post = new HttpPost( com.ibuildapp.romanblack.CataloguePlugin.Statics.ENDPOINT.contains("http")
                    ? com.ibuildapp.romanblack.CataloguePlugin.Statics.ENDPOINT
                    : "http://"+com.ibuildapp.romanblack.CataloguePlugin.Statics.ENDPOINT);
            HttpResponse httpResponse;

            post.setEntity(new UrlEncodedFormEntity(
                    new ArrayList<NameValuePair>() {{
                        add(new BasicNameValuePair(PARAM_APP_ID, String.valueOf(com.ibuildapp.romanblack.CataloguePlugin.Statics.appId)));
                        add(new BasicNameValuePair(PARAM_WIDGET_ID, String.valueOf(com.ibuildapp.romanblack.CataloguePlugin.Statics.widgetId)));
                        add(new BasicNameValuePair(PARAM_ORDER_INFO, orderInfo));
                        add(new BasicNameValuePair(PARAM_ITEMS, items));
                    }}, "UTF-8"));
            httpResponse = client.execute(post);

            if (STATUS_CODE_500 == httpResponse.getStatusLine().getStatusCode())
                return new JsonResponse();

            return parseQueryError(EntityUtils.toString(httpResponse.getEntity()));
        } catch (IOException e) {
            Log.e("", "An extremely unlikely failure occurred: ", e);
        }

        return new JsonResponse();
    }

    /**
     * Convert string response to object
     *
     * @param source string value of response
     * @return response
     */
    private static JsonResponse parseQueryError(String source) {
        JSONObject mainObject;
        JsonResponse response = new JsonResponse();
        try {
            mainObject = new JSONObject(source);
            response.orderNumber = mainObject.getInt("order_number");
            response.status = mainObject.getString("status");
            response.description = mainObject.getString("description");
            return response;
        } catch (JSONException e) {
            e.printStackTrace();
            return response;
        }
    }

}
