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
import org.hibernate.annotations.NaturalId;
import org.hibernate.validator.constraints.NotBlank;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.HashSet;
import java.util.Set;

@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@Table
public abstract class AbstractUser extends WritableEntity {
    @NaturalId
    private String loginName;
    private String loginPassword;
    private boolean accountExpired = false;
    private boolean accountLocked = false;
    private boolean credentialsExpired = false;
    private boolean enabled = true;

    @OneToMany(mappedBy = "user")
    private Set<AbstractUserRole> userRoles = new HashSet<AbstractUserRole>();

    public AbstractUser() {
    }

    public AbstractUser(String loginName, String loginPassword) {
        this.loginName = loginName;
        this.loginPassword = loginPassword;
    }

    @NotBlank
    @NotNull
    @Size(min = 4, max = 16)
    public String getLoginName() {
        return loginName;
    }

    public void setLoginName(String loginName) {
        this.loginName = loginName;
    }

    @NotBlank
    @NotNull
    @Size(min = 4, max = 16)
    public String getLoginPassword() {
        return loginPassword;
    }

    public void setLoginPassword(String loginPassword) {
        this.loginPassword = loginPassword;
    }

    public boolean isAccountExpired() {
        return accountExpired;
    }

    public void setAccountExpired(boolean accountExpired) {
        this.accountExpired = accountExpired;
    }

    public boolean isAccountLocked() {
        return accountLocked;
    }

    public void setAccountLocked(boolean accountLocked) {
        this.accountLocked = accountLocked;
    }

    public boolean isCredentialsExpired() {
        return credentialsExpired;
    }

    public void setCredentialsExpired(boolean credentialsExpired) {
        this.credentialsExpired = credentialsExpired;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Set<AbstractUserRole> getUserRoles() {
        return userRoles;
    }

    public void setUserRoles(Set<AbstractUserRole> userRoles) {
        this.userRoles = userRoles;
    }

    public Set<AbstractRole> getRoles() {
        Set<AbstractRole> roles = new HashSet<AbstractRole>();

        Set<AbstractUserRole> userRoles = (Set<AbstractUserRole>) getUserRoles();
        for (AbstractUserRole userRole : userRoles) {
            roles.add(userRole.getRole());
        }

        return roles;
    }

    public boolean isViewAllowed(String entityType) {
        Set<AbstractUserRole> roles = (Set<AbstractUserRole>) getUserRoles();
        for (AbstractUserRole role : roles) {
            if (role.getRole().isViewAllowed(entityType)) {
                return true;
            }
        }

        return false;
    }

    public boolean isEditAllowed(String entityType) {
        Set<AbstractUserRole> roles = (Set<AbstractUserRole>) getUserRoles();
        for (AbstractUserRole role : roles) {
            if (role.getRole().isEditAllowed(entityType)) {
                return true;
            }
        }

        return false;
    }

    public boolean isCreateAllowed(String entityType) {
        Set<AbstractUserRole> roles = (Set<AbstractUserRole>) getUserRoles();
        for (AbstractUserRole role : roles) {
            if (role.getRole().isCreateAllowed(entityType)) {
                return true;
            }
        }

        return false;
    }

    public boolean isDeleteAllowed(String entityType) {
        Set<AbstractUserRole> roles = (Set<AbstractUserRole>) getUserRoles();
        for (AbstractUserRole role : roles) {
            if (role.getRole().isDeleteAllowed(entityType)) {
                return true;
            }
        }

        return false;
    }

    public boolean isViewAllowed(String entityType, String field) {
        Set<AbstractUserRole> roles = (Set<AbstractUserRole>) getUserRoles();
        for (AbstractUserRole role : roles) {
            if (role.getRole().isViewAllowed(entityType, field)) {
                return true;
            }
        }

        return false;
    }

    public boolean isEditAllowed(String entityType, String field) {
        Set<AbstractUserRole> roles = (Set<AbstractUserRole>) getUserRoles();
        for (AbstractUserRole role : roles) {
            if (role.getRole().isEditAllowed(entityType, field)) {
                return true;
            }
        }

        return false;
    }
}
