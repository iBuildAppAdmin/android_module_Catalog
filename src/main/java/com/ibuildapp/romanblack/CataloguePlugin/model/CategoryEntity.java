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

/**
 * Class describes category
 */
public class CategoryEntity implements Serializable {

    public int id = 0;                     // category id
    public int parentId = 0;               // parent id for category
    public boolean visibility = true;      // category visibility flag
    public boolean valid = true;           // update\delete product from DB
    public int order = 0;                  // update\delete product from DB

    public String name = "";               // name
    public String imageURL = "";           // http url for category
    public String imageRes = "";           // assets url for category
    public String imagePath = "";          // cache url for category

    public CategoryEntity() {
        this.id = 0;
        this.parentId = 0;
        this.visibility = true;
        this.valid = true;
        this.name = "";
        this.imageURL = "";
        this.imageRes = "";
        this.imagePath = "";
    }

    public CategoryEntity(CategoryEntity categoryEntity) {
        id = categoryEntity.id;
        parentId = categoryEntity.parentId;
        visibility = categoryEntity.visibility;
        valid = categoryEntity.valid;
        name = categoryEntity.name;
        imageURL = categoryEntity.imageURL;
        imageRes = categoryEntity.imageRes;
        imagePath = categoryEntity.imagePath;
    }

    @Override
    public String toString() {
        return "CategoryEntity{" +
                "id=" + id +
                ", parentId=" + parentId +
                ", visibility=" + visibility +
                ", valid=" + valid +
                ", name='" + name + '\'' +
                ", imageURL='" + imageURL + '\'' +
                ", imageRes='" + imageRes + '\'' +
                ", imagePath='" + imagePath + '\'' +
                '}';
    }
}
