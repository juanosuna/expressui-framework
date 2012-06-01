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

import org.springframework.stereotype.Component;

import java.util.*;

/**
 * A registry for managing UI display labels, used internally by security permissions to provide
 * admin user with meaningful names to link permissions with entities or UI components.
 */
@Component
public class LabelRegistry {

    private Map<String, String> typeLabels = new TreeMap<String, String>();
    private Map<String, Set<String>> typePropertyIds = new HashMap<String, Set<String>>();
    private Map<String, Set<DisplayLabel>> labels = new HashMap<String, Set<DisplayLabel>>();

    /**
     * Put label into registry.
     *
     * @param type  type
     * @param label label
     */
    public void putTypeLabel(String type, String label) {
        typeLabels.put(type, label);
        if (!typePropertyIds.containsKey(type)) {
            typePropertyIds.put(type, new TreeSet<String>());
        }
    }

    /**
     * Get label associated with type
     *
     * @param type type for looking up label
     * @return label
     */
    public String getTypeLabel(String type) {
        return typeLabels.get(type);
    }

    /**
     * Get Map of all type labels keyed by type name
     *
     * @return map of all type labels
     */
    public Map<String, String> getTypeLabels() {
        return typeLabels;
    }

    /**
     * Get all property ids that have been registered
     *
     * @param type
     * @return
     */
    public Map<Object, String> getPropertyIds(String type) {
        Map<Object, String> fieldItems = new LinkedHashMap<Object, String>();

        Set<String> propertyIds = typePropertyIds.get(type);
        for (String propertyId : propertyIds) {
            fieldItems.put(propertyId, propertyId);
        }

        return fieldItems;
    }

    /**
     * Field (property) label into registry
     *
     * @param type       type for the label
     * @param propertyId property id of the field
     * @param section    section of field (tab)
     * @param label      label to put into registry
     */
    public void putFieldLabel(String type, String propertyId, String section, String label) {
        if (!typePropertyIds.containsKey(type)) {
            typePropertyIds.put(type, new TreeSet<String>());
        }

        Set<String> propertyIds = typePropertyIds.get(type);
        if (!propertyIds.contains(propertyId)) {
            propertyIds.add(propertyId);
        }

        String propertyPath = type + "." + propertyId;
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
     * Get field label for given type and property
     *
     * @param type       type
     * @param propertyId property in the type
     * @return field label
     */
    public String getFieldLabel(String type, String propertyId) {
        String propertyPath = type + "." + propertyId;
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
     * Register all the labels from FieldSet.
     *
     * @param fieldSet collection of all fields associated with a display component
     */
    public void registerLabels(FieldSet fieldSet) {
        Collection<DisplayField> displayFields = fieldSet.getFields();

        for (DisplayField displayField : displayFields) {
            String label = fieldSet.getLabel(displayField.getPropertyId());
            if (label == null) {
                label = displayField.getPropertyId();
            } else {
                label = label.replaceAll("<.*>.*</.*>", "");
            }
            putFieldLabel(fieldSet.getType().getName(), displayField.getPropertyId(),
                    displayField.getLabelSectionDisplayName(), label);

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
         *
         * @param propertyId id of property (field)
         * @param section    section (tab)
         * @param label      label
         */
        public DisplayLabel(String propertyId, String section, String label) {
            this.propertyId = propertyId;
            this.section = section;
            this.label = label;
        }

        /**
         * Get property id of the label.
         *
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
