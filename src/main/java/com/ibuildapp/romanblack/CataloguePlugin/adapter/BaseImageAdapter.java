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

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.ibuildapp.romanblack.CataloguePlugin.task.GetBitmapTask;
import com.ibuildapp.romanblack.CataloguePlugin.view.AlphaImageView;
import com.ibuildapp.romanblack.CataloguePlugin.view.RoundView;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Base image adapter class provides easy access to download images for adapter asynchronous
 */
public class BaseImageAdapter extends BaseAdapter implements BaseImageAdapterInterface {

    // constants
    private final String TAG = BaseImageAdapter.class.getCanonicalName();
    private final int UPDATE_IMAGES = 10003;
    private final int JUST_UPDATE_LISTVIEW = 10004;
    private final int THREAD_POOL_SIZE = 20;
    protected Context context;
    protected ConcurrentHashMap<Integer, Bitmap> imageMap;
    protected ConcurrentHashMap<Integer, ImageView> imageViewMap;
    protected ViewGroup uiView;
    private ConcurrentLinkedQueue<TaskItem> taskQueue;
    private QueueManager queueTask;
    private onLoadedListener onLoadedListener;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case UPDATE_IMAGES: {
                    if (uiView != null) {
                        try {
                            View v = uiView.findViewWithTag(msg.arg1);
                            if (v != null && imageMap.get(msg.arg1) != null) {
                                //Log.e(TAG, "v != null");

                                if (v instanceof AlphaImageView)
                                    ((AlphaImageView) v).setImageBitmapWithAlpha(imageMap.get(msg.arg1));
                                if (v instanceof RoundView)
                                    ((RoundView) v).setImageBitmapWithAlpha(imageMap.get(msg.arg1));
                                else
                                    ((ImageView) v).setImageBitmap(imageMap.get(msg.arg1));

                                //Log.e(TAG, "UPDATE_IMAGES uid = " + msg.arg1 + " imageHolder = " + ((ImageView) v).toString());
                            } else
                                notifyDataSetChanged();
                        } catch (Exception e) {
                            notifyDataSetChanged();
                            //Log.e(TAG, "v == null");
                        }
                    } else
                        notifyDataSetChanged();
                }
                break;
                case JUST_UPDATE_LISTVIEW: {
                    notifyDataSetChanged();
                }
                break;
            }
        }
    };

    public BaseImageAdapter(Context context, ViewGroup uiView) {
        this.uiView = uiView;
        this.context = context;
        this.imageMap = new ConcurrentHashMap<Integer, Bitmap>();
        this.imageViewMap = new ConcurrentHashMap<Integer, ImageView>();
        this.taskQueue = new ConcurrentLinkedQueue();
        queueTask = new QueueManager();
        queueTask.start();
    }

    /**
     * Sets {@link BaseImageAdapter.onLoadedListener}
     *
     * @param onLoadedListener listener
     */
    public void setOnLoadedListener(BaseImageAdapter.onLoadedListener onLoadedListener) {
        this.onLoadedListener = onLoadedListener;
    }

    @Override
    public void addTask(ImageView imageHolder, int uid, String DUBUG_PRODUCT_NAME, String resPath, String cachePath, String url, int width, int height, int reactionType) {
        // refresh imageview for this uid!
        imageViewMap.put(uid, imageHolder);

        if (!imageMap.containsKey(uid)) {
            taskQueue.add(new TaskItem(imageHolder, uid, DUBUG_PRODUCT_NAME, resPath, cachePath, url, width, height, reactionType));
            Bitmap.Config conf = Bitmap.Config.valueOf("RGB_565");
            imageMap.put(uid, Bitmap.createBitmap(1, 1, conf));
        }
    }

    @Override
    public void stopAllTasks() {
    }

    @Override
    public int getCount() {
        return 0;
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        return null;
    }

    /**
     * Clear bitmaps
     */
    public void clearBitmaps() {
        for (Integer key : imageMap.keySet()) {
            imageMap.get(key).recycle();
        }
        imageMap.clear();
        System.gc();
        queueTask.interrupt();
        queueTask.isInterrupted = true;
    }

    public interface onLoadedListener {

        /**
         * Callback after image downloading
         *
         * @param uid                 - image uid
         * @param downloadedImagePath - downloaded image path
         */
        void onImageLoaded(int uid, String downloadedImagePath);
    }

    /**
     * TASK object
     */
    private class TaskItem {

        private ImageView imageView;
        private int uid;
        private String uri;
        private String resPath;
        private String cachePath;
        private String name;
        private int width;
        private int height;
        private int reactionType;

        public TaskItem(ImageView imageView, int uid, String name, String resPath, String cachePath, String uri, int width, int height, int reactionType) {
            this.uid = uid;
            this.name = name;
            this.imageView = imageView;
            this.resPath = resPath;
            this.cachePath = cachePath;
            this.uri = uri;

            this.width = width;
            this.height = height;
            this.reactionType = reactionType;
        }
    }

    /**
     * This manager handle queue limits and creates new thread to download images
     */
    private class QueueManager extends Thread implements OnImageDoneListener {

        private boolean isInterrupted;
        private ConcurrentHashMap<Integer, Thread> threadList;

        private QueueManager() {
            this.threadList = new ConcurrentHashMap<Integer, Thread>();
        }

        @Override
        public void run() {
            super.run();

            while (!isInterrupted() && !isInterrupted) {
                if (threadList.size() < THREAD_POOL_SIZE) {
                    TaskItem taskItem = (TaskItem) taskQueue.poll();
                    if (taskItem != null) {
                        if (!threadList.contains(taskItem.uid)) {
                            GetBitmapTask task = new GetBitmapTask(context, taskItem.uid, taskItem.name, taskItem.imageView, taskItem.resPath, taskItem.cachePath, taskItem.uri, taskItem.width, taskItem.height, -1, taskItem.reactionType);
                            task.setListener(this);
                            threadList.put(taskItem.uid, task);
                            task.start();
                        }
                    }
                }

                // sleep для разгрузки процессора
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            taskQueue.clear();
            taskQueue = null;

            for(Thread thread : threadList.values())
                thread.interrupt();

            threadList.clear();
            threadList = null;
            context = null;
        }

        @Override
        public void onImageLoaded(int uid, ImageView imageHolder, String name, Bitmap image, String downloadedImagePath, int reactionType) {
            if(threadList == null || imageMap == null || handler == null)
                return;

            threadList.remove(uid);

            // put image to storage and refresh adapter

            switch (reactionType) {
                case REACTION_DEFAULT: {
                    if (image != null) {
                        imageMap.put(uid, image);
                        Message msg = handler.obtainMessage(UPDATE_IMAGES, uid, -1);
                        handler.sendMessage(msg);

                        if (!TextUtils.isEmpty(downloadedImagePath)) {
                            if (onLoadedListener != null) {
                                onLoadedListener.onImageLoaded(uid, downloadedImagePath);
                            }
                        }
                    }
                }
                break;
                case REACTION_JUST_DOWNLOAD: {
                    if (image != null) {
                        imageMap.put(uid, image);
                        Message msg = handler.obtainMessage(JUST_UPDATE_LISTVIEW, uid, -1);
                        handler.sendMessage(msg);

                        if (!TextUtils.isEmpty(downloadedImagePath)) {
                            if (onLoadedListener != null) {
                                onLoadedListener.onImageLoaded(uid, downloadedImagePath);
                            }
                        }
                    }
                }
                break;
            }
        }
    }
}
