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

package com.expressui.sample.view.myprofile;

import com.expressui.core.formatter.UrlPropertyFormatter;
import com.expressui.core.view.form.FormFieldSet;
import com.expressui.core.view.form.UserOwnedEntityForm;
import com.expressui.sample.entity.Phone;
import com.expressui.sample.entity.Profile;
import com.expressui.sample.formatter.PhonePropertyFormatter;
import com.expressui.sample.validator.PhoneConversionValidator;
import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.PasswordField;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import static org.springframework.beans.factory.config.BeanDefinition.SCOPE_PROTOTYPE;

@Component
@Scope(SCOPE_PROTOTYPE)
@SuppressWarnings({"rawtypes", "serial"})
public class MyProfileForm<T> extends UserOwnedEntityForm<Profile> {

    @Override
    public void init(FormFieldSet formFields) {

        formFields.setCoordinates("firstName", 1, 1);
        formFields.setCoordinates("lastName", 1, 2);

        formFields.setCoordinates("title", 2, 1);
        formFields.setCoordinates("companyWebsite", 2, 2);

        formFields.setCoordinates("email", 3, 1);
        formFields.setCoordinates("phone", 3, 2);
        formFields.setCoordinates("phoneType", 3, 2);

        formFields.setCoordinates("user.loginName", 4, 1);
        formFields.setCoordinates("user.loginPassword", 4, 2);
        formFields.setCoordinates("user.repeatLoginPassword", 5, 2);

        formFields.setPropertyFormatter("companyWebsite", new UrlPropertyFormatter());

        formFields.setReadOnly("user.loginName", true);

        formFields.setLabel("phoneType", null);
        formFields.setWidth("phoneType", 7, Sizeable.UNITS_EM);
        formFields.setToolTip("phone", Phone.TOOL_TIP);

        formFields.addConversionValidator("phone", PhoneConversionValidator.class);
        formFields.setPropertyFormatter("phone", new PhonePropertyFormatter());

        formFields.setField("user.loginPassword", new PasswordField());
        formFields.setField("user.repeatLoginPassword", new PasswordField());
    }

    @Override
    public String getTypeCaption() {
        return "My Profile";
    }
}
