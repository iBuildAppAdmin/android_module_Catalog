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

import java.io.Serializable;

public class CategoryProduct implements Serializable {
    public CategoryEntity category;
    public ProductEntity product;

    public CategoryProduct(CategoryEntity category, ProductEntity product) {
        this.category = category;
        this.product = product;
    }
}
