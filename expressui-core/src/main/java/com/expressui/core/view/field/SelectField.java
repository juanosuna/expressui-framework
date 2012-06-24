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

import com.expressui.core.util.StringUtil;
import com.expressui.core.view.entityselect.EntitySelect;
import com.expressui.core.view.form.TypedForm;
import com.expressui.core.view.util.MessageSource;
import com.vaadin.data.Property;
import com.vaadin.data.Validator;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.TextField;
import org.apache.commons.beanutils.PropertyUtils;
import org.vaadin.addon.customfield.CustomField;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;

/**
 * A custom field in a TypedForm for selecting an entity in a many-to-one relationship. EntitySelect represents the
 * popup selection window, whereas this custom field consists of a text input that displays a property in the selected
 * entity and the popup button for opening EntitySelect.
 *
 * @see EntitySelect
 */
public class SelectField extends CustomField {

    private MessageSource uiMessageSource;

    private TextField field;
    private EntitySelect entitySelect;

    private Button clearButton;
    private Button searchButton;

    private TypedForm typedForm;
    private String propertyId;

    /**
     * Construct in the given typed form for given property id
     *
     * @param typedForm    typed form that contains this field
     * @param propertyId   property id bound to this field
     * @param entitySelect popup component for selecting entity
     */
    public SelectField(TypedForm typedForm, String propertyId, EntitySelect entitySelect) {
        this.typedForm = typedForm;
        this.propertyId = propertyId;
        this.entitySelect = entitySelect;
        this.uiMessageSource = typedForm.uiMessageSource;
        initialize();
    }

    /**
     * Get popup component for selecting entity.
     *
     * @return component for selecting entity
     */
    public EntitySelect getEntitySelect() {
        return entitySelect;
    }

    /**
     * Make clear and search buttons (in)visible
     *
     * @param isVisible true to make visible
     */
    public void setButtonVisible(boolean isVisible) {
        clearButton.setVisible(isVisible);
        searchButton.setVisible(isVisible);
    }

    private void initialize() {
        setSizeUndefined();
        field = new TextField();
        FormField.initAbstractFieldDefaults(field);
        FormField.initTextFieldDefaults(field);
        field.setReadOnly(true);

        HorizontalLayout selectFieldLayout = new HorizontalLayout();
        String id = StringUtil.generateDebugId("e", this, selectFieldLayout, "selectFieldLayout");
        selectFieldLayout.setDebugId(id);
        selectFieldLayout.addComponent(field);

        searchButton = new Button();
        searchButton.setDescription(uiMessageSource.getToolTip("selectField.search.toolTip"));
        searchButton.setSizeUndefined();
        searchButton.addStyleName("borderless");
        searchButton.setIcon(new ThemeResource("../chameleon/img/magnifier.png"));
        searchButton.addListener(new Button.ClickListener() {
            public void buttonClick(Button.ClickEvent event) {
                entitySelect.open();
            }
        });
        selectFieldLayout.addComponent(searchButton);

        clearButton = new Button();
        clearButton.setDescription(uiMessageSource.getToolTip("selectField.clear.toolTip"));
        clearButton.setSizeUndefined();
        clearButton.addStyleName("borderless");
        clearButton.setIcon(new ThemeResource("../runo/icons/16/cancel.png"));
        selectFieldLayout.addComponent(clearButton);

        entitySelect.getResults().setSelectButtonListener(this, "itemSelected");
        clearButton.addListener(Button.ClickEvent.class, this, "itemCleared");

        setCompositionRoot(selectFieldLayout);
    }

    /**
     * Listener method invoked when user selects item.
     */
    public void itemSelected() {
        Object selectedValue = getSelectedValue();
        Object bean = typedForm.getBean();
        try {
            PropertyUtils.setProperty(bean, propertyId, selectedValue);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }

        Property property = field.getPropertyDataSource();
        field.setPropertyDataSource(property);
        entitySelect.close();
    }

    /**
     * Listener method invoked when user clicks clear button.
     */
    public void itemCleared() {
        Object bean = typedForm.getBean();
        try {
            PropertyUtils.setProperty(bean, propertyId, null);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        Property property = field.getPropertyDataSource();
        field.setPropertyDataSource(property);
    }

    /**
     * Get selected value.
     *
     * @return selected value
     */
    public Object getSelectedValue() {
        return entitySelect.getResults().getSelectedValue();
    }

    @Override
    public String getRequiredError() {
        return field.getRequiredError();
    }

    @Override
    public boolean isRequired() {
        return field.isRequired();
    }

    @Override
    public void setRequired(boolean required) {
        field.setRequired(required);
    }

    @Override
    public void setRequiredError(String requiredMessage) {
        field.setRequiredError(requiredMessage);
    }

    @Override
    public boolean isInvalidCommitted() {
        return field.isInvalidCommitted();
    }

    @Override
    public void setInvalidCommitted(boolean isCommitted) {
        field.setInvalidCommitted(isCommitted);
    }

    @Override
    public void commit() throws SourceException, Validator.InvalidValueException {
//        field.commit();
    }

    @Override
    public void discard() throws SourceException {
        field.discard();
    }

    @Override
    public boolean isModified() {
        return field.isModified();
    }

    @Override
    public boolean isReadThrough() {
        return field.isReadThrough();
    }

    @Override
    public boolean isWriteThrough() {
        return field.isWriteThrough();
    }

    @Override
    public void setReadThrough(boolean readThrough) throws SourceException {
        field.setReadThrough(readThrough);
    }

    @Override
    public void setWriteThrough(boolean writeThrough) throws SourceException,
            Validator.InvalidValueException {
        field.setWriteThrough(writeThrough);
    }

    @Override
    public void addValidator(Validator validator) {
        field.addValidator(validator);
    }

    @Override
    public Collection<Validator> getValidators() {
        return field.getValidators();
    }

    @Override
    public boolean isInvalidAllowed() {
        return field.isInvalidAllowed();
    }

    @Override
    public boolean isValid() {
        return field.isValid();
    }

    @Override
    public void removeValidator(Validator validator) {
        field.removeValidator(validator);

    }

    @Override
    public void setInvalidAllowed(boolean invalidValueAllowed)
            throws UnsupportedOperationException {
        field.setInvalidAllowed(invalidValueAllowed);
    }

    @Override
    public void validate() throws Validator.InvalidValueException {
        field.validate();
    }

    @Override
    public Class<?> getType() {
        return field.getType();
    }

    @Override
    public void setValue(Object newValue) throws ReadOnlyException,
            ConversionException {
        field.setValue(newValue);
    }

    @Override
    public void addListener(ValueChangeListener listener) {
        field.addListener(listener);
    }

    @Override
    public void addListener(Class<?> eventType, Object target, Method method) {
        field.addListener(eventType, target, method);
    }

    @Override
    public void addListener(Class<?> eventType, Object target, String methodName) {
        field.addListener(eventType, target, methodName);
    }

    @Override
    public void removeListener(ValueChangeListener listener) {
        field.removeListener(listener);
    }

    @Override
    public void removeListener(Class<?> eventType, Object target) {
        field.removeListener(eventType, target);
    }

    @Override
    public void removeListener(Class<?> eventType, Object target, Method method) {
        field.removeListener(eventType, target, method);
    }

    @Override
    public void valueChange(com.vaadin.data.Property.ValueChangeEvent event) {
        field.valueChange(event);
    }

    @Override
    public Property getPropertyDataSource() {
        return field.getPropertyDataSource();
    }

    @Override
    public void setPropertyDataSource(Property newDataSource) {
        field.setPropertyDataSource(newDataSource);

    }

    @Override
    public void focus() {
        field.focus();
    }

    @Override
    public int getTabIndex() {
        return field.getTabIndex();
    }

    @Override
    public void setTabIndex(int tabIndex) {
        field.setTabIndex(tabIndex);
    }

    @Override
    public Object getValue() {
        return field.getValue();
    }
}
