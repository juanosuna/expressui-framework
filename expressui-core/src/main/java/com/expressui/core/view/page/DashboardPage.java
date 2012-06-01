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

package com.expressui.core.view.page;

import com.expressui.core.view.RootComponent;
import com.expressui.core.view.TypedComponent;
import com.expressui.core.view.ViewBean;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ChameleonTheme;

import javax.annotation.PostConstruct;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Dashboard for displaying a grid of any custom components. Components can be added to
 * the Dashboard's grid layout, using X, Y coordinates, in the same way that fields are
 * added to forms.
 */
public abstract class DashboardPage extends RootComponent implements Page {

    private GridLayout rootLayout;

    @PostConstruct
    @Override
    public void postConstruct() {
        super.postConstruct();
        rootLayout = new GridLayout();
        setDebugId(rootLayout, "rootLayout");
        rootLayout.setMargin(true);
        rootLayout.setSpacing(true);

        setCompositionRoot(rootLayout);
        setSizeUndefined();

        addCodePopupButtonIfEnabled(DashboardPage.class);
    }

    @Override
    public void postWire() {
        super.postWire();
        Set<ViewBean> viewBeanComponents = findViewBeanComponents();
        for (ViewBean viewBeanComponent : viewBeanComponents) {
            viewBeanComponent.postWire();
        }
    }

    @Override
    public void onDisplay() {
    }

    /**
     * Default caption is null, since Dashboard is not associated with any specific entity type.
     *
     * @return null
     */
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
        if (component instanceof TypedComponent && !((TypedComponent) component).isViewAllowed()) return;

        Panel panel = new Panel(caption);
        panel.addStyleName(ChameleonTheme.PANEL_BUBBLE);
        HorizontalLayout panelLayout = new HorizontalLayout();
        panelLayout.setSizeUndefined();
        panel.setSizeUndefined();
        panel.setContent(panelLayout);
        panelLayout.setMargin(true);
        panelLayout.setSpacing(true);
        panelLayout.addComponent(component);
        int rows = Math.max(rootLayout.getRows() - 1, endRow);
        int columns = Math.max(rootLayout.getColumns() - 1, endColumn);

        if (rootLayout.getRows() < rows) {
            rootLayout.setRows(rows);
        }
        if (rootLayout.getColumns() < columns) {
            rootLayout.setColumns(columns);
        }

        removeComponent(startRow, startColumn);
        rootLayout.addComponent(panel, startColumn - 1, startRow - 1, endColumn - 1, endRow - 1);
    }


    /**
     * Remove component from the dashboard
     *
     * @param startRow    start row coordinate in Dashboard grid
     * @param startColumn start column coordinate in Dashboard grid
     */
    public void removeComponent(int startRow, int startColumn) {
        rootLayout.removeComponent(startColumn - 1, startRow - 1);
    }

    private Set<ViewBean> findViewBeanComponents() {
        Set<ViewBean> viewBeans = new HashSet<ViewBean>();

        Iterator<Component> iterator = rootLayout.getComponentIterator();
        while (iterator.hasNext()) {
            Panel panel = (Panel) iterator.next();
            ComponentContainer layout = panel.getContent();
            Component component = layout.getComponentIterator().next();
            if (component instanceof ViewBean) {
                viewBeans.add((ViewBean) component);
            }
        }

        return viewBeans;
    }
}
