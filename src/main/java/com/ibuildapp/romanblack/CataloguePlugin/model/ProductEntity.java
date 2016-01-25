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
import java.util.ArrayList;
import java.util.List;

/**
 * Class describes product entity
 */
public class ProductEntity implements Serializable {

    public int id = 0;                                              // product id
    public int categoryId = 0;                                      // category list to which product belongs
    public boolean visibility = true;                               // product visibility
    public boolean valid = true;                                    // update/delete flag
    public int order = 0;                                           // update\delete product from DB

    public long item_id = 0;                                         // item id

    public boolean marginBottom = false;
    public boolean marginTop = false;

    public String name = "";                                        // product name
    public String description = "";                                 // product desc
    public String sku = "";                                         // product sku
    public float price = 0;                                         // product price
    public String imageURL = "";                                    // http url for category
    public String imageRes = "";                                    // assets url for category
    public String imagePath = "";                                   // cache url for category

    public String thumbnailURL = "";                                // http thumbnail url for category
    public String thumbnailRes = "";                                // assets thumbnail url for category
    public String thumbnailPath = "";                               // cache thumbnail url for category
    public float oldprice = 0;
    public ProductItemType itemType = ProductItemType.SIMPLE;
    public String itemButtonText="";
    public String itemUrl="";
    public List<String> imageUrls = new ArrayList<>();
    @Override
    public String toString() {
        return "ProductEntity{" +
                "id=" + id +
                ", categoryId=" + categoryId +
                ", visibility=" + visibility +
                ", valid=" + valid +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", sku='" + sku + '\'' +
                ", price=" + price +
                ", imageURL='" + imageURL + '\'' +
                ", imageRes='" + imageRes + '\'' +
                ", imagePath='" + imagePath + '\'' +
                '}';
    }
}
