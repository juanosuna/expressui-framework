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

package com.expressui.sample.view.opportunity;

import com.expressui.core.view.EntityForm;
import com.expressui.core.view.field.FormFields;
import com.expressui.core.view.field.SelectField;
import com.expressui.sample.entity.Opportunity;
import com.expressui.sample.view.select.AccountSelect;
import com.expressui.sample.view.select.UserSelect;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
@Scope("prototype")
@SuppressWarnings({"serial"})
public class OpportunityForm extends EntityForm<Opportunity> {

    @Resource
    private AccountSelect accountSelect;

    @Resource
    private UserSelect userSelect;

    @Override
    public void configureFields(FormFields formFields) {

        formFields.setPosition("Overview", "name", 1, 1);
        formFields.setPosition("Overview", "opportunityType", 1, 2);

        formFields.setPosition("Overview", "account.name", 2, 1);
        formFields.setPosition("Overview", "leadSource", 2, 2);

        formFields.setPosition("Overview", "salesStage", 3, 1);
        formFields.setPosition("Overview", "assignedTo.loginName", 3, 2);

        formFields.setPosition("Overview", "amount", 4, 1);
        formFields.setPosition("Overview", "currency", 4, 2);

        formFields.setPosition("Overview", "probability", 5, 1);
        formFields.setPosition("Overview", "amountWeightedInUSD", 5, 2);

        formFields.setPosition("Overview", "expectedCloseDate", 6, 1);

        formFields.setPosition("Description", "description", 1, 1);

        formFields.setLabel("description", null);
        formFields.setLabel("opportunityType", "Type");
        formFields.setLabel("account.name", "Account");
        formFields.setLabel("assignedTo.loginName", "Assigned to");

        SelectField selectField = new SelectField(this, "assignedTo", userSelect);
        formFields.setField("assignedTo.loginName", selectField);

        selectField = new SelectField(this, "account", accountSelect);
        formFields.setField("account.name", selectField);
    }

    @Override
    public String getEntityCaption() {
        if (getEntity().getName() == null) {
            return "Opportunity Form - New";
        } else {
            return "Opportunity Form - " + getEntity().getName();
        }
    }
}
