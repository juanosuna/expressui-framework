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

package com.expressui.core.util;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * API access to application.properties.
 */
@Configuration
public class ApplicationProperties {

    @Value("${http.proxyHost:}")
    private String httpProxyHost;

    @Value("${http.proxyPort:}")
    private Integer httpProxyPort;

    @Value("${http.proxyUsername:}")
    private String httpProxyUsername;

    @Value("${http.proxyPassword:}")
    private String httpProxyPassword;

    @Value("${expressui.restartApplicationUrl}")
    private String restartApplicationUrl;

    @Value("${expressui.codePopupEnabled:false}")
    private boolean codePopupEnabled = false;

    @Value("${expressui.baseCodeUrl:}")
    private String baseCodeUrl;

    @Value("${expressui.baseDocUrl:}")
    private String baseDocUrl;

    @Value("${expressui.sessionTimeout:30}")
    private Integer sessionTimeout = 30;

    @Value("${expressui.sessionTimeoutWarning:5}")
    private Integer sessionTimeoutWarning = 5;

    @Value("${expressui.defaultDateStyle:3}")
    private Integer defaultDateStyle = DateFormat.SHORT;

    @Value("${expressui.defaultTimeStyle:3}")
    private Integer defaultTimeStyle = DateFormat.SHORT;

    @Value("${expressui.defaultTextFieldWidth:1}")
    private Integer defaultTextFieldWidth = 11;

    @Value("${expressui.defaultSelectFieldWidth:1}")
    private Integer defaultSelectFieldWidth = 11;

    @Value("${expressui.pageSizeOptions:5,10,25,50,100}")
    private String pageSizeOptions = "5,10,25,50,100";

    @Value("${expressui.defaultPageSize:10}")
    private Integer defaultPageSize = 10;

    @Value("${expressui.defaultToManyPageSize:5}")
    private Integer defaultToManyPageSize = 5;

    @Value("${expressui.defaultSelectPageSize:10}")
    private Integer defaultSelectPageSize = 10;


    /**
     * Gets the HTTP proxy hostname.
     *
     * @return HTTP proxy hostname
     */
    public String getHttpProxyHost() {
        return httpProxyHost;
    }

    /**
     * Gets the HTTP proxy port.
     *
     * @return HTTP proxy port
     */
    public Integer getHttpProxyPort() {
        return httpProxyPort;
    }

    /**
     * Gets the HTTP proxy username.
     *
     * @return HTTP username
     */
    public String getHttpProxyUsername() {
        return httpProxyUsername;
    }

    /**
     * Gets the HTTP proxy password.
     *
     * @return HTTP password
     */
    public String getHttpProxyPassword() {
        return httpProxyPassword;
    }

    /**
     * Gets URL for restarting the app and creating a new session, for example upon logout or when there is fatal
     * communication error.
     *
     * @return URL for restarting the app
     */
    public String getRestartApplicationUrl() {
        return restartApplicationUrl;
    }

    /**
     * Asks if code popups should be displayed. This should only be set true for demo applications.
     * Default is false.
     *
     * @return true if code popups should be displayed
     */
    public boolean isCodePopupEnabled() {
        return codePopupEnabled;
    }

    /**
     * Gets base URL for fetching code to be displayed in popups. This should only be set true for demo applications.
     *
     * @return base URL
     */
    public String getBaseCodeUrl() {
        return baseCodeUrl;
    }

    /**
     * Gets the URL for fetching source code for the given class. This should only be set true for demo applications.
     *
     * @param clazz class to fetch source code for
     * @return URL
     */
    public String getCodeUrl(Class clazz) {
        String url = baseCodeUrl;
        url = url.replace("\\", "/");
        if (!baseCodeUrl.endsWith("/")) {
            url += "/";
        }

        String outerClassName;
        int dollarIndex = clazz.getName().indexOf("$");
        if (dollarIndex >= 0) {
            outerClassName = clazz.getName().substring(0, dollarIndex);
        } else {
            outerClassName = clazz.getName();
        }

        return url + outerClassName.replace(".", "/") + ".java";
    }

    /**
     * Gets base URL for fetching Javadoc to be displayed in popups. This should only be set true for demo applications.
     *
     * @return base URL
     */
    public String getBaseDocUrl() {
        return baseDocUrl;
    }

    /**
     * Gets the URL for fetching Javadoc for the given class. This should only be set true for demo applications.
     *
     * @param clazz class to fetch Javadoc for
     * @return URL
     */
    public String getDocUrl(Class clazz) {
        String url = baseDocUrl;
        url = url.replace("\\", "/");
        if (!baseDocUrl.endsWith("/")) {
            url += "/";
        }
        return url + clazz.getName().replace(".", "/").replace("$", ".") + ".html";
    }

    /**
     * Gets the session timeout period in minutes. Default is 30 minutes.
     *
     * @return session timeout period in minutes
     */
    public Integer getSessionTimeout() {
        return sessionTimeout;
    }

    /**
     * Gets the warning period in minutes before session expiration. Default is 5 minutes.
     *
     * @return session warning timeout period in minutes
     */
    public Integer getSessionTimeoutWarning() {
        return sessionTimeoutWarning;
    }

    /**
     * Gets default style for displaying dates, as defined in java.text.DateFormat
     * # Options are: FULL=0, LONG=1, MEDIUM=2, SHORT=3. Default is 3 or SHORT.
     *
     * @return default date style
     */
    public Integer getDefaultDateStyle() {
        return defaultDateStyle;
    }

    /**
     * Gets default style for displaying times, as defined in java.text.DateFormat
     * # Options are: FULL=0, LONG=1, MEDIUM=2, SHORT=3. Default is 3 or SHORT.
     *
     * @return default time style
     */
    public Integer getDefaultTimeStyle() {
        return defaultTimeStyle;
    }

    /**
     * Gets default text field width in EM, which is used only if automatically adjustment is turned off and
     * a programmatic setting is not used.
     */
    public Integer getDefaultTextFieldWidth() {
        return defaultTextFieldWidth;
    }

    /**
     * Gets default select field width in EM, which is used only if automatically adjustment is turned off and
     * a programmatic setting is not used.
     */
    public Integer getDefaultSelectFieldWidth() {
        return defaultSelectFieldWidth;
    }

    /**
     * Gets the available page-size options for results components.
     * Default is 5,10,25,50,100.
     *
     * @return available page-size options for results components
     */
    public List<Integer> getPageSizeOptions() {
        String[] pageSizeStrings = StringUtils.split(pageSizeOptions, ",");
        List<Integer> pageSizes = new ArrayList<Integer>();
        for (String pageSizeString : pageSizeStrings) {
            Integer pageSize = Integer.parseInt(pageSizeString.trim());
            pageSizes.add(pageSize);
        }

        return pageSizes;
    }

    /**
     * Gets default page size for results.
     * @return default page size for results
     */
    public Integer getDefaultPageSize() {
        return defaultPageSize;
    }

    /**
     * Gets default page size for results in to-many relationship.
     * @return default page size for results in to-many relationship
     */
    public Integer getDefaultToManyPageSize() {
        return defaultToManyPageSize;
    }

    /**
     * Gets default page size for results in popup entity select
     * @return default page size for results in popup entity select
     */
    public Integer getDefaultSelectPageSize() {
        return defaultSelectPageSize;
    }

    /**
     * Lifecycle method called after bean is constructed. Sets http.proxyHost and http.proxyPort system property
     * and sets a proxy authenticator if httpProxyUsername and httpProxyPassword are not empty.
     */
    @PostConstruct
    public void postConstruct() {
        if (!StringUtil.isEmpty(httpProxyHost)) {
            System.setProperty("http.proxyHost", httpProxyHost);
        }
        if (!StringUtil.isEmpty(httpProxyPort)) {
            System.setProperty("http.proxyPort", httpProxyPort.toString());
        }
        if (!StringUtil.isEmpty(httpProxyUsername) && !StringUtil.isEmpty(httpProxyPassword)) {
            Authenticator.setDefault(new ProxyAuthenticator(httpProxyUsername, httpProxyPassword));
        }
    }

    /**
     * Proxy authenticator that provides username and password from properties file
     */
    public static class ProxyAuthenticator extends Authenticator {

        private String user, password;

        public ProxyAuthenticator(String user, String password) {
            this.user = user;
            this.password = password;
        }

        @Override
        protected PasswordAuthentication getPasswordAuthentication() {
            return new PasswordAuthentication(user, password.toCharArray());
        }
    }
}
