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

package com.expressui.sample.dao.query;

import com.expressui.core.dao.query.StructuredEntityQuery;
import com.expressui.sample.entity.Account;
import com.expressui.sample.entity.Country;
import com.expressui.sample.entity.State;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

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
public class AccountQuery extends StructuredEntityQuery<Account> {

    private String name;
    private Set<State> states = new HashSet<State>();
    private Country country;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
    public List<Predicate> buildCriteria(CriteriaBuilder builder, CriteriaQuery<Account> query, Root<Account> account) {
        List<Predicate> predicates = new ArrayList<Predicate>();

        if (hasValue(name)) {
            ParameterExpression<String> nameExp = builder.parameter(String.class, "name");
            predicates.add(builder.like(builder.upper(account.<String>get("name")), nameExp));
        }
        if (hasValue(states)) {
            ParameterExpression<Set> statesExp = builder.parameter(Set.class, "states");
            predicates.add(builder.in(account.get("billingAddress").get("state")).value(statesExp));
        }
        if (hasValue(country)) {
            ParameterExpression<Country> countryExp = builder.parameter(Country.class, "country");
            predicates.add(builder.equal(account.get("billingAddress").get("country"), countryExp));
        }

        return predicates;
    }

    @Override
    public void setParameters(TypedQuery<Serializable> typedQuery) {
        if (hasValue(name)) {
            typedQuery.setParameter("name", "%" + name.toUpperCase() + "%");
        }
        if (hasValue(states)) {
            typedQuery.setParameter("states", states);
        }
        if (hasValue(country)) {
            typedQuery.setParameter("country", country);
        }
    }

    @Override
    public Path buildOrderBy(Root<Account> account) {
        if (getOrderByPropertyId().equals("billingAddress.country")) {
            return account.join("billingAddress", JoinType.LEFT).join("country", JoinType.LEFT);
        } else if (getOrderByPropertyId().equals("billingAddress.state.code")) {
            return account.join("billingAddress", JoinType.LEFT).join("state", JoinType.LEFT).get("code");
        } else {
            return null;
        }
    }

    @Override
    public void addFetchJoins(Root<Account> account) {
        account.fetch("billingAddress", JoinType.LEFT);
    }

    @Override
    public String toString() {
        return "AccountQuery{" +
                "name='" + name + '\'' +
                ", states=" + states +
                '}';
    }
}
