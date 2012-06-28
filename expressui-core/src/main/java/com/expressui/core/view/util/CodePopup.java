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
import com.expressui.core.util.ApplicationProperties;
import com.expressui.core.util.StringUtil;
import com.vaadin.terminal.ExternalResource;
import com.vaadin.terminal.Sizeable;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.BaseTheme;
import org.springframework.stereotype.Component;
import org.vaadin.codelabel.CodeLabel;

import javax.annotation.Resource;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

/**
 * Used in demo applications for popping up sample code associated with visual components.
 */
@Component
public class CodePopup {

    @Resource
    private ApplicationProperties applicationProperties;

    @Resource
    public MessageSource uiMessageSource;

    /**
     * Create a popup code button.
     *
     * @param classes classes for displaying related source code and Javadoc. If
     *                class is within com.expressui.core or com.expressui.domain,
     *                then Javadoc is displayed, otherwise source code.
     * @return popup code button
     */
    public Button createPopupCodeButton(final Class... classes) {
        Button codeButton = new Button(null, new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                open(classes);
            }
        });
        codeButton.setIcon(new ThemeResource("../expressui/icons/32/java.png"));
        codeButton.setStyleName(BaseTheme.BUTTON_LINK);
        codeButton.setDescription(uiMessageSource.getToolTip("codePopup.toolTip"));
        return codeButton;
    }

    /**
     * Open popup for given classes.
     *
     * @param classes classes for displaying related source code and Javadoc. If
     *                class is within com.expressui.core or com.expressui.domain,
     *                then Javadoc is displayed, otherwise source code.
     */
    public void open(Class... classes) {

        Window codeWindow = new Window();
        codeWindow.addStyleName("code-popup");
        codeWindow.setPositionX(20);
        codeWindow.setPositionY(40);
        codeWindow.setWidth("90%");
        codeWindow.setHeight("90%");

        TabSheet codePopupTabSheet = new TabSheet();
        String id = StringUtil.generateDebugId("e", this, codePopupTabSheet, "codePopupTabSheet");
        codePopupTabSheet.setDebugId(id);

        codePopupTabSheet.setSizeFull();
        codeWindow.addComponent(codePopupTabSheet);

        String windowCaption = "";
        Set<String> shownUrls = new HashSet<String>();
        for (Class clazz : classes) {
            String tabCaption;
            String url;
            if (clazz.getName().startsWith("com.expressui.core") || clazz.getName().startsWith("com.expressui.domain")) {
                url = applicationProperties.getDocUrl(clazz);
                if (shownUrls.contains(url)) continue;
                tabCaption = clazz.getSimpleName() + " API";
                Embedded embedded = getEmbeddedDoc(url);
                codePopupTabSheet.addTab(embedded, tabCaption);
            } else {
                url = applicationProperties.getCodeUrl(clazz);
                if (shownUrls.contains(url)) continue;
                tabCaption = clazz.getSimpleName() + ".java";
                String code = getCodeContents(url);
                Label label = new CodeLabel(code);
                codePopupTabSheet.addTab(label, tabCaption);
            }
            shownUrls.add(url);
            if (windowCaption.length() > 0)
                windowCaption += ", ";

            windowCaption += tabCaption;
        }

        codeWindow.setCaption(windowCaption);
        MainApplication.getInstance().getMainWindow().addWindow(codeWindow);
    }

    private String getCodeContents(String urlStr) {
        try {
            URL url = new URL(urlStr);
            BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));

            StringBuilder builder = new StringBuilder();
            boolean startBuilder = false;
            String line;
            while ((line = in.readLine()) != null) {
                if (startBuilder) {
                    builder.append(line);
                    builder.append("\n");
                } else if (line.matches("^\\s*@.*$") || line.matches("^.*\\s+class\\s+.*$")) {
                    startBuilder = true;
                    builder.append(line);
                    builder.append("\n");
                }
            }


            in.close();
            return builder.toString();
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Embedded getEmbeddedDoc(String u) {
        try {
            URL url = new URL(u);
            Embedded browser = new Embedded("", new ExternalResource(url));
            browser.setHeight(15000, Sizeable.UNITS_PIXELS);
            browser.setType(Embedded.TYPE_BROWSER);
            return browser;
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }
}
