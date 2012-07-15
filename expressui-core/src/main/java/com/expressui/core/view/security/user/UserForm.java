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

package com.expressui.core.view.security.user;

import com.expressui.core.dao.security.RoleDao;
import com.expressui.core.dao.security.UserDao;
import com.expressui.core.dao.security.UserRoleDao;
import com.expressui.core.entity.security.Role;
import com.expressui.core.entity.security.User;
import com.expressui.core.entity.security.UserRole;
import com.expressui.core.view.form.EntityForm;
import com.expressui.core.view.form.FormFieldSet;
import com.expressui.core.view.security.user.related.RelatedRoles;
import com.expressui.core.view.tomanyrelationship.ToManyRelationship;
import com.vaadin.ui.PasswordField;
import org.springframework.context.annotation.Scope;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.persistence.NoResultException;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.beans.factory.config.BeanDefinition.SCOPE_PROTOTYPE;

/**
 * Form for viewing or editing users.
 */
@Component
@Scope(SCOPE_PROTOTYPE)
@SuppressWarnings({"rawtypes", "serial"})
public class UserForm extends EntityForm<User> {

    @Resource
    private RelatedRoles relatedRoles;

    @Resource
    private RoleDao roleDao;

    @Resource
    private UserRoleDao userRoleDao;

    @Resource
    private UserDao userDao;

    @Override
    public List<ToManyRelationship> getToManyRelationships() {
        List<ToManyRelationship> toManyRelationships = new ArrayList<ToManyRelationship>();
        toManyRelationships.add(relatedRoles);

        return toManyRelationships;
    }

    @Override
    public void init(FormFieldSet formFields) {
        formFields.setCoordinates("loginName", 1, 1);
        formFields.setCoordinates("loginPassword", 1, 2);
        formFields.setField("loginPassword", new PasswordField());
        formFields.setCoordinates("repeatLoginPassword", 2, 2);
        formFields.setField("repeatLoginPassword", new PasswordField());

        formFields.setCoordinates("accountExpired", 3, 1);
        formFields.setCoordinates("accountLocked", 3, 2);

        formFields.setCoordinates("credentialsExpired", 4, 1);
        formFields.setCoordinates("enabled", 4, 2);
    }

    @Override
    public void preSave(User entity) {
        super.preSave(entity);

        User existingUser;
        try {
            existingUser = userDao.findByLoginName(entity.getLoginName());
            if (!existingUser.equals(entity)) {
                throw new DataIntegrityViolationException(
                        uiMessageSource.getMessage("uniqueLoginNameValidator.errorMessage"));
            }
        } catch (NoResultException e) {
            // login name doesn't already exist
        }
    }

    @Override
    public void postSave(User user) {
        super.postSave(user);

        Role anyUserRole = roleDao.findByName("ROLE_USER");
        if (!user.hasRole(anyUserRole)) {
            UserRole userRole = new UserRole(user, anyUserRole);
            userRoleDao.persist(userRole);
        }
    }
}
