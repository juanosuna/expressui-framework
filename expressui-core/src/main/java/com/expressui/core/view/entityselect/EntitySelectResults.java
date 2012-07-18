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

import com.expressui.core.view.menu.ActionContextMenu;
import com.expressui.core.view.results.Results;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.Collection;

/**
 * Results component embedded in EntitySelect popup.
 *
 * @param <T> type of entity displayed in the results
 */
public abstract class EntitySelectResults<T> extends Results<T> {

    @Resource
    private ActionContextMenu actionContextMenu;

    private Button selectButton;

    protected EntitySelectResults() {
        super();
    }

    @PostConstruct
    @Override
    public void postConstruct() {
        super.postConstruct();
        addSelectionChangedListener(this, "selectionChanged");

        HorizontalLayout crudButtons = new HorizontalLayout();
        setDebugId(crudButtons, "crudButtons");
        crudButtons.setMargin(false);
        crudButtons.setSpacing(true);

        selectButton = new Button(uiMessageSource.getMessage("results.select"));
        selectButton.setDescription(uiMessageSource.getToolTip("results.select.toolTip"));
        selectButton.setEnabled(false);
        selectButton.addStyleName("small default");
        crudButtons.addComponent(selectButton);

        getCrudButtons().addComponent(crudButtons, 0);
        getCrudButtons().setComponentAlignment(crudButtons, Alignment.MIDDLE_LEFT);

        getEntityQuery().setPageSize(applicationProperties.getDefaultSelectPageSize());

        addCodePopupButtonIfEnabled(EntitySelectResults.class);
    }

    @Override
    public void onDisplay() {
    }

    /**
     * Listener method invoked when the user selects an entity in the results table.
     */
    public void selectionChanged() {
        Object itemId = getResultsTable().getValue();

        getResultsTable().turnOnContentRefreshing();

        if (itemId instanceof Collection) {
            if (((Collection) itemId).size() > 0) {
                selectButton.setEnabled(true);
                getResultsTable().addActionHandler(actionContextMenu);
            } else {
                selectButton.setEnabled(false);
                getResultsTable().removeActionHandler(actionContextMenu);
            }
        } else {
            if (itemId != null) {
                selectButton.setEnabled(true);
                getResultsTable().addActionHandler(actionContextMenu);
            } else {
                selectButton.setEnabled(false);
                getResultsTable().removeActionHandler(actionContextMenu);
            }
        }
    }

    /**
     * Adds a listener to be invoked when the user performs the select action,
     * by click select button or selecting from context menu.
     *
     * @param target     target object to be invoked
     * @param methodName listener method to be invoked
     */
    public void addSelectActionListener(Object target, String methodName) {
        selectButton.removeListener(Button.ClickEvent.class, target, methodName);
        selectButton.addListener(Button.ClickEvent.class, target, methodName);
        actionContextMenu.addAction("results.select", target, methodName);
        actionContextMenu.setActionEnabled("results.select", true);
    }
}
