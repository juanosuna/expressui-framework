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

package com.expressui.core.view.form.layout;


import com.expressui.core.util.StringUtil;
import com.expressui.core.view.field.FormField;
import com.vaadin.ui.*;

/**
 * A special layout for forms, which handles labels and can support labels on top
 * of fields or to the left. Note some differences between ExpressUI and Vaadin
 * columns and rows:
 * <ul>
 * <li>ExpressUI columns and rows start at 1, where as Vaadin starts as 0.</li>
 * <li>In left-label layout, each ExpressUI logical column includes label, field and spacers, whereas
 * the underlying Vaadin layout provides separate columns for each.</li>
 * </ul>
 * Compared to using Vaadin directly, ExpressUI simplifies the layout so the developer does not have to worry about
 * labels and spacers.
 */
public abstract class FormGridLayout extends CustomComponent implements Layout {

    private GridLayout rootLayout;

    /**
     * Constructs specifying the actual number of columns and rows, from Vaadin perspective.
     *
     * @param columns number of columns
     * @param rows    number of rows
     */
    protected FormGridLayout(int columns, int rows) {
        rootLayout = new GridLayout(columns, rows);
        String id = StringUtil.generateDebugId("e", this, rootLayout, "rootLayout");
        rootLayout.setDebugId(id);
        setCompositionRoot(rootLayout);
    }

    /**
     * Sets the number of columns in the form, from ExpressUI perspective.
     *
     * @param columns number of columns in the two-dimension form grid
     */
    public abstract void setFormColumns(int columns);

    /**
     * Adds a field to the form.
     *
     * @param formField field to add
     */
    public abstract void addField(FormField formField);

    /**
     * Removes field from the form.
     *
     * @param formField field to remove
     */
    public abstract void removeField(FormField formField);

    @Override
    public void setMargin(boolean enabled) {
        rootLayout.setEnabled(enabled);
    }

    @Override
    public void setMargin(boolean top, boolean right, boolean bottom, boolean left) {
        rootLayout.setMargin(top, right, bottom, left);
    }

    /**
     * Adds and positions component in layout.
     *
     * @param component component to add
     * @param column1   column start position from Vaadin perspective
     * @param row1      row start position from Vaadin perspective
     * @param column2   column end position from Vaadin perspective
     * @param row2      row end position from Vaadin perspective
     * @throws GridLayout.OverlapsException
     * @throws GridLayout.OutOfBoundsException
     *
     */
    protected void addComponent(Component component, int column1, int row1, int column2, int row2) throws GridLayout.OverlapsException, GridLayout.OutOfBoundsException {
        rootLayout.addComponent(component, column1, row1, column2, row2);
    }

    /**
     * Do not use. Throws UnsupportedOperationException. Instead use:
     * addComponent(Component component, int column1, int row1, int column2, int row2)
     *
     * @param component component to be added
     */
    @Override
    public void addComponent(Component component) {
        throw new UnsupportedOperationException("Do not call this method. Instead use: " +
                "addComponent(Component component, int column1, int row1, int column2, int row2) ");
    }

    /**
     * Sets number of columns from Vaadin perspective, used internally.
     *
     * @param columns number of columns
     */
    protected void setColumns(int columns) {
        rootLayout.setColumns(columns);
    }

    /**
     * Sets number of rows from Vaadin perspective, used internally.
     *
     * @param rows number of rows
     */
    public void setRows(int rows) {
        rootLayout.setRows(rows);
    }

    /**
     * Gets component at column, row coordinates
     *
     * @param column coordinate
     * @param row    coordinate
     * @return component
     */
    protected Component getComponent(int column, int row) {
        return rootLayout.getComponent(column, row);
    }

    /**
     * Adds and positions component to layout.
     *
     * @param component component
     * @param column    column from Vaadin perspective
     * @param row       row from Vaadin perspective
     * @throws GridLayout.OverlapsException
     * @throws GridLayout.OutOfBoundsException
     *
     */
    protected void addComponent(Component component, int column, int row) throws GridLayout.OverlapsException, GridLayout.OutOfBoundsException {
        rootLayout.addComponent(component, column, row);
    }

    /**
     * Removes component at column, row coordinates.
     *
     * @param column coordinate
     * @param row    coordinate
     */
    protected void removeComponent(int column, int row) {
        rootLayout.removeComponent(column, row);
    }

    /**
     * Sets component alignment.
     *
     * @param childComponent component to align
     * @param alignment      alignment
     */
    protected void setComponentAlignment(Component childComponent, Alignment alignment) {
        rootLayout.setComponentAlignment(childComponent, alignment);
    }

    /**
     * Sets whether or not spacing is enabled.
     *
     * @param enabled true if spacing is enabled
     */
    public void setSpacing(boolean enabled) {
        rootLayout.setSpacing(enabled);
    }

    @Override
    public void removeAllComponents() {
        rootLayout.removeAllComponents();
    }
}
