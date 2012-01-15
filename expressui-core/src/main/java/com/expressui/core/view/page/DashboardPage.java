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

package com.expressui.core.view.page;

import com.expressui.core.view.RootComponent;
import com.expressui.core.view.results.Results;
import com.expressui.core.view.util.MessageSource;
import com.vaadin.ui.Component;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Panel;
import com.vaadin.ui.themes.ChameleonTheme;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

/**
 * Dashboard for displaying as the first tab in the application. Components can be added to
 * the Dashboard grid layout, using X,Y coordinates, in the same way that fields are
 * added to entity forms.
 */
public abstract class DashboardPage extends RootComponent implements Page {

    @Resource
    protected MessageSource entityMessageSource;

    @Resource
    protected MessageSource uiMessageSource;

    private GridLayout gridLayout;

    @PostConstruct
    @Override
    public void postConstruct() {
        super.postConstruct();
        gridLayout = new GridLayout();
        gridLayout.setMargin(true);
        gridLayout.setSpacing(true);

        setCompositionRoot(gridLayout);
        setSizeUndefined();
        getCompositionRoot().setSizeUndefined();
    }

    /**
     * Can be overridden if any initialization is required after all Spring beans have been wired.
     * Overriding methods should call super.
     */
    @Override
    public void postWire() {
        super.postWire();
    }

    @Override
    public void onLoad() {
    }

    @Override
    public String getCaption() {
        return null;
    }

    /**
     * Add a component to the dashboard.
     *
     * @param component   component to add
     * @param caption     caption to display above component
     * @param startRow    start row coordinate in Dashboard grid
     * @param startColumn start column coordinate in Dashboard grid
     */
    public void addComponent(Component component, String caption, int startRow, int startColumn) {
        addComponent(component, caption, startRow, startColumn, startRow, startColumn);
    }

    /**
     * Add a component to the dashboard.
     *
     * @param component   component to add
     * @param caption     caption to display above component
     * @param startRow    start row coordinate in Dashboard grid
     * @param startColumn start column coordinate in Dashboard grid
     * @param endRow      end row coordinate in Dashboard grid
     * @param endColumn   end column coordinate in Dashboard grid
     */
    public void addComponent(Component component, String caption, int startRow, int startColumn, int endRow, int endColumn) {
        Panel panel = new Panel(caption);
        panel.addStyleName(ChameleonTheme.PANEL_BUBBLE);
        HorizontalLayout layout = new HorizontalLayout();
        panel.setContent(layout);
        layout.setMargin(true);
        layout.setSpacing(true);
        layout.addComponent(component);
        int rows = Math.max(gridLayout.getRows() - 1, endRow);
        int columns = Math.max(gridLayout.getColumns() - 1, endColumn);

        if (gridLayout.getRows() < rows) {
            gridLayout.setRows(rows);
        }
        if (gridLayout.getColumns() < columns) {
            gridLayout.setColumns(columns);
        }

        removeComponent(startRow, startColumn);
        gridLayout.addComponent(panel, startColumn - 1, startRow - 1, endColumn - 1, endRow - 1);
        if (component instanceof Results) {
            ((Results) component).search();
        }
    }

    /**
     * Remove component from the dashboard
     *
     * @param startRow    start row coordinate in Dashboard grid
     * @param startColumn start column coordinate in Dashboard grid
     */
    public void removeComponent(int startRow, int startColumn) {
        gridLayout.removeComponent(startColumn - 1, startRow - 1);
    }

    @Override
    public boolean isViewAllowed() {
        return true;
    }
}
