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


import com.expressui.core.entity.ReferenceEntity;
import com.expressui.core.view.util.MessageSource;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.ForeignKey;
import org.hibernate.annotations.Index;

import javax.annotation.Resource;
import javax.persistence.*;

import static com.expressui.core.entity.ReferenceEntity.READ_ONLY_CACHE;

@Entity
@Table
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE, region = READ_ONLY_CACHE)
public class Country extends ReferenceEntity {


    @Resource
    @Transient
    private MessageSource domainMessageSource;

    private String countryType;
    private String minPostalCode;
    private String maxPostalCode;

    @Index(name = "IDX_COUNTRY_CURRENCY")
    @ForeignKey(name = "FK_COUNTRY_CURRENCY")
    @ManyToOne(fetch = FetchType.LAZY)
    private Currency currency;

    public Country() {
    }

    public Country(String id) {
        super(id);
    }

    public Country(String id, String name) {
        super(id, name);
    }

    public String getCountryType() {
        return countryType;
    }

    public void setCountryType(String countryType) {
        this.countryType = countryType;
    }

    public String getMinPostalCode() {
        return minPostalCode;
    }

    public void setMinPostalCode(String minPostalCode) {
        this.minPostalCode = minPostalCode;
    }

    public String getMaxPostalCode() {
        return maxPostalCode;
    }

    public void setMaxPostalCode(String maxPostalCode) {
        this.maxPostalCode = maxPostalCode;
    }

    public Currency getCurrency() {
        return currency;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
    }

    public boolean isZipCodeValid(String zipCode) {
        if (getMinPostalCode() != null) {
            String minRegex = "^";
            char[] chars = getMinPostalCode().toCharArray();
            for (Character aChar : chars) {
                if (aChar.toString().matches("\\d")) {
                    minRegex += "\\d";
                } else if (aChar.toString().matches("\\w")) {
                    minRegex += "\\w";
                } else if (aChar.toString().matches("\\s")) {
                    minRegex += "\\s";
                } else {
                    minRegex += aChar;
                }
            }
            minRegex += "$";

            if (!zipCode.matches(minRegex) || zipCode.compareTo(getMinPostalCode()) < 0) {
                return false;
            }
        }

        if (getMaxPostalCode() != null) {
            String maxRegex = "^";
            char[] chars = getMaxPostalCode().toCharArray();
            for (Character aChar : chars) {
                if (aChar.toString().matches("\\d")) {
                    maxRegex += "\\d";
                } else if (aChar.toString().matches("\\w")) {
                    maxRegex += "\\w";
                } else if (aChar.toString().matches("\\s")) {
                    maxRegex += "\\s";
                } else {
                    maxRegex += aChar;
                }
            }
            maxRegex += "$";

            if (!zipCode.matches(maxRegex) || zipCode.compareTo(getMaxPostalCode()) > 0) {
                return false;
            }
        }

        return true;
    }

    public String getZipCodeToolTip() {
        if (getMinPostalCode() != null && getMaxPostalCode() != null) {
            return domainMessageSource.getMessage("zipCode") +
                    "<ul>" +
                    "  <li>" + getMinPostalCode() + " - " + getMaxPostalCode() + "</li>" +
                    "</ul>";
        } else {
            return null;
        }
    }
}
