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

import com.vaadin.terminal.ExternalResource;
import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.AbstractComponentContainer;
import com.vaadin.ui.Embedded;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Utility class for managing URLs.
 */
public class UrlUtil {

    public static String EXPRESSUI_TEST_PAGE = "http://www.expressui.com/expressui-demo-test-page/?tag=";

    /**
     * Invokes URL and get the contents returned.
     *
     * @param urlStr url
     * @return contents, for example HTML
     * @throws IOException
     */
    public static String getContents(String urlStr) throws IOException {
        URL url = new URL(urlStr);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.connect();
        InputStream inputStream = connection.getInputStream();

        BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));

        StringBuilder builder = new StringBuilder();
        String line;
        while ((line = in.readLine()) != null) {
            builder.append(line);
            builder.append("\n");
        }

        in.close();
        return builder.toString();
    }

    /**
     * Invokes URL and get the contents returned.
     *
     * @param urlStr url
     * @throws IOException
     */
    public static void checkValid(String urlStr) throws IOException {
        URL url = new URL(urlStr);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.connect();
    }

    /**
     * Only used by sample application to track usage statistics
     *
     * @param container container for adding the embedded iframe to
     */
    public static void addTrackingUrl(AbstractComponentContainer container, String tag) {
        try {
            URL url = new URL(EXPRESSUI_TEST_PAGE + tag);
            Embedded browser = new Embedded(null, new ExternalResource(url));
            browser.setType(Embedded.TYPE_BROWSER);
            browser.setWidth(0, Sizeable.UNITS_PIXELS);
            browser.setHeight(0, Sizeable.UNITS_PIXELS);
            container.addComponent(browser);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }
}
