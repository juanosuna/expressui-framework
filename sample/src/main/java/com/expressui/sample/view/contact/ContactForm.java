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

package com.expressui.sample.view.contact;

import com.expressui.core.MainApplication;
import com.expressui.core.entity.security.User;
import com.expressui.core.view.field.SelectField;
import com.expressui.core.view.form.EntityForm;
import com.expressui.core.view.form.FormFieldSet;
import com.expressui.core.view.form.FormTab;
import com.expressui.core.view.security.select.UserSelect;
import com.expressui.sample.dao.StateDao;
import com.expressui.sample.entity.*;
import com.expressui.sample.formatter.PhonePropertyFormatter;
import com.expressui.sample.validator.PhoneConversionValidator;
import com.expressui.sample.view.select.AccountSelect;
import com.vaadin.data.Property;
import com.vaadin.terminal.Sizeable;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

import static org.springframework.beans.factory.config.BeanDefinition.SCOPE_PROTOTYPE;

@Component
@Scope(SCOPE_PROTOTYPE)
@SuppressWarnings({"serial"})
public class ContactForm extends EntityForm<Contact> {

    @Resource
    private StateDao stateDao;

    @Resource
    private UserSelect userSelect;

    @Resource
    private AccountSelect accountSelect;

    @Override
    public void init(FormFieldSet formFields) {

        FormTab overview = formFields.createTab(getDomainMessage("overview"));
        overview.setCoordinates("firstName", 1, 1);
        overview.setCoordinates("lastName", 1, 2);

        overview.setCoordinates("title", 2, 1);
        overview.setCoordinates("birthDate", 2, 2);

        overview.setCoordinates("account.name", 3, 1);
        overview.setCoordinates("leadSource", 3, 2);

        overview.setCoordinates("email", 4, 1);
        overview.setCoordinates("doNotEmail", 4, 2);

        overview.setCoordinates("mainPhone", 5, 1);
        overview.setCoordinates("mainPhoneType", 5, 1);
        overview.setCoordinates("doNotCall", 5, 2);

        overview.setCoordinates("assignedTo.loginName", 6, 1);

        FormTab mailingAddress = formFields.createTab(getDomainMessage("mailingAddress"));
        mailingAddress.setCoordinates("mailingAddress.street", 1, 1);
        mailingAddress.setCoordinates("mailingAddress.city", 1, 2);
        mailingAddress.setCoordinates("mailingAddress.country", 2, 1);
        mailingAddress.setCoordinates("mailingAddress.zipCode", 2, 2);
        mailingAddress.setCoordinates("mailingAddress.state", 3, 1);

        FormTab otherAddress = formFields.createTab(getDomainMessage("otherAddress"));
        otherAddress.setCoordinates("otherAddress.street", 1, 1);
        otherAddress.setCoordinates("otherAddress.city", 1, 2);
        otherAddress.setCoordinates("otherAddress.country", 2, 1);
        otherAddress.setCoordinates("otherAddress.zipCode", 2, 2);
        otherAddress.setCoordinates("otherAddress.state", 3, 1);
        otherAddress.setOptional(this, "addOtherAddress", this, "removeOtherAddress");

        FormTab description = formFields.createTab(getDomainMessage("description"));
        description.setCoordinates("description", 1, 1);

        formFields.setWidth("mainPhoneType", 7, Sizeable.UNITS_EM);

        getFormFieldSet().setToolTipArgs("mainPhone", Phone.getExampleNumber(
                getMainApplication().getLocale().getCountry()));

        formFields.addConversionValidator("mainPhone", new PhoneConversionValidator());
        formFields.setPropertyFormatter("mainPhone", new PhonePropertyFormatter());

        formFields.clearSelectItems("mailingAddress.state");
        formFields.addValueChangeListener("mailingAddress.country", this, "mailingCountryChanged");

        formFields.clearSelectItems("otherAddress.state");
        formFields.addValueChangeListener("otherAddress.country", this, "otherCountryChanged");

        SelectField<Contact, User> assignedToField = new SelectField<Contact, User>(this, "assignedTo", userSelect);
        formFields.setField("assignedTo.loginName", assignedToField);

        SelectField<Contact, Account> accountField = new SelectField<Contact, Account>(this, "account", accountSelect);
        formFields.setField("account.name", accountField);
    }

    public void addOtherAddress() {
        getBean().setOtherAddress(new Address(AddressType.OTHER));
    }

    public void removeOtherAddress() {
        getBean().setOtherAddress(null);
    }

    public void mailingCountryChanged(Property.ValueChangeEvent event) {
        countryChangedImpl(event, "mailingAddress");
    }

    public void otherCountryChanged(Property.ValueChangeEvent event) {
        countryChangedImpl(event, "otherAddress");
    }

    private void countryChangedImpl(Property.ValueChangeEvent event, String addressPropertyId) {
        Country newCountry = (Country) event.getProperty().getValue();
        List<State> states = stateDao.findByCountry(newCountry);

        getFormFieldSet().setVisible(addressPropertyId + ".state", !states.isEmpty());
        getFormFieldSet().setSelectItems(addressPropertyId + ".state", states);
        if (newCountry != null) {
            getFormFieldSet().setToolTipArgs(addressPropertyId + ".zipCode",
                    newCountry.getMinPostalCode(), newCountry.getMaxPostalCode());
        }
    }

    @Override
    public void onDisplay() {
        super.onDisplay();
        getMainApplication().showTrayMessage(
                "<h3>Feature Tips:</h3>" +
                        "<ul>" +
                        "<li>Right-mouse click on form tabs to add and remove sections" +
                        "<li>Enter phone as 704.555.1212 and see how it is automatically formatted" +
                        "<li>Go to an address tab and see how zip codes are validated against selected country" +
                        "</ul>"
        );
    }
}
