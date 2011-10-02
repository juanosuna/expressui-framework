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

package com.expressui.core.entity.security;

import com.expressui.core.entity.WritableEntity;
import com.expressui.core.util.assertion.Assert;
import org.hibernate.validator.constraints.NotBlank;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.HashSet;
import java.util.Set;

@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@Table
public abstract class AbstractRole extends WritableEntity {

    private String name;

    @Enumerated(EnumType.STRING)
    private AllowOrDeny allowOrDenyByDefault = AllowOrDeny.ALLOW;

    @Lob
    private String description;


    @OneToMany(mappedBy = "role", cascade = CascadeType.ALL)
    private Set<AbstractUserRole> userRoles = new HashSet<AbstractUserRole>();

    @OneToMany(mappedBy = "role", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<AbstractPermission> permissions = new HashSet<AbstractPermission>();

    public AbstractRole() {
    }

    public AbstractRole(String name) {
        this.name = name;
    }

    @NotBlank
    @NotNull
    @Size(min = 4, max = 64)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public AllowOrDeny getAllowOrDenyByDefault() {
        return allowOrDenyByDefault;
    }

    public void setAllowOrDenyByDefault(AllowOrDeny allowOrDenyByDefault) {
        this.allowOrDenyByDefault = allowOrDenyByDefault;
    }

    @Size(min = 4, max = 255)
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Set<AbstractUserRole> getUserRoles() {
        return userRoles;
    }

    public void setUserRoles(Set<AbstractUserRole> userRoles) {
        this.userRoles = userRoles;
    }

    public Set<AbstractPermission> getPermissions() {
        return permissions;
    }

    public void setPermissions(Set<AbstractPermission> permissions) {
        this.permissions = permissions;
    }

    public AbstractPermission getPermission(String entityType) {
        Set<? extends AbstractPermission> permissions = getPermissions();

        AbstractPermission foundPermission = null;
        for (AbstractPermission permission : permissions) {
            if (permission.getEntityType().equals(entityType) && permission.getField() == null) {
                Assert.DATABASE.assertTrue(foundPermission == null, "Database must not contain two records" +
                        " with the same entityType and field: " + entityType);
                foundPermission = permission;
            }
        }

        return foundPermission;
    }

    public AbstractPermission getPermission(String entityType, String field) {
        Set<? extends AbstractPermission> permissions = getPermissions();

        AbstractPermission foundPermission = null;
        for (AbstractPermission permission : permissions) {
            if (permission.getEntityType().equals(entityType) && permission.getField() != null
                    && permission.getField().equals(field)) {
                Assert.DATABASE.assertTrue(foundPermission == null, "Database must not contain two records" +
                        " with the same entityType and field: " + entityType + "." + field);
                foundPermission = permission;
            }
        }

        return foundPermission;
    }

    public boolean isViewAllowed(String entityType) {
        return getPermission(entityType) == null ? allowOrDenyByDefault == AllowOrDeny.ALLOW
                : getPermission(entityType).isView();
    }

    public boolean isEditAllowed(String entityType) {
        return getPermission(entityType) == null ? allowOrDenyByDefault == AllowOrDeny.ALLOW
                : getPermission(entityType).isEdit();
    }

    public boolean isCreateAllowed(String entityType) {
        return getPermission(entityType) == null ? allowOrDenyByDefault == AllowOrDeny.ALLOW
                : getPermission(entityType).isCreate();
    }

    public boolean isDeleteAllowed(String entityType) {
        return getPermission(entityType) == null ? allowOrDenyByDefault == AllowOrDeny.ALLOW
                : getPermission(entityType).isDelete();
    }

    public boolean isViewAllowed(String entityType, String field) {
        return getPermission(entityType, field) == null ?
                allowOrDenyByDefault == AllowOrDeny.ALLOW && isViewAllowed(entityType)
                : getPermission(entityType, field).isView();
    }

    public boolean isEditAllowed(String entityType, String field) {
        return getPermission(entityType, field) == null ?
                allowOrDenyByDefault == AllowOrDeny.ALLOW && isEditAllowed(entityType)
                : getPermission(entityType, field).isEdit();

    }
}
