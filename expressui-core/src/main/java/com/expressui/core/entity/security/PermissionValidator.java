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

package com.expressui.core.entity.security;

import com.expressui.core.dao.security.PermissionDao;
import com.expressui.core.util.SpringApplicationContext;
import com.expressui.core.view.util.MessageSource;

import javax.annotation.Resource;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.List;

/**
 * Validates that a permission is not already redundant with existing permission with the
 * same role, entity type and field.
 */
public class PermissionValidator implements ConstraintValidator<ValidPermission, Permission> {

    @Resource
    private PermissionDao permissionDao;

    @Resource
    private MessageSource validationMessageSource;

    public PermissionValidator() {
        SpringApplicationContext.autowire(this);
    }

    @Override
    public void initialize(ValidPermission constraintAnnotation) {
    }

    @Override
    public boolean isValid(Permission permission, ConstraintValidatorContext context) {
        if (permission == null) return true;

        if (permission.getTargetType() != null) {
            List<Permission> result = permissionDao.findByRoleEntityTypeAndField(permission.getRole(), permission.getTargetType(), permission.getField());
            if (result.isEmpty()) {
                return true;
            } else {
                Permission existingPermission = result.get(0);
                if (!existingPermission.equals(permission)) {
                    context.disableDefaultConstraintViolation();
                    context.buildConstraintViolationWithTemplate(
                            validationMessageSource.getMessage("com.expressui.core.entity.security.ValidPermission.message"))
                            .addConstraintViolation();
                    return false;
                } else {
                    return true;
                }
            }
        } else {
            return true;
        }
    }
}
