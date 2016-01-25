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
package com.ibuildapp.romanblack.CataloguePlugin.view;

import android.content.Context;
import android.graphics.Color;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ibuildapp.romanblack.CataloguePlugin.R;


public class SearchView extends LinearLayout implements TextWatcher {

    private EditText edit;
    private LinearLayout cancel;
    private LinearLayout root;
    private OnClickListener cancelClick;

    public SearchView(Context context) {
        super(context);
        init();
    }

    public SearchView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    /**
     * Sets {@link android.widget.TextView.OnEditorActionListener} for this view
     *
     * @param listener listener
     */
    public void setOnEditorActionListener(TextView.OnEditorActionListener listener) throws NullPointerException {
        if (listener == null)
            throw new NullPointerException("Listener is null!");

        edit.setOnEditorActionListener(listener);
    }

    /**
     * Requests focus to edit
     */
    public void myRequestFocus() {
        edit.requestFocus();
    }

    /**
     * Sets on cancel click listener
     *
     * @param cancelClick listener
     */
    public void setCancelClick(OnClickListener cancelClick) {
        this.cancelClick = cancelClick;
    }

    /**
     * Sets background value transparent
     */
    public void noBackground() {
        root.setBackgroundColor(Color.TRANSPARENT);
    }

    /**
     * Sets cursor position of edit
     */
    public void setSelection(int idx) {
        edit.setSelection(idx);
    }

    /**
     * Init view
     */
    private void init() {
        setGravity(Gravity.CENTER_VERTICAL);

        View v = LayoutInflater.from(getContext()).inflate(R.layout.search_view_layout, null);
        v.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        root = (LinearLayout) v.findViewById(R.id.search_root);

        edit = (EditText) v.findViewById(R.id.search);
        edit.addTextChangedListener(this);

        cancel = (LinearLayout) v.findViewById(R.id.cancel);
        cancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                edit.setText("");
                cancel.setVisibility(GONE);
                if (cancelClick != null)
                    cancelClick.onClick(cancel);
            }
        });


        addView(v);
    }

    /**
     * @return value of edit
     */
    public String getText() {
        return edit.getText().toString();
    }

    /**
     * Set text to edit
     *
     * @param text text
     */
    public void setText(String text) {
        edit.setText(text);
    }

    /**
     * Clear edit
     */
    public void clearText() {
        edit.setText("");
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {

    }

    @Override
    public void afterTextChanged(Editable editable) {
        if (editable.length() > 0)
            cancel.setVisibility(VISIBLE);
        else
            cancel.setVisibility(GONE);
    }
}