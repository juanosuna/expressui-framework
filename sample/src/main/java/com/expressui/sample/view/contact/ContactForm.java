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

package com.expressui.sample.view.contact;

import com.expressui.core.view.EntityForm;
import com.expressui.core.view.field.FormFields;
import com.expressui.core.view.field.SelectField;
import com.expressui.sample.dao.StateDao;
import com.expressui.sample.entity.*;
import com.expressui.sample.util.PhoneConversionValidator;
import com.expressui.sample.util.PhonePropertyFormatter;
import com.expressui.sample.view.select.AccountSelect;
import com.expressui.sample.view.select.UserSelect;
import com.vaadin.data.Property;
import com.vaadin.terminal.Sizeable;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Component
@Scope("prototype")
@SuppressWarnings({"rawtypes", "serial"})
public class ContactForm extends EntityForm<Contact> {

    @Resource
    private StateDao stateDao;

    @Resource
    private UserSelect userSelect;

    @Resource
    private AccountSelect accountSelect;

    @Override
    public void configureFields(FormFields formFields) {
        formFields.setPosition("Overview", "firstName", 1, 1);
        formFields.setPosition("Overview", "lastName", 1, 2);

        formFields.setPosition("Overview", "title", 2, 1);
        formFields.setPosition("Overview", "birthDate", 2, 2);

        formFields.setPosition("Overview", "account.name", 3, 1);
        formFields.setPosition("Overview", "leadSource", 3, 2);

        formFields.setPosition("Overview", "email", 4, 1);
        formFields.setPosition("Overview", "doNotEmail", 4, 2);

        formFields.setPosition("Overview", "mainPhone", 5, 1);
        formFields.setPosition("Overview", "mainPhone.phoneType", 5, 1);
        formFields.setPosition("Overview", "doNotCall", 5, 2);

        formFields.setPosition("Overview", "assignedTo.loginName", 6, 1);

        formFields.setPosition("Mailing Address", "mailingAddress.street", 1, 1);
        formFields.setPosition("Mailing Address", "mailingAddress.city", 1, 2);
        formFields.setPosition("Mailing Address", "mailingAddress.country", 2, 1);
        formFields.setPosition("Mailing Address", "mailingAddress.zipCode", 2, 2);
        formFields.setPosition("Mailing Address", "mailingAddress.state", 3, 1);

        formFields.setPosition("Other Address", "otherAddress.street", 1, 1);
        formFields.setPosition("Other Address", "otherAddress.city", 1, 2);
        formFields.setPosition("Other Address", "otherAddress.country", 2, 1);
        formFields.setPosition("Other Address", "otherAddress.zipCode", 2, 2);
        formFields.setPosition("Other Address", "otherAddress.state", 3, 1);
        formFields.setTabOptional("Other Address", this, "addOtherAddress", this, "removeOtherAddress");

        formFields.setPosition("Description", "description", 1, 1);

        formFields.setLabel("description", null);
        formFields.setLabel("mainPhone.phoneType", null);
        formFields.setLabel("account.name", "Account");
        formFields.setWidth("mainPhone.phoneType", 7, Sizeable.UNITS_EM);
        formFields.setLabel("assignedTo.loginName", "Assigned to");

        formFields.addValidator("mainPhone", PhoneConversionValidator.class);
        formFields.setPropertyFormatter("mainPhone", new PhonePropertyFormatter());

        formFields.setDescription("mainPhone",
                "<strong>Example formats:</strong>" +
                        "<ul>" +
                        "  <li>US: (919) 975-5331</li>" +
                        "  <li>Germany: +49 30/70248804</li>" +
                        "</ul>");

        formFields.setSelectItems("mailingAddress.state", new ArrayList());
        formFields.addValueChangeListener("mailingAddress.country", this, "countryChanged");

        formFields.setSelectItems("otherAddress.state", new ArrayList());
        formFields.addValueChangeListener("otherAddress.country", this, "otherCountryChanged");

        SelectField selectField = new SelectField(this, "assignedTo", userSelect);
        formFields.setField("assignedTo.loginName", selectField);

        selectField = new SelectField(this, "account", accountSelect);
        formFields.setField("account.name", selectField);
    }

    public void addOtherAddress() {
        getEntity().setOtherAddress(new Address(AddressType.OTHER));
    }

    public void removeOtherAddress() {
        getEntity().setOtherAddress(null);
    }

    public void countryChanged(Property.ValueChangeEvent event) {
        countryChangedImpl(event, "mailingAddress");
    }

    public void otherCountryChanged(Property.ValueChangeEvent event) {
        countryChangedImpl(event, "otherAddress");
    }

    public void countryChangedImpl(Property.ValueChangeEvent event, String addressPropertyId) {
        Country newCountry = (Country) event.getProperty().getValue();
        List<State> states = stateDao.findByCountry(newCountry);

        String fullStatePropertyId = addressPropertyId + ".state";
        FormFields formFields = getFormFields();
        formFields.setVisible(fullStatePropertyId, !states.isEmpty());
        formFields.setSelectItems(fullStatePropertyId, states);

        String fullZipCodePropertyId = addressPropertyId + ".zipCode";

        if (newCountry != null && newCountry.getMinPostalCode() != null && newCountry.getMaxPostalCode() != null) {
            formFields.setDescription(fullZipCodePropertyId,
                    "<strong>Postal code range:</strong>" +
                            "<ul>" +
                            "  <li>" + newCountry.getMinPostalCode() + " - " + newCountry.getMaxPostalCode() + "</li>" +
                            "</ul>");
        } else {
            formFields.setDescription(fullZipCodePropertyId, null);
        }
    }

    @Override
    public String getEntityCaption() {
        if (getEntity().getName() == null) {
            return "Contact Form - New";
        } else {
            return "Contact Form - " + getEntity().getName();
        }
    }
}
