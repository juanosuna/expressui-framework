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

package com.expressui.core.view.field;

import com.expressui.core.security.SecurityService;
import com.expressui.core.util.CollectionsUtil;
import com.expressui.core.view.field.format.DefaultFormats;
import com.expressui.core.view.form.EntityForm;
import com.expressui.core.view.util.MessageSource;
import com.vaadin.data.util.PropertyFormatter;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;

import static org.springframework.beans.factory.config.BeanDefinition.SCOPE_PROTOTYPE;

/**
 * Collection of fields for display in UI component, e.g. results table
 */
@Component
@Scope(SCOPE_PROTOTYPE)
public class DisplayFields {

    @Resource(name = "entityMessageSource")
    private MessageSource messageSource;

    @Resource
    private DefaultFormats defaultFormats;

    @Resource
    private SecurityService securityService;

    private Class entityType;
    private Map<String, DisplayField> fields = new LinkedHashMap<String, DisplayField>();

    public DisplayFields() {
    }

    /**
     * Get the type of entity these fields are bound to
     *
     * @return type of entity in the Datasource
     */
    public Class getEntityType() {
        return entityType;
    }

    /**
     * Set the type of entity these fields are bound to
     *
     * @param entityType type of entity in the Datasource
     */
    public void setEntityType(Class entityType) {
        this.entityType = entityType;
    }

    DefaultFormats getDefaultFormats() {
        return defaultFormats;
    }

    MessageSource getMessageSource() {
        return messageSource;
    }

    /**
     * Set the property ids for data binding these fields to a Datasource of entities
     *
     * @param propertyIds names of entity properties
     */
    public void setPropertyIds(String... propertyIds) {
        for (String propertyId : propertyIds) {
            DisplayField displayField = createField(propertyId);
            fields.put(propertyId, displayField);
        }
    }

    /**
     * Get the property ids for data binding these fields to a Datasource of entities
     *
     * @return property ids
     */
    public List<String> getPropertyIds() {
        return new ArrayList(fields.keySet());
    }

    public List<String> getViewablePropertyIds() {
        List<String> viewablePropertyIds = new ArrayList<String>();
        List<String> propertyIds = getPropertyIds();

        for (String propertyId : propertyIds) {
            if (securityService.getCurrentUser().isViewAllowed(getEntityType().getName(), propertyId)) {
                viewablePropertyIds.add(propertyId);
            }
        }

        return viewablePropertyIds;
    }

    /**
     * Get property ids as array
     *
     * @return array of property ids
     */
    public String[] getViewablePropertyIdsAsArray() {
        return CollectionsUtil.toStringArray(getViewablePropertyIds());
    }

    /**
     * Get labels (column headings) as array
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

    /**
     * Ask if these fields contains a field bound to given property id
     *
     * @param propertyId property id to check
     * @return true if a field is bound to given property id
     */
    public boolean containsPropertyId(String propertyId) {
        return fields.containsKey(propertyId);
    }

    /**
     * Get display field bound to given property id
     *
     * @param propertyId property id
     * @return display field bound to given property id
     */
    public DisplayField getField(String propertyId) {
        if (!containsPropertyId(propertyId)) {
            DisplayField displayField = createField(propertyId);
            fields.put(propertyId, displayField);
        }

        return fields.get(propertyId);
    }

    DisplayField createField(String propertyId) {
        return new DisplayField(this, propertyId);
    }

    /**
     * Get collection of DisplayField objects
     *
     * @return collection of DisplayField objects
     */
    public Collection<DisplayField> getFields() {
        return fields.values();
    }

    /**
     * Get all property ids that have been set as non-sortable
     *
     * @return non-sortable properties
     */
    public Set<String> getNonSortablePropertyIds() {
        Set<String> nonSortablePropertyIds = new HashSet<String>();
        for (DisplayField displayField : fields.values()) {
            if (!displayField.isSortable()) {
                nonSortablePropertyIds.add(displayField.getPropertyId());
            }
        }

        return nonSortablePropertyIds;
    }

    /**
     * Get display label (column heading) bound to given property.
     *
     * @param propertyId property path in entity tree
     * @return display label (column heading)
     */
    public String getLabel(String propertyId) {
        return getField(propertyId).getLabel();
    }

    /**
     * Set display label (column heading) bound to given property.
     *
     * @param propertyId property path in entity tree
     * @param label      label (column heading)
     */
    public void setLabel(String propertyId, String label) {
        getField(propertyId).setLabel(label);
    }

    /**
     * Set column associated with given property id as sortable or not
     *
     * @param propertyId id for identify column
     * @param isSortable true if sortable
     */
    public void setSortable(String propertyId, boolean isSortable) {
        getField(propertyId).setSortable(isSortable);
    }

    /**
     * Set a link to open an entity form related to this field. Enables embedding
     * links in results table to open up related entity
     *
     * @param propertyId       id property path for display content in the link
     * @param entityPropertyId property path that is many-to-one relationship with another entity
     * @param entityForm       entity form component to open when link is clicked
     */
    public void setFormLink(String propertyId, String entityPropertyId, EntityForm entityForm) {
        getField(propertyId).setFormLink(entityPropertyId, entityForm);
    }

    /**
     * Set property formatter to be used for formatting display value.
     *
     * @param propertyId        property path in entity tree
     * @param propertyFormatter property formatter
     */
    public void setPropertyFormatter(String propertyId, PropertyFormatter propertyFormatter) {
        getField(propertyId).setPropertyFormatter(propertyFormatter);
    }
}
