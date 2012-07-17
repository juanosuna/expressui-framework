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

package com.expressui.core.view.results;

import com.expressui.core.util.CollectionsUtil;
import com.expressui.core.util.assertion.Assert;
import com.expressui.core.view.field.DisplayField;
import com.expressui.core.view.field.FieldSet;
import com.expressui.core.view.field.ResultsField;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.*;

import static org.springframework.beans.factory.config.BeanDefinition.SCOPE_PROTOTYPE;

/**
 * Collection of fields/columns to display in the results table.
 */
@Component
@Scope(SCOPE_PROTOTYPE)
public class ResultsFieldSet extends FieldSet {

    public ResultsFieldSet() {
        super();
    }

    /**
     * Sets the property ids for binding columns to a Datasource of entities.
     *
     * @param propertyIds names of entity properties
     */
    public void setPropertyIds(String... propertyIds) {
        for (String propertyId : propertyIds) {
            createField(propertyId);
        }
    }

    /**
     * Gets labels (column headings) as array.
     *
     * @return array of property labels
     */
    public String[] getViewableLabelsAsArray() {
        List<String> labels = new ArrayList<String>();
        List<String> propertyIds = getViewablePropertyIds();
        for (String propertyId : propertyIds) {
            labels.add(getField(propertyId).getLabel());
        }

        return CollectionsUtil.toStringArray(labels);
    }

    @Override
    protected DisplayField createField(String propertyId) {
        Assert.PROGRAMMING.isTrue(!containsPropertyId(propertyId), "Field has already been created for property "
                + propertyId);

        ResultsField resultsField = new ResultsField(this, propertyId);
        addField(propertyId, resultsField);

        return resultsField;
    }

    /**
     * Gets collection of ResultsFields.
     *
     * @return collection of ResultsFields
     */
    public Collection<ResultsField> getResultFields() {
        Collection<ResultsField> resultsFields = new HashSet<ResultsField>();
        Collection<DisplayField> displayFields = getFields();
        for (DisplayField displayField : displayFields) {
            resultsFields.add((ResultsField) displayField);
        }

        return resultsFields;
    }

    /**
     * Gets field/column bound to given property id.
     *
     * @param propertyId property id
     * @return results field bound to given property id
     */
    public ResultsField getResultsField(String propertyId) {
        return (ResultsField) getField(propertyId);
    }

    /**
     * Get all property ids that have been set as non-sortable.
     *
     * @return non-sortable properties
     */
    public Set<String> getNonSortablePropertyIds() {
        Set<String> nonSortablePropertyIds = new HashSet<String>();
        for (ResultsField resultsField : getResultFields()) {
            if (!resultsField.isSortable()) {
                nonSortablePropertyIds.add(resultsField.getPropertyId());
            }
        }

        return nonSortablePropertyIds;
    }

    /**
     * Sets field/column associated with given property id as sortable or not
     *
     * @param propertyId id for identify column
     * @param isSortable true if sortable
     */
    public void setSortable(String propertyId, boolean isSortable) {
        getResultsField(propertyId).setSortable(isSortable);
    }

    /**
     * Sets column width.
     *
     * @param propertyId id for identify column
     * @param width      column width, null if adjusted automatically
     */
    public void setWidth(String propertyId, Integer width) {
        getResultsField(propertyId).setWidth(width);
    }

    /**
     * Sets alignment.
     * <p/>
     * See constants defined in {@link com.vaadin.ui.Table}
     *
     * @param alignment alignment
     */
    public void setAlignment(String propertyId, String alignment) {
        getResultsField(propertyId).setAlignment(alignment);
    }
}
