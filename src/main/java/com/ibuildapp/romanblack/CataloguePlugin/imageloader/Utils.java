/****************************************************************************
*                                                                           *
*  Copyright (C) 2014-2015 iBuildApp, Inc. ( http://ibuildapp.com )         *
*                                                                           *
*  This file is part of iBuildApp.                                          *
*                                                                           *
*  This Source Code Form is subject to the terms of the iBuildApp License.  *
*  You can obtain one at http://ibuildapp.com/license/                      *
*                                                                           *
****************************************************************************/
package com.ibuildapp.romanblack.CataloguePlugin.imageloader;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import org.apache.http.HttpStatus;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;

public class Utils {


    public static String downloadFile(Context context, String url) {
       return downloadFile(context, url, com.appbuilder.sdk.android.Utils.md5(url));
    }
    public static String downloadFile(Context context, String url, String md5) {
        final int BYTE_ARRAY_SIZE = 8024;
        final int CONNECTION_TIMEOUT = 30000;
        final int READ_TIMEOUT = 30000;

        try {
            for(int i = 0; i < 3; i++) {
                URL fileUrl = new URL(URLDecoder.decode(url));
                HttpURLConnection connection = (HttpURLConnection)fileUrl.openConnection();
                connection.setConnectTimeout(CONNECTION_TIMEOUT);
                connection.setReadTimeout(READ_TIMEOUT);
                connection.connect();

                int status = connection.getResponseCode();

                if(status >= HttpStatus.SC_BAD_REQUEST) {
                    connection.disconnect();

                    continue;
                }

                BufferedInputStream bufferedInputStream = new BufferedInputStream(connection.getInputStream());
                File file = new File(StaticData.getCachePath(context) + md5);

                if(!file.exists()) {
                    new File(StaticData.getCachePath(context)).mkdirs();
                    file.createNewFile();
                }

                FileOutputStream fileOutputStream = new FileOutputStream(file, false);
                int byteCount;
                byte[] buffer = new byte[BYTE_ARRAY_SIZE];

                while ((byteCount = bufferedInputStream.read(buffer, 0, BYTE_ARRAY_SIZE)) != -1)
                    fileOutputStream.write(buffer, 0, byteCount);

                bufferedInputStream.close();
                fileOutputStream.flush();
                fileOutputStream.close();

                return file.getAbsolutePath();
            }
        } catch (Exception e) {
            e.printStackTrace();

            return null;
        }

        return null;
    }

    public static String downloadFileAsString(String url) {
        final int CONNECTION_TIMEOUT = 30000;
        final int READ_TIMEOUT = 30000;

        try {
            for(int i = 0; i < 3; i++) {
                URL fileUrl = new URL(URLDecoder.decode(url));
                HttpURLConnection connection = (HttpURLConnection)fileUrl.openConnection();
                connection.setConnectTimeout(CONNECTION_TIMEOUT);
                connection.setReadTimeout(READ_TIMEOUT);
                connection.connect();

                int status = connection.getResponseCode();

                if(status >= HttpStatus.SC_BAD_REQUEST) {
                    connection.disconnect();

                    continue;
                }

                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder stringBuilder = new StringBuilder();
                String line;

                while((line = bufferedReader.readLine()) != null)
                    stringBuilder.append(line);

                bufferedReader.close();

                return stringBuilder.toString();
            }
        } catch (Exception e) {
            e.printStackTrace();

            return null;
        }

        return null;
    }

    public static Bitmap processBitmap(String fileName, Bitmap.Config config, int widthLimit) {
        Bitmap bitmap = null;

        try {File tempFile = new File(fileName);
            BitmapFactory.Options opts = new BitmapFactory.Options();
            BufferedInputStream fileInputStream = new BufferedInputStream(new FileInputStream(tempFile));

            opts.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(fileInputStream, null, opts);
            fileInputStream.close();
            fileInputStream = new BufferedInputStream(new FileInputStream(tempFile));

            //Find the correct scale value. It should be the power of 2.
            int width = opts.outWidth;
            int scale = 1;

            while (true) {
                int halfWidth = width / 2;

                if (halfWidth < widthLimit && (widthLimit - halfWidth) > widthLimit / 4)
                    break;

                width = halfWidth;
                scale *= 2;
            }

            opts = new BitmapFactory.Options();
            opts.inSampleSize = scale;
            opts.inPreferredConfig = config;

            try {
                System.gc();
                bitmap = BitmapFactory.decodeStream(fileInputStream, null, opts);

                if (bitmap != null)
                    return bitmap;
            } catch (Exception ex) {
            } catch (OutOfMemoryError e) {
            }

            fileInputStream.close();
            fileInputStream = new BufferedInputStream(new FileInputStream(tempFile));

            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            try {
                System.gc();
                bitmap = BitmapFactory.decodeStream(fileInputStream, null, opts);

                if (bitmap != null)
                    return bitmap;
            } catch (Exception ex) {
            } catch (OutOfMemoryError ex) {
            }

            fileInputStream.close();
            fileInputStream = new BufferedInputStream(new FileInputStream(tempFile));

            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            try {
                System.gc();
                bitmap = BitmapFactory.decodeStream(fileInputStream, null, opts);
            } catch (Exception ex) {
            } catch (OutOfMemoryError ex) {
            }

            fileInputStream.close();
        } catch (Exception exception) {
            exception.printStackTrace();
        }

        return bitmap;
    }
}
