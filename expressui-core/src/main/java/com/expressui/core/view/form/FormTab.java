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

package com.expressui.core.view.form;

import com.expressui.core.view.field.FormField;

import java.util.Set;

/**
 * Tab contained within a form. One logical tab without a name is displayed as no tabs to the user.
 */
public class FormTab {

    private FormFieldSet formFieldSet;
    private String tabName;

    /**
     * Constructs FormTab based on FormFieldSet and tab name.
     *
     * @param formFieldSet set of all fields in the form, this tab contains only a subset
     * @param tabName      name of tab
     */
    public FormTab(FormFieldSet formFieldSet, String tabName) {
        this.formFieldSet = formFieldSet;
        this.tabName = tabName;
    }

    /**
     * Sets the position of a field in this tab. Field occupies just one cell.
     *
     * @param propertyId  property id in entity to bind field to
     * @param rowStart    row start coordinate
     * @param columnStart column start coordinate
     */
    public void setCoordinates(String propertyId, int rowStart, int columnStart) {
        formFieldSet.setCoordinates(tabName, propertyId, rowStart, columnStart);
    }

    /**
     * Sets the position of a field in this tab.
     *
     * @param propertyId  property id in entity to bind field to
     * @param rowStart    row start coordinate
     * @param columnStart column start coordinate
     * @param rowEnd      row end coordinate, field height is stretched to row end coordinate
     * @param columnEnd   column end coordinate, field width is stretched to column end coordinate
     */
    public void setCoordinates(String propertyId, int rowStart, int columnStart, Integer rowEnd, Integer columnEnd) {
        formFieldSet.setCoordinates(tabName, propertyId, rowStart, columnStart, rowEnd, columnEnd);
    }

    /**
     * Gets method delegate that listens for adding and removing this tab.
     *
     * @return delegate
     */
    public FormFieldSet.AddRemoveTabMethodDelegate getTabAddRemoveDelegate() {
        return formFieldSet.getTabAddRemoveDelegate(tabName);
    }

    /**
     * Gets number of columns in this tab.
     *
     * @return number of columns in this tab
     */
    public int getColumns() {
        return formFieldSet.getColumns(tabName);
    }

    /**
     * Gets number of rows in this tab.
     *
     * @return number of rows in this form's tab
     */
    public int getRows() {
        return formFieldSet.getRows(tabName);
    }

    /**
     * Asks if this tab is optional.
     *
     * @return true if optional
     */
    public boolean isOptional() {
        return formFieldSet.isTabOptional(tabName);
    }

    /**
     * Sets this tab as optional.
     *
     * @param addTarget    target object to invoke method on, when tab is added
     * @param addMethod    method to invoke, when tab is added
     * @param removeTarget target object to invoke method on, when tab is removed
     * @param removeMethod method to invoke, when tab is removed
     */
    public void setOptional(Object addTarget, String addMethod, Object removeTarget, String removeMethod) {
        formFieldSet.setTabOptional(tabName, addTarget, addMethod, removeTarget, removeMethod);
    }

    /**
     * Asks if this form contains property in this tab.
     *
     * @param propertyId property id
     * @return true if this form contains property in the given tab
     */
    public boolean containsPropertyId(String propertyId) {
        return formFieldSet.containsPropertyId(tabName, propertyId);
    }

    /**
     * Gets all the FormFields positioned in this tab.
     *
     * @return all the FormField under the tab
     */
    public Set<FormField> getFormFields() {
        return formFieldSet.getFormFields(tabName);
    }

    /**
     * Asks if any field has an error in this tab.
     *
     * @return true if any field has an error in this tab
     */
    public boolean hasError() {
        return formFieldSet.hasError(tabName);
    }
}
