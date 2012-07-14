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

package com.expressui.core.view.field.format;

import com.expressui.core.MainApplication;
import com.expressui.core.util.ApplicationProperties;
import com.vaadin.data.util.PropertyFormatter;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * Defines some default formats, e.g. for dates, times, numbers
 */
@Component
public class DefaultFormats {

    @Resource
    private ApplicationProperties applicationProperties;

    public PropertyFormatter getEmptyFormat() {
        return new EmptyPropertyFormatter();
    }

    public PropertyFormatter getCurrencyFormat(int maximumFractionDigits) {
        return getCurrencyFormat(MainApplication.getInstance().getLocale(), maximumFractionDigits);
    }

    public PropertyFormatter getCurrencyFormat(Locale locale, int maximumFractionDigits) {
        NumberFormat numberFormat = NumberFormat.getCurrencyInstance(locale);
        numberFormat.setMaximumFractionDigits(maximumFractionDigits);
        return new JDKBridgePropertyFormatter(numberFormat);
    }

    /**
     * Get default number format, NumberFormat.getNumberInstance()
     *
     * @return default number format
     */
    public PropertyFormatter getNumberFormat() {
        return new JDKBridgePropertyFormatter(
                NumberFormat.getNumberInstance(MainApplication.getInstance().getLocale())
        );
    }

    /**
     * Get default number format, NumberFormat.getNumberInstance()
     *
     * @param defaultValueWhenEmpty when parsing Strings, returns this value when String is empty
     * @return default number format
     */
    public PropertyFormatter getNumberFormat(int maximumFractionDigits, Object defaultValueWhenEmpty) {
        NumberFormat numberFormat = NumberFormat.getNumberInstance(MainApplication.getInstance().getLocale());
        numberFormat.setMaximumFractionDigits(maximumFractionDigits);
        return new JDKBridgePropertyFormatter(numberFormat, defaultValueWhenEmpty);
    }

    /**
     * Get default date format.
     *
     * @return default date format
     */
    public PropertyFormatter getDateFormat() {
        return new JDKBridgePropertyFormatter(
                DateFormat.getDateInstance(applicationProperties.getDefaultDateStyle(),
                        MainApplication.getInstance().getLocale())
        );
    }

    /**
     * Get default date-time format.
     *
     * @return default date-time format
     */
    public PropertyFormatter getDateTimeFormat() {
        return new JDKBridgePropertyFormatter(
                DateFormat.getDateTimeInstance(applicationProperties.getDefaultDateStyle(),
                        applicationProperties.getDefaultTimeStyle(),
                        MainApplication.getInstance().getLocale())
        );
    }
}
