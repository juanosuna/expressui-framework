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

package com.expressui.sample.view;

import com.expressui.core.MainApplication;
import com.expressui.core.view.RootComponent;
import com.expressui.core.view.page.Page;
import com.vaadin.terminal.ExternalResource;
import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.Embedded;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.net.MalformedURLException;
import java.net.URL;

import static org.springframework.web.context.WebApplicationContext.SCOPE_SESSION;

@Component
@Scope(SCOPE_SESSION)
@SuppressWarnings({"serial"})
public class HomePage extends RootComponent implements Page {

    @Override
    public void postConstruct() {
        super.postConstruct();
    }

    @Override
    public void onDisplay() {
        try {
            URL url = new URL("http://www.expressui.com/");
            Embedded browser = new Embedded("", new ExternalResource(url));
            browser.setType(Embedded.TYPE_BROWSER);
            browser.setWidth(100, Sizeable.UNITS_PERCENTAGE);
            browser.setHeight(15000, Sizeable.UNITS_PIXELS);
            setCompositionRoot(browser);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }

        MainApplication.getInstance().showTrayMessage(
                "<h3>Feature Tip:</h3>" +
                        "<ul>" +
                        "<li>This illustrates how to embed any external page into ExpressUI" +
                        "</ul>"
        );
    }
}
