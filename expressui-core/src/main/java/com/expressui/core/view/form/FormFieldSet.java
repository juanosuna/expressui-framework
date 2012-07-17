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

package com.expressui.core.view.form;

import com.expressui.core.util.MethodDelegate;
import com.expressui.core.util.assertion.Assert;
import com.expressui.core.validation.AbstractConversionValidator;
import com.expressui.core.view.field.DisplayField;
import com.expressui.core.view.field.FieldSet;
import com.expressui.core.view.field.FormField;
import com.expressui.core.view.field.SelectField;
import com.expressui.core.view.form.layout.FormGridLayout;
import com.expressui.core.view.form.layout.LeftLabelGridLayout;
import com.expressui.core.view.form.layout.TopLabelGridLayout;
import com.vaadin.data.Validator;
import com.vaadin.terminal.ErrorMessage;
import com.vaadin.ui.*;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

import static org.springframework.beans.factory.config.BeanDefinition.SCOPE_PROTOTYPE;

/**
 * Collection of fields for display in a form component, along with configuration information about
 * how to position and display these fields.
 */
@Component
@Scope(SCOPE_PROTOTYPE)
public class FormFieldSet extends FieldSet {

    private TypedForm form;
    private Map<String, AddRemoveTabMethodDelegate> optionalTabs = new HashMap<String, AddRemoveTabMethodDelegate>();

    /**
     * Gets form component that contains these fields.
     *
     * @return form component containing these fields
     */
    public TypedForm getForm() {
        return form;
    }

    /**
     * Sets the form component that contains these fields.
     *
     * @param form component containing these fields
     */
    public void setForm(TypedForm form) {
        this.form = form;
        setType(form.getType());
    }

    /**
     * Creates a form tab that contains a subset of these fields.
     *
     * @param tabName name of the tab
     * @return newly created form tab
     */
    public FormTab createTab(String tabName) {
        Assert.PROGRAMMING.notNull(tabName);
        return new FormTab(this, tabName);
    }

    /**
     * Gets number of columns in this form, assuming form has no tabs.
     *
     * @return number of columns in this form
     */
    public int getColumns() {
        return getColumns("");
    }

    /**
     * Gets number of columns in this form's tab.
     *
     * @param tabName name of tab
     * @return number of columns in this form's tab
     */
    public int getColumns(String tabName) {
        int columns = 0;
        Collection<DisplayField> displayFields = getFields();
        for (DisplayField displayField : displayFields) {
            FormField formField = (FormField) displayField;
            if (formField.getTabName().equals(tabName)) {
                columns = Math.max(columns, formField.getColumnStart() - 1);
                if (formField.getColumnEnd() != null) {
                    columns = Math.max(columns, formField.getColumnEnd() - 1);
                }
            }
        }

        return ++columns;
    }

    /**
     * Gets number of rows in this form, assuming form has no tabs.
     *
     * @return number of rows in this form
     */
    public int getRows() {
        return getRows("");
    }

    /**
     * Gets number of rows in this form's tab.
     *
     * @param tabName name of tab
     * @return number of rows in this form's tab
     */
    public int getRows(String tabName) {
        int rows = 0;
        Collection<DisplayField> displayFields = getFields();
        for (DisplayField displayField : displayFields) {
            FormField formField = (FormField) displayField;
            if (formField.getTabName().equals(tabName)) {
                rows = Math.max(rows, formField.getRowStart() - 1);
                if (formField.getRowEnd() != null) {
                    rows = Math.max(rows, formField.getRowEnd() - 1);
                }
            }
        }

        return ++rows;
    }

    /**
     * Gets the name of the first tab in this form.
     *
     * @return name of first tab
     */
    public String getFirstTabName() {
        return getTabNames().iterator().next();
    }

    /**
     * Creates form grid layout for containing form fields, assuming form has no tabs.
     *
     * @return form grid layout
     */
    public Layout createGridLayout() {
        return createGridLayout(getFirstTabName());
    }

    /**
     * Creates form grid layout for containing form fields in given tab;
     *
     * @param tabName tab subsection of form
     * @return grid layout for tab
     */
    public Layout createGridLayout(String tabName) {
        FormGridLayout gridLayout;
        if (form instanceof EntityForm) {
            gridLayout = new LeftLabelGridLayout(getColumns(tabName), getRows(tabName));
        } else {
            gridLayout = new TopLabelGridLayout(getColumns(tabName), getRows(tabName));
        }
        gridLayout.setMargin(true, true, true, true);
        gridLayout.setSpacing(true);
        gridLayout.setSizeUndefined();

        return gridLayout;
    }

    @Override
    protected FormField createField(String propertyId) {
        Assert.PROGRAMMING.isTrue(!containsPropertyId(propertyId), "Field has already been created for property "
                + propertyId);

        FormField formField = new FormField(this, propertyId);
        addField(propertyId, formField);

        return formField;
    }

    /**
     * Sets the position of a field in the form's tab, where the field occupies just one cell.
     *
     * @param tabName     tab to place field
     * @param propertyId  property id in entity to bind field to
     * @param rowStart    row start coordinate
     * @param columnStart column start coordinate
     */
    public void setCoordinates(String tabName, String propertyId, int rowStart, int columnStart) {
        setCoordinates(tabName, propertyId, rowStart, columnStart, null, null);
    }

    /**
     * Sets the position of a field in the form, where the field occupies just one cell in form without tabs.
     *
     * @param propertyId  property id in entity to bind field to
     * @param rowStart    row start coordinate
     * @param columnStart column start coordinate
     */
    public void setCoordinates(String propertyId, int rowStart, int columnStart) {
        setCoordinates(propertyId, rowStart, columnStart, null, null);
    }

    /**
     * Sets the position of a field in the form without tabs, where field can span multiple cells.
     *
     * @param propertyId  property id in entity to bind field to
     * @param rowStart    row start coordinate
     * @param columnStart column start coordinate
     * @param rowEnd      row end coordinate, field height is stretched to row end coordinate
     * @param columnEnd   column end coordinate, field width is stretched to column end coordinate
     */
    public void setCoordinates(String propertyId, int rowStart, int columnStart, Integer rowEnd, Integer columnEnd) {
        setCoordinates("", propertyId, rowStart, columnStart, rowEnd, columnEnd);
    }

    /**
     * Sets the position of a field in the form's tab, where field can span multiple cells.
     *
     * @param tabName     tab to place field
     * @param propertyId  property id in entity to bind field to
     * @param rowStart    row start coordinate
     * @param columnStart column start coordinate
     * @param rowEnd      row end coordinate, field height is stretched to row end coordinate
     * @param columnEnd   column end coordinate, field width is stretched to column end coordinate
     */
    public void setCoordinates(String tabName, String propertyId, int rowStart, int columnStart, Integer rowEnd, Integer columnEnd) {
        Assert.PROGRAMMING.notNull(tabName);
        Assert.PROGRAMMING.isTrue(rowStart > 0,
                "rowStart arg must be greater than 0 for property " + propertyId + (tabName.isEmpty() ? "" : ", for tab " + tabName));
        Assert.PROGRAMMING.isTrue(columnStart > 0,
                "columnStart arg must be greater than 0 for property " + propertyId + (tabName.isEmpty() ? "" : ", for tab " + tabName));

        FormField formField = createField(propertyId);
        formField.setTabName(tabName);
        formField.setRowStart(rowStart);
        formField.setColumnStart(columnStart);
        formField.setRowEnd(rowEnd);
        formField.setColumnEnd(columnEnd);
    }

    /**
     * Asserts that all the FormFields in this form are valid.
     */
    public void assertValid() {
        Collection<FormField> formFields = getFormFields();
        for (FormField formField : formFields) {
            formField.assertValid();
        }
    }

    /**
     * Finds FormField based on the Vaadin field it contains.
     *
     * @param field Vaadin field for looking up FormField
     * @return FormField
     */
    public FormField findByField(Field field) {
        Collection<DisplayField> displayFields = getFields();
        for (DisplayField displayField : displayFields) {
            FormField formField = (FormField) displayField;
            if (formField.getField().equals(field)) {
                return formField;
            }
        }

        return null;
    }

    /**
     * Gets method delegate that listens for adding and removing a tab.
     *
     * @param tabName name of tab to trigger the delegate
     * @return delegate
     */
    public AddRemoveTabMethodDelegate getTabAddRemoveDelegate(String tabName) {
        return optionalTabs.get(tabName);
    }

    /**
     * Asks if the given tab is optional.
     *
     * @param tabName name of tab
     * @return true if optional
     */
    public boolean isTabOptional(String tabName) {
        return optionalTabs.containsKey(tabName);
    }

    /**
     * Asks if form contains any optional tabs.
     *
     * @return true if form contains optional tabs
     */
    public boolean hasOptionalTabs() {
        return !optionalTabs.isEmpty();
    }

    /**
     * Sets a tab as optional.
     *
     * @param tabName      name of tab to set
     * @param addTarget    target object to invoke method on, when tab is added
     * @param addMethod    method to invoke, when tab is added
     * @param removeTarget target object to invoke method on, when tab is removed
     * @param removeMethod method to invoke, when tab is removed
     */
    public void setTabOptional(String tabName, Object addTarget, String addMethod,
                               Object removeTarget, String removeMethod) {

        MethodDelegate addMethodDelegate = new MethodDelegate(addTarget, addMethod);
        MethodDelegate removeMethodDelegate = new MethodDelegate(removeTarget, removeMethod);
        AddRemoveTabMethodDelegate addRemoveTabMethodDelegate = new AddRemoveTabMethodDelegate(addMethodDelegate,
                removeMethodDelegate);

        optionalTabs.put(tabName, addRemoveTabMethodDelegate);
    }

    /**
     * Gets FormField bound to given property.
     *
     * @param propertyId property id (name)
     * @return FormField bound to property
     */
    public FormField getFormField(String propertyId) {
        return (FormField) getField(propertyId);
    }

    /**
     * Sets Vaadin field to be displayed.
     *
     * @param propertyId property id to identify FormField
     * @param field      Vaadin field
     */
    public void setField(String propertyId, Field field) {
        FormField formField = (FormField) getField(propertyId);
        formField.setField(field);
    }

    /**
     * Asks if this form contains property in the given tab.
     *
     * @param tabName    name of tab
     * @param propertyId property id
     * @return true if this form contains property in the given tab
     */
    public boolean containsPropertyId(String tabName, String propertyId) {
        Assert.PROGRAMMING.notNull(getFormField(propertyId).getTabName());
        return containsPropertyId(propertyId) && getFormField(propertyId).getTabName().equals(tabName);
    }

    /**
     * Gets all the FormFields in given tab.
     *
     * @param tabName name of tab
     * @return all the FormField under the tab
     */
    public Set<FormField> getFormFields(String tabName) {
        Set<FormField> formFields = new HashSet<FormField>();
        Collection<DisplayField> displayFields = getFields();
        for (DisplayField displayField : displayFields) {
            FormField formField = (FormField) displayField;
            if (formField.getTabName() != null && formField.getTabName().equals(tabName)) {
                formFields.add(formField);
            }
        }

        return formFields;
    }

    /**
     * Gets all the FormFields in all tabs.
     *
     * @return all FormFields
     */
    public Set<FormField> getFormFields() {
        Set<FormField> formFields = new HashSet<FormField>();
        Collection<DisplayField> displayFields = getFields();
        for (DisplayField displayField : displayFields) {
            FormField formField = (FormField) displayField;
            formFields.add(formField);
        }

        return formFields;
    }

    /**
     * Clears all errors associated with all fields.
     *
     * @param clearConversionErrors true to clear conversion errors as well
     */
    public void clearErrors(boolean clearConversionErrors) {
        Collection<DisplayField> displayFields = getFields();
        for (DisplayField displayField : displayFields) {
            FormField formField = (FormField) displayField;
            formField.clearError(clearConversionErrors);
        }
    }

    /**
     * Asks if any field has an error in a tab.
     *
     * @param tabName name of tab
     * @return true if any field has an error in given tab
     */
    public boolean hasError(String tabName) {
        Set<FormField> formFields = getFormFields(tabName);
        for (FormField formField : formFields) {
            if (formField.hasError()) {
                return true;
            }
        }

        return false;
    }

    /**
     * Gets names of all tabs in this form.
     *
     * @return names of all tabs
     */
    public Set<String> getTabNames() {
        Set<String> tabNames = new LinkedHashSet<String>();
        Collection<DisplayField> displayFields = getFields();
        for (DisplayField displayField : displayFields) {
            FormField formField = (FormField) displayField;
            if (formField.getTabName() != null) {
                tabNames.add(formField.getTabName());
            }
        }

        return tabNames;
    }

    /**
     * Asks if this form has tabs.
     *
     * @return true if this form has tabs
     */
    public boolean hasTabs() {
        return getTabNames().size() > 1;
    }

    /**
     * Gets all tabs that are viewable, based on whether each tab contains viewable fields
     * according to security permissions.
     *
     * @return all viewable tabs
     */
    public Set<String> getViewableTabNames() {
        if (getForm() instanceof SearchForm) return getTabNames();

        Set<String> viewableTabNames = new LinkedHashSet<String>();
        Set<String> tabNames = getTabNames();
        for (String tabName : tabNames) {
            Set<FormField> fields = getFormFields(tabName);
            for (FormField field : fields) {
                if (securityService.getCurrentUser().isViewAllowed(getType().getName(), field.getPropertyId())) {
                    viewableTabNames.add(tabName);
                    break;
                }
            }
        }

        return viewableTabNames;
    }

    /**
     * Gets all viewable FormFields, based on security permissions.
     *
     * @return all viewable form fields
     */
    public Set<FormField> getViewableFormFields() {
        if (getForm() instanceof SearchForm) return getFormFields();

        Set<FormField> viewableFormFields = new LinkedHashSet<FormField>();
        Set<FormField> formFields = getFormFields();
        for (FormField formField : formFields) {
            if (securityService.getCurrentUser().isViewAllowed(getType().getName(), formField.getPropertyId())) {
                viewableFormFields.add(formField);
                break;
            }
        }

        return viewableFormFields;
    }

    /**
     * Gets all editable FormFields, based on security permissions.
     *
     * @return all editable form fields
     */
    public Set<FormField> getEditableFormFields() {
        if (getForm() instanceof SearchForm) return getFormFields();

        Set<FormField> editableFormFields = new LinkedHashSet<FormField>();
        Set<FormField> formFields = getFormFields();
        for (FormField formField : formFields) {
            if (securityService.getCurrentUser().isEditAllowed(getType().getName(), formField.getPropertyId())) {
                editableFormFields.add(formField);
                break;
            }
        }

        return editableFormFields;
    }

    @Override
    public String getLabel(String propertyId) {
        if (((FormField) getField(propertyId)).getFieldLabel().getValue() == null) {
            return null;
        } else {
            return ((FormField) getField(propertyId)).getFieldLabel().getValue().toString();
        }
    }

    @Override
    public void setLabel(String propertyId, String label) {
        ((FormField) getField(propertyId)).setFieldLabel(label);
    }

    /**
     * Manually sets width of the field and turn off auto width adjustment.
     *
     * @param propertyId property id to identify field to set
     * @param width      size of width
     * @param unit       unit of measurement defined in Sizeable
     * @see com.vaadin.terminal.Sizeable
     */
    public void setWidth(String propertyId, float width, int unit) {
        getFormField(propertyId).setWidth(width, unit);
    }

    /**
     * Sets height of the field.
     *
     * @param propertyId property id to identify field to set
     * @param height     size of width
     * @param unit       unit of measurement defined in Sizeable
     * @see com.vaadin.terminal.Sizeable
     */
    public void setHeight(String propertyId, float height, int unit) {
        getFormField(propertyId).setHeight(height, unit);
    }

    /**
     * Automatically adjusts widths of fields based on their value contents.
     */
    public void autoAdjustWidths() {
        Set<FormField> formFields = getFormFields();
        for (FormField formField : formFields) {
            if (formField.getField() instanceof AbstractTextField) {
                formField.autoAdjustTextFieldWidth();
            } else if (formField.getField() instanceof AbstractSelect) {
                formField.autoAdjustSelectWidth();
            }
        }
    }

    /**
     * Sets auto-adjust-width mode.
     *
     * @param propertyId          property id to identify field to set
     * @param autoAdjustWidthMode auto-adjust-width mode
     */
    public void setAutoAdjustWidthMode(String propertyId, FormField.AutoAdjustWidthMode autoAdjustWidthMode) {
        getFormField(propertyId).setAutoAdjustWidthMode(autoAdjustWidthMode);
    }

    /**
     * Adds a validator to field.
     *
     * @param propertyId property id to identify field to be validated
     * @param validator  validator to attach to field
     */
    public void addValidator(String propertyId, Validator validator) {
        getFormField(propertyId).addValidator(validator);
    }

    /**
     * Adds a conversion validator to field by reflectively instantiating given class.
     *
     * @param propertyId     property id to identify field to be validated
     * @param validatorClass class of the validator that to be instantiated and set on the field
     */
    public void addConversionValidator(String propertyId, Class<? extends AbstractConversionValidator> validatorClass) {
        try {
            Constructor<? extends Validator> constructor = validatorClass.getConstructor(FormField.class);
            Validator validator = constructor.newInstance(getFormField(propertyId));
            getFormField(propertyId).addValidator(validator);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Adds a conversion validator to field.
     *
     * @param propertyId property id to identify field to be validated
     * @param validator  validator to be attached to the field
     */
    public void addConversionValidator(String propertyId, AbstractConversionValidator validator) {
        validator.setFormField(getFormField(propertyId));
        getFormField(propertyId).addValidator(validator);
    }

    /**
     * Gets the description displayed during mouse-over/hovering.
     *
     * @param propertyId property id to identify field to set
     * @return description displayed to user
     */
    public String getToolTip(String propertyId) {
        return getFormField(propertyId).getToolTip();
    }

    /**
     * Sets the description displayed during mouse-over/hovering.
     *
     * @param propertyId property id to identify field to set
     * @param toolTip    description displayed to user
     */
    public void setToolTip(String propertyId, String toolTip) {
        getFormField(propertyId).setToolTip(toolTip);
    }

    /**
     * Generates or re-generates tooltip, passing in arguments for interpolation using standard {0}, {1}, {2}
     * notation. This feature only works with resource bundle messages defined in domainMessages/.
     *
     * @param propertyId property id to identify field to set
     * @param args
     */
    public void setToolTipArgs(String propertyId, Object... args) {
        getFormField(propertyId).setToolTipArgs(args);
    }

    /**
     * Clears the menu options in a select.
     *
     * @param propertyId property id to identify field to set
     */
    public void clearSelectItems(String propertyId) {
        getFormField(propertyId).setSelectItems(new ArrayList());
    }

    /**
     * Sets the menu options in a select.
     *
     * @param propertyId property id to identify field to set
     * @param items      list of items
     *                   see com.expressui.core.entity.ReferenceEntity.DISPLAY_PROPERTY
     */
    public void setSelectItems(String propertyId, List items) {
        getFormField(propertyId).setSelectItems(items);
    }

    /**
     * Sets menu options in a select.
     *
     * @param propertyId property id to identify field to set
     * @param items      map of items where key is bound to entity and value is the display caption
     */
    public void setSelectItems(String propertyId, Map<Object, String> items) {
        getFormField(propertyId).setSelectItems(items);
    }

    /**
     * Sets menu options in a select.
     *
     * @param propertyId  property id to identify field to set
     * @param items       map of items where key is bound to entity and value is the display caption
     * @param nullCaption caption displayed to represent null or no selection
     */
    public void setSelectItems(String propertyId, Map<Object, String> items, String nullCaption) {
        getFormField(propertyId).setSelectItems(items, nullCaption);
    }

    /**
     * Sets the dimensions of a multi-select menu
     *
     * @param propertyId property id to identify field to set
     * @param rows       height
     * @param columns    width
     */
    public void setMultiSelectDimensions(String propertyId, int rows, int columns) {
        getFormField(propertyId).setMultiSelectDimensions(rows, columns);
    }

    /**
     * Sets the visibility of this field and label.
     *
     * @param propertyId property id to identify field to set
     * @param isVisible  true if visible
     */
    public void setVisible(String propertyId, boolean isVisible) {
        getFormField(propertyId).setVisible(isVisible);
    }

    /**
     * Sets whether or not this field is required.
     *
     * @param propertyId property id to identify field to set
     * @param isRequired true if required
     */
    public void setCurrentlyRequired(String propertyId, boolean isRequired) {
        getFormField(propertyId).setCurrentlyRequired(isRequired);
    }

    /**
     * Sets whether or not field is enabled.
     *
     * @param propertyId property id to identify field to set
     * @param isEnabled  true if enabled
     */
    public void setEnabled(String propertyId, boolean isEnabled) {
        getFormField(propertyId).setEnabled(isEnabled);
    }

    /**
     * Sets whether or not field is read-only.
     *
     * @param propertyId property id to identify field to set
     * @param isReadOnly true if read-only
     */
    public void setReadOnly(String propertyId, boolean isReadOnly) {
        getFormField(propertyId).setReadOnly(isReadOnly);
    }

    /**
     * Sets whether or not all fields are read-only.
     *
     * @param isReadOnly true if read-only
     */
    public void setReadOnly(boolean isReadOnly) {
        Collection<FormField> formFields = getFormFields();
        for (FormField formField : formFields) {
            formField.setReadOnly(isReadOnly);
        }
    }

    /**
     * Restores read-only setting for all fields, if they were temporarily changed for view-only mode.
     */
    public void restoreIsReadOnly() {
        Collection<FormField> formFields = getFormFields();
        for (FormField formField : formFields) {
            formField.restoreIsReadOnly();
        }
    }

    /**
     * Applies security is-editable permissions to all fields.
     */
    public void applySecurityIsEditable() {
        Collection<FormField> formFields = getFormFields();
        for (FormField formField : formFields) {
            if (formField.getField() instanceof SelectField) {
                SelectField selectField = (SelectField) formField.getField();
                String toOneType = selectField.getEntitySelect().getType().getName();
                if (!securityService.getCurrentUser().isEditAllowed(getType().getName(), formField.getPropertyId())
                        || !securityService.getCurrentUser().isViewAllowed(toOneType)
                        || selectField.getEntitySelect().getResults().getResultsFieldSet().getViewablePropertyIds().isEmpty()) {
                    formField.setReadOnly(true);
                } else {
                    formField.restoreIsReadOnly();
                }
            } else {
                if (!securityService.getCurrentUser().isEditAllowed(getType().getName(), formField.getPropertyId())) {
                    formField.setReadOnly(true);
                } else {
                    formField.restoreIsReadOnly();
                }
            }
            if (getForm() instanceof SearchForm
                    || securityService.getCurrentUser().isViewAllowed(getType().getName(), formField.getPropertyId())) {
                formField.allowView();
            } else {
                formField.denyView();
            }
        }
    }

    /**
     * Sets the value of the field.
     *
     * @param value value of field
     */
    public void setValue(String propertyId, Object value) {
        getFormField(propertyId).setValue(value);
    }

    /**
     * Sets component error.
     *
     * @param propertyId   property id to identify field to set
     * @param errorMessage error message
     */
    public void setComponentError(String propertyId, ErrorMessage errorMessage) {
        Assert.PROGRAMMING.instanceOf(getFormField(propertyId).getField(), AbstractComponent.class);

        AbstractComponent abstractComponent = (AbstractComponent) getFormField(propertyId).getField();
        abstractComponent.setComponentError(errorMessage);
    }

    /**
     * Adds change listener to field.
     *
     * @param propertyId property id to identify field to set
     * @param target     target of the listener
     * @param methodName listener method to invoke
     */
    public void addValueChangeListener(String propertyId, Object target, String methodName) {
        FormField formField = (FormField) getField(propertyId);
        formField.addValueChangeListener(target, methodName);
    }

    /**
     * Asks if this form is EntityForm.
     *
     * @return true if EntityForm
     */
    public boolean isEntityForm() {
        return form instanceof EntityForm;
    }

    /**
     * Delegate that contains methods for handling add and remove actions
     */
    public static class AddRemoveTabMethodDelegate {
        private MethodDelegate addTabMethodDelegate;
        private MethodDelegate removeTabMethodDelegate;

        private AddRemoveTabMethodDelegate(MethodDelegate addTabMethodDelegate, MethodDelegate removeTabMethodDelegate) {
            this.addTabMethodDelegate = addTabMethodDelegate;
            this.removeTabMethodDelegate = removeTabMethodDelegate;
        }

        /**
         * Gets delegate for handling add tab action.
         *
         * @return delegate for handling add tab action
         */
        public MethodDelegate getAddTabMethodDelegate() {
            return addTabMethodDelegate;
        }

        /**
         * Gets delegate for handling remove tab action.
         *
         * @return delegate for handling remove tab action
         */
        public MethodDelegate getRemoveTabMethodDelegate() {
            return removeTabMethodDelegate;
        }
    }
}
