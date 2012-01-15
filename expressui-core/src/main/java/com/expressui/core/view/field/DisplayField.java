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

import com.expressui.core.util.BeanPropertyType;
import com.expressui.core.util.StringUtil;
import com.expressui.core.util.assertion.Assert;
import com.expressui.core.view.field.format.DefaultFormats;
import com.expressui.core.view.field.format.JDKFormatPropertyFormatter;
import com.expressui.core.view.form.EntityForm;
import com.vaadin.data.util.PropertyFormatter;
import org.springframework.beans.BeanUtils;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.text.Format;

/**
 * A field for non-editable display in the UI, e.g. as a column in results table.
 * For editable fields, use FormField.
 */
public class DisplayField {

    private DisplayFields displayFields;

    private String propertyId;
    private BeanPropertyType beanPropertyType;
    private FormLink formLink;
    private PropertyFormatter propertyFormatter;
    private boolean isSortable = true;
    private String columnHeader;

    /**
     * Construct with reference to parent and property name this field is bound to
     *
     * @param displayFields parent that contains collection of all display fields
     * @param propertyId    name of the property this field is bound to
     */
    public DisplayField(DisplayFields displayFields, String propertyId) {
        this.displayFields = displayFields;
        this.propertyId = propertyId;
        beanPropertyType = BeanPropertyType.getBeanPropertyType(getDisplayFields().getEntityType(), propertyId);
        Assert.PROGRAMMING.assertTrue(beanPropertyType != null, "beanPropertyType must not be null");
    }

    /**
     * Get parent collection of all display properties bound to UI component, e.g. results table
     *
     * @return parent DisplayFields that contains this DisplayField
     */
    public DisplayFields getDisplayFields() {
        return displayFields;
    }

    /**
     * Get the name of the property this field is bound to
     *
     * @return name of the property
     */
    public String getPropertyId() {
        return propertyId;
    }

    protected BeanPropertyType getBeanPropertyType() {
        return beanPropertyType;
    }

    /**
     * Get the type of property this field is bound to.
     *
     * @return type of property
     */
    public Class getPropertyType() {
        return beanPropertyType.getType();
    }

    /**
     * Get the PropertyFormatter used to format values for display and parse values
     * entered by user.
     *
     * @return Vaadin property formatter
     */
    public PropertyFormatter getPropertyFormatter() {
        if (propertyFormatter == null) {
            propertyFormatter = generateDefaultPropertyFormatter();
        }

        return propertyFormatter;
    }

    /**
     * Set the PropertyFormatter used to format values for display and parse values
     * entered by user.
     *
     * @param propertyFormatter Vaadin property formatter
     */
    public void setPropertyFormatter(PropertyFormatter propertyFormatter) {
        this.propertyFormatter = propertyFormatter;
    }

    /**
     * Set the JDK format used to format values for display and parse values
     * entered by user.
     *
     * @param format JDK format
     */
    public void setFormat(Format format) {
        setPropertyFormatter(new JDKFormatPropertyFormatter(format));
    }

    private PropertyFormatter generateDefaultPropertyFormatter() {
        DefaultFormats defaultFormats = getDisplayFields().getDefaultFormats();

        if (getBeanPropertyType().getBusinessType() == BeanPropertyType.BusinessType.DATE) {
            return defaultFormats.getDateFormat();
        } else if (getBeanPropertyType().getBusinessType() == BeanPropertyType.BusinessType.DATE_TIME) {
            return defaultFormats.getDateTimeFormat();
        } else if (getBeanPropertyType().getBusinessType() == BeanPropertyType.BusinessType.NUMBER) {
            return defaultFormats.getNumberFormat();
        } else if (getBeanPropertyType().getBusinessType() == BeanPropertyType.BusinessType.MONEY) {
            return defaultFormats.getNumberFormat();
        }


        return defaultFormats.getEmptyFormat();
    }

    /**
     * Ask if this field is a sortable column in results table
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

    /**
     * Get the label used for this field, e.g. column header.
     *
     * @return display label
     */
    public String getLabel() {
        if (columnHeader == null) {
            columnHeader = generateLabelText();
        }

        return columnHeader;
    }

    /**
     * Set the label used for this field, e.g. column header.
     *
     * @param columnHeader label
     */
    public void setLabel(String columnHeader) {
        this.columnHeader = columnHeader;
    }

    String generateLabelText() {
        String labelText = getLabelTextFromMessageSource();
        if (labelText == null) {
            labelText = getLabelTextFromAnnotation();
        }
        if (labelText == null) {
            labelText = getLabelTextFromCode();
        }

        return labelText;
    }

    String getLabelSectionDisplayName() {
        return "Column";
    }

    private String getLabelTextFromMessageSource() {
        String fullPropertyPath = displayFields.getEntityType().getName() + "." + getPropertyId();
        return displayFields.getMessageSource().getMessage(fullPropertyPath);
    }

    private String getLabelTextFromAnnotation() {
        Class propertyContainerType = beanPropertyType.getContainerType();
        String propertyIdRelativeToContainerType = beanPropertyType.getId();
        PropertyDescriptor descriptor = BeanUtils.getPropertyDescriptor(propertyContainerType,
                propertyIdRelativeToContainerType);
        Method method = descriptor.getReadMethod();
        Label labelAnnotation = method.getAnnotation(Label.class);
        if (labelAnnotation == null) {
            return null;
        } else {
            return labelAnnotation.value();
        }
    }

    private String getLabelTextFromCode() {
        String afterPeriod = StringUtil.extractAfterPeriod(getPropertyId());
        return StringUtil.humanizeCamelCase(afterPeriod);
    }

    /**
     * Set a link to open an entity form related to this field. Enables embedding
     * links in results table to open up related entity
     *
     * @param propertyId property path that is many-to-one relationship with another entity
     * @param entityForm entity form component to open when link is clicked
     */
    public void setFormLink(String propertyId, EntityForm entityForm) {
        formLink = new FormLink(propertyId, entityForm);
        entityForm.postWire();
    }

    /**
     * Get a link to open an entity form related to this field. Enables embedding
     * links in results table to open up related entity
     *
     * @return form link
     */
    public FormLink getFormLink() {
        return formLink;
    }

    /**
     * A link for opening an EntityForm
     */
    public static class FormLink {
        private String propertyId;
        private EntityForm entityForm;

        private FormLink(String propertyId, EntityForm entityForm) {
            this.propertyId = propertyId;
            this.entityForm = entityForm;
        }

        /**
         * Get property path of many-to-one link to another entity
         *
         * @return name of property
         */
        public String getPropertyId() {
            return propertyId;
        }

        /**
         * Get EntityForm that is opened when link is clicked.
         *
         * @return entity form
         */
        public EntityForm getEntityForm() {
            return entityForm;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DisplayField)) return false;

        DisplayField that = (DisplayField) o;

        if (!getPropertyId().equals(that.getPropertyId())) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return getPropertyId().hashCode();
    }

    @Override
    public String toString() {
        return "EntityField{" +
                "propertyId='" + getPropertyId() + '\'' +
                '}';
    }
}
