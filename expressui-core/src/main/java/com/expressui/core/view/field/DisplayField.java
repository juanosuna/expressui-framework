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

import com.expressui.core.util.BeanPropertyType;
import com.expressui.core.util.StringUtil;
import com.expressui.core.util.assertion.Assert;
import com.expressui.core.view.field.format.DefaultFormats;
import com.expressui.core.view.form.EntityForm;
import com.vaadin.data.util.PropertyFormatter;
import org.springframework.beans.BeanUtils;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.List;

/**
 * A field for display in the UI, as a column in results table or input field in a form.
 */
public abstract class DisplayField {

    private FieldSet fieldSet;

    private String propertyId;
    private BeanPropertyType beanPropertyType;
    private FormLink formLink;
    private PropertyFormatter propertyFormatter;
    private String label;

    /**
     * Constructs with reference to fieldSet this field belongs to and the property name this field is bound to,
     * often in an entity object.
     *
     * @param fieldSet   fieldSet that contains this field
     * @param propertyId name of the property this field is bound to
     */
    public DisplayField(FieldSet fieldSet, String propertyId) {
        this.fieldSet = fieldSet;
        this.propertyId = propertyId;
        beanPropertyType = BeanPropertyType.getBeanPropertyType(getFieldSet().getType(), propertyId);
        Assert.PROGRAMMING.notNull(beanPropertyType != null, "beanPropertyType must not be null");
    }

    /**
     * Gets FieldSet that contains this field.
     *
     * @return FieldSet that contains this field
     */
    public FieldSet getFieldSet() {
        return fieldSet;
    }

    /**
     * Gets the name of the property this field is bound to, often in an entity object
     *
     * @return name of the property
     */
    public String getPropertyId() {
        return propertyId;
    }

    /**
     * Gets static type information about the property this field is bound to.
     *
     * @return bean property type information
     */
    protected BeanPropertyType getBeanPropertyType() {
        return beanPropertyType;
    }

    /**
     * Gets the type of property this field is bound to.
     *
     * @return type of property
     */
    public Class getPropertyType() {
        return beanPropertyType.getType();
    }

    /**
     * Gets the PropertyFormatter used to format values for display and parse values
     * entered by user. If one is not already set, generates a default one automatically from
     * {@link DefaultFormats}.
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
     * Sets the PropertyFormatter used to format values for display and parse values
     * entered by user.
     *
     * @param propertyFormatter Vaadin property formatter
     */
    public void setPropertyFormatter(PropertyFormatter propertyFormatter) {
        this.propertyFormatter = propertyFormatter;
    }

    /**
     * Generates the default property formatter for this field. Can be overridden to refine the behavior.
     *
     * @return Vaadin proprety formatter
     */
    protected PropertyFormatter generateDefaultPropertyFormatter() {
        DefaultFormats defaultFormats = getFieldSet().defaultFormats;

        if (getBeanPropertyType().getBusinessType() == BeanPropertyType.BusinessType.DATE) {
            return defaultFormats.getDateFormat();
        } else if (getBeanPropertyType().getBusinessType() == BeanPropertyType.BusinessType.DATE_TIME) {
            return defaultFormats.getDateTimeFormat();
        } else if (getBeanPropertyType().getBusinessType() == BeanPropertyType.BusinessType.NUMBER) {
            if (getBeanPropertyType().getType().isPrimitive()) {
                return defaultFormats.getNumberFormat(0);
            } else {
                return defaultFormats.getNumberFormat();
            }
        } else if (getBeanPropertyType().getBusinessType() == BeanPropertyType.BusinessType.MONEY) {
            return defaultFormats.getNumberFormat();
        }


        return defaultFormats.getEmptyFormat();
    }

    /**
     * Gets the label used for this field. Generates one automatically, if not already set.
     * Generated one can be derived from the property name in the code, @Label annotation on the bound property
     * or looked up from resource bundle properties file, using the property name as the key.
     * <P/>
     * If I18n is required, then define labels in resource bundle properties files resources/domainMessages/.
     * These messages have priority over annotations. Generating a label from the property name in code is done as a
     * last resort.
     *
     * @return display label
     */
    public String getLabel() {
        if (label == null) {
            label = generateLabelText();
        }

        return label;
    }

    /**
     * Sets the label used for this field, overwriting any automatically generated label.
     *
     * @param label label
     */
    public void setLabel(String label) {
        this.label = label;
    }

    /**
     * Generates or re-generates label, passing in arguments for interpolation using standard {0}, {1}, {2}
     * notation. This feature only works with resource bundle messages defined in resources/domainMessages/.
     * @param args
     */
    public void setLabelArgs(Object... args) {
        setLabel(generateLabelText(args));
    }

    /**
     * Generates label automatically, maybe override to refine default behavior.
     *
     * @param args arguments use for interpolation
     * @return generated label
     */
    protected String generateLabelText(Object... args) {
        String labelText = getLabelTextFromMessageSource(false, args);
        if (labelText == null) {
            labelText = getLabelTextFromAnnotation();
        }
        if (labelText == null) {
            labelText = getLabelTextFromCode();
        }

        return labelText;
    }

    /**
     * Gets name for the type of label or section in a form where the label is found.
     * This is used internally by security admin components to indicate to the user
     * where components are located for assigning permissions.
     *
     * @return label section display name
     */
    protected abstract String getLabelSectionDisplayName();

    private String getLabelTextFromMessageSource(boolean useDefaultLocale, Object... args) {
        List<BeanPropertyType> ancestors = beanPropertyType.getAncestors();
        String label = null;
        for (int i = 0; i < ancestors.size(); i++) {
            BeanPropertyType ancestor = ancestors.get(i);
            Class currentType = ancestor.getContainerType();

            String currentPropertyId = ancestor.getId();
            for (int y = i + 1; y < ancestors.size(); y++) {
                BeanPropertyType bpt = ancestors.get(y);
                currentPropertyId += "." + bpt.getId();
            }

            while (label == null && currentType != null) {
                label = getLabelTextFromMessageSource(useDefaultLocale, currentType, currentPropertyId, args);
                currentType = currentType.getSuperclass();
            }

            if (label != null) break;

            Class[] interfaces = ancestor.getContainerType().getInterfaces();
            for (Class anInterface : interfaces) {
                Class currentInterface = anInterface;
                while (label == null && currentInterface != null) {
                    label = getLabelTextFromMessageSource(useDefaultLocale, currentInterface, currentPropertyId, args);
                    currentInterface = currentInterface.getSuperclass();
                }
                if (label != null) break;
            }

            if (label != null) break;
        }

        if (label == null) {
            label = getLabelTextFromMessageSource(useDefaultLocale, beanPropertyType.getType(), null, args);
        }

        if (label != null && label.contains("{0}") && args.length == 0) {
            return null;
        } else {
            if (label == null && !useDefaultLocale) {
                return getLabelTextFromMessageSource(true, args);
            } else {
                return label;
            }
        }
    }

    private String getLabelTextFromMessageSource(boolean useDefaultLocale, Class type, String propertyId,
                                                 Object... args) {
        String fullPropertyPath = type.getName() + (propertyId == null ? "" : "." + propertyId);
        if (useDefaultLocale) {
            return getFieldSet().domainMessageSourceNoFallback.getOptionalMessageFromDefaultLocale(fullPropertyPath, args);
        } else {
            return getFieldSet().domainMessageSourceNoFallback.getOptionalMessage(fullPropertyPath, args);
        }
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
     * Sets a link to open an entity form related to this field. Enables embedding
     * links in results table to open up related entity form.
     *
     * @param propertyId property id path that is many-to-one relationship with another entity
     * @param entityForm entity form component to open when link is clicked
     */
    public void setFormLink(String propertyId, EntityForm entityForm) {
        formLink = new FormLink(propertyId, entityForm);
        entityForm.postWire();
    }

    /**
     * Gets a link to open an entity form related to this field. Enables embedding
     * links in results table to open up related entity
     *
     * @return form link
     */
    public FormLink getFormLink() {
        return formLink;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DisplayField)) return false;

        DisplayField that = (DisplayField) o;

        return getPropertyId().equals(that.getPropertyId());

    }

    @Override
    public int hashCode() {
        return getPropertyId().hashCode();
    }

    @Override
    public String toString() {
        return "DisplayField{" +
                "propertyId='" + getPropertyId() + '\'' +
                '}';
    }
}
