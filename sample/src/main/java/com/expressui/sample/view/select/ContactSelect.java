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

package com.expressui.sample.view.select;

import com.expressui.core.view.entityselect.EntitySelect;
import com.expressui.core.view.entityselect.EntitySelectResults;
import com.expressui.core.view.field.DisplayFields;
import com.expressui.sample.dao.ContactDao;
import com.expressui.sample.entity.Contact;
import com.expressui.sample.util.PhonePropertyFormatter;
import com.expressui.sample.view.contact.ContactQuery;
import com.expressui.sample.view.contact.ContactSearchForm;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
@Scope("prototype")
@SuppressWarnings({"serial"})
public class ContactSelect extends EntitySelect<Contact> {

    @Resource
    private ContactSearchForm contactSearchForm;

    @Resource
    private ContactSelectResults contactSelectResults;

    @Override
    public ContactSearchForm getSearchForm() {
        return contactSearchForm;
    }

    @Override
    public ContactSelectResults getResults() {
        return contactSelectResults;
    }

    @Override
    public String getEntityCaption() {
        return "Select Contact";
    }

    @Component
    @Scope("prototype")
    public static class ContactSelectResults extends EntitySelectResults<Contact> {

        @Resource
        private ContactDao contactDao;

        @Resource
        private ContactQuery contactQuery;

        @Override
        public ContactDao getEntityDao() {
            return contactDao;
        }

        @Override
        public ContactQuery getEntityQuery() {
            return contactQuery;
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
            displayFields.setPropertyFormatter("mainPhone", new PhonePropertyFormatter());
        }
    }
}

