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

import com.vaadin.data.util.PropertyFormatter;
import org.springframework.stereotype.Component;

import java.text.Format;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;

/**
 * Defines some default formats, e.g. for dates, times, numbers
 */
@Component
public class DefaultFormats {

    private PropertyFormatter emptyFormat = new EmptyPropertyFormatter();
    private Format numberFormat = NumberFormat.getNumberInstance();
    private Format dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private Format dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    public PropertyFormatter getEmptyFormat() {
        return emptyFormat;
    }

    /**
     * Get default number format, NumberFormat.getNumberInstance()
     *
     * @return default number format
     */
    public PropertyFormatter getNumberFormat() {
        return new JDKBridgePropertyFormatter(numberFormat);
    }

    /**
     * Get default number format, NumberFormat.getNumberInstance()
     * @param defaultValueWhenEmpty when parsing Strings, returns this value when String is empty
     *
     * @return default number format
     */
    public PropertyFormatter getNumberFormat(Object defaultValueWhenEmpty) {
        return new JDKBridgePropertyFormatter(numberFormat, defaultValueWhenEmpty);
    }

    /**
     * Set default number format
     *
     * @param numberFormat default number format
     */
    public void setNumberFormat(Format numberFormat) {
        this.numberFormat = numberFormat;
    }

    /**
     * Get default date format.
     *
     * @return default date format
     */
    public PropertyFormatter getDateFormat() {
        return new JDKBridgePropertyFormatter(dateFormat);
    }

    /**
     * Set default date format
     *
     * @param dateFormat default date format
     */
    public void setDateFormat(Format dateFormat) {
        this.dateFormat = dateFormat;
    }

    /**
     * Get default date-time format.
     *
     * @return default date-time format
     */
    public PropertyFormatter getDateTimeFormat() {
        return new JDKBridgePropertyFormatter(dateTimeFormat);
    }

    /**
     * Set default date-time format.
     *
     * @param dateTimeFormat default date-time format
     */
    public void setDateTimeFormat(Format dateTimeFormat) {
        this.dateTimeFormat = dateTimeFormat;
    }
}
