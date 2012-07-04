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

package com.expressui.sample.view.account;

import com.expressui.core.MainApplication;
import com.expressui.core.entity.security.User;
import com.expressui.core.view.field.format.UrlPropertyFormatter;
import com.expressui.core.view.field.SelectField;
import com.expressui.core.view.form.EntityForm;
import com.expressui.core.view.form.FormFieldSet;
import com.expressui.core.view.form.FormTab;
import com.expressui.core.view.security.select.UserSelect;
import com.expressui.core.view.tomanyrelationship.ToManyRelationship;
import com.expressui.sample.dao.StateDao;
import com.expressui.sample.entity.*;
import com.expressui.sample.formatter.PhonePropertyFormatter;
import com.expressui.sample.validator.PhoneConversionValidator;
import com.expressui.sample.view.account.related.RelatedContacts;
import com.expressui.sample.view.account.related.RelatedOpportunities;
import com.vaadin.data.Property;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.beans.factory.config.BeanDefinition.SCOPE_PROTOTYPE;

@Component
@Scope(SCOPE_PROTOTYPE)
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
    public void init(FormFieldSet formFields) {

        FormTab overview = formFields.createTab("Overview");
        overview.setCoordinates("name", 1, 1);
        overview.setCoordinates("assignedTo.loginName", 1, 2);

        overview.setCoordinates("website", 2, 1);
        overview.setCoordinates("accountTypes", 2, 2, 3, 2);

        overview.setCoordinates("email", 3, 1);

        FormTab details = formFields.createTab("Details");
        details.setCoordinates("tickerSymbol", 1, 1);
        details.setCoordinates("industry", 1, 2);

        details.setCoordinates("numberOfEmployees", 2, 1);
        details.setCoordinates("annualRevenue", 2, 2);
        details.setCoordinates("annualRevenueInUSD", 3, 1);
        details.setCoordinates("currency", 3, 2);

        FormTab billingAddress = formFields.createTab("Billing Address");
        billingAddress.setCoordinates("billingAddress.street", 1, 1);
        billingAddress.setCoordinates("billingAddress.city", 1, 2);
        billingAddress.setCoordinates("billingAddress.country", 2, 1);
        billingAddress.setCoordinates("billingAddress.zipCode", 2, 2);
        billingAddress.setCoordinates("billingAddress.state", 3, 1);
        billingAddress.setCoordinates("mainPhone", 3, 2);

        FormTab mailingAddress = formFields.createTab("Mailing Address");
        mailingAddress.setCoordinates("mailingAddress.street", 1, 1);
        mailingAddress.setCoordinates("mailingAddress.city", 1, 2);
        mailingAddress.setCoordinates("mailingAddress.country", 2, 1);
        mailingAddress.setCoordinates("mailingAddress.zipCode", 2, 2);
        mailingAddress.setCoordinates("mailingAddress.state", 3, 1);
        mailingAddress.setOptional(this, "addMailingAddress", this, "removeMailingAddress");

        formFields.setMultiSelectDimensions("accountTypes", 3, 10);

        formFields.setPropertyFormatter("website", new UrlPropertyFormatter());

        formFields.setLabel("accountTypes", "Types");
        formFields.setLabel("assignedTo.loginName", "Assigned to");
        formFields.setLabel("mainPhone", "Phone");

        formFields.setToolTip("currency", "Change to currency changes amount in USD");

        formFields.addConversionValidator("mainPhone", new PhoneConversionValidator());
        formFields.setPropertyFormatter("mainPhone", new PhonePropertyFormatter());

        formFields.clearSelectItems("billingAddress.state");
        formFields.addValueChangeListener("billingAddress.country", this, "billingCountryChanged");

        formFields.clearSelectItems("mailingAddress.state");
        formFields.addValueChangeListener("mailingAddress.country", this, "mailingCountryChanged");

        SelectField<Account, User> assignedToField = new SelectField<Account, User>(this, "assignedTo", userSelect);
        formFields.setField("assignedTo.loginName", assignedToField);
    }

    public void addMailingAddress() {
        getBean().setMailingAddress(new Address(AddressType.MAILING));
    }

    public void removeMailingAddress() {
        getBean().setMailingAddress(null);
    }

    public void billingCountryChanged(Property.ValueChangeEvent event) {
        countryChangedImpl(event, "billingAddress");
    }

    public void mailingCountryChanged(Property.ValueChangeEvent event) {
        countryChangedImpl(event, "mailingAddress");
    }

    private void countryChangedImpl(Property.ValueChangeEvent event, String addressPropertyId) {
        Country newCountry = (Country) event.getProperty().getValue();
        List<State> states = stateDao.findByCountry(newCountry);

        getFormFieldSet().setVisible(addressPropertyId + ".state", !states.isEmpty());
        getFormFieldSet().setSelectItems(addressPropertyId + ".state", states);
        if (newCountry != null) {
            getFormFieldSet().setToolTip(addressPropertyId + ".zipCode", newCountry.getZipCodeToolTip());
        }
    }

    @Override
    public void onDisplay() {
        super.onDisplay();
        MainApplication.getInstance().showTrayMessage(
                "<h3>Feature Tips:</h3>" +
                        "<ul>" +
                        "<li>Right-mouse click on form tabs to add and remove sections" +
                        "<li>Add any number of contacts or opportunities to the one-to-many entity relationship" +
                        "<li>Open two browsers and modify the same entity concurrently to see handling of optimistic locking" +
                        "<li>Input invalid data and then mouse-over input to see error message" +
                        "</ul>"
        );
    }

    @Override
    public String getTypeCaption() {
        if (getBean().getName() == null) {
            return "Account Form - New";
        } else {
            return "Account Form - " + getBean().getName();
        }
    }
}
