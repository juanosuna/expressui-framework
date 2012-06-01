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

import com.expressui.core.dao.query.ToManyRelationshipQuery;
import com.expressui.sample.entity.Account;
import com.expressui.sample.entity.Opportunity;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.beans.factory.config.BeanDefinition.SCOPE_PROTOTYPE;

@Component
@Scope(SCOPE_PROTOTYPE)
public class RelatedOpportunitiesQuery extends ToManyRelationshipQuery<Opportunity, Account> {

    private Account account;

    @Override
    public void setParent(Account parent) {
        this.account = parent;
    }

    @Override
    public Account getParent() {
        return account;
    }

    @Override
    public List<Predicate> buildCriteria(CriteriaBuilder builder, CriteriaQuery query, Root<Opportunity> opportunity) {
        List<Predicate> predicates = new ArrayList<Predicate>();

        if (hasValue(account)) {
            ParameterExpression<Account> accountExp = builder.parameter(Account.class, "account");
            predicates.add(builder.equal(opportunity.get("account"), accountExp));
        }

        return predicates;
    }

    @Override
    public void setParameters(TypedQuery typedQuery) {
        if (hasValue(account)) {
            typedQuery.setParameter("account", account);
        }
    }

    @Override
    public Path buildOrderBy(Root<Opportunity> opportunity) {
        if (getOrderByPropertyId().equals("account.name")) {
            return opportunity.join("account", JoinType.LEFT).get("name");
        } else {
            return null;
        }
    }

    @Override
    public void addFetchJoins(Root<Opportunity> opportunity) {
        opportunity.fetch("account", JoinType.LEFT);
    }

    @Override
    public String toString() {
        return "RelatedOpportunitiesQuery{" +
                "account='" + account + '\'' +
                '}';
    }
}
