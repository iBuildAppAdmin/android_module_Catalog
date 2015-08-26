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

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.Log;

import com.appbuilder.sdk.android.authorization.Authorization;
import com.ibuildapp.romanblack.CataloguePlugin.Statics;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Currency;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class Utils {

    public static String TAG = Utils.class.getCanonicalName();

    /**
     * convert background color to font color
     */
    public static int BackColorToFontColor(int backColor) {
        int r = (backColor >> 16) & 0xFF;
        int g = (backColor >> 8) & 0xFF;
        int b = (backColor >> 0) & 0xFF;

        double Y = (0.299 * r + 0.587 * g + 0.114 * b);
        if (Y > 127) {
            return Color.BLACK;
        } else {
            return Color.WHITE;
        }
    }

    /**
     * compution currency positon
     */
    public static String currencyToPosition(String currencyStr, float price) {
        try {
            Locale locale = Locale.US;
            Currency currency = Currency.getInstance(currencyStr);
            java.text.NumberFormat format = java.text.NumberFormat.getCurrencyInstance(locale);
            format.setCurrency(currency);

            return format.format(price);
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * Opens Bitmap from file
     *
     * @param fileName - file path
     * @return
     */
    public static Bitmap proccessBitmap(String fileName, Bitmap.Config config, int widthLimit) {

        Bitmap bitmap = null;
        File tempFile = null;
        BitmapFactory.Options opts = new BitmapFactory.Options();
        try {
            // decode image with appropriate options
            tempFile = new File(fileName);
            opts.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(new FileInputStream(tempFile), null, opts);
        } catch (Exception e) {
        }

        //Find the correct scale value. It should be the power of 2.
        int width = opts.outWidth, height = opts.outHeight;
        ;
        int scale = 1;
        while (true) {
            if (width / 2 <= widthLimit || height / 2 <= widthLimit) {
                break;
            }
            width /= 2;
            height /= 2;
            scale *= 2;
        }

        opts = new BitmapFactory.Options();
        opts.inSampleSize = scale;
        opts.inPreferredConfig = config;

        try {
            System.gc();
            bitmap = BitmapFactory.decodeStream(new FileInputStream(tempFile), null, opts);
            if (bitmap != null) {
                return bitmap;
            }
        } catch (Exception ex) {
        } catch (OutOfMemoryError e) {
        }

        try {
            Thread.sleep(300);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        try {
            System.gc();
            bitmap = BitmapFactory.decodeStream(new FileInputStream(tempFile), null, opts);
            if (bitmap != null) {
                return bitmap;
            }
        } catch (Exception ex) {
        } catch (OutOfMemoryError ex) {
        }

        try {
            Thread.sleep(300);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        try {
            System.gc();
            bitmap = BitmapFactory.decodeStream(new FileInputStream(tempFile), null, opts);
        } catch (Exception ex) {
        } catch (OutOfMemoryError ex) {
        }

        return bitmap;
    }

    /**
     * Opens Bitmap from stream
     *
     * @param stream - input stream
     * @param config decoding config
     * @return bitmap
     */
    public static Bitmap proccessBitmap(InputStream stream, Bitmap.Config config) {
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inPreferredConfig = config;
        Bitmap bitmap = null;
        try {
            // decode image with appropriate options
            try {
                System.gc();
                bitmap = BitmapFactory.decodeStream(stream, null, opts);
            } catch (Exception ex) {
                Log.d("", "");
            } catch (OutOfMemoryError e) {
                Log.d("", "");
                System.gc();
                try {
                    bitmap = BitmapFactory.decodeStream(stream, null, opts);
                } catch (Exception ex) {
                    Log.d("", "");
                } catch (OutOfMemoryError ex) {
                    Log.e("decodeImageFile", "OutOfMemoryError");
                }
            }
        } catch (Exception e) {
            Log.d("", "");
            return null;
        }

        return bitmap;
    }

    /**
     * download file url and save it
     *
     * @param url
     */
    public static String downloadFile(String url) {
        int BYTE_ARRAY_SIZE = 1024;
        int CONNECTION_TIMEOUT = 30000;
        int READ_TIMEOUT = 30000;

        // downloading cover image and saving it into file
        try {
            URL imageUrl = new URL(URLDecoder.decode(url));
            URLConnection conn = imageUrl.openConnection();
            conn.setConnectTimeout(CONNECTION_TIMEOUT);
            conn.setReadTimeout(READ_TIMEOUT);
            BufferedInputStream bis = new BufferedInputStream(conn.getInputStream());

            File resFile = new File(Statics.moduleCachePath + File.separator + com.appbuilder.sdk.android.Utils.md5(url));
            if (!resFile.exists()) {
                resFile.createNewFile();
            }

            FileOutputStream fos = new FileOutputStream(resFile);
            int current = 0;
            byte[] buf = new byte[BYTE_ARRAY_SIZE];
            Arrays.fill(buf, (byte) 0);
            while ((current = bis.read(buf, 0, BYTE_ARRAY_SIZE)) != -1) {
                fos.write(buf, 0, current);
                Arrays.fill(buf, (byte) 0);
            }

            bis.close();
            fos.flush();
            fos.close();
            Log.d("", "");
            return resFile.getAbsolutePath();
        } catch (SocketTimeoutException e) {
            return null;
        } catch (IllegalArgumentException e) {
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Get Facebook likes for url
     *
     * @param url url
     * @return map of dependencies url key with likes count value
     */
    public static Map<String, String> getFbLikesForUrl(String url) {
        getFbToken();

        if (TextUtils.isEmpty(Statics.FACEBOOK_APP_TOKEN))
            return null;

        if (TextUtils.isEmpty(url))
            return null;

        // collect urls list
        StringBuilder sb = new StringBuilder();
        sb.append("\"");
        sb.append(URLEncoder.encode(url));
        sb.append("\"");

        String likesResult = loadURLData("https://graph.facebook.com/fql?"
                + "q=SELECT+total_count,+url+FROM+link_stat+WHERE+url+IN+("
                + sb.toString() + ")&access_token=" + Statics.FACEBOOK_APP_TOKEN);

        JSONObject mainObject = null;
        HashMap<String, String> tempResultMap = new HashMap<String, String>();
        try {
            mainObject = new JSONObject(likesResult);
            JSONArray likesJSONArray = mainObject.getJSONArray("data");

            for (int i = 0; i < likesJSONArray.length(); i++) {
                JSONObject likeObject = likesJSONArray.getJSONObject(i);
                tempResultMap.put(likeObject.getString("url"),
                        likeObject.getString("total_count"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return tempResultMap;
    }

    /**
     * Sets Facebook token to {@link com.ibuildapp.romanblack.CataloguePlugin.Statics}
     */
    private static void getFbToken() {
        try {
            String tokenUrl = "https://graph.facebook.com/oauth/access_token?"
                    + "client_id="
                    + com.appbuilder.sdk.android.Statics.FACEBOOK_APP_ID +
                    "&client_secret="
                    + com.appbuilder.sdk.android.Statics.FACEBOOK_APP_SECRET +
                    "&grant_type=client_credentials";

            String accessResult = loadURLData(tokenUrl);

            Statics.FACEBOOK_APP_TOKEN = accessResult.split("=")[1];

            Log.d("", "");
        } catch (Exception ex) {
            Log.d("", "");
        }
    }

    /**
     * Download URL data to String.
     *
     * @param msgsUrl URL to download
     * @return data string
     */
    private static String loadURLData(String msgsUrl) {
        try {
            URL url = new URL(msgsUrl);
            URLConnection conn = url.openConnection();
            InputStreamReader streamReader = new InputStreamReader(conn.getInputStream());

            BufferedReader br = new BufferedReader(streamReader);
            StringBuilder sb = new StringBuilder();
            String line = null;
            while ((line = br.readLine()) != null) {
                sb.append(line);
                sb.append("\n");
            }
            br.close();
            String resp = sb.toString();

            return resp;
        } catch (IOException iOEx) {
            return "";
        }
    }

    /**
     * Likes video with given position.
     */
    public static boolean like(final String innerurl) {
        try {
            String url = "https://graph.facebook.com/me/og.likes";

            HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setRequestProperty("Accept-Encoding", "identity");
            conn.setRequestProperty("charset", "utf-8");
            conn.setRequestProperty("User-Agent",
                    "Mozilla/5.0 (iPhone; U; "
                            + "CPU iPhone OS 4_0 like Mac OS X; en-us) AppleWebKit/532.9 "
                            + "(KHTML, like Gecko) Version/4.0.5 Mobile/8A293 "
                            + "Safari/6531.22.7");

            conn.setRequestMethod("POST");
            conn.setDoOutput(true);

            StringBuilder sb = new StringBuilder();
            sb.append("method=");
            sb.append("POST");
            sb.append("&");
            sb.append("access_token=");
            sb.append(Authorization.getAuthorizedUser(Authorization.AUTHORIZATION_TYPE_FACEBOOK).getAccessToken());
            sb.append("&");
            sb.append("object=");
            sb.append(URLEncoder.encode(innerurl));

            String params = sb.toString();

            conn.getOutputStream().write(params.getBytes("UTF-8"));

            String response = "";
            try {
                InputStream in = conn.getInputStream();

                StringBuilder sbr = new StringBuilder();
                BufferedReader r = new BufferedReader(new InputStreamReader(in), 1000);
                for (String line = r.readLine(); line != null; line = r.readLine()) {
                    sbr.append(line);
                }
                in.close();

                response = sbr.toString();
            } catch (FileNotFoundException e) {
                InputStream in = conn.getErrorStream();

                StringBuilder sbr = new StringBuilder();
                BufferedReader r = new BufferedReader(new InputStreamReader(in), 1000);
                for (String line = r.readLine(); line != null; line = r.readLine()) {
                    sbr.append(line);
                }
                in.close();

                response = sbr.toString();
                Log.e(TAG, "response = " + response);
            }

            try {
                JSONObject obj = new JSONObject(response);
                obj.getString("id");
                return true;


            } catch (JSONException jSONEx) {
                // какого то Х fb выдает ошибку когда лайкаешь картинку первый раз
//                      // со второго раза все норм поэтому дублируем запрос в таком случае
                conn = (HttpURLConnection) new URL(url).openConnection();
                conn.setRequestProperty("Accept-Encoding", "identity");
                conn.setRequestProperty("charset", "utf-8");
                conn.setRequestProperty("User-Agent",
                        "Mozilla/5.0 (iPhone; U; "
                                + "CPU iPhone OS 4_0 like Mac OS X; en-us) AppleWebKit/532.9 "
                                + "(KHTML, like Gecko) Version/4.0.5 Mobile/8A293 "
                                + "Safari/6531.22.7");

                conn.setRequestMethod("POST");
                conn.setDoOutput(true);

                sb = new StringBuilder();
                sb.append("method=");
                sb.append("POST");
                sb.append("&");
                sb.append("access_token=");
                sb.append(Authorization.getAuthorizedUser(Authorization.AUTHORIZATION_TYPE_FACEBOOK).getAccessToken());
                sb.append("&");
                sb.append("object=");
                sb.append(URLEncoder.encode(innerurl));

                params = sb.toString();

                conn.getOutputStream().write(params.getBytes("UTF-8"));

                try {
                    InputStream in = conn.getInputStream();

                    StringBuilder sbr = new StringBuilder();
                    BufferedReader r = new BufferedReader(new InputStreamReader(in), 1000);
                    for (String line = r.readLine(); line != null; line = r.readLine()) {
                        sbr.append(line);
                    }
                    in.close();

                    response = sbr.toString();
                    Log.e(TAG, "response2 = " + response);

                    try {
                        JSONObject obj = new JSONObject(response);
                        obj.getString("id");
                        return true;
                    } catch (JSONException e) {
                        return false;
                    }
                } catch (FileNotFoundException e) {
                    return false;
                }
            }
        } catch (MalformedURLException mURLEx) {
            Log.d("", "");
            return false;
        } catch (IOException iOEx) {
            Log.d("", "");
            return false;
        } catch (Exception ex) {
            Log.d("", "");
            return false;
        }
    }

    /**
     * Validate passed email
     *
     * @param email email
     * @return true if email is valid and false otherwise
     */
    public static boolean isValidEmail(String email) {
        return !TextUtils.isEmpty(email) && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    /**
     * Show alert dialog with passed params
     *
     * @param context                     context
     * @param title                       title of dialog
     * @param message                     message of dialog
     * @param positiveButtonText          text of positive button (OK, for example)
     * @param negativeButtonText          text of positive button (CANCEL, for example)
     * @param onDialogButtonClickListener listener of clicks dialog's buttons
     */
    public static void showDialog(final Context context, final int title, final String message, final int positiveButtonText, final int negativeButtonText, final OnDialogButtonClickListener onDialogButtonClickListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(message);
        builder.setTitle(title);
        builder.setCancelable(false);
        builder.setPositiveButton(positiveButtonText, onDialogButtonClickListener != null ? new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                onDialogButtonClickListener.onPositiveClick(dialog);
            }
        } : null);
        builder.setNegativeButton(negativeButtonText, onDialogButtonClickListener != null ? new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                onDialogButtonClickListener.onNegativeClick(dialog);
            }
        } : null);
        builder.create().show();
    }

    public interface OnDialogButtonClickListener {
        void onPositiveClick(DialogInterface dialog);

        void onNegativeClick(DialogInterface dialog);
    }
}
