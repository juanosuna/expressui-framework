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
import com.expressui.core.view.EntityForm;
import com.expressui.core.view.util.MessageSource;
import com.expressui.core.view.field.format.DefaultFormats;
import com.vaadin.data.util.PropertyFormatter;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;

@Component
@Scope("prototype")
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

    public void setPropertyIds(String[] propertyIds) {
        for (String propertyId : propertyIds) {
            DisplayField displayField = createField(propertyId);
            fields.put(propertyId, displayField);
        }
    }

    public Class getEntityType() {
        return entityType;
    }

    public void setEntityType(Class entityType) {
        this.entityType = entityType;
    }

    public DefaultFormats getDefaultFormats() {
        return defaultFormats;
    }

    public MessageSource getMessageSource() {
        return messageSource;
    }

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

    public String[] getViewablePropertyIdsAsArray() {
        return CollectionsUtil.toStringArray(getViewablePropertyIds());
    }

    public String[] getViewableLabelsAsArray() {
        List<String> labels = new ArrayList<String>();
        List<String> propertyIds = getViewablePropertyIds();
        for (String propertyId : propertyIds) {
            labels.add(getField(propertyId).getLabel());
        }

        return CollectionsUtil.toStringArray(labels);
    }

    public boolean containsPropertyId(String propertyId) {
        return fields.containsKey(propertyId);
    }

    public DisplayField getField(String propertyId) {
        if (!containsPropertyId(propertyId)) {
            DisplayField displayField = createField(propertyId);
            fields.put(propertyId, displayField);
        }

        return fields.get(propertyId);
    }

    protected DisplayField createField(String propertyId) {
        return new DisplayField(this, propertyId);
    }

    public Collection<DisplayField> getFields() {
        return fields.values();
    }

    public Set<String> getNonSortablePropertyIds() {
        Set<String> nonSortablePropertyIds = new HashSet<String>();
        for (DisplayField displayField : fields.values()) {
            if (!displayField.isSortable()) {
                nonSortablePropertyIds.add(displayField.getPropertyId());
            }
        }

        return nonSortablePropertyIds;
    }

    public String getLabel(String propertyId) {
        return getField(propertyId).getLabel();
    }

    public void setLabel(String propertyId, String label) {
        getField(propertyId).setLabel(label);
    }

    public void setSortable(String propertyId, boolean isSortable) {
        getField(propertyId).setSortable(isSortable);
    }

    public void setFormLink(String propertyId, String entityPropertyId, EntityForm entityForm) {
        getField(propertyId).setFormLink(entityPropertyId, entityForm);
    }

    public void setPropertyFormatter(String propertyId, PropertyFormatter propertyFormatter) {
        getField(propertyId).setPropertyFormatter(propertyFormatter);
    }
}
