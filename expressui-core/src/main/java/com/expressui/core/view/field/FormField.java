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

import com.expressui.core.MainApplication;
import com.expressui.core.dao.EntityDao;
import com.expressui.core.dao.ReferenceEntityDao;
import com.expressui.core.entity.ReferenceEntity;
import com.expressui.core.util.*;
import com.expressui.core.util.assertion.Assert;
import com.expressui.core.validation.NumberConversionValidator;
import com.expressui.core.view.field.format.EmptyPropertyFormatter;
import com.expressui.core.view.form.EntityForm;
import com.expressui.core.view.form.FormFieldSet;
import com.vaadin.addon.beanvalidation.BeanValidationValidator;
import com.vaadin.data.Property;
import com.vaadin.data.Validator;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.data.util.PropertyFormatter;
import com.vaadin.terminal.CompositeErrorMessage;
import com.vaadin.terminal.ErrorMessage;
import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.*;
import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.beans.BeanUtils;

import javax.annotation.Resource;
import javax.persistence.Lob;
import javax.validation.constraints.NotNull;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.*;

/**
 * A field in a form. Wraps Vaadin field component, while providing other features and integration with ExpressUI.
 * <p/>
 * Automatically generates labels with required asterisks.
 * Keeps track of row and column positions in the form grid layout.
 */
public class FormField extends DisplayField {

    /**
     * Default text field width in EM
     */
    public static final Integer DEFAULT_TEXT_FIELD_WIDTH = 11;

    /**
     * Default select field width in EM
     */
    public static final Integer DEFAULT_SELECT_FIELD_WIDTH = 11;

    private String tabName;
    private Field field;
    private Integer columnStart;
    private Integer rowStart;
    private Integer columnEnd;
    private Integer rowEnd;
    private boolean isRequired;
    private boolean isReadOnly;
    private com.vaadin.ui.Label label;
    private boolean isVisible;
    private AutoAdjustWidthMode autoAdjustWidthMode = AutoAdjustWidthMode.PARTIAL;
    private Integer defaultWidth;
    private boolean hasConversionError;

    @Resource
    private ReferenceEntityDao referenceEntityDao;

    /**
     * Construct with reference to fieldSet this field belongs to and the property name this field is bound to, e.g.
     * an entity object.
     *
     * @param formFieldSet fieldSet that contains this field
     * @param propertyId   name of the property this field is bound to
     */
    public FormField(FormFieldSet formFieldSet, String propertyId) {
        super(formFieldSet, propertyId);

        SpringApplicationContext.autowire(this);
    }

    /**
     * Get Vaadin label for this field. Label is automatically generated from property Id unless configured
     * by the application. Generated one can be derived from the property name, @Label annotation on the bound property
     * or looked up from domainMessageSource bean, using property name as key.
     *
     * @return Vaadin label for this field
     */
    public com.vaadin.ui.Label getFieldLabel() {
        getField(); // make sure field is initialized before label
        if (label == null) {
            String labelText = generateLabelText();
            if (isRequired()) {
                labelText = "<span class=\"e-required-field-indicator\">*</span>" + labelText;
            }
            label = new com.vaadin.ui.Label(labelText, com.vaadin.ui.Label.CONTENT_XHTML);
            label.setSizeUndefined();

            setToolTip(generateTooltip());
        }

        return label;
    }

    @Override
    protected String getLabelSectionDisplayName() {
        if (tabName.isEmpty()) {
            return getFieldSet().uiMessageSource.getMessage("formField.defaultLabelSectionDisplayName");
        } else {
            return tabName;
        }
    }

    /**
     * Set the field label, thus overriding default generated label.
     *
     * @param labelText display label
     */
    public void setFieldLabel(String labelText) {
        getFieldLabel().setValue(labelText);
    }

    /**
     * Get the name of the form tab this field resides in.
     *
     * @return name of form tab that contains this field
     */
    public String getTabName() {
        return tabName;
    }

    /**
     * Set the name of the form tab this field resides in.
     *
     * @param tabName name of form tab that contains this field
     */
    public void setTabName(String tabName) {
        Assert.PROGRAMMING.isTrue(!(tabName.isEmpty() && getFormFieldSet().hasTabs()), "tabName arg must not be empty" +
                " if named tabs already exist");


        Set<String> tabNames = getFormFieldSet().getTabNames();
        for (String name : tabNames) {
            Assert.PROGRAMMING.isTrue(tabName.isEmpty() || !name.isEmpty(), "tabName arg must be empty" +
                    " if empty tabNames already exist");
        }

        this.tabName = tabName;
    }

    /**
     * Get the column start coordinate of this field, starting with 1 not 0
     *
     * @return column start coordinate
     */
    public Integer getColumnStart() {
        return columnStart;
    }

    /**
     * Set the column start coordinate of this field, starting with 1 not 0
     *
     * @param columnStart column start coordinate
     */
    public void setColumnStart(Integer columnStart) {
        this.columnStart = columnStart;
    }

    /**
     * Get the row start coordinate of this field, starting with 1 not 0
     *
     * @return row start coordinate
     */
    public Integer getRowStart() {
        return rowStart;
    }

    /**
     * Set the row start coordinate of this field, starting with 1 not 0
     *
     * @param rowStart row start coordinate
     */
    public void setRowStart(Integer rowStart) {
        this.rowStart = rowStart;
    }

    /**
     * Get the column end coordinate of this field
     *
     * @return column end coordinate
     */
    public Integer getColumnEnd() {
        return columnEnd;
    }

    /**
     * Set the column end coordinate of this field
     *
     * @param columnEnd column end coordinate
     */
    public void setColumnEnd(Integer columnEnd) {
        this.columnEnd = columnEnd;
    }

    /**
     * Get the row end coordinate of this field
     *
     * @return row end coordinate
     */
    public Integer getRowEnd() {
        return rowEnd;
    }

    /**
     * Set the row end coordinate of this field
     *
     * @param rowEnd row end coordinate
     */
    public void setRowEnd(Integer rowEnd) {
        this.rowEnd = rowEnd;
    }

    /**
     * Assert that column start and row start are not null.
     */
    public void assertValid() {
        Assert.PROGRAMMING.notNull(columnStart, "columnStart must not be null");
        Assert.PROGRAMMING.notNull(rowStart, "rowStart must not be null");
    }

    /**
     * Get the underlying Vaadin field. The field is intelligently and automatically generated based on the property
     * type.
     * <p/>
     * In most cases, applications will not need to access Vaadin APIs directly. However,
     * it is exposed in case Vaadin features are needed that are not exposed by ExpressUI.
     *
     * @return Vaadin field
     */
    public Field getField() {
        if (field == null) {
            field = generateField();
            initializeFieldDefaults();
        }

        return field;
    }

    /**
     * Get the underlying Vaadin field. The field is intelligently and automatically generated based on the property type.
     * <p/>
     * In most cases, applications will not need to access Vaadin APIs directly. However,
     * it is exposed in case Vaadin features are needed that are not exposed by ExpressUI.
     */
    public void setField(Field field) {
        setField(field, true);
    }

    /**
     * Set the underlying Vaadin field, overriding the automatically generated one.
     *
     * @param field              Vaadin field
     * @param initializeDefaults allow ExpressUI to initialize the default settings for Vaadin field
     */
    public void setField(Field field, boolean initializeDefaults) {
        this.field = field;
        if (initializeDefaults) {
            initializeFieldDefaults();
        }
    }

    private void initWidthAndMaxLengthDefaults(AbstractTextField abstractTextField) {
        defaultWidth = MathUtil.maxIgnoreNull(DEFAULT_TEXT_FIELD_WIDTH, getBeanPropertyType().getMinimumLength());
        abstractTextField.setWidth(defaultWidth, Sizeable.UNITS_EM);

        Integer maxWidth = getBeanPropertyType().getMaximumLength();
        if (maxWidth != null) {
            abstractTextField.setMaxLength(maxWidth);
        }
    }

    /**
     * Get auto-adjust-width mode
     *
     * @return auto-adjust-width mode
     */
    public AutoAdjustWidthMode getAutoAdjustWidthMode() {
        return autoAdjustWidthMode;
    }

    /**
     * Set auto-adjust-width mode
     *
     * @param autoAdjustWidthMode auto-adjust-width mode
     */
    public void setAutoAdjustWidthMode(AutoAdjustWidthMode autoAdjustWidthMode) {
        this.autoAdjustWidthMode = autoAdjustWidthMode;
    }

    /**
     * Intelligently adjusts the width of fields to accommodate currently populated data.
     */
    public void autoAdjustTextFieldWidth() {
        Assert.PROGRAMMING.instanceOf(getField(), AbstractTextField.class,
                "FormField.autoAdjustWidth can only be called on text fields");

        if (autoAdjustWidthMode == AutoAdjustWidthMode.NONE) return;

        Object value = getField().getPropertyDataSource().getValue();
        if (value != null) {
            AbstractTextField textField = (AbstractTextField) getField();
            int approximateWidth = StringUtil.approximateEmWidth(value.toString());
            if (autoAdjustWidthMode == AutoAdjustWidthMode.FULL) {
                textField.setWidth(approximateWidth, Sizeable.UNITS_EM);
            } else if (autoAdjustWidthMode == AutoAdjustWidthMode.PARTIAL) {
                textField.setWidth(MathUtil.maxIgnoreNull(approximateWidth, defaultWidth), Sizeable.UNITS_EM);
            }
        }
    }

    /**
     * Get width of the field
     *
     * @return width of the field
     */
    public float getWidth() {
        return getField().getWidth();
    }

    /**
     * Manually set width of the field and turn off auto width adjustment.
     *
     * @param width size of width
     * @param unit  unit of measurement defined in Sizeable
     * @see Sizeable
     */
    public void setWidth(float width, int unit) {
        setAutoAdjustWidthMode(FormField.AutoAdjustWidthMode.NONE);
        getField().setWidth(width, unit);
    }

    /**
     * Set height of the field.
     *
     * @param height size of width
     * @param unit   unit of measurement defined in Sizeable
     * @see Sizeable
     */
    public void setHeight(float height, int unit) {
        getField().setHeight(height, unit);
    }

    /**
     * Intelligently adjusts the width of select fields to accommodate currently populated data.
     */
    public void autoAdjustSelectWidth() {
        Assert.PROGRAMMING.instanceOf(getField(), AbstractSelect.class,
                "FormField.autoAdjustSelectWidth can only be called on select fields");

        if (autoAdjustWidthMode == AutoAdjustWidthMode.NONE) return;

        AbstractSelect selectField = (AbstractSelect) getField();
        Collection itemsIds = selectField.getItemIds();

        int maxWidth = 0;
        for (Object itemsId : itemsIds) {
            String caption = selectField.getItemCaption(itemsId);
            int approximateWidth = StringUtil.approximateEmWidth(caption);
            maxWidth = Math.max(maxWidth, approximateWidth);
        }

        if (autoAdjustWidthMode == AutoAdjustWidthMode.FULL) {
            selectField.setWidth(maxWidth, Sizeable.UNITS_EM);
        } else if (autoAdjustWidthMode == AutoAdjustWidthMode.PARTIAL) {
            selectField.setWidth(MathUtil.maxIgnoreNull(maxWidth, DEFAULT_SELECT_FIELD_WIDTH), Sizeable.UNITS_EM);
        }
    }

    /**
     * Set the menu options in a select.
     *
     * @param items list of items
     * @see com.expressui.core.entity.ReferenceEntity.DISPLAY_PROPERTY
     */
    public void setSelectItems(List items) {
        // could be either collection or single item
        Object selectedItems = getSelectedItems();

        Field field = getField();
        Assert.PROGRAMMING.instanceOf(field, AbstractSelect.class,
                "property " + getPropertyId() + " is not a AbstractSelect field");
        AbstractSelect selectField = (AbstractSelect) field;
        if (selectField.getContainerDataSource() == null
                || !(selectField.getContainerDataSource() instanceof BeanItemContainer)) {
            BeanItemContainer container;
            if (getBeanPropertyType().isCollectionType()) {
                container = new BeanItemContainer(getBeanPropertyType().getCollectionValueType(), items);
            } else {
                container = new BeanItemContainer(getPropertyType(), items);
            }

            selectField.setContainerDataSource(container);
        } else {
            BeanItemContainer container = (BeanItemContainer) selectField.getContainerDataSource();
            container.removeAllItems();
            container.addAll(items);

            if (!getBeanPropertyType().isCollectionType() && !container.containsId(selectedItems)) {
                selectField.select(selectField.getNullSelectionItemId());
            }
        }
        autoAdjustSelectWidth();
    }

    /**
     * Set menu options in a select.
     *
     * @param items map of items where key is bound to entity and value is the display caption
     */
    public void setSelectItems(Map<Object, String> items) {
        String nullCaption = getFieldSet().uiMessageSource.getMessage("formFieldSet.select.nullCaption");
        setSelectItems(items, nullCaption);
    }

    /**
     * Set menu options in a select.
     *
     * @param items       map of items where key is bound to entity and value is the display caption
     * @param nullCaption caption displayed to represent null or no selection
     */
    public void setSelectItems(Map<Object, String> items, String nullCaption) {
        Field field = getField();
        Assert.PROGRAMMING.instanceOf(field, AbstractSelect.class,
                "property " + getPropertyId() + " is not a AbstractSelect field");
        AbstractSelect selectField = (AbstractSelect) field;

        Object previouslySelectedValue = selectField.getValue();

        selectField.setItemCaptionMode(Select.ITEM_CAPTION_MODE_EXPLICIT);
        selectField.removeAllItems();

        if (nullCaption != null) {
            selectField.addItem(nullCaption);
            selectField.setItemCaption(nullCaption, nullCaption);
            selectField.setNullSelectionItemId(nullCaption);
        }

        for (Object item : items.keySet()) {
            String caption = items.get(item);
            selectField.addItem(item);
            selectField.setItemCaption(item, caption);
            if (previouslySelectedValue != null && previouslySelectedValue.equals(item)) {
                selectField.setValue(item);
            }
        }

        autoAdjustSelectWidth();
    }

    /**
     * Get selected items, which could be a single item or collection.
     *
     * @return single item or collection
     */
    public Object getSelectedItems() {
        Field field = getField();
        Assert.PROGRAMMING.instanceOf(field, AbstractSelect.class,
                "property " + getPropertyId() + " is not a AbstractSelect field");
        AbstractSelect selectField = (AbstractSelect) field;
        return selectField.getValue();
    }

    /**
     * Set the dimensions of a multi-select menu
     *
     * @param rows    height
     * @param columns width
     */
    public void setMultiSelectDimensions(int rows, int columns) {
        Field field = getField();
        Assert.PROGRAMMING.instanceOf(field, ListSelect.class,
                "property " + getPropertyId() + " is not a AbstractSelect field");
        ListSelect selectField = (ListSelect) field;
        selectField.setRows(rows);
        selectField.setColumns(columns);
    }

    /**
     * Set the property Id to be used as display caption in select menu.
     *
     * @param displayCaptionPropertyId bean property name
     */
    public void setDisplayCaptionPropertyId(String displayCaptionPropertyId) {
        Assert.PROGRAMMING.instanceOf(field, AbstractSelect.class,
                "property " + getPropertyId() + " is not a Select field");

        ((AbstractSelect) field).setItemCaptionPropertyId(displayCaptionPropertyId);
    }

    /**
     * Add listener for changes in this field's value.
     *
     * @param target     target object to invoke
     * @param methodName name of method to invoke
     */
    public void addValueChangeListener(Object target, String methodName) {
        AbstractComponent component = (AbstractComponent) getField();
        component.addListener(Property.ValueChangeEvent.class, target, methodName);
    }

    /**
     * Get the FormFieldSet that contains this field.
     *
     * @return FormFieldSet that contains this field
     */
    public FormFieldSet getFormFieldSet() {
        return (FormFieldSet) getFieldSet();
    }

    /**
     * Set the visibility of this field and label
     *
     * @param isVisible true if visible
     */
    public void setVisible(boolean isVisible) {
        this.isVisible = isVisible;
        getField().setVisible(isVisible);
        getFieldLabel().setVisible(isVisible);
    }

    /**
     * Allow the field to be visible from a security permissions standpoint, if it is configured to be visible
     */
    public void allowView() {
        getField().setVisible(isVisible);
        getFieldLabel().setVisible(isVisible);
    }

    /**
     * Deny the field from being visible from a security permissions standpoint
     */
    public void denyView() {
        getField().setVisible(false);
        getFieldLabel().setVisible(false);
    }

    public boolean isRequired() {
        return isRequired;
    }

    /**
     * Ask if this field is required.
     *
     * @return true if required
     */
    public boolean isCurrentlyRequired() {
        return getField().isRequired();
    }

    /**
     * Set whether or not this field is required
     *
     * @param isRequired true if required
     */
    public void setCurrentlyRequired(boolean isRequired) {
        getField().setRequired(isRequired);
    }

    /**
     * Restore is-required setting to originally configured value, as specified in validation annotations
     */
    public void restoreIsRequired() {
        getField().setRequired(isRequired);
    }

    private String generateTooltip(Object... args) {
        String toolTipText = getToolTipTextFromMessageSource(false, args);
        if (toolTipText == null) {
            toolTipText = getToolTipTextFromAnnotation();
        }

        return toolTipText;
    }

    private String getToolTipTextFromMessageSource(boolean useDefaultLocale, Object... args) {
        List<BeanPropertyType> ancestors = getBeanPropertyType().getAncestors();
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
                label = getToolTipTextFromMessageSource(useDefaultLocale, currentType, currentPropertyId, args);
                currentType = currentType.getSuperclass();
            }

            if (label != null) break;

            Class[] interfaces = ancestor.getContainerType().getInterfaces();
            for (Class anInterface : interfaces) {
                Class currentInterface = anInterface;
                while (label == null && currentInterface != null) {
                    label = getToolTipTextFromMessageSource(useDefaultLocale, currentInterface, currentPropertyId, args);
                    currentInterface = currentInterface.getSuperclass();
                }
                if (label != null) break;
            }

            if (label != null) break;
        }

        if (label == null) {
            label = getToolTipTextFromMessageSource(useDefaultLocale, getBeanPropertyType().getType(), null, args);
        }

        if (label != null && label.contains("{0}") && args.length == 0) {
            return null;
        } else {
            if (label == null && !useDefaultLocale) {
                return getToolTipTextFromMessageSource(true, args);
            } else {
                return label;
            }
        }
    }

    private String getToolTipTextFromMessageSource(boolean useDefaultLocale, Class type, String propertyId,
                                                   Object... args) {
        String fullPropertyPath = type.getName() + (propertyId == null ? "" : "." + propertyId) + ".toolTip";
        if (useDefaultLocale) {
            return getFieldSet().domainMessageSourceNoFallback.getOptionalToolTipFromDefaultLocale(fullPropertyPath, args);
        } else {
            return getFieldSet().domainMessageSourceNoFallback.getOptionalToolTip(fullPropertyPath, args);
        }
    }

    private String getToolTipTextFromAnnotation() {
        Class propertyContainerType = getBeanPropertyType().getContainerType();
        String propertyIdRelativeToContainerType = getBeanPropertyType().getId();
        PropertyDescriptor descriptor = BeanUtils.getPropertyDescriptor(propertyContainerType,
                propertyIdRelativeToContainerType);
        Method method = descriptor.getReadMethod();
        ToolTip toolTipAnnotation = method.getAnnotation(ToolTip.class);
        if (toolTipAnnotation == null) {
            return null;
        } else {
            return toolTipAnnotation.value();
        }
    }

    /**
     * Get the description displayed during mouse-over/hovering
     *
     * @return description displayed to user
     */
    public String getToolTip() {
        return getField().getDescription();
    }

    /**
     * Set the description displayed during mouse-over/hovering
     *
     * @param toolTip description displayed to user
     */
    public void setToolTip(String toolTip) {
        if (toolTip != null) {
            getField().setDescription(toolTip);
        }
    }

    public void setToolTipArgs(Object... args) {
        setToolTip(generateTooltip(args));
    }

    /**
     * Set whether or not field is enabled.
     *
     * @param isEnabled true if enabled
     */
    public void setEnabled(boolean isEnabled) {
        getField().setEnabled(isEnabled);
    }

    /**
     * Set whether or not field is read-only.
     *
     * @param isReadOnly true if read-only
     */
    public void setReadOnly(boolean isReadOnly) {
        if (getField() instanceof SelectField) {
            ((SelectField) getField()).setButtonVisible(!isReadOnly);
        }

        if (!(getField() instanceof ListSelect)) {
            getField().setReadOnly(isReadOnly);
        }
    }

    /**
     * Restore read-only setting to originally configured value
     */
    public void restoreIsReadOnly() {
        if (getField() instanceof SelectField) {
            ((SelectField) getField()).setButtonVisible(!isReadOnly);
        }
        getField().setReadOnly(isReadOnly);
    }

    /**
     * Set the value of the field.
     *
     * @param value value of field
     */
    public void setValue(Object value) {
        getField().setValue(value);
    }

    /**
     * Asks if field currently has an error.
     *
     * @return true if field currently has an error
     */
    public boolean hasError() {
        if (hasConversionError) {
            return true;
        } else if (getField() instanceof AbstractComponent) {
            AbstractComponent abstractComponent = (AbstractComponent) getField();
            return abstractComponent.getComponentError() != null || hasIsRequiredError();
        } else {
            return false;
        }
    }

    /**
     * Ask if field currently has error because field is empty but is required
     *
     * @return true if field currently has error because field is empty but is required
     */
    public boolean hasIsRequiredError() {
        return getField().isRequired() && StringUtil.isEmpty(getField().getValue());
    }

    /**
     * Clear any errors on this field.
     *
     * @param clearConversionError true to clear data-type conversion error as well
     */
    public void clearError(boolean clearConversionError) {
        if (clearConversionError) {
            hasConversionError = false;
        }
        if (getField() instanceof AbstractComponent) {
            AbstractComponent abstractComponent = (AbstractComponent) getField();
            abstractComponent.setComponentError(null);
        }
    }

    /**
     * Ask if this field has a data-type conversion error.
     *
     * @return true if this field as a data-type conversion error
     */
    public boolean hasConversionError() {
        return hasConversionError;
    }

    /**
     * Set whether or not this field has a data-type conversion error.
     *
     * @param hasConversionError true if this field as a data-type conversion error
     */
    public void setHasConversionError(boolean hasConversionError) {
        this.hasConversionError = hasConversionError;
    }

    /**
     * Add error message to this field.
     *
     * @param errorMessage error message, builds Vaadin composite error message
     */
    public void addError(ErrorMessage errorMessage) {
        Assert.PROGRAMMING.instanceOf(getField(), AbstractComponent.class,
                "Error message cannot be added to field that is not an AbstractComponent");

        AbstractComponent abstractComponent = (AbstractComponent) getField();
        ErrorMessage existingErrorMessage = abstractComponent.getComponentError();
        if (existingErrorMessage == null) {
            abstractComponent.setComponentError(errorMessage);
        } else if (existingErrorMessage instanceof CompositeErrorMessage) {
            CompositeErrorMessage existingCompositeErrorMessage = (CompositeErrorMessage) existingErrorMessage;
            Iterator<ErrorMessage> iterator = existingCompositeErrorMessage.iterator();
            Set<ErrorMessage> newErrorMessages = new LinkedHashSet<ErrorMessage>();
            while (iterator.hasNext()) {
                ErrorMessage next = iterator.next();
                newErrorMessages.add(next);
            }
            newErrorMessages.add(errorMessage);
            CompositeErrorMessage newCompositeErrorMessage = new CompositeErrorMessage(newErrorMessages);
            abstractComponent.setComponentError(newCompositeErrorMessage);
        } else {
            Set<ErrorMessage> newErrorMessages = new LinkedHashSet<ErrorMessage>();
            newErrorMessages.add(existingErrorMessage);
            newErrorMessages.add(errorMessage);
            CompositeErrorMessage newCompositeErrorMessage = new CompositeErrorMessage(newErrorMessages);
            abstractComponent.setComponentError(newCompositeErrorMessage);
        }
    }

    @Override
    public PropertyFormatter getPropertyFormatter() {
        if (getField() instanceof AbstractTextField) {
            return super.getPropertyFormatter();
        } else {
            return new EmptyPropertyFormatter();
        }
    }

    private Field generateField() {
        Class propertyType = getPropertyType();

        if (propertyType == null) {
            return null;
        }

        if (Date.class.isAssignableFrom(propertyType)) {
            return new DateField();
        }

        if (boolean.class.isAssignableFrom(propertyType) || Boolean.class.isAssignableFrom(propertyType)) {
            return new CheckBox();
        }

        if (ReferenceEntity.class.isAssignableFrom(propertyType)) {
            return new Select();
        }

        if (Currency.class.isAssignableFrom(propertyType)) {
            return new Select();
        }

        if (propertyType.isEnum()) {
            return new Select();
        }

        if (Collection.class.isAssignableFrom(propertyType)) {
            return new ListSelect();
        }

        if (getBeanPropertyType().hasAnnotation(Lob.class)) {
            return new RichTextArea();
        }

        return new TextField();
    }

    private void initializeFieldDefaults() {
        if (field == null) {
            return;
        }

        field.setInvalidAllowed(true);

        if (field instanceof AbstractField) {
            initAbstractFieldDefaults((AbstractField) field);
        }

        if (field instanceof AbstractTextField) {
            initTextFieldDefaults((AbstractTextField) field);
            initWidthAndMaxLengthDefaults((AbstractTextField) field);
        }

        if (field instanceof RichTextArea) {
            initRichTextFieldDefaults((RichTextArea) field);
        }

        if (field instanceof DateField) {
            initDateFieldDefaults((DateField) field);
        }

        if (field instanceof AbstractSelect) {
            initAbstractSelectDefaults((AbstractSelect) field);

            if (field instanceof Select) {
                initSelectDefaults((Select) field);
            }

            if (field instanceof ListSelect) {
                initListSelectDefaults((ListSelect) field);
            }

            Class valueType = getPropertyType();
            if (getBeanPropertyType().isCollectionType()) {
                valueType = getBeanPropertyType().getCollectionValueType();
            }

            List referenceEntities = null;
            if (Currency.class.isAssignableFrom(valueType)) {
                referenceEntities = CurrencyUtil.getAvailableCurrencies();
                ((AbstractSelect) field).setItemCaptionPropertyId("currencyCode");
            } else if (valueType.isEnum()) {
                Object[] enumConstants = valueType.getEnumConstants();
                referenceEntities = Arrays.asList(enumConstants);
            } else if (ReferenceEntity.class.isAssignableFrom(valueType)) {
                EntityDao propertyDao = SpringApplicationContext.getBeanByTypeAndGenericArgumentType(EntityDao.class,
                        valueType);
                if (propertyDao != null) {
                    referenceEntities = propertyDao.findAll();
                } else {
                    referenceEntities = referenceEntityDao.findAll(valueType);
                }
            }

            if (referenceEntities != null) {
                setSelectItems(referenceEntities);
            }
        }


        if (getFormFieldSet().isEntityForm()) {
            if (getBeanPropertyType().isValidatable()) {
                initializeIsRequired();
                initializeValidators();
            }

            // Change listener causes erratic behavior for RichTextArea
            if (!(field instanceof RichTextArea)) {
                field.addListener(new FieldValueChangeListener());
            }
        }

        isReadOnly = field.isReadOnly();
        isVisible = field.isVisible();
    }

    private void initializeValidators() {
        if (field instanceof AbstractTextField) {
            if (getBeanPropertyType().getBusinessType() != null &&
                    getBeanPropertyType().getBusinessType().equals(BeanPropertyType.BusinessType.NUMBER)) {
                addValidator(new NumberConversionValidator(this));
            }
        }
    }

    /**
     * Add Vaadin validator to this field.
     *
     * @param validator Vaadin validator
     */
    public void addValidator(Validator validator) {
        getField().addValidator(validator);
    }

    private void initializeIsRequired() {
        BeanValidationValidator validator = new BeanValidationValidator(getBeanPropertyType().getContainerType(),
                getBeanPropertyType().getId());
        if (validator.isRequired()) {
            field.setRequired(true);
            field.setRequiredError(
                    MainApplication.getInstance().validationMessageSource.getMessage(
                            "com.expressui.core.view.field.FormField.required.message")
            );
        }

        isRequired = field.isRequired();
    }

    /**
     * Initialize field to default settings.
     *
     * @param field Vaadin field to initialize
     */
    public static void initAbstractFieldDefaults(AbstractField field) {
        field.setRequiredError(
                MainApplication.getInstance().validationMessageSource.getMessage(
                        "com.expressui.core.view.field.FormField.required.message")
        );
        field.setImmediate(true);
        field.setInvalidCommitted(false);
        field.setWriteThrough(true);
    }

    /**
     * Initialize field to default settings.
     *
     * @param field Vaadin field to initialize
     */
    public static void initTextFieldDefaults(AbstractTextField field) {
        field.setWidth(DEFAULT_TEXT_FIELD_WIDTH, Sizeable.UNITS_EM);
        field.setNullRepresentation("");
        field.setNullSettingAllowed(true);
    }

    /**
     * Initialize field to default settings.
     *
     * @param field Vaadin field to initialize
     */
    public static void initRichTextFieldDefaults(RichTextArea field) {
        field.setNullRepresentation("");
        field.setNullSettingAllowed(false);
    }

    /**
     * Initialize field to default settings.
     *
     * @param field Vaadin field to initialize
     */
    public static void initDateFieldDefaults(DateField field) {
        field.setResolution(DateField.RESOLUTION_DAY);
    }

    /**
     * Initialize field to default settings.
     *
     * @param field Vaadin field to initialize
     */
    public void initAbstractSelectDefaults(AbstractSelect field) {
        field.setWidth(DEFAULT_SELECT_FIELD_WIDTH, Sizeable.UNITS_EM);
        field.setItemCaptionMode(Select.ITEM_CAPTION_MODE_PROPERTY);
        if (getBeanPropertyType().hasAnnotation(NotNull.class) || getBeanPropertyType().hasAnnotation(NotEmpty.class)
                || getBeanPropertyType().hasAnnotation(NotBlank.class)) {
            field.setNullSelectionAllowed(false);
        } else {
            field.setNullSelectionAllowed(true);
        }

        field.setItemCaptionPropertyId(ReferenceEntity.DISPLAY_PROPERTY);
    }

    /**
     * Initialize field to default settings.
     *
     * @param field Vaadin field to initialize
     */
    public static void initSelectDefaults(Select field) {
        field.setFilteringMode(Select.FILTERINGMODE_CONTAINS);
    }

    /**
     * Initialize field to default settings.
     *
     * @param field Vaadin field to initialize
     */
    public static void initListSelectDefaults(ListSelect field) {
        field.setMultiSelect(true);
    }

    private class FieldValueChangeListener implements Property.ValueChangeListener {
        @Override
        public void valueChange(Property.ValueChangeEvent event) {
            EntityForm entityForm = (EntityForm) getFormFieldSet().getForm();

            if (entityForm.isValidationEnabled()) {
                entityForm.validate(false);
            }
        }
    }

    /**
     * Mode for automatically adjusting field widths
     */
    public enum AutoAdjustWidthMode {
        /**
         * Fully automatic
         */
        FULL,
        /**
         * Automatic but with minimum width specified by DEFAULT_TEXT_FIELD_WIDTH and DEFAULT_SELECT_FIELD_WIDTH
         */
        PARTIAL,
        /**
         * Turn off automatic width adjustment
         */
        NONE
    }
}
