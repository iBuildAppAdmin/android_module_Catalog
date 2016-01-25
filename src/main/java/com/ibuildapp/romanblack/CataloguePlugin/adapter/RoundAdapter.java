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
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ibuildapp.romanblack.CataloguePlugin.R;
import com.ibuildapp.romanblack.CataloguePlugin.holders.RoundHolder;

import java.util.List;

public class RoundAdapter extends RecyclerView.Adapter<RoundHolder> {
    private Context context;
    private int currentItem;
    private List<String> imageUrls;

    public RoundAdapter(Context context, List<String> imageUrls){
        this.context = context;
        this.imageUrls = imageUrls;
    }

    @Override
    public RoundHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.details_round_layout, parent, false);
        RoundHolder holder = new RoundHolder(v);
        return holder;
    }

    @Override
    public void onBindViewHolder(RoundHolder holder, int position) {
        if (position == currentItem)
            holder.roundIcon.setImageResource(R.drawable.active_image);
        else holder.roundIcon.setImageResource(R.drawable.image);
    }

    @Override
    public int getItemCount() {
        return imageUrls.size();
    }

    public void setCurrentItem(int currentItem) {
        this.currentItem = currentItem;
    }

}
