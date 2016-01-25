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

public class UserProfile {

    private final String firstName;
    private final String lastName;
    private final String emailAddress;
    private final String phone;
    private final String country;
    private final String streetAddress;
    private final String city;
    private final String state;
    private final String zipCode;
    private final String note;

    private UserProfile(Builder builder) {
        firstName = builder.firstName;
        lastName = builder.lastName;
        emailAddress = builder.emailAddress;
        phone = builder.phone;
        country = builder.country;
        streetAddress = builder.streetAddress;
        city = builder.city;
        state = builder.state;
        zipCode = builder.zipCode;
        note = builder.note;
    }


    /**
     * Gets lastName.
     *
     * @return Value of lastName.
     */
    public String getLastName() {
        return lastName;
    }

    /**
     * Gets emailAddress.
     *
     * @return Value of emailAddress.
     */
    public String getEmailAddress() {
        return emailAddress;
    }

    /**
     * Gets note.
     *
     * @return Value of note.
     */
    public String getNote() {
        return note;
    }

    /**
     * Gets country.
     *
     * @return Value of country.
     */
    public String getCountry() {
        return country;
    }

    /**
     * Gets phone.
     *
     * @return Value of phone.
     */
    public String getPhone() {
        return phone;
    }

    /**
     * Gets firstName.
     *
     * @return Value of firstName.
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * Gets zipCode.
     *
     * @return Value of zipCode.
     */
    public String getZipCode() {
        return zipCode;
    }

    /**
     * Gets city.
     *
     * @return Value of city.
     */
    public String getCity() {
        return city;
    }

    /**
     * Gets streetAddress.
     *
     * @return Value of streetAddress.
     */
    public String getStreetAddress() {
        return streetAddress;
    }

    /**
     * Gets state.
     *
     * @return Value of state.
     */
    public String getState() {
        return state;
    }

    public static class Builder {

        private String firstName;
        private String lastName;
        private String emailAddress;
        private String phone;
        private String country;
        private String streetAddress;
        private String city;
        private String state;
        private String zipCode;
        private String note;

        /**
         * Sets new firstName.
         *
         * @param firstName New value of firstName.
         */
        public Builder setFirstName(String firstName) {
            this.firstName = firstName;

            return this;
        }

        /**
         * Sets new phone.
         *
         * @param phone New value of phone.
         */
        public Builder setPhone(String phone) {
            this.phone = phone;

            return this;
        }

        /**
         * Sets new country.
         *
         * @param country New value of country.
         */
        public Builder setCountry(String country) {
            this.country = country;

            return this;
        }

        /**
         * Sets new emailAddress.
         *
         * @param emailAddress New value of emailAddress.
         */
        public Builder setEmailAddress(String emailAddress) {
            this.emailAddress = emailAddress;

            return this;
        }

        /**
         * Sets new zipCode.
         *
         * @param zipCode New value of zipCode.
         */
        public Builder setZipCode(String zipCode) {
            this.zipCode = zipCode;

            return this;
        }

        /**
         * Sets new city.
         *
         * @param city New value of city.
         */
        public Builder setCity(String city) {
            this.city = city;

            return this;
        }

        /**
         * Sets new note.
         *
         * @param note New value of note.
         */
        public Builder setNote(String note) {
            this.note = note;

            return this;
        }

        /**
         * Sets new state.
         *
         * @param state New value of state.
         */
        public Builder setState(String state) {
            this.state = state;

            return this;
        }

        /**
         * Sets new streetAddress.
         *
         * @param streetAddress New value of streetAddress.
         */
        public Builder setStreetAddress(String streetAddress) {
            this.streetAddress = streetAddress;

            return this;
        }

        /**
         * Sets new lastName.
         *
         * @param lastName New value of lastName.
         */
        public Builder setLastName(String lastName) {
            this.lastName = lastName;

            return this;
        }

        /**
         * Build UserProfile with set params.
         *
         * @return new instance of UserProfile
         */
        public UserProfile build() {
            return new UserProfile(this);
        }

    }

}
