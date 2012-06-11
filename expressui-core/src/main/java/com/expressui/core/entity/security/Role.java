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

import com.expressui.core.entity.WritableEntity;
import com.expressui.core.util.assertion.Assert;
import org.hibernate.validator.constraints.NotBlank;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.HashSet;
import java.util.Set;

/**
 * Security role that can be assigned to Users
 */
@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@Table
public class Role extends WritableEntity {

    private String name;

    @Enumerated(EnumType.STRING)
    private AllowOrDeny allowOrDenyByDefault = AllowOrDeny.ALLOW;

    @Lob
    private String description;

    @OneToMany(mappedBy = "role", cascade = CascadeType.ALL)
    private Set<UserRole> userRoles = new HashSet<UserRole>();

    @OneToMany(mappedBy = "role", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Permission> permissions = new HashSet<Permission>();

    public Role() {
    }

    public Role(String name) {
        this.name = name;
    }

    /**
     * Get the name of this role
     *
     * @return name of this role
     */
    @NotBlank
    @NotNull
    @Size(min = 4, max = 64)
    public String getName() {
        return name;
    }

    /**
     * Set the name of this role
     *
     * @param name name of this role, must be between 4 and 64 characters
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Get the default permission logic, i.e. allow or deny
     *
     * @return AllowOrDeny.ALLOW or AllowOrDeny.DENY
     */
    public AllowOrDeny getAllowOrDenyByDefault() {
        return allowOrDenyByDefault;
    }

    /**
     * Set the default permission logic, i.e. allow or deny
     *
     * @param allowOrDenyByDefault AllowOrDeny.ALLOW or AllowOrDeny.DENY
     */
    public void setAllowOrDenyByDefault(AllowOrDeny allowOrDenyByDefault) {
        this.allowOrDenyByDefault = allowOrDenyByDefault;
    }

    /**
     * Get description of this role.
     *
     * @return user-friendly description
     */
    @Size(min = 4, max = 255)
    public String getDescription() {
        return description;
    }

    /**
     * Set the description for this role.
     *
     * @param description user-friendly description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Get user-role association entities.
     *
     * @return user-role association entities
     */
    public Set<UserRole> getUserRoles() {
        return userRoles;
    }

    /**
     * Set user-role association entities.
     *
     * @param userRoles user-role association entities
     */
    public void setUserRoles(Set<UserRole> userRoles) {
        this.userRoles = userRoles;
    }

    /**
     * Get access permissions assigned to this role.
     *
     * @return permissions assigned to this role
     */
    public Set<Permission> getPermissions() {
        return permissions;
    }

    /**
     * Set access permissions assigned to this role.
     *
     * @param permissions permissions assigned to this role
     */
    public void setPermissions(Set<Permission> permissions) {
        this.permissions = permissions;
    }

    /**
     * Get permission for accessing given type.
     *
     * @param type type for which permission is retrieved
     * @return permission for accessing given type
     */
    public Permission getPermission(String type) {
        Set<? extends Permission> permissions = getPermissions();

        Permission foundPermission = null;
        for (Permission permission : permissions) {
            if (permission.getTargetType().equals(type) && permission.getField() == null) {
                Assert.DATABASE.isNull(foundPermission, "Database must not contain two records" +
                        " with the same type and field: " + type);
                foundPermission = permission;
            }
        }

        return foundPermission;
    }

    /**
     * Get permission for accessing given type and field (property)
     *
     * @param type  type
     * @param field field (property) of the type
     * @return permission for field within type
     */
    public Permission getPermission(String type, String field) {
        Set<? extends Permission> permissions = getPermissions();

        Permission foundPermission = null;
        for (Permission permission : permissions) {
            if (permission.getTargetType().equals(type) && permission.getField() != null
                    && permission.getField().equals(field)) {
                Assert.DATABASE.isNull(foundPermission, "Database must not contain two records" +
                        " with the same type and field: " + type + "." + field);
                foundPermission = permission;
            }
        }

        return foundPermission;
    }

    /**
     * Ask if view access is allowed for given type
     *
     * @param type type
     * @return true if view access is allowed
     */
    public boolean isViewAllowed(String type) {
        return getPermission(type) == null ? allowOrDenyByDefault == AllowOrDeny.ALLOW
                : getPermission(type).isViewAllowed();
    }

    /**
     * Ask if edit access is allowed for given type
     *
     * @param type type
     * @return true if edit access is allowed
     */
    public boolean isEditAllowed(String type) {
        return getPermission(type) == null ? allowOrDenyByDefault == AllowOrDeny.ALLOW
                : getPermission(type).isEditAllowed();
    }

    /**
     * Ask if create access is allowed for given type
     *
     * @param type type
     * @return true if create access is allowed
     */
    public boolean isCreateAllowed(String type) {
        return getPermission(type) == null ? allowOrDenyByDefault == AllowOrDeny.ALLOW
                : getPermission(type).isCreateAllowed();
    }

    /**
     * Ask if delete access is allowed for given type
     *
     * @param type type
     * @return true if delete access is allowed
     */
    public boolean isDeleteAllowed(String type) {
        return getPermission(type) == null ? allowOrDenyByDefault == AllowOrDeny.ALLOW
                : getPermission(type).isDeleteAllowed();
    }

    /**
     * Ask if view access is allowed for given field (property) within given type
     *
     * @param type  type
     * @param field field
     * @return true if view access is allowed
     */
    public boolean isViewAllowed(String type, String field) {
        return getPermission(type, field) == null ?
                isViewAllowed(type) : getPermission(type, field).isViewAllowed();
    }

    /**
     * Ask if edit access is allowed for given field (property) within given type
     *
     * @param type  type
     * @param field field
     * @return true if edit access is allowed
     */
    public boolean isEditAllowed(String type, String field) {
        return getPermission(type, field) == null ?
                isEditAllowed(type) || isCreateAllowed(type) : getPermission(type, field).isEditAllowed();
    }
}
