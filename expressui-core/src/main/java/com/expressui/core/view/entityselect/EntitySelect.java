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

package com.expressui.core.view.entityselect;

import com.expressui.core.view.TypedComponent;
import com.expressui.core.view.form.SearchForm;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

import javax.annotation.PostConstruct;

/**
 * A popup component for soliciting a entity selection from the user in a form.
 * This is useful in many-to-one relationships, where the user must search among
 * a large number of available entities and select one for assignment in a
 * many-to-one relationship.
 *
 * @param <T> type of entity to be selected
 */
public abstract class EntitySelect<T> extends TypedComponent<T> {

    private Window popupWindow;

    protected EntitySelect() {
        super();
    }

    /**
     * Get the search form component.
     *
     * @return search form component
     */
    public abstract SearchForm getSearchForm();

    /**
     * Get the results component presented to user for selection.
     *
     * @return results from which entity is selected
     */
    public abstract EntitySelectResults<T> getResults();

    public void configurePopupWindow(Window popupWindow) {
        popupWindow.setSizeUndefined();
        popupWindow.setHeight("95%");
    }

    /**
     * Type caption is null to avoid redundant captions.
     *
     * @return null
     */
    @Override
    public String getTypeCaption() {
        return null;
    }

    /**
     * Caption is null to avoid redundant captions.
     *
     * @return null
     */
    @Override
    public String getCaption() {
        return null;
    }

    @PostConstruct
    @Override
    public void postConstruct() {
        super.postConstruct();

        setWidthSizeFull();

        getResults().selectPageSize(5);

        addComponent(getSearchForm());
        addComponent(getResults());

        addCodePopupButtonIfEnabled(EntitySelect.class);
    }

    @Override
    public void postWire() {
        super.postWire();
        getSearchForm().setResults(getResults());
        getSearchForm().postWire();
        getResults().postWire();
    }

    @Override
    public void onDisplay() {
        getSearchForm().onDisplay();
        getResults().onDisplay();
    }

    /**
     * Open a popup window with this component
     */
    public void open() {
        popupWindow = new Window(getTypeCaption());
        popupWindow.addStyleName("e-entity-select-window");
        popupWindow.addStyleName("opaque");
        VerticalLayout layout = (VerticalLayout) popupWindow.getContent();
        layout.setMargin(true);
        layout.setSpacing(true);
        layout.setSizeUndefined();
        popupWindow.setSizeUndefined();
        popupWindow.setModal(true);
        popupWindow.setClosable(true);

        getResults().getEntityQuery().clear();
        getResults().selectPageSize(5);
        getResults().search();
        configurePopupWindow(popupWindow);
        popupWindow.addComponent(this);

        getMainApplication().getMainWindow().addWindow(popupWindow);

        onDisplay();
    }

    /**
     * Close this popup.
     */
    public void close() {
        getMainApplication().getMainWindow().removeWindow(popupWindow);
    }
}
