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

package com.expressui.sample.view.account;

import com.expressui.core.view.EntityForm;
import com.expressui.core.view.field.FormFields;
import com.expressui.core.view.field.SelectField;
import com.expressui.core.view.tomanyrelationship.ToManyRelationship;
import com.expressui.sample.dao.StateDao;
import com.expressui.sample.entity.*;
import com.expressui.sample.util.PhoneConversionValidator;
import com.expressui.sample.util.PhonePropertyFormatter;
import com.expressui.sample.view.account.related.RelatedContacts;
import com.expressui.sample.view.account.related.RelatedOpportunities;
import com.expressui.sample.view.select.UserSelect;
import com.vaadin.data.Property;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Component
@Scope("prototype")
@SuppressWarnings({"serial", "rawtypes"})
public class AccountForm extends EntityForm<Account> {

    @Resource
    private StateDao stateDao;

    @Resource
    private RelatedContacts relatedContacts;

    @Resource
    private RelatedOpportunities relatedOpportunities;

    @Resource
    private UserSelect userSelect;

    @Override
    public List<ToManyRelationship> getToManyRelationships() {
        List<ToManyRelationship> toManyRelationships = new ArrayList<ToManyRelationship>();
        toManyRelationships.add(relatedContacts);
        toManyRelationships.add(relatedOpportunities);

        return toManyRelationships;
    }

    @Override
    public void configureFields(FormFields formFields) {

        formFields.setPosition("Overview", "name", 1, 1);
        formFields.setPosition("Overview", "website", 1, 2);

        formFields.setPosition("Overview", "mainPhone", 2, 1);
        formFields.setPosition("Overview", "assignedTo.loginName", 2, 2);

        formFields.setPosition("Overview", "email", 3, 1);
        formFields.setPosition("Overview", "accountTypes", 3, 2);

        formFields.setPosition("Details", "tickerSymbol", 1, 1);
        formFields.setPosition("Details", "industry", 1, 2);

        formFields.setPosition("Details", "numberOfEmployees", 2, 1);
        formFields.setPosition("Details", "annualRevenue", 2, 2);
        formFields.setPosition("Details", "annualRevenueInUSD", 3, 1);
        formFields.setPosition("Details", "currency", 3, 2);

        formFields.setPosition("Billing Address", "billingAddress.street", 1, 1);
        formFields.setPosition("Billing Address", "billingAddress.city", 1, 2);
        formFields.setPosition("Billing Address", "billingAddress.country", 2, 1);
        formFields.setPosition("Billing Address", "billingAddress.zipCode", 2, 2);
        formFields.setPosition("Billing Address", "billingAddress.state", 3, 1);

        formFields.setPosition("Mailing Address", "mailingAddress.street", 1, 1);
        formFields.setPosition("Mailing Address", "mailingAddress.city", 1, 2);
        formFields.setPosition("Mailing Address", "mailingAddress.country", 2, 1);
        formFields.setPosition("Mailing Address", "mailingAddress.zipCode", 2, 2);
        formFields.setPosition("Mailing Address", "mailingAddress.state", 3, 1);
        formFields.setTabOptional("Mailing Address", this, "addMailingAddress", this, "removeMailingAddress");

        formFields.setMultiSelectDimensions("accountTypes", 3, 10);

        formFields.setLabel("accountTypes", "Types");
        formFields.setLabel("mainPhone", "Phone");
        formFields.setLabel("assignedTo.loginName", "Assigned to");

        formFields.addValidator("mainPhone", PhoneConversionValidator.class);
        formFields.setPropertyFormatter("mainPhone", new PhonePropertyFormatter());

        formFields.setSelectItems("billingAddress.state", new ArrayList());
        formFields.addValueChangeListener("billingAddress.country", this, "countryChanged");

        formFields.setSelectItems("mailingAddress.state", new ArrayList());
        formFields.addValueChangeListener("mailingAddress.country", this, "otherCountryChanged");

        SelectField selectField = new SelectField(this, "assignedTo", userSelect);
        formFields.setField("assignedTo.loginName", selectField);
    }

    public void addMailingAddress() {
        getEntity().setMailingAddress(new Address(AddressType.MAILING));
    }

    public void removeMailingAddress() {
        getEntity().setMailingAddress(null);
    }

    public void countryChanged(Property.ValueChangeEvent event) {
        countryChangedImpl(event, "billingAddress");
    }

    public void otherCountryChanged(Property.ValueChangeEvent event) {
        countryChangedImpl(event, "mailingAddress");
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
            return "Account Form - New";
        } else {
            return "Account Form - " + getEntity().getName();
        }
    }
}
