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

package com.expressui.core.view.form.layout;


import com.expressui.core.view.field.FormField;
import com.vaadin.data.Property;
import com.vaadin.data.util.EnhancedNestedMethodProperty;
import com.vaadin.ui.*;

/**
 * A special layout for forms, which can support labels on top
 * of fields or to the left.
 */
public abstract class FormGridLayout extends CustomComponent implements Layout {

    private GridLayout gridLayout;

    public FormGridLayout(int columns, int rows) {
        gridLayout = new GridLayout(columns, rows);
        setCompositionRoot(gridLayout);
    }

    /**
     * Set the number of columns in the form.
     *
     * @param columns number of columns in the two-dimension form grid
     */
    public abstract void setFormColumns(int columns);

    /**
     * Add a field to the form
     *
     * @param formField field to add
     */
    public abstract void addField(FormField formField);

    /**
     * Remove field from the form.
     *
     * @param formField field to remove
     */
    public abstract void removeField(FormField formField);

    @Override
    public void setMargin(boolean enabled) {
        gridLayout.setEnabled(enabled);
    }

    @Override
    public void setMargin(boolean top, boolean right, boolean bottom, boolean left) {
        gridLayout.setMargin(top, right, bottom, left);
    }

    public void addComponent(Component component, int column1, int row1, int column2, int row2) throws GridLayout.OverlapsException, GridLayout.OutOfBoundsException {
        gridLayout.addComponent(component, column1, row1, column2, row2);
    }

    @Override
    public void addComponent(Component component) {
//        Field field = (Field) component;
//        EnhancedNestedMethodProperty property = (EnhancedNestedMethodProperty) field.getPropertyDataSource();
//        String propertyName = property.getPropertyName();

//        gridLayout.addComponent(component);
    }

    public void setColumns(int columns) {
        gridLayout.setColumns(columns);
    }

    public void setRows(int rows) {
        gridLayout.setRows(rows);
    }

    public Component getComponent(int x, int y) {
        return gridLayout.getComponent(x, y);
    }

    public void addComponent(Component c, int column, int row) throws GridLayout.OverlapsException, GridLayout.OutOfBoundsException {
        gridLayout.addComponent(c, column, row);
    }

    public void removeComponent(int column, int row) {
        gridLayout.removeComponent(column, row);
    }

    public void setComponentAlignment(Component childComponent, Alignment alignment) {
        gridLayout.setComponentAlignment(childComponent, alignment);
    }

    public void setSpacing(boolean enabled) {
        gridLayout.setSpacing(enabled);
    }

    @Override
    public void removeAllComponents() {
        gridLayout.removeAllComponents();
    }
}
