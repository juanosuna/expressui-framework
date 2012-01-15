/*
 * Copyright (c) 2011 Brown Bag Consulting.
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

package com.expressui.sample.util.validator;

import com.expressui.sample.entity.Phone;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class PhoneValidator implements ConstraintValidator<ValidPhone, Phone> {

    private ValidPhone validPhone;

    @Override
    public void initialize(ValidPhone constraintAnnotation) {
        validPhone = constraintAnnotation;
    }

    @Override
    public boolean isValid(Phone phone, ConstraintValidatorContext context) {
        if (phone == null) return true;

        Phonenumber.PhoneNumber phoneNumber = new Phonenumber.PhoneNumber();
        phoneNumber.setCountryCode(phone.getCountryCode());
        phoneNumber.setNationalNumber(phone.getPhoneNumber());

        PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
        PhoneNumberUtil.ValidationResult result = phoneUtil.isPossibleNumberWithReason(phoneNumber);
        if (result == PhoneNumberUtil.ValidationResult.IS_POSSIBLE) {
            if (phoneUtil.isValidNumber(phoneNumber)) {
                return true;
            } else {
                String regionCode = phoneUtil.getRegionCodeForNumber(phoneNumber);
                if (regionCode == null) {
                    regionCode = validPhone.defaultRegionCode();
                }
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate(
                        "Invalid phone number for region: " + regionCode
                                + ". Use format: " + getExampleNumber(regionCode)).addConstraintViolation();
                return false;
            }
        } else {
            String message = null;
            String regionCode = phoneUtil.getRegionCodeForNumber(phoneNumber);
            if (regionCode == null) {
                regionCode = validPhone.defaultRegionCode();
            }

            switch (result) {
                case INVALID_COUNTRY_CODE:
                    message = "Invalid region code: " + regionCode;
                    break;
                case TOO_SHORT:
                    message = "Phone number invalid for region: " + regionCode
                            + ". Use format: " + getExampleNumber(regionCode);
                    break;
                case TOO_LONG:
                    message = "Phone number invalid for region: " + regionCode
                            + ". Use format: " + getExampleNumber(regionCode);
                    break;
            }
            if (message != null) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate(message).addConstraintViolation();
            }
            return false;
        }
    }

    public String getExampleNumber(String regionCode) {
        return getExampleNumber(validPhone.defaultRegionCode(), regionCode);
    }

    public static String getExampleNumber(String defaultRegionCode, String regionCode) {
        if (regionCode == null) {
            regionCode = defaultRegionCode;
        }
        PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
        Phonenumber.PhoneNumber example = phoneUtil.getExampleNumber(regionCode);
        PhoneNumberUtil.PhoneNumberFormat format;
        if (regionCode.equals(defaultRegionCode)) {
            format = PhoneNumberUtil.PhoneNumberFormat.NATIONAL;
        } else {
            format = PhoneNumberUtil.PhoneNumberFormat.INTERNATIONAL;
        }

        return phoneUtil.format(example, format);
    }
}
