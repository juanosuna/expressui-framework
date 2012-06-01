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

package com.expressui.core.view.field;

/**
 * A field for non-editable display in the results component.
 */
public class ResultsField extends DisplayField {

    private boolean isSortable = true;

    /**
     * Construct with reference to fieldSet this field belongs to and the property name this field is bound to, e.g.
     * an entity object.
     *
     * @param fieldSet   fieldSet that contains this field
     * @param propertyId name of the property this field is bound to
     */
    public ResultsField(FieldSet fieldSet, String propertyId) {
        super(fieldSet, propertyId);
    }

    /**
     * Ask if this field is a sortable column in results table. Default is true. This should be disabled for derived
     * fields that do not correspond to columns in the database.
     *
     * @return true if sortable
     */
    public boolean isSortable() {
        return isSortable;
    }

    /**
     * Set whether or not this field is a sortable column in results table
     *
     * @param sortable true if sortable
     */
    public void setSortable(boolean sortable) {
        isSortable = sortable;
    }

    @Override
    protected String getLabelSectionDisplayName() {
        return "Column";
    }

    @Override
    public String toString() {
        return "ResultsField{" +
                "propertyId='" + getPropertyId() + '\'' +
                '}';
    }
}
