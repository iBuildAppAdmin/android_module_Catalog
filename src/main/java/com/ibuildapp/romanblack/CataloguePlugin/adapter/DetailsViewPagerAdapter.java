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

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.ibuildapp.romanblack.CataloguePlugin.fragments.DetailsItemFragment;

import com.ibuildapp.romanblack.CataloguePlugin.model.ProductEntity;

import java.util.List;

public class DetailsViewPagerAdapter extends FragmentPagerAdapter {

    private List<String> entity;

    public DetailsViewPagerAdapter(FragmentManager fm, List<String> entity ) {
        super(fm);
        this.entity = entity;
    }

    @Override
    public Fragment getItem(int position) {
        DetailsItemFragment fragment = new DetailsItemFragment();
        fragment.setEntity(entity.get(position));
        return fragment;
    }

    @Override
    public int getCount() {
        return entity.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return "";
    }
}
