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
import com.expressui.core.util.MethodDelegate;
import com.expressui.core.util.assertion.Assert;
import com.expressui.core.view.EntityForm;
import com.expressui.core.view.FormComponent;
import com.expressui.core.view.SearchForm;
import com.expressui.core.view.layout.LeftLabelGridLayout;
import com.expressui.core.view.layout.TopLabelGridLayout;
import com.vaadin.data.Validator;
import com.vaadin.terminal.ErrorMessage;
import com.vaadin.ui.*;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

@Component
@Scope("prototype")
public class FormFields extends DisplayFields {

    @Resource
    private SecurityService securityService;

    private FormComponent form;
    private Map<String, AddRemoveMethodDelegate> optionalTabs = new HashMap<String, AddRemoveMethodDelegate>();

    public FormComponent getForm() {
        return form;
    }

    public void setForm(FormComponent form) {
        this.form = form;
        setEntityType(form.getEntityType());
    }

    public int getColumns() {
        return getColumns("");
    }

    public int getColumns(String tabName) {
        int columns = 0;
        Collection<DisplayField> fields = getFields();
        for (DisplayField field : fields) {
            FormField formField = (FormField) field;
            if (formField.getTabName().equals(tabName)) {
                columns = Math.max(columns, formField.getColumnStart() - 1);
                if (formField.getColumnEnd() != null) {
                    columns = Math.max(columns, formField.getColumnEnd() - 1);
                }
            }
        }

        return ++columns;
    }

    public int getRows() {
        return getRows("");
    }

    public int getRows(String tabName) {
        int rows = 0;
        Collection<DisplayField> fields = getFields();
        for (DisplayField field : fields) {
            FormField formField = (FormField) field;
            if (formField.getTabName().equals(tabName)) {
                rows = Math.max(rows, formField.getRowStart() - 1);
                if (formField.getRowEnd() != null) {
                    rows = Math.max(rows, formField.getRowEnd() - 1);
                }
            }
        }

        return ++rows;
    }

    public GridLayout createGridLayout() {
        return createGridLayout(getFirstTabName());
    }

    public String getFirstTabName() {
        return getTabNames().iterator().next();
    }

    public GridLayout createGridLayout(String tabName) {
        GridLayout gridLayout;
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
        return new FormField(this, propertyId);
    }

    public void setPosition(String tabName, String propertyId, int rowStart, int columnStart) {
        setPosition(tabName, propertyId, rowStart, columnStart, null, null);
    }

    public void setPosition(String propertyId, int rowStart, int columnStart) {
        setPosition(propertyId, rowStart, columnStart, null, null);
    }

    public void setPosition(String propertyId, int rowStart, int columnStart, Integer rowEnd, Integer columnEnd) {
        setPosition("", propertyId, rowStart, columnStart, rowEnd, columnEnd);
    }

    public void setPosition(String tabName, String propertyId, int rowStart, int columnStart, Integer rowEnd, Integer columnEnd) {
        Assert.PROGRAMMING.assertTrue(rowStart > 0,
                "rowStart arg must be greater than 0 for property " + propertyId + (tabName.isEmpty() ? "" : ", for tab " + tabName));
        Assert.PROGRAMMING.assertTrue(columnStart > 0,
                "columnStart arg must be greater than 0 for property " + propertyId + (tabName.isEmpty() ? "" : ", for tab " + tabName));

        FormField formField = (FormField) getField(propertyId);
        formField.setTabName(tabName);
        formField.setColumnStart(columnStart);
        formField.setRowStart(rowStart);
        formField.setColumnEnd(columnEnd);
        formField.setRowEnd(rowEnd);
    }

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

    public AddRemoveMethodDelegate getTabAddRemoveDelegate(String tabName) {
        return optionalTabs.get(tabName);
    }

    public boolean isTabOptional(String tabName) {
        return optionalTabs.containsKey(tabName);
    }

    public boolean hasOptionalTabs() {
        return !optionalTabs.isEmpty();
    }

    public void setTabOptional(String tabName, Object addTarget, String addMethod,
                               Object removeTarget, String removeMethod) {

        MethodDelegate addMethodDelegate = new MethodDelegate(addTarget, addMethod);
        MethodDelegate removeMethodDelegate = new MethodDelegate(removeTarget, removeMethod);
        AddRemoveMethodDelegate addRemoveMethodDelegate = new AddRemoveMethodDelegate(addMethodDelegate,
                removeMethodDelegate);

        optionalTabs.put(tabName, addRemoveMethodDelegate);
    }

    public FormField getFormField(String propertyId) {
        return (FormField) getField(propertyId);
    }

    public void setField(String propertyId, Field field) {
        FormField formField = (FormField) getField(propertyId);
        formField.setField(field);
    }

    public boolean containsPropertyId(String tabName, String propertyId) {
        return containsPropertyId(propertyId) && getFormField(propertyId).getTabName().equals(tabName);
    }

    public Set<FormField> getFormFields(String tabName) {
        Set<FormField> formFields = new HashSet<FormField>();
        Collection<DisplayField> displayFields = getFields();
        for (DisplayField displayField : displayFields) {
            FormField formField = (FormField) displayField;
            if (formField.getTabName().equals(tabName)) {
                formFields.add(formField);
            }
        }

        return formFields;
    }

    public Set<FormField> getFormFields() {
        Set<FormField> formFields = new HashSet<FormField>();
        Collection<DisplayField> displayFields = getFields();
        for (DisplayField displayField : displayFields) {
            FormField formField = (FormField) displayField;
            formFields.add(formField);
        }

        return formFields;
    }

    public void clearErrors(boolean clearConversionErrors) {
        Collection<DisplayField> fields = getFields();
        for (DisplayField field : fields) {
            FormField formField = (FormField) field;
            formField.clearError(clearConversionErrors);
        }
    }

    public void clearErrors(String tabName, boolean clearConversionErrors) {
        Set<FormField> formFields = getFormFields(tabName);
        for (FormField formField : formFields) {
            formField.clearError(clearConversionErrors);
        }
    }

    public boolean hasError(String tabName) {
        Set<FormField> formFields = getFormFields(tabName);
        for (FormField formField : formFields) {
            if (formField.hasError()) {
                return true;
            }
        }

        return false;
    }

    public Set<String> getTabNames() {
        Set<String> tabNames = new LinkedHashSet<String>();
        Collection<DisplayField> displayFields = getFields();
        for (DisplayField displayField : displayFields) {
            FormField formField = (FormField) displayField;
            tabNames.add(formField.getTabName());
        }

        return tabNames;
    }

    public boolean hasTabs() {
        return getTabNames().size() > 1;
    }

    public Set<String> getViewableTabNames() {
        if (getForm() instanceof SearchForm) return getTabNames();

        Set<String> viewableTabNames = new LinkedHashSet<String>();
        Set<String> tabNames = getTabNames();
        for (String tabName : tabNames) {
            Set<FormField> fields = getFormFields(tabName);
            for (FormField field : fields) {
                if (securityService.getCurrentUser().isViewAllowed(getEntityType().getName(), field.getPropertyId())) {
                    viewableTabNames.add(tabName);
                    break;
                }
            }
        }

        return viewableTabNames;
    }

    public Set<FormField> getViewableFormFields() {
        if (getForm() instanceof SearchForm) return getFormFields();

        Set<FormField> viewableFormFields = new LinkedHashSet<FormField>();
        Set<FormField> formFields = getFormFields();
        for (FormField formField : formFields) {
            if (securityService.getCurrentUser().isViewAllowed(getEntityType().getName(), formField.getPropertyId())) {
                viewableFormFields.add(formField);
                break;
            }
        }

        return viewableFormFields;
    }

    public Set<FormField> getEditableFormFields() {
        if (getForm() instanceof SearchForm) return getFormFields();

        Set<FormField> editableFormFields = new LinkedHashSet<FormField>();
        Set<FormField> formFields = getFormFields();
        for (FormField formField : formFields) {
            if (securityService.getCurrentUser().isEditAllowed(getEntityType().getName(), formField.getPropertyId())) {
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

    public float getWidth(String propertyId) {
        return getFormField(propertyId).getWidth();
    }

    public void setWidth(String propertyId, float width, int unit) {
        getFormField(propertyId).setWidth(width, unit);
    }

    public void setHeight(String propertyId, float height, int unit) {
        getFormField(propertyId).setHeight(height, unit);
    }

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

    public void setAutoAdjustWidthMode(String propertyId, FormField.AutoAdjustWidthMode mode) {
        getFormField(propertyId).setAutoAdjustWidthMode(mode);
    }

    public void addValidator(String propertyId, Class<? extends Validator> validatorClass) {
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

    public String getDescription(String propertyId) {
        return getFormField(propertyId).getDescription();
    }

    public void setDescription(String propertyId, String description) {
        getFormField(propertyId).setDescription(description);
    }

    public void setSelectItems(String propertyId, List items) {
        getFormField(propertyId).setSelectItems(items);
    }

    public void setSelectItems(String propertyId, Map<Object, String> items) {
        getFormField(propertyId).setSelectItems(items);
    }

    public void setSelectItems(String propertyId, Map<Object, String> items, String nullCaption) {
        getFormField(propertyId).setSelectItems(items, nullCaption);
    }

    public void setMultiSelectDimensions(String propertyId, int rows, int columns) {
        getFormField(propertyId).setMultiSelectDimensions(rows, columns);
    }

    public void setVisible(String propertyId, boolean isVisible) {
        getFormField(propertyId).setVisible(isVisible);
    }

    public void setRequired(String propertyId, boolean isRequired) {
        getFormField(propertyId).setRequired(isRequired);
    }

    public void setEnabled(String propertyId, boolean isEnabled) {
        getFormField(propertyId).setEnabled(isEnabled);
    }

    public void setReadOnly(String propertyId, boolean isReadOnly) {
        getFormField(propertyId).setReadOnly(isReadOnly);
    }

    public void setReadOnly(boolean isReadOnly) {
        Collection<FormField> formFields = getFormFields();
        for (FormField formField : formFields) {
            formField.setReadOnly(isReadOnly);
        }
    }

    public void restoreIsReadOnly() {
        Collection<FormField> formFields = getFormFields();
        for (FormField formField : formFields) {
            formField.restoreIsReadOnly();
        }
    }

    public void applySecurityIsEditable() {
        Collection<FormField> formFields = getFormFields();
        for (FormField formField : formFields) {
            if (formField.getField() instanceof SelectField) {
                SelectField selectField = (SelectField) formField.getField();
                String toOneType = selectField.getEntitySelect().getEntityType().getName();
                if (!securityService.getCurrentUser().isEditAllowed(getEntityType().getName(), formField.getPropertyId())
                        || !securityService.getCurrentUser().isViewAllowed(toOneType)
                        || selectField.getEntitySelect().getResults().getDisplayFields().getViewablePropertyIds().isEmpty()) {
                    formField.setReadOnly(true);
                } else {
                    formField.restoreIsReadOnly();
                }
            } else {
                if (!securityService.getCurrentUser().isEditAllowed(getEntityType().getName(), formField.getPropertyId())) {
                    formField.setReadOnly(true);
                } else {
                    formField.restoreIsReadOnly();
                }
            }
            if (getForm() instanceof SearchForm
                    || securityService.getCurrentUser().isViewAllowed(getEntityType().getName(), formField.getPropertyId())) {
                formField.allowView();
            } else {
                formField.denyView();
            }
        }
    }

    public void setValue(String propertyId, Object value) {
        getFormField(propertyId).setValue(value);
    }

    public void setComponentError(String propertyId, ErrorMessage errorMessage) {
        Assert.PROGRAMMING.assertTrue(getFormField(propertyId).getField() instanceof AbstractComponent,
                "field is not of the right type");

        AbstractComponent abstractComponent = (AbstractComponent) getFormField(propertyId).getField();
        abstractComponent.setComponentError(errorMessage);
    }

    public void addValueChangeListener(String propertyId, Object target, String methodName) {
        FormField formField = (FormField) getField(propertyId);
        formField.addValueChangeListener(target, methodName);
    }

    public boolean isEntityForm() {
        return form instanceof EntityForm;
    }

    public static class AddRemoveMethodDelegate {
        private MethodDelegate addMethodDelegate;
        private MethodDelegate removeMethodDelegate;

        private AddRemoveMethodDelegate(MethodDelegate addMethodDelegate, MethodDelegate removeMethodDelegate) {
            this.addMethodDelegate = addMethodDelegate;
            this.removeMethodDelegate = removeMethodDelegate;
        }

        public MethodDelegate getAddMethodDelegate() {
            return addMethodDelegate;
        }

        public MethodDelegate getRemoveMethodDelegate() {
            return removeMethodDelegate;
        }
    }
}
