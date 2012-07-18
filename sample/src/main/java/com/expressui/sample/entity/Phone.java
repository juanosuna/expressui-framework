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

import com.expressui.core.util.SpringApplicationContext;
import com.expressui.core.view.util.MessageSource;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;

import javax.annotation.Resource;
import javax.persistence.Embeddable;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

@SuppressWarnings("serial")
@Embeddable
public class Phone implements Serializable {

    @Resource
    @Transient
    private MessageSource validationMessageSource;

    private Integer countryCode;
    private Long phoneNumber;

    public Phone() {
        SpringApplicationContext.autowire(this);
    }

    public Phone(String fullNumber, String defaultRegionCode) throws NumberParseException {
        this();
        if (fullNumber.matches(".*[a-zA-Z]+.*")) {
            String message = validationMessageSource.getMessage("com.expressui.sample.entity.Phone.phoneNumberWithLetters");
            throw new NumberParseException(NumberParseException.ErrorType.NOT_A_NUMBER, message);
        }

        PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
        Phonenumber.PhoneNumber phoneNumber = phoneUtil.parse(fullNumber, defaultRegionCode);

        this.countryCode = phoneNumber.getCountryCode();
        this.phoneNumber = phoneNumber.getNationalNumber();
    }

    public String getFormatted(String defaultRegionCode) {
        Phonenumber.PhoneNumber phoneNumber = new Phonenumber.PhoneNumber();
        phoneNumber.setCountryCode(countryCode);
        phoneNumber.setNationalNumber(this.phoneNumber);

        PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
        return phoneUtil.format(phoneNumber, PhoneNumberUtil.PhoneNumberFormat.INTERNATIONAL);
    }

    @NotNull
    public Integer getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(Integer countryCode) {
        this.countryCode = countryCode;
    }

    @NotNull
    public Long getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(Long phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public static String getExampleNumber(String regionCode) {

        Phonenumber.PhoneNumber phoneNumber = PhoneNumberUtil.getInstance().getExampleNumber(regionCode);

        PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
        PhoneNumberUtil.PhoneNumberFormat format;
        format = PhoneNumberUtil.PhoneNumberFormat.INTERNATIONAL;

        return phoneUtil.format(phoneNumber, format);
    }

    @Override
    public String toString() {
        return getFormatted("US");
    }
}
