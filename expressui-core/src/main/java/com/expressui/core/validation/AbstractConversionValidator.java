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

package com.expressui.core.validation;


import com.expressui.core.view.field.FormField;
import com.expressui.core.view.form.EntityForm;
import com.vaadin.data.Validator;

/**
 * Abstract superclass for implementing conversion validators, which validate whether a user-entered value
 * is convertible to specific data type.
 */
public abstract class AbstractConversionValidator implements Validator {
    private String errorMessage;
    private FormField formField;

    /**
     * Construct validator based on the form field the validator is bound to and error message if validation were to
     * fail.
     *
     * @param formField    form field that this conversion validator is bound to
     * @param errorMessage error message to display to user if validation fails
     */
    protected AbstractConversionValidator(FormField formField, String errorMessage) {
        this.formField = formField;
        this.errorMessage = errorMessage;
    }

    /**
     * Construct validator based on the form field the validator is bound to
     *
     * @param formField form field that this conversion validator is bound to
     */
    protected AbstractConversionValidator(FormField formField) {
        this.formField = formField;
    }

    protected AbstractConversionValidator() {
    }

    /**
     * Get error message that is displayed to user if validation were to fail. If error message is null, then this
     * class uses the message contained in any validation exception.
     *
     * @return error message
     */
    public String getErrorMessage() {
        return errorMessage;
    }

    /**
     * Set error message that is displayed to user if validation were to fail. If error message is null, then this
     * class uses the message contained in any validation exception.
     * @param errorMessage error message
     */
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    /**
     * Get form field that this validator is bound to
     *
     * @return form field
     */
    public FormField getFormField() {
        return formField;
    }

    /**
     *  Set form field that this validator is bound to.
     * @param formField form field
     */
    public void setFormField(FormField formField) {
        this.formField = formField;
    }

    /**
     * Subclass implementation should throw exception if validation fails.
     *
     * @param value value to be validated
     * @throws Exception signifies validation failed, should contain message that is displayed to end user
     */
    protected abstract void validateImpl(Object value) throws Exception;

    public void validate(Object value) throws InvalidValueException {
        try {
            validateImpl(value);
            formField.setHasConversionError(false);
        } catch (Exception e) {
            formField.setHasConversionError(true);
            if (getErrorMessage() != null) {
                throw new InvalidValueException(getErrorMessage());
            } else {
                throw new InvalidValueException(e.getMessage());
            }
        } finally {
            EntityForm entityForm = (EntityForm) formField.getFormFieldSet().getForm();
            entityForm.syncTabAndSaveButtonErrors();
        }
    }

    @Override
    public boolean isValid(Object value) {
        try {
            validate(value);
            return true;
        } catch (InvalidValueException e) {
            return false;
        }
    }
}
