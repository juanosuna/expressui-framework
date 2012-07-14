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
import org.apache.log4j.Logger;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;

import java.util.Locale;

/**
 * Provides access to internationalized messages in messages_* files. Automatically uses
 * the locale set in the user's session.
 * <p/>
 * Subclasses Spring's ReloadableResourceBundleMessageSource and adds a few convenient
 * overloaded methods.
 */
public class MessageSource extends ReloadableResourceBundleMessageSource {

    private final Logger log = Logger.getLogger(getClass());

    /**
     * Gets message, use code itself if message not found.
     *
     * @param code property key to look up message
     * @return message value from messages_* file or code itself, if not found
     */
    public String getMessageWithDefault(String code) {
        return getMessage(code, null, code);
    }

    /**
     * Gets message.
     *
     * @param code property key to look up message
     * @return message value from messages_* file
     */
    public String getMessage(String code) {
        try {
            return super.getMessage(code, null, getLocale());
        } catch (NoSuchMessageException e) {
            log.warn(e);
            return null;
        }
    }

    public String getOptionalMessage(String code) {
        try {
            return super.getMessage(code, null, getLocale());
        } catch (NoSuchMessageException e) {
            return null;
        }
    }

    /**
     * Gets message, use code itself if message not found.
     *
     * @param code property key to look up message
     * @param args passed for substitution in the message, {0}, {1}, {2}, etc.
     * @return message value from messages_* file
     */
    public String getMessageWithDefault(String code, Object[] args) {
        return getMessage(code, args, code);
    }


    /**
     * Gets message.
     *
     * @param code           property key to look up message
     * @param args           passed for substitution in the message, {0}, {1}, {2}, etc.
     * @return message value from messages_* file
     */
    public String getMessage(String code, Object[] args) {
        try {
            return super.getMessage(code, args, getLocale());
        } catch (NoSuchMessageException e) {
            log.warn(e);
            return null;
        }
    }

    public String getOptionalMessage(String code, Object[] args) {
        try {
            return super.getMessage(code, args, getLocale());
        } catch (NoSuchMessageException e) {
            return null;
        }
    }

    public String getOptionalMessageFromDefaultLocale(String code, Object[] args) {
        try {
            return super.getMessage(code, args, Locale.getDefault());
        } catch (NoSuchMessageException e) {
            return null;
        }
    }

    /**
     * Gets message
     *
     * @param code           property key to look up message
     * @param defaultMessage message if property key not found in messages file
     * @return message value from messages_* file
     */
    public String getMessage(String code, String defaultMessage) {
        return getMessage(code, null, defaultMessage);
    }

    /**
     * Get message.
     *
     * @param code           property key to look up message
     * @param args           passed for substitution in the message, {0}, {1}, {2}, etc.
     * @param defaultMessage message if property key not found in messages file
     * @return message value from messages_* file
     */
    public String getMessage(String code, Object[] args, String defaultMessage) {
        return super.getMessage(code, args, defaultMessage, getLocale());
    }

    /**
     * Gets message wrapped with span with express-ui-toolTip style
     *
     * @param code key
     * @return message value from messages_* file
     */
    public String getToolTip(String code) {
        return wrapWithToolTipStyle(getMessage(code));
    }

    public String getOptionalToolTip(String code) {
        return wrapWithToolTipStyle(getOptionalMessage(code));
    }

    /**
     * Gets message wrapped with span with express-ui-toolTip style
     *
     * @param args passed for substitution in the message, {0}, {1}, {2}, etc.
     * @param code key
     * @return message value from messages_* file
     */
    public String getToolTip(String code, Object[] args) {
        return wrapWithToolTipStyle(getMessage(code, args));
    }

    public String getOptionalToolTip(String code, Object[] args) {
        return wrapWithToolTipStyle(getOptionalMessage(code, args));
    }

    public String getOptionalToolTipFromDefaultLocale(String code, Object[] args) {
        return wrapWithToolTipStyle(getOptionalMessageFromDefaultLocale(code, args));
    }

    /**
     * Gets message wrapped with span with express-ui-toolTip style
     *
     * @param code           key
     * @param defaultMessage message if property key not found in messages file
     * @return message value from messages_* file
     */
    public String getToolTip(String code, String defaultMessage) {
        return wrapWithToolTipStyle(getMessage(code, null, defaultMessage));
    }

    /**
     * Gets message wrapped with span with express-ui-toolTip style
     *
     * @param code key
     * @return message value from messages_* file
     */
    public String getToolTip(String code, Object[] args, String defaultMessage) {
        return wrapWithToolTipStyle(getMessage(code, args, defaultMessage, getLocale()));
    }

    private static String wrapWithToolTipStyle(String message) {
        return message == null ? null : "<span class=\"expressui-tool-tip\">" + message + "</span>";
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
