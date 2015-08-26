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

public class ShoppingCartFields {

    public Field firstName = new Field.Builder().setVisible(true).setRequired(false).setMultiline(false).build();
    public Field lastName = new Field.Builder().setVisible(true).setRequired(false).setMultiline(false).build();
    public Field emailAddress = new Field.Builder().setVisible(true).setRequired(false).setMultiline(false).build();
    public Field phone = new Field.Builder().setVisible(true).setRequired(false).setMultiline(false).build();
    public Field country = new Field.Builder().setVisible(true).setRequired(false).setMultiline(false).build();
    public Field streetAddress = new Field.Builder().setVisible(true).setRequired(false).setMultiline(false).build();
    public Field city = new Field.Builder().setVisible(true).setRequired(false).setMultiline(false).build();
    public Field state = new Field.Builder().setVisible(true).setRequired(false).setMultiline(false).build();
    public Field zipCode = new Field.Builder().setVisible(true).setRequired(false).setMultiline(false).build();
    public Field note = new Field.Builder().setVisible(true).setRequired(false).setMultiline(false).build();
    public String orderTitle = "";
    public String orderText = "";

    public static class Field {

        private final boolean required;
        private final boolean visible;
        private final boolean multiline;
        private final String hint;

        private Field(Builder builder) {
            required = builder.required;
            visible = builder.visible;
            multiline = builder.multiline;
            hint = builder.hint;
        }

        /**
         * Gets hint.
         *
         * @return Value of hint.
         */
        public String getHint() {
            return hint;
        }

        /**
         * Gets required.
         *
         * @return Value of required.
         */
        public boolean isRequired() {
            return required;
        }

        /**
         * Gets visible.
         *
         * @return Value of visible.
         */
        public boolean isVisible() {
            return visible;
        }

        /**
         * Gets multiline.
         *
         * @return Value of multiline.
         */
        public boolean isMultiline() {
            return multiline;
        }

        public static class Builder {

            private boolean required;
            private boolean visible;
            private boolean multiline;
            private String hint;

            /**
             * Sets new visible.
             *
             * @param visible New value of visible.
             */
            public Builder setVisible(boolean visible) {
                this.visible = visible;

                return this;
            }

            /**
             * Sets new multiline.
             *
             * @param multiline New value of multiline.
             */
            public Builder setMultiline(boolean multiline) {
                this.multiline = multiline;

                return this;
            }

            /**
             * Sets new required.
             *
             * @param required New value of required.
             */
            public Builder setRequired(boolean required) {
                this.required = required;

                return this;
            }

            /**
             * Sets new hint.
             *
             * @param hint New value of hint.
             */
            public Builder setHint(String hint) {
                this.hint = hint;

                return this;
            }

            /**
             * Build Field with set params.
             *
             * @return new instance of Field
             */
            public Field build() {
                return new Field(this);
            }

        }

    }

}
