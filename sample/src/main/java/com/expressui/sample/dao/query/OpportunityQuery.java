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
import com.expressui.sample.entity.Opportunity;
import com.expressui.sample.entity.SalesStage;
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
public class OpportunityQuery extends StructuredEntityQuery<Opportunity> {

    private String accountName;
    private Set<SalesStage> salesStages = new HashSet<SalesStage>();

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public Set<SalesStage> getSalesStages() {
        return salesStages;
    }

    public void setSalesStages(Set<SalesStage> salesStages) {
        this.salesStages = salesStages;
    }

    @Override
    public List<Predicate> buildCriteria(CriteriaBuilder builder, CriteriaQuery<Opportunity> query, Root<Opportunity> opportunity) {
        List<Predicate> predicates = new ArrayList<Predicate>();

        if (hasValue(accountName)) {
            ParameterExpression<String> accountNameExp = builder.parameter(String.class, "accountName");
            predicates.add(builder.like(builder.upper(opportunity.get("account").<String>get("name")), accountNameExp));
        }
        if (hasValue(salesStages)) {
            ParameterExpression<Set> salesStagesExp = builder.parameter(Set.class, "salesStages");
            predicates.add(builder.in(opportunity.get("salesStage")).value(salesStagesExp));
        }

        return predicates;
    }

    @Override
    public void setParameters(TypedQuery<Serializable> typedQuery) {
        if (hasValue(accountName)) {
            typedQuery.setParameter("accountName", "%" + accountName.toUpperCase() + "%");
        }
        if (hasValue(salesStages)) {
            typedQuery.setParameter("salesStages", salesStages);
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
        return "OpportunityQuery{" +
                "accountName='" + accountName + '\'' +
                ", salesStages=" + salesStages +
                '}';
    }
}
