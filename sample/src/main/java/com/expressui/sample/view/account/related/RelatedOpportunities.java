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

package com.expressui.sample.view.account.related;

import com.expressui.core.dao.ToManyRelationshipQuery;
import com.expressui.core.view.field.DisplayFields;
import com.expressui.core.view.field.format.JDKFormatPropertyFormatter;
import com.expressui.core.view.tomanyrelationship.ToManyAggregationRelationshipResults;
import com.expressui.core.view.tomanyrelationship.ToManyRelationship;
import com.expressui.sample.dao.OpportunityDao;
import com.expressui.sample.entity.Account;
import com.expressui.sample.entity.Opportunity;
import com.expressui.sample.view.select.OpportunitySelect;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@SuppressWarnings("serial")
@Component
@Scope("prototype")
public class RelatedOpportunities extends ToManyRelationship<Opportunity> {

    @Resource
    private RelatedOpportunitiesResults relatedOpportunitiesResults;

    @Override
    public String getEntityCaption() {
        return "Company Sales Opportunities";
    }

    @Override
    public RelatedOpportunitiesResults getResults() {
        return relatedOpportunitiesResults;
    }

    @Component
    @Scope("prototype")
    public static class RelatedOpportunitiesResults extends ToManyAggregationRelationshipResults<Opportunity> {

        @Resource
        private OpportunityDao opportunityDao;

        @Resource
        private RelatedOpportunitiesQuery relatedOpportunitiesQuery;

        @Resource
        private OpportunitySelect opportunitySelect;

        @Override
        public OpportunityDao getEntityDao() {
            return opportunityDao;
        }

        @Override
        public RelatedOpportunitiesQuery getEntityQuery() {
            return relatedOpportunitiesQuery;
        }

        @Override
        public OpportunitySelect getEntitySelect() {
            return opportunitySelect;
        }

        @Override
        public void configureFields(DisplayFields displayFields) {
            displayFields.setPropertyIds(new String[]{
                    "name",
                    "salesStage",
                    "amountWeightedInUSD",
                    "expectedCloseDate"
            });

            displayFields.setLabel("amountWeightedInUSD", "Weighted Amount");
            NumberFormat numberFormat = NumberFormat.getCurrencyInstance(Locale.US);
            numberFormat.setMaximumFractionDigits(0);
            JDKFormatPropertyFormatter formatter = new JDKFormatPropertyFormatter(numberFormat);
            displayFields.setPropertyFormatter("amountWeightedInUSD", formatter);
        }

        @Override
        public String getChildPropertyId() {
            return "opportunities";
        }

        @Override
        public String getParentPropertyId() {
            return "account";
        }

        @Override
        public String getEntityCaption() {
            return "Opportunities";
        }
    }

    @Component
    @Scope("prototype")
    @SuppressWarnings("rawtypes")
    public static class RelatedOpportunitiesQuery extends ToManyRelationshipQuery<Opportunity, Account> {

        @Resource
        private OpportunityDao opportunityDao;

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
        public List<Opportunity> execute() {
            return opportunityDao.execute(this);
        }

        @Override
        public List<Predicate> buildCriteria(CriteriaBuilder builder, Root<Opportunity> rootEntity) {
            List<Predicate> criteria = new ArrayList<Predicate>();

            if (!isEmpty(account)) {
                ParameterExpression<Account> p = builder.parameter(Account.class, "account");
                criteria.add(builder.equal(rootEntity.get("account"), p));
            }

            return criteria;
        }

        @Override
        public void setParameters(TypedQuery typedQuery) {
            if (!isEmpty(account)) {
                typedQuery.setParameter("account", account);
            }
        }

        @Override
        public Path buildOrderBy(Root<Opportunity> rootEntity) {
            if (getOrderByPropertyId().equals("account.name")) {
                return rootEntity.join("account", JoinType.LEFT).get("name");
            } else {
                return null;
            }
        }

        @Override
        public void addFetchJoins(Root<Opportunity> rootEntity) {
            rootEntity.fetch("account", JoinType.LEFT);
        }

        @Override
        public String toString() {
            return "RelatedOpportunities{" +
                    "account='" + account + '\'' +
                    '}';
        }
    }
}

