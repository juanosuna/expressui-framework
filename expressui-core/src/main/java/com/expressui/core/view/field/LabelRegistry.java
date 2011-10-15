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

import org.springframework.stereotype.Component;

import java.util.*;

/**
 * A registry for managing UI display labels.
 */
@Component
public class LabelRegistry {
    private Map<String, String> entityTypeLabels = new TreeMap<String, String>();
    private Map<String, Set<String>> entityTypePropertyIds = new HashMap<String, Set<String>>();
    private Map<String, Set<DisplayLabel>> labels = new HashMap<String, Set<DisplayLabel>>();

    /**
     * Put entity label into registry.
     * @param entityType type of entity
     * @param label label
     */
    public void putEntityLabel(String entityType, String label) {
        entityTypeLabels.put(entityType, label);
    }

    /**
     * Get entity label associated with entity type
     * @param entityType entity type for looking up label
     * @return label
     */
    public String getEntityLabel(String entityType) {
        return entityTypeLabels.get(entityType);
    }

    /**
     * Get Map of all entity type labels keyed by entity type
     * @return map of all entity type labels
     */
    public Map<String, String> getEntityTypeLabels() {
        return entityTypeLabels;
    }

    /**
     * Get all property ids that have been registered
     * @param entityType
     * @return
     */
    public Map<Object, String> getPropertyIds(String entityType) {
        Map<Object, String> fieldItems = new LinkedHashMap<Object, String>();

        Set<String> propertyIds = entityTypePropertyIds.get(entityType);
        for (String propertyId : propertyIds) {
            fieldItems.put(propertyId, propertyId);
        }

        return fieldItems;
    }

    /**
     * Field (property) label into registry
     * @param entityType entity type for the label
     * @param propertyId property id of the field
     * @param section section of field (tab)
     * @param label label to put into registry
     */
    public void putFieldLabel(String entityType, String propertyId, String section, String label) {
        if (!entityTypePropertyIds.containsKey(entityType)) {
            entityTypePropertyIds.put(entityType, new TreeSet<String>());
        }

        Set<String> propertyIds = entityTypePropertyIds.get(entityType);
        if (!propertyIds.contains(propertyId)) {
            propertyIds.add(propertyId);
        }

        String propertyPath = entityType + "." + propertyId;
        if (!labels.containsKey(propertyPath)) {
            labels.put(propertyPath, new HashSet<DisplayLabel>());
        }

        Set<DisplayLabel> displayLabels = labels.get(propertyPath);

        DisplayLabel displayLabel = new DisplayLabel(propertyId, section, label);
        if (!displayLabels.contains(displayLabel)) {
            displayLabels.add(displayLabel);
        }
    }

    /**
     * Get field label for given entity type and property
     * @param entityType entity type
     * @param propertyId property in the entity type
     * @return field label
     */
    public String getFieldLabel(String entityType, String propertyId) {
        String propertyPath = entityType + "." + propertyId;
        String label = "";
        Set<DisplayLabel> displayLabels = labels.get(propertyPath);
        for (DisplayLabel displayLabel : displayLabels) {
            if (!label.isEmpty()) {
                label += ", ";
            }
            label += displayLabel.getDisplayName();
        }

        return label;
    }

    /**
     * Register all the labels from DisplayFields.
     *
     * @param displayFields collection of all fields associated with a display component
     */
    public void registerLabels(DisplayFields displayFields) {
        Collection<DisplayField> fields = displayFields.getFields();

        for (DisplayField field : fields) {
            String label = displayFields.getLabel(field.getPropertyId());
            if (label == null) {
                label = field.getPropertyId();
            } else {
                label = label.replaceAll("<.*>.*</.*>", "");
            }
            putFieldLabel(displayFields.getEntityType().getName(), field.getPropertyId(),
                    field.getLabelSectionDisplayName(), label);

        }
    }

    /**
     * Label for display to end user in UI
     */
    public static class DisplayLabel {
        private String propertyId;
        private String section;
        String label;

        /**
         * Construct based on property, section and label.
         * @param propertyId id of property (field)
         * @param section section (tab)
         * @param label label
         */
        public DisplayLabel(String propertyId, String section, String label) {
            this.propertyId = propertyId;
            this.section = section;
            this.label = label;
        }

        /**
         * Get property id of the label.
         * @return property id
         */
        public String getPropertyId() {
            return propertyId;
        }

        /**
         * Get display label for display to end user in UI.
         *
         * @return display label
         */
        public String getDisplayName() {
            if (section == null || section.isEmpty()) {
                return label;
            } else {
                return label + " (" + section + ")";
            }
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            DisplayLabel that = (DisplayLabel) o;

            if (!label.equals(that.label)) return false;
            if (!propertyId.equals(that.propertyId)) return false;
            if (!section.equals(that.section)) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = propertyId.hashCode();
            result = 31 * result + section.hashCode();
            result = 31 * result + label.hashCode();
            return result;
        }
    }
}
