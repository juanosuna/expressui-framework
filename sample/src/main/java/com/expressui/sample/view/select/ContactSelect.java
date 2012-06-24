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

package com.expressui.sample.view.select;

import com.expressui.core.dao.query.StructuredEntityQuery;
import com.expressui.core.view.entityselect.EntitySelect;
import com.expressui.core.view.entityselect.EntitySelectResults;
import com.expressui.core.view.form.FormFieldSet;
import com.expressui.core.view.form.SearchForm;
import com.expressui.core.view.results.ResultsFieldSet;
import com.expressui.sample.dao.StateDao;
import com.expressui.sample.entity.Contact;
import com.expressui.sample.entity.Country;
import com.expressui.sample.entity.State;
import com.expressui.sample.formatter.PhonePropertyFormatter;
import com.vaadin.data.Property;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.springframework.beans.factory.config.BeanDefinition.SCOPE_PROTOTYPE;

@Component
@Scope(SCOPE_PROTOTYPE)
@SuppressWarnings({"serial"})
public class ContactSelect extends EntitySelect<Contact> {

    @Resource
    private ContactSelectSearchForm contactSelectSearchForm;

    @Resource
    private ContactSelectResults contactSelectResults;

    @Override
    public ContactSelectSearchForm getSearchForm() {
        return contactSelectSearchForm;
    }

    @Override
    public ContactSelectResults getResults() {
        return contactSelectResults;
    }

    @Override
    public String getTypeCaption() {
        return "Select Contact";
    }

    @Component
    @Scope(SCOPE_PROTOTYPE)
    public static class ContactSelectResults extends EntitySelectResults<Contact> {

        @Resource
        private ContactSelectQuery contactSelectQuery;

        @Override
        public ContactSelectQuery getEntityQuery() {
            return contactSelectQuery;
        }

        @Override
        public void init(ResultsFieldSet resultsFields) {
            resultsFields.setPropertyIds(
                    "name",
                    "title",
                    "mailingAddress.state.code",
                    "mailingAddress.country",
                    "mainPhone"
            );

            resultsFields.setLabel("mailingAddress.state.code", "State");
            resultsFields.setLabel("mainPhone", "Phone");
            resultsFields.setSortable("name", false);
            resultsFields.setPropertyFormatter("mainPhone", new PhonePropertyFormatter());
        }
    }

    @Component
    @Scope(SCOPE_PROTOTYPE)
    @SuppressWarnings({"serial", "rawtypes"})
    public static class ContactSelectSearchForm extends SearchForm<ContactSelectQuery> {

        @Resource
        private StateDao stateDao;

        @Override
        public void init(FormFieldSet formFields) {

            formFields.setCoordinates("lastName", 1, 1);
            formFields.setCoordinates("country", 1, 2);
            formFields.getFormField("country").getField().setDescription("<strong>Select US,CA,MX,AU to see states</strong>");
            formFields.setCoordinates("states", 1, 3);

            formFields.clearSelectItems("states");
            formFields.setVisible("states", false);
            formFields.setMultiSelectDimensions("states", 5, 15);

            formFields.addValueChangeListener("country", this, "countryChanged");
        }

        public void countryChanged(Property.ValueChangeEvent event) {
            Country newCountry = (Country) event.getProperty().getValue();
            List<State> states = stateDao.findByCountry(newCountry);

            getFormFieldSet().setSelectItems("states", states);
            getFormFieldSet().setVisible("states", !states.isEmpty());
        }

        @Override
        public String getTypeCaption() {
            return "Contact Search Form";
        }
    }

    @Component
    @Scope(SCOPE_PROTOTYPE)
    public static class ContactSelectQuery extends StructuredEntityQuery<Contact> {

        private String lastName;
        private Set<State> states = new HashSet<State>();
        private Country country;

        public String getLastName() {
            return lastName;
        }

        public void setLastName(String lastName) {
            this.lastName = lastName;
        }

        public Set<State> getStates() {
            return states;
        }

        public void setStates(Set<State> states) {
            this.states = states;
        }

        public Country getCountry() {
            return country;
        }

        public void setCountry(Country country) {
            this.country = country;
        }

        @Override
        public List<Predicate> buildCriteria(CriteriaBuilder builder, CriteriaQuery query, Root<Contact> contact) {
            List<Predicate> predicates = new ArrayList<Predicate>();

            if (hasValue(lastName)) {
                ParameterExpression<String> lastNameExp = builder.parameter(String.class, "lastName");
                predicates.add(builder.like(builder.upper(contact.<String>get("lastName")), lastNameExp));
            }
            if (hasValue(states)) {
                ParameterExpression<Set> statesExp = builder.parameter(Set.class, "states");
                predicates.add(builder.in(contact.get("mailingAddress").get("state")).value(statesExp));
            }
            if (hasValue(country)) {
                ParameterExpression<Country> countryExp = builder.parameter(Country.class, "country");
                predicates.add(builder.equal(contact.get("mailingAddress").get("country"), countryExp));
            }

            return predicates;
        }

        @Override
        public void setParameters(TypedQuery typedQuery) {
            if (hasValue(lastName)) {
                typedQuery.setParameter("lastName", "%" + lastName.toUpperCase() + "%");
            }
            if (hasValue(states)) {
                typedQuery.setParameter("states", states);
            }
            if (hasValue(country)) {
                typedQuery.setParameter("country", country);
            }
        }

        @Override
        public Path buildOrderBy(Root<Contact> contact) {
            if (getOrderByPropertyId().equals("mailingAddress.country")) {
                return contact.join("mailingAddress", JoinType.LEFT).join("country", JoinType.LEFT);
            } else if (getOrderByPropertyId().equals("mailingAddress.state.code")) {
                return contact.join("mailingAddress", JoinType.LEFT).join("state", JoinType.LEFT).get("code");
            } else if (getOrderByPropertyId().equals("account.name")) {
                return contact.join("account", JoinType.LEFT).get("name");
            } else {
                return null;
            }
        }

        @Override
        public void addFetchJoins(Root<Contact> contact) {
            contact.fetch("mailingAddress", JoinType.LEFT).fetch("state", JoinType.LEFT);
            contact.fetch("account", JoinType.LEFT);
        }

        @Override
        public String toString() {
            return "ContactQuery{" +
                    "lastName='" + lastName + '\'' +
                    ", states=" + states +
                    '}';
        }
    }
}

