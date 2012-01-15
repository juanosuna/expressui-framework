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

import com.expressui.core.dao.query.ToManyRelationshipQuery;
import com.expressui.core.view.field.DisplayFields;
import com.expressui.core.view.tomanyrelationship.ToManyAggregationRelationshipResults;
import com.expressui.core.view.tomanyrelationship.ToManyRelationship;
import com.expressui.sample.dao.query.RelatedContactsQuery;
import com.expressui.sample.entity.Contact;
import com.expressui.sample.util.formatter.PhonePropertyFormatter;
import com.expressui.sample.view.select.ContactSelect;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

import static org.springframework.beans.factory.config.BeanDefinition.SCOPE_PROTOTYPE;

@Component
@Scope(SCOPE_PROTOTYPE)
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

    @Component
    @Scope(SCOPE_PROTOTYPE)
    @SuppressWarnings("rawtypes")
    public static class RelatedContactsResults extends ToManyAggregationRelationshipResults<Contact> {

        @Resource
        private ContactSelect contactSelect;

        @Resource
        private RelatedContactsQuery relatedContactsQuery;

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
            displayFields.setPropertyIds(
                    "name",
                    "title",
                    "mailingAddress.state.code",
                    "mailingAddress.country",
                    "mainPhone"
            );

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
}

