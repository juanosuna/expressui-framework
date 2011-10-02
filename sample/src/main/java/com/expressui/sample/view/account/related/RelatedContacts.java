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
import com.expressui.core.view.tomanyrelationship.ToManyAggregationRelationshipResults;
import com.expressui.core.view.tomanyrelationship.ToManyRelationship;
import com.expressui.sample.dao.ContactDao;
import com.expressui.sample.entity.Account;
import com.expressui.sample.entity.Contact;
import com.expressui.sample.util.PhonePropertyFormatter;
import com.expressui.sample.view.select.ContactSelect;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("serial")
@Component
@Scope("prototype")
public class RelatedContacts extends ToManyRelationship<Contact> {

    @Resource
    private RelatedContactsResults relatedContactsResults;

    @Override
    public String getEntityCaption() {
        return "Company Contacts";
    }

    @Override
    public RelatedContactsResults getResults() {
        return relatedContactsResults;
    }

    @SuppressWarnings("rawtypes")
	@Component
    @Scope("prototype")
    public static class RelatedContactsResults extends ToManyAggregationRelationshipResults<Contact> {

        @Resource
        private ContactDao contactDao;

        @Resource
        private ContactSelect contactSelect;

        @Resource
        private RelatedContactsQuery relatedContactsQuery;

        @Override
        public ContactDao getEntityDao() {
            return contactDao;
        }

        @Override
        public ContactSelect getEntitySelect() {
            return contactSelect;
        }

		@Override
        public ToManyRelationshipQuery getEntityQuery() {
            return relatedContactsQuery;
        }

        @Override
        public void configureFields(DisplayFields displayFields) {
            displayFields.setPropertyIds(new String[]{
                    "name",
                    "title",
                    "mailingAddress.state.code",
                    "mailingAddress.country",
                    "mainPhone"
            });

            displayFields.setLabel("mailingAddress.state.code", "State");
            displayFields.setLabel("mainPhone", "Phone");
            displayFields.setSortable("name", false);
            displayFields.setSortable("mainPhone", false);
            displayFields.setPropertyFormatter("mainPhone", new PhonePropertyFormatter());
        }

        @Override
        public String getChildPropertyId() {
            return "contacts";
        }

        @Override
        public String getParentPropertyId() {
            return "account";
        }

        @Override
        public String getEntityCaption() {
            return "Contacts";
        }
    }

    @Component
    @Scope("prototype")
    @SuppressWarnings("rawtypes")
    public static class RelatedContactsQuery extends ToManyRelationshipQuery<Contact, Account> {

        @Resource
        private ContactDao contactDao;

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
        public List<Contact> execute() {
            return contactDao.execute(this);
        }

        @Override
        public List<Predicate> buildCriteria(CriteriaBuilder builder, Root<Contact> rootEntity) {
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
        public Path buildOrderBy(Root<Contact> rootEntity) {
            if (getOrderByPropertyId().equals("mailingAddress.country")) {
                return rootEntity.join("mailingAddress", JoinType.LEFT).join("country", JoinType.LEFT);
            } else if (getOrderByPropertyId().equals("mailingAddress.state.code")) {
                return rootEntity.join("mailingAddress", JoinType.LEFT).join("state", JoinType.LEFT).get("code");
            } else {
                return null;
            }
        }

        @Override
        public void addFetchJoins(Root<Contact> rootEntity) {
            rootEntity.fetch("mailingAddress", JoinType.LEFT);
            rootEntity.fetch("account", JoinType.LEFT);
        }

        @Override
        public String toString() {
            return "RelatedContacts{" +
                    "account='" + account + '\'' +
                    '}';
        }

    }
}

