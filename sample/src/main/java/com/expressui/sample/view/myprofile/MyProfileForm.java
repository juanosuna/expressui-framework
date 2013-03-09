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

import com.expressui.core.view.field.format.UrlPropertyFormatter;
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
@SuppressWarnings({"serial"})
public class MyProfileForm<T extends Profile> extends UserOwnedEntityForm<Profile> {

    @Override
    public void init(FormFieldSet formFields) {

        formFields.setCoordinates(id(p.getFirstName()), 1, 1);
        formFields.setCoordinates(id(p.getLastName()), 1, 2);

        formFields.setCoordinates(id(p.getTitle()), 2, 1);
        formFields.setCoordinates(id(p.getCompanyWebsite()), 2, 2);

        formFields.setCoordinates(id(p.getEmail()), 3, 1);
        formFields.setCoordinates(id(p.getPhone()), 3, 2);
        formFields.setCoordinates(id(p.getPhoneType()), 3, 2);

        formFields.setCoordinates(id(p.getUser().getLoginName()), 4, 1);
        formFields.setCoordinates(id(p.getUser().getLoginPassword()), 4, 2);
        formFields.setCoordinates(id(p.getUser().getRepeatLoginPassword()), 5, 2);

        formFields.setPropertyFormatter(id(p.getCompanyWebsite()), new UrlPropertyFormatter());

        formFields.setOriginalReadOnly(id(p.getUser().getLoginName()), true);

        formFields.setWidth(id(p.getPhoneType()), 7, Sizeable.UNITS_EM);
        getFormFieldSet().setToolTipArgs(id(p.getPhone()), Phone.getExampleNumber(
                getMainApplication().getLocale().getCountry()));

        formFields.addConversionValidator(id(p.getPhone()), new PhoneConversionValidator());
        formFields.setPropertyFormatter(id(p.getPhone()), new PhonePropertyFormatter());

        formFields.setField(id(p.getUser().getLoginPassword()), new PasswordField());
        formFields.setField(id(p.getUser().getRepeatLoginPassword()), new PasswordField());
    }
}
