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

import com.expressui.core.entity.security.User;
import com.expressui.core.view.field.SelectField;
import com.expressui.core.view.field.format.UrlPropertyFormatter;
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

        FormTab overview = formFields.createTab(getDomainMessage("overview"));
        overview.setCoordinates(id(p.getName()), 1, 1);
        overview.setCoordinates(id(p.getAssignedTo().getLoginName()), 1, 2);

        overview.setCoordinates(id(p.getWebsite()), 2, 1);
        overview.setCoordinates(id(p.getAccountTypes()), 2, 2, 3, 2);

        overview.setCoordinates(id(p.getEmail()), 3, 1);

        FormTab details = formFields.createTab(getDomainMessage("details"));
        details.setCoordinates(id(p.getTickerSymbol()), 1, 1);
        details.setCoordinates(id(p.getIndustry()), 1, 2);

        details.setCoordinates(id(p.getNumberOfEmployees()), 2, 1);
        details.setCoordinates(id(p.getAnnualRevenue()), 2, 2);
        details.setCoordinates(id(p.getAnnualRevenueInUSD()), 3, 1);
        details.setCoordinates(id(p.getCurrency()), 3, 2);

        FormTab billingAddress = formFields.createTab(getDomainMessage("billingAddress"));
        billingAddress.setCoordinates(id(p.getBillingAddress().getStreet()), 1, 1);
        billingAddress.setCoordinates(id(p.getBillingAddress().getCity()), 1, 2);
        billingAddress.setCoordinates(id(p.getBillingAddress().getCountry()), 2, 1);
        billingAddress.setCoordinates(id(p.getBillingAddress().getZipCode()), 2, 2);
        billingAddress.setCoordinates(id(p.getBillingAddress().getState()), 3, 1);
        billingAddress.setCoordinates(id(p.getMainPhone()), 3, 2);

        FormTab mailingAddress = formFields.createTab(getDomainMessage("mailingAddress"));
        mailingAddress.setCoordinates(id(p.getMailingAddress().getStreet()), 1, 1);
        mailingAddress.setCoordinates(id(p.getMailingAddress().getCity()), 1, 2);
        mailingAddress.setCoordinates(id(p.getMailingAddress().getCountry()), 2, 1);
        mailingAddress.setCoordinates(id(p.getMailingAddress().getZipCode()), 2, 2);
        mailingAddress.setCoordinates(id(p.getMailingAddress().getState()), 3, 1);
        mailingAddress.setOptional(this, "addMailingAddress", this, "removeMailingAddress");

        formFields.setMultiSelectDimensions(id(p.getAccountTypes()), 3, 10);

        formFields.setPropertyFormatter(id(p.getWebsite()), new UrlPropertyFormatter());

        formFields.addConversionValidator(id(p.getMainPhone()), new PhoneConversionValidator());
        formFields.setPropertyFormatter(id(p.getMainPhone()), new PhonePropertyFormatter());

        formFields.clearSelectItems(id(p.getBillingAddress().getState()));
        formFields.addValueChangeListener(id(p.getBillingAddress().getCountry()), this, "billingCountryChanged");

        formFields.clearSelectItems(id(p.getMailingAddress().getState()));
        formFields.addValueChangeListener(id(p.getMailingAddress().getCountry()), this, "mailingCountryChanged");

        SelectField<Account, User> assignedToField = new SelectField<Account, User>(this, id(p.getAssignedTo()), userSelect);
        formFields.setField(id(p.getAssignedTo().getLoginName()), assignedToField);
    }

    public void addMailingAddress() {
        getBean().setMailingAddress(new Address(AddressType.MAILING));
    }

    public void removeMailingAddress() {
        getBean().setMailingAddress(null);
    }

    public void billingCountryChanged(Property.ValueChangeEvent event) {
        countryChangedImpl(event, p.getBillingAddress());
    }

    public void mailingCountryChanged(Property.ValueChangeEvent event) {
        countryChangedImpl(event, p.getMailingAddress());
    }

    private void countryChangedImpl(Property.ValueChangeEvent event, Address mockAddress) {
        Country newCountry = (Country) event.getProperty().getValue();
        List<State> states = stateDao.findByCountry(newCountry);

        getFormFieldSet().setVisible(id(mockAddress.getState()), !states.isEmpty());
        getFormFieldSet().setSelectItems(id(mockAddress.getState()), states);
        if (newCountry != null) {
            getFormFieldSet().setToolTipArgs(id(mockAddress.getZipCode()),
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
                        "<li>Input invalid data and then mouse-over input to see error message" +
                        "</ul>"
        );
    }
}
