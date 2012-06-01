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

package com.expressui.core.view.util;

import com.expressui.core.MainApplication;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;

import java.util.Locale;

/**
 * Provides access to internationalized messages.
 * <p/>
 * Subclasses Spring's ReloadableResourceBundleMessageSource and adds a few convenient
 * overloaded methods.
 */
public class MessageSource extends ReloadableResourceBundleMessageSource {

    public String getMessageWithDefault(String code) {
        return getMessage(code, null, code);
    }

    public String getMessage(String code) {
        return getMessage(code, null, null, getLocale());
    }

    public String getMessage(String code, Object[] args) {
        return getMessage(code, args, code);
    }

    public String getMessage(String code, String defaultMessage) {
        return getMessage(code, null, defaultMessage);
    }

    public String getMessage(String code, Object[] args, String defaultMessage) {
        return getMessage(code, args, defaultMessage, getLocale());
    }

    /**
     * Get the locale associated with Vaadin application. If Application has not been
     * initialized yet, get JDK default locale.
     *
     * @return current locale
     */
    public Locale getLocale() {
        Locale locale;
        if (MainApplication.getInstance() == null) {
            locale = Locale.getDefault();
        } else {
            locale = MainApplication.getInstance().getLocale();
        }

        return locale;
    }
}
