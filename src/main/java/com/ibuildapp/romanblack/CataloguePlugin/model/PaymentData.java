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

import android.os.Parcel;
import android.os.Parcelable;

public class PaymentData implements Parcelable {

    public static final Creator CREATOR = new Creator() {
        public PaymentData createFromParcel(Parcel in) {
            return new PaymentData(in);
        }

        public PaymentData[] newArray(int size) {
            return new PaymentData[size];
        }
    };
    private final String clientId;

    private PaymentData(Parcel in) {
        clientId = in.readString();
    }

    private PaymentData(Builder builder) {
        clientId = builder.clientId;
    }

    /**
     * Gets clientId.
     *
     * @return Value of clientId.
     */
    public String getClientId() {
        return clientId;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(clientId);
    }

    public static class Builder {

        private String clientId;

        /**
         * Sets new clientId.
         *
         * @param clientId New value of clientId.
         */
        public Builder setClientId(String clientId) {
            this.clientId = clientId;

            return this;
        }

        /**
         * Build PaymentData with set params.
         *
         * @return new instance of PaymentData
         */
        public PaymentData build() {
            return new PaymentData(this);
        }

    }

}
