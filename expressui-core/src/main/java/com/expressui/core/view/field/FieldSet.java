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

import com.expressui.core.security.SecurityService;
import com.expressui.core.util.CollectionsUtil;
import com.expressui.core.util.assertion.Assert;
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
 * Collection of fields for display in a UI component, for example in a form or results component.
 */
@Component
@Scope(SCOPE_PROTOTYPE)
public abstract class FieldSet {

    /**
     * MessageSource for internationalizing entity or domain-level messages.
     */
    @Resource
    public MessageSource domainMessageSource;

    /**
     * MessageSource for internationalizing UI-level messages.
     */
    @Resource
    public MessageSource uiMessageSource;

    /**
     * MessageSource for internationalizing entity or domain-level messages, where system does not
     * fallback to system locale if message is not found in user's locale. This is useful internally
     * for trying different options in a single resource defined for a single locale.
     */
    @Resource
    public MessageSource domainMessageSourceNoFallback;

    /**
     * Default property formats.
     */
    @Resource
    public DefaultFormats defaultFormats;

    /**
     * Security service.
     */
    @Resource
    public SecurityService securityService;

    private Class type;
    protected Map<String, DisplayField> fields = new LinkedHashMap<String, DisplayField>();

    public FieldSet() {
    }

    /**
     * Adds a field to this collection.
     *
     * @param propertyId   property name
     * @param displayField DisplayField to add
     */
    public void addField(String propertyId, DisplayField displayField) {
        fields.put(propertyId, displayField);
    }

    /**
     * Gets the type of entity these fields are bound to.
     *
     * @return type of entity in the Datasource
     */
    public Class getType() {
        return type;
    }

    /**
     * Sets the type of entity these fields are bound to.
     *
     * @param type type of entity in the Datasource
     */
    public void setType(Class type) {
        this.type = type;
    }

    /**
     * Gets a list of property ids for data binding these fields to a Datasource of entities.
     *
     * @return property ids
     */
    public List<String> getPropertyIds() {
        return new ArrayList(fields.keySet());
    }

    /**
     * Gets list of property ids that current user is allowed to view, based on security permissions.
     *
     * @return list of viewable property ids
     */
    public List<String> getViewablePropertyIds() {
        List<String> viewablePropertyIds = new ArrayList<String>();
        List<String> propertyIds = getPropertyIds();

        for (String propertyId : propertyIds) {
            if (securityService.getCurrentUser().isViewAllowed(getType().getName(), propertyId)) {
                viewablePropertyIds.add(propertyId);
            }
        }

        return viewablePropertyIds;
    }

    /**
     * Gets viewable property ids as array.
     *
     * @return array of property ids
     */
    public String[] getViewablePropertyIdsAsArray() {
        return CollectionsUtil.toStringArray(getViewablePropertyIds());
    }

    /**
     * Gets labels (column headings) of viewable properties as an array.
     *
     * @return array of viewable property labels
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
     * Asks if these fields contain a field bound to given property id.
     *
     * @param propertyId property id to check
     * @return true if some field in this collection is bound to given property id
     */
    public boolean containsPropertyId(String propertyId) {
        return fields.containsKey(propertyId);
    }

    /**
     * Gets DisplayField bound to given property id.
     *
     * @param propertyId property id
     * @return DisplayField bound to given property id
     */
    public DisplayField getField(String propertyId) {
        DisplayField field = fields.get(propertyId);
        Assert.PROGRAMMING.notNull(field, "no field exists for property " + getType().getName() + "." + propertyId);

        return field;
    }

    /**
     * Create DisplayField based on property id.
     *
     * @param propertyId name of property in the bound entity
     * @return newly created DisplayField
     */
    abstract protected DisplayField createField(String propertyId);

    /**
     * Get raw collection of DisplayFields
     *
     * @return raw collection of DisplayFields
     */
    public Collection<DisplayField> getFields() {
        return fields.values();
    }

    /**
     * Gets the display label for field bound to given property.
     *
     * @param propertyId property path in entity tree
     * @return display label
     */
    public String getLabel(String propertyId) {
        return getField(propertyId).getLabel();
    }

    /**
     * Sets display label for field bound to given property.
     *
     * @param propertyId property path in entity tree
     * @param label      label (column heading)
     */
    public void setLabel(String propertyId, String label) {
        getField(propertyId).setLabel(label);
    }

    /**
     * Generates or re-generates label, passing in arguments for interpolation using standard {0}, {1}, {2}
     * notation. This feature only works with resource bundle messages defined in domainMessages/.
     *
     * @param propertyId property path in entity tree
     * @param args
     */
    public void setLabelArgs(String propertyId, Object... args) {
        getField(propertyId).setLabelArgs(args);
    }

    /**
     * Sets a link to open an entity form related to this field. Enables embedding
     * links in results table to open up related entity form.
     *
     * @param propertyId       id property path for display content in the link
     * @param entityPropertyId property path that is many-to-one relationship with another entity
     * @param entityForm       entity form component to open when link is clicked
     */
    public void setFormLink(String propertyId, String entityPropertyId, EntityForm entityForm) {
        getField(propertyId).setFormLink(entityPropertyId, entityForm);
    }

    /**
     * Sets property formatter to be used for formatting display value.
     *
     * @param propertyId        property path in entity tree
     * @param propertyFormatter property formatter
     */
    public void setPropertyFormatter(String propertyId, PropertyFormatter propertyFormatter) {
        getField(propertyId).setPropertyFormatter(propertyFormatter);
    }

    @Override
    public String toString() {
        return "FieldSet{" +
                "type=" + getType().getName() +
                '}';
    }
}
