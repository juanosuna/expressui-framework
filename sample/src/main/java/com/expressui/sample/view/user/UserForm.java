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

package com.expressui.sample.view.user;

import com.expressui.core.view.EntityForm;
import com.expressui.core.view.field.FormFields;
import com.expressui.core.view.tomanyrelationship.ToManyRelationship;
import com.expressui.sample.dao.RoleDao;
import com.expressui.sample.dao.UserRoleDao;
import com.expressui.sample.entity.security.Role;
import com.expressui.sample.entity.security.User;
import com.expressui.sample.entity.security.UserRole;
import com.expressui.sample.view.user.related.RelatedRoles;
import com.vaadin.ui.PasswordField;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Component
@Scope("prototype")
@SuppressWarnings({"rawtypes", "serial"})
public class UserForm extends EntityForm<User> {

    @Resource
    private RelatedRoles relatedRoles;

    @Resource
    private RoleDao roleDao;

    @Resource
    private UserRoleDao userRoleDao;

    @Override
    public List<ToManyRelationship> getToManyRelationships() {
        List<ToManyRelationship> toManyRelationships = new ArrayList<ToManyRelationship>();
        toManyRelationships.add(relatedRoles);

        return toManyRelationships;
    }

    @Override
    public void configureFields(FormFields formFields) {
        formFields.setPosition("loginName", 1, 1);
        formFields.setPosition("loginPassword", 1, 2);
        formFields.setField("loginPassword", new PasswordField());

        formFields.setPosition("accountExpired", 2, 1);
        formFields.setPosition("accountLocked", 2, 2);
        formFields.setPosition("credentialsExpired", 3, 1);
        formFields.setPosition("enabled", 3, 2);

        addPersistListener(this, "onPersist");
    }

    public void onPersist() {
        Role anyUserRole = roleDao.findByName("ROLE_USER");
        UserRole userRole = new UserRole(getEntity(), anyUserRole);
        userRoleDao.persist(userRole);
    }

    @Override
    public String getEntityCaption() {
        return "User Form";
    }
}
