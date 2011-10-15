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

package com.expressui.core.view;

import com.expressui.core.MainApplication;
import com.expressui.core.view.field.LabelRegistry;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

/**
 * A main entry point for the user to work with entities of a particular type.
 *
 * The difference between main entry point and a regular entry point: a main entry point is presented to the user
 * as a Vaadin Tab in the initial "home page" of the application, a TabSheet. A regular entry point can be
 * presented anywhere in the application, e.g. a pop-up EntitySelect  also provides a search form
 * and results for selecting a entity in a many-to-one relationship.
 *
 * @param <T> type of business entity for this entry point
 */
public abstract class MainEntryPoint<T> extends EntryPoint<T> {

    @Resource
    private LabelRegistry labelDepot;

    private Button logoutButton;

    @PostConstruct
    @Override
    public void postConstruct() {
        super.postConstruct();

        labelDepot.putEntityLabel(getEntityType().getName(), getEntityCaption());

        HorizontalLayout searchAndLogout = new HorizontalLayout();
        searchAndLogout.setSizeFull();
        searchAndLogout.addComponent(getSearchForm());

        logoutButton = new Button(null);
        logoutButton.setDescription(uiMessageSource.getMessage("mainApplication.logout"));
        logoutButton.setSizeUndefined();
        logoutButton.addStyleName("borderless");
        logoutButton.setIcon(new ThemeResource("icons/16/logout.png"));

        searchAndLogout.addComponent(logoutButton);
        searchAndLogout.setComponentAlignment(logoutButton, Alignment.TOP_RIGHT);

        addComponent(searchAndLogout);
        addComponent(getResults());
    }

    @Override
    public void postWire() {
        super.postWire();

        MainApplication.getInstance().setLogoutURL("mvc/login.do");
        logoutButton.addListener(Button.ClickEvent.class, MainApplication.getInstance(), "logout");
    }
}
