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

import com.expressui.core.view.EntityForm;
import com.expressui.core.view.util.MessageSource;
import com.expressui.core.view.entityselect.EntitySelect;
import com.vaadin.data.Property;
import com.vaadin.data.Validator;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.TextField;
import org.apache.commons.beanutils.PropertyUtils;
import org.vaadin.addon.customfield.CustomField;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;

public class SelectField extends CustomField {

    private MessageSource uiMessageSource;

    private TextField field;
    private EntitySelect entitySelect;

    private Button clearButton;
    private Button searchButton;

    private EntityForm entityForm;
    private String propertyId;

    public SelectField(EntityForm entityForm, String propertyId, EntitySelect entitySelect) {
        this.entityForm = entityForm;
        this.propertyId = propertyId;
        this.entitySelect = entitySelect;
        this.uiMessageSource = entityForm.getUiMessageSource();
        initialize();
    }

    public EntitySelect getEntitySelect() {
        return entitySelect;
    }

    public void setButtonVisible(boolean isVisible) {
        clearButton.setVisible(isVisible);
        searchButton.setVisible(isVisible);
    }

    public void initialize() {
        setSizeUndefined();
        field = new TextField();
        FormField.initAbstractFieldDefaults(field);
        FormField.initTextFieldDefaults(field);
        field.setReadOnly(true);

        HorizontalLayout layout = new HorizontalLayout();
        layout.addComponent(field);

        searchButton = new Button();
        searchButton.setDescription(uiMessageSource.getMessage("selectField.search.description"));
        searchButton.setSizeUndefined();
        searchButton.addStyleName("borderless");
        searchButton.setIcon(new ThemeResource("../chameleon/img/magnifier.png"));
        searchButton.addListener(new Button.ClickListener() {
            public void buttonClick(Button.ClickEvent event) {
                entitySelect.open();
            }
        });
        layout.addComponent(searchButton);

        clearButton = new Button();
        clearButton.setDescription(uiMessageSource.getMessage("selectField.clear.description"));
        clearButton.setSizeUndefined();
        clearButton.addStyleName("borderless");
        clearButton.setIcon(new ThemeResource("../runo/icons/16/cancel.png"));
        layout.addComponent(clearButton);

        entitySelect.getResults().setSelectButtonListener(this, "itemSelected");
        addClearListener(this, "itemCleared");

        setCompositionRoot(layout);
    }

    public void itemSelected() {
        Object selectedValue = getSelectedValue();
        Object entity = entityForm.getEntity();
        try {
            PropertyUtils.setProperty(entity, propertyId, selectedValue);
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

    public void itemCleared() {
        Object entity = entityForm.getEntity();
        try {
            PropertyUtils.setProperty(entity, propertyId, null);
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

    public void addClearListener(Object target, String methodName) {
        clearButton.addListener(Button.ClickEvent.class, target, methodName);
    }

    public Object getSelectedValue() {
        return entitySelect.getResults().getSelectedValue();
    }

    public String getRequiredError() {
        return field.getRequiredError();
    }

    public boolean isRequired() {
        return field.isRequired();
    }

    public void setRequired(boolean required) {
        field.setRequired(required);
    }

    public void setRequiredError(String requiredMessage) {
        field.setRequiredError(requiredMessage);
    }

    public boolean isInvalidCommitted() {
        return field.isInvalidCommitted();
    }

    public void setInvalidCommitted(boolean isCommitted) {
        field.setInvalidCommitted(isCommitted);
    }

    public void commit() throws SourceException, Validator.InvalidValueException {
//        field.commit();
    }

    public void discard() throws SourceException {
        field.discard();
    }

    public boolean isModified() {
        return field.isModified();
    }

    public boolean isReadThrough() {
        return field.isReadThrough();
    }

    public boolean isWriteThrough() {
        return field.isWriteThrough();
    }

    public void setReadThrough(boolean readThrough) throws SourceException {
        field.setReadThrough(readThrough);
    }

    public void setWriteThrough(boolean writeThrough) throws SourceException,
            Validator.InvalidValueException {
        field.setWriteThrough(writeThrough);
    }

    public void addValidator(Validator validator) {
        field.addValidator(validator);
    }

    public Collection<Validator> getValidators() {
        return field.getValidators();
    }

    public boolean isInvalidAllowed() {
        return field.isInvalidAllowed();
    }

    public boolean isValid() {
        return field.isValid();
    }

    public void removeValidator(Validator validator) {
        field.removeValidator(validator);

    }

    public void setInvalidAllowed(boolean invalidValueAllowed)
            throws UnsupportedOperationException {
        field.setInvalidAllowed(invalidValueAllowed);
    }

    public void validate() throws Validator.InvalidValueException {
        field.validate();
    }

    public Class<?> getType() {
        return field.getType();
    }

    public void setValue(Object newValue) throws ReadOnlyException,
            ConversionException {
        field.setValue(newValue);
    }

    public void addListener(ValueChangeListener listener) {
        field.addListener(listener);
    }

    public void removeListener(ValueChangeListener listener) {
        field.removeListener(listener);
    }

    public void valueChange(com.vaadin.data.Property.ValueChangeEvent event) {
        field.valueChange(event);
    }

    public Property getPropertyDataSource() {
        return field.getPropertyDataSource();
    }

    public void setPropertyDataSource(Property newDataSource) {
        field.setPropertyDataSource(newDataSource);

    }

    public void focus() {
        field.focus();
    }

    public int getTabIndex() {
        return field.getTabIndex();
    }

    public void setTabIndex(int tabIndex) {
        field.setTabIndex(tabIndex);
    }

    public Object getValue() {
        return field.getValue();
    }
}
