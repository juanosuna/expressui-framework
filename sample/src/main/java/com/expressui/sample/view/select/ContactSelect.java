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
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.springframework.beans.factory.config.BeanDefinition.SCOPE_PROTOTYPE;

@Component
@Scope(SCOPE_PROTOTYPE)
@SuppressWarnings("serial")
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
                    id(p.getName()),
                    id(p.getTitle()),
                    id(p.getMailingAddress().getState().getCode()),
                    id(p.getMailingAddress().getCountry()),
                    id(p.getMainPhone())
            );

            resultsFields.setSortable(id(p.getName()), false);
            resultsFields.setPropertyFormatter(id(p.getMainPhone()), new PhonePropertyFormatter());
        }
    }

    @Component
    @Scope(SCOPE_PROTOTYPE)
    public static class ContactSelectSearchForm extends SearchForm<ContactSelectQuery> {

        @Resource
        private StateDao stateDao;

        @Override
        public void init(FormFieldSet formFields) {

            formFields.setCoordinates(id(p.getLastName()), 1, 1);
            formFields.setCoordinates(id(p.getCountry()), 1, 2);
            formFields.setCoordinates(id(p.getStates()), 1, 3);

            formFields.clearSelectItems(id(p.getStates()));
            formFields.setVisible(id(p.getStates()), false);
            formFields.setMultiSelectDimensions(id(p.getStates()), 5, 15);

            formFields.addValueChangeListener(id(p.getCountry()), this, "countryChanged");
        }

        public void countryChanged(Property.ValueChangeEvent event) {
            Country newCountry = (Country) event.getProperty().getValue();
            List<State> states = stateDao.findByCountry(newCountry);

            getFormFieldSet().setSelectItems(id(p.getStates()), states);
            getFormFieldSet().setVisible(id(p.getStates()), !states.isEmpty());
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
        public List<Predicate> buildCriteria(CriteriaBuilder builder, CriteriaQuery<Contact> query, Root<Contact> contact) {
            List<Predicate> predicates = new ArrayList<Predicate>();

            if (hasValue(lastName)) {
                ParameterExpression<String> lastNameExp = builder.parameter(String.class, "lastName");
                predicates.add(builder.like(builder.upper(this.<String>path(contact, p.getLastName())), lastNameExp));
            }
            if (hasValue(states)) {
                ParameterExpression<Set> statesExp = builder.parameter(Set.class, "states");
                predicates.add(builder.in(this.<Set>path(contact, p.getMailingAddress().getState())).value(statesExp));
            }
            if (hasValue(country)) {
                ParameterExpression<Country> countryExp = builder.parameter(Country.class, "country");
                predicates.add(builder.equal(this.<Set>path(contact, p.getMailingAddress().getCountry()), countryExp));
            }

            return predicates;
        }

        @Override
        public void setParameters(TypedQuery<Serializable> typedQuery) {
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
            if (getOrderByPropertyId().equals(id(p.getMailingAddress().getCountry()))) {
                return orderByPath(contact, p.getMailingAddress().getCountry());
            } else if (getOrderByPropertyId().equals(id(p.getMailingAddress().getState().getCode()))) {
                return orderByPath(contact, p.getMailingAddress().getState().getCode());
            } else if (getOrderByPropertyId().equals(id(p.getAccount().getName()))) {
                return orderByPath(contact, p.getAccount().getName());
            } else {
                return null;
            }
        }

        @Override
        public void addFetchJoins(Root<Contact> contact) {
            fetch(contact, JoinType.LEFT, p.getMailingAddress());
            fetch(contact, JoinType.LEFT, p.getAccount());
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

