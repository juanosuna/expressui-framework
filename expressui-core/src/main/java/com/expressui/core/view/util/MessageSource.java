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
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;

import java.util.Locale;

/**
 * Provides access to internationalized messages (labels, tooltips, etc.) found in the following locations:
 * <ul>
 * <li>uiMessageSource bean (uiMessages/messages_*) - UI messages that are independent of domain entities or
 * business logic</li>
 * <li>domainMessageSource bean (domainMessages/messages_*) - messages that pertain to domain entities or business
 * logic</li>
 * <li>validationMessageSource bean (ValidationMessages_*) - messages that pertain to validation errors. Note that
 * Hibernate uses these files directly and ExpressUI also uses them for custom validation error messages as well as
 * for Vaadin validator errors that don't use JSR 303.</li>
 * </ul>
 * <p/>
 * All methods automatically apply the user's locale stored in the user's session.
 * <p/>
 * Note about UTF-8: by inheriting from ReloadableResourceBundleMessageSource, this message source automatically
 * reads UTF-8 encoded files. However, Hibernate relies on JDK resource bundle for loading the validation message files,
 * which doesn't support UTF-8. Therefore, all special characters in validation errors read directly by Hibernate must
 * be escaped. You can use Java's native2ascii command to do this.
 */
public class MessageSource extends ReloadableResourceBundleMessageSource implements BeanNameAware {

    private final Logger log = Logger.getLogger(getClass());

    private String beanName;

    @Override
    public void setBeanName(String name) {
        this.beanName = name;
    }

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
            MainApplication.getInstance().showError("Could not find property '" + code
                    + "' in " + beanName + ":" + toString() + ":" + getLocale());
            return code;
        }
    }

    /**
     * Gets optional message, meaning it may return null without error or warning.
     *
     * @param code property key to look up message
     * @return message value from messages_* file
     */
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
     * @param code property key to look up message
     * @param args passed for substitution in the message, {0}, {1}, {2}, etc.
     * @return message value from messages_* file
     */
    public String getMessage(String code, Object[] args) {
        try {
            return super.getMessage(code, args, getLocale());
        } catch (NoSuchMessageException e) {
            MainApplication.getInstance().showError("Could not find property '" + code
                    + "' in " + beanName + ":" + toString() + ":" + getLocale());
            return code;
        }
    }

    /**
     * Gets optional message, meaning it may return null without error or warning.
     *
     * @param code property key to look up message
     * @param args passed for substitution in the message, {0}, {1}, {2}, etc.
     * @return message value from messages_* file
     */
    public String getOptionalMessage(String code, Object[] args) {
        try {
            return super.getMessage(code, args, getLocale());
        } catch (NoSuchMessageException e) {
            return null;
        }
    }

    /**
     * Gets optional message using the system default locale rather than user locale.
     *
     * @param code property key to look up message
     * @param args passed for substitution in the message, {0}, {1}, {2}, etc.
     * @return message value from messages_* file
     */
    public String getOptionalMessageFromDefaultLocale(String code, Object[] args) {
        try {
            return super.getMessage(code, args, Locale.getDefault());
        } catch (NoSuchMessageException e) {
            return null;
        }
    }

    /**
     * Gets message.
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
     * Gets message wrapped with span with express-ui-toolTip style.
     *
     * @param code key
     * @return message value from messages_* file
     */
    public String getToolTip(String code) {
        return wrapWithToolTipStyle(getMessage(code));
    }

    /**
     * Gets message wrapped with span with express-ui-toolTip style. This may return null without error or warning.
     *
     * @param code key
     * @return message value from messages_* file
     */
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

    /**
     * Gets message wrapped with span with express-ui-toolTip style. This may return null without error or warning.
     *
     * @param args passed for substitution in the message, {0}, {1}, {2}, etc.
     * @param code key
     * @return message value from messages_* file
     */
    public String getOptionalToolTip(String code, Object[] args) {
        return wrapWithToolTipStyle(getOptionalMessage(code, args));
    }

    /**
     * Gets message wrapped with span with express-ui-toolTip style, using the system default locale rather
     * than user locale.
     *
     * @param args passed for substitution in the message, {0}, {1}, {2}, etc.
     * @param code key
     * @return message value from messages_* file
     */
    public String getOptionalToolTipFromDefaultLocale(String code, Object[] args) {
        return wrapWithToolTipStyle(getOptionalMessageFromDefaultLocale(code, args));
    }

    /**
     * Gets message wrapped with span with express-ui-toolTip style.
     *
     * @param code           key
     * @param defaultMessage message if property key not found in messages file
     * @return message value from messages_* file
     */
    public String getToolTip(String code, String defaultMessage) {
        return wrapWithToolTipStyle(getMessage(code, null, defaultMessage));
    }

    /**
     * Gets message wrapped with span with express-ui-toolTip style.
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
     * Gets the locale associated with Vaadin application. If Application has not been
     * initialized yet, gets JDK default locale.
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
