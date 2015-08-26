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

import android.text.InputFilter;
import android.text.Spanned;

/**
 * Custom edittext input filter.
 * Filters numbers less then min and greater then max
 */
public class InputFilterMinMax implements InputFilter {

    private int min, max;

    /**
     * Constructor
     *
     * @param min - min value of edit text
     * @param max - max value of edit text
     */
    public InputFilterMinMax(int min, int max) {
        this.min = min;
        this.max = max;
    }

    /**
     * Constructor
     *
     * @param min - min value of edit text
     * @param max - max value of edit text
     */
    public InputFilterMinMax(String min, String max) {
        this.min = Integer.parseInt(min);
        this.max = Integer.parseInt(max);
    }

    @Override
    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
        try {
            // Remove the string out of destination that is to be replaced
            String newVal = dest.toString().substring(0, dstart) + dest.toString().substring(dend, dest.toString().length());
            // Add the new string in
            newVal = newVal.substring(0, dstart) + source.toString() + newVal.substring(dstart, newVal.length());
            int input = Integer.parseInt(newVal);
            if (isInRange(min, max, input)) {
                return null;
            }
        } catch (NumberFormatException nfe) {
        }
        return "";
    }

    /**
     * Detect if c in range between a and b
     *
     * @param a - min value of edit text
     * @param b - max value of edit text
     * @param c - value to detect in range
     * @return true if in range and false otherwise
     */
    private boolean isInRange(int a, int b, int c) {
        return b > a ? c >= a && c <= b : c >= b && c <= a;
    }
}
