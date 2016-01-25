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
package com.ibuildapp.romanblack.CataloguePlugin.holders;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;

import com.ibuildapp.romanblack.CataloguePlugin.R;

public class RoundHolder extends RecyclerView.ViewHolder {
    public ImageView roundIcon;
    public RoundHolder(View itemView) {
        super(itemView);

        roundIcon = (ImageView) itemView.findViewById(R.id.details_round_image);
    }
}
