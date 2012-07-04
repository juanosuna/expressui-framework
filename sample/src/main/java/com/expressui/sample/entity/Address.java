/*
 * Copyright (c) 2012 Brown Bag Consulting.
 * This file is part of the ExpressUI project.
 * Author: Juan Osuna
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License Version 3
 * as published by the Free Software Foundation with the addition of the
 * following permission added to Section 15 as permitted in Section 7(a):
 * FOR ANY PART OF THE COVERED WORK IN WHICH THE COPYRIGHT IS OWNED BY
 * Brown Bag Consulting, Brown Bag Consulting DISCLAIMS THE WARRANTY OF
 * NON INFRINGEMENT OF THIRD PARTY RIGHTS.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * The interactive user interfaces in modified source and object code versions
 * of this program must display Appropriate Legal Notices, as required under
 * Section 5 of the GNU Affero General Public License.
 *
 * You can be released from the requirements of the license by purchasing
 * a commercial license. Buying such a license is mandatory as soon as you
 * develop commercial activities involving the ExpressUI software without
 * disclosing the source code of your own applications. These activities
 * include: offering paid services to customers as an ASP, providing
 * services from a web application, shipping ExpressUI with a closed
 * source product.
 *
 * For more information, please contact Brown Bag Consulting at this
 * address: juan@brownbagconsulting.com.
 */

package com.expressui.sample.entity;


import com.expressui.core.entity.WritableEntity;
import com.expressui.core.validation.AssertTrueForProperties;
import org.hibernate.annotations.ForeignKey;
import org.hibernate.annotations.Index;
import org.hibernate.validator.constraints.NotBlank;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import static com.expressui.core.util.StringUtil.isEmpty;
import static com.expressui.core.util.StringUtil.isEqual;

@Entity
@Table
public class Address extends WritableEntity {

    @Enumerated(EnumType.STRING)
    private AddressType addressType;

    private String street;

    private String city;

    private String zipCode;

    @Index(name = "IDX_ADDRESS_STATE")
    @ForeignKey(name = "FK_ADDRESS_STATE")
    @ManyToOne(fetch = FetchType.LAZY)
    private State state;

    @Index(name = "IDX_ADDRESS_COUNTRY")
    @ForeignKey(name = "FK_ADDRESS_COUNTRY")
    @ManyToOne(fetch = FetchType.LAZY)
    private Country country = new Country("US");

    public Address() {
    }

    public Address(AddressType addressType) {
        this.addressType = addressType;
    }

    @NotNull
    public AddressType getAddressType() {
        return addressType;
    }

    public void setAddressType(AddressType type) {
        this.addressType = type;
    }

    @NotNull
    @NotBlank
    @Size(min = 1, max = 32)
    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    @NotNull
    @NotBlank
    @Size(min = 1, max = 32)
    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getZipCode() {
        return zipCode;
    }

    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }

    @AssertTrueForProperties(errorProperty = "zipCode", message = "US zip code must be 5 or 9 digits")
    public boolean isUsZipCodeValid() {
        if (!isEmpty(getZipCode()) && isCountryId("US")) {
            return getZipCode().matches("^\\d{5}$|^\\d{5}$");
        } else {
            return true;
        }
    }

    @AssertTrueForProperties(errorProperty = "zipCode", message = "CA zip code must be have the format: A0A 0A0")
    public boolean isCaZipCodeValid() {
        if (!isEmpty(getZipCode()) && isCountryId("CA")) {
            return getZipCode().matches("^[a-zA-Z]\\d[a-zA-Z] \\d[a-zA-Z]\\d$");
        } else {
            return true;
        }
    }

    @AssertTrueForProperties(errorProperty = "zipCode", message = "Zip code invalid for selected country")
    public boolean isZipCodeValidForCountry() {
        if (!isEmpty(getZipCode()) && getCountry() != null && !isCountryId("US", "CA")) {
            return getCountry().isZipCodeValid(getZipCode());
        } else {
            return true;
        }
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    @AssertTrueForProperties(errorProperty = "state", message = "State is required for selected country")
    public boolean isStateValid() {
        if (getCountry() != null && isEqual(getCountry().getId(), "US", "CA", "MX", "AU")) {
            return getState() != null;
        } else {
            return true;
        }
    }

    @NotNull
    public Country getCountry() {
        return country;
    }

    public void setCountry(Country country) {
        this.country = country;
    }

    public boolean isCountryId(String... countryId) {
        if (getCountry() != null) {
            for (String id : countryId) {
                if (getCountry().getId().equals(id)) {
                    return true;
                }
            }
        }

        return false;
    }

    public String getFormatted() {
        String formattedAddress = getStreet() + ", " + getCity() + ",";
        if (getState() != null) {
            formattedAddress += getState().getCode() + ", ";
        }
        formattedAddress += getZipCode() + ", ";
        formattedAddress += getCountry().getDisplayName();

        return formattedAddress;
    }

    @Override
    public String toString() {
        return getFormatted();
    }
}