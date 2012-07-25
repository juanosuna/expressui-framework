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

import com.expressui.core.entity.NamedEntity;
import com.expressui.core.entity.WritableEntity;
import com.expressui.core.util.ObjectUtil;
import com.expressui.core.validation.AssertTrueForProperties;
import org.hibernate.validator.constraints.NotBlank;
import org.jasypt.util.password.BasicPasswordEncryptor;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.HashSet;
import java.util.Set;

/**
 * User, authorized to login into application.
 */
@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@Table(name = "UZER") // avoid table named user, as this is a reserved word in Oracle
public class User extends WritableEntity implements NamedEntity {

    @Column(unique=true)
    private String loginName;

    private String loginPasswordEncrypted;
    @Transient
    private String loginPassword;
    @Transient
    private String repeatLoginPassword;

    private boolean accountExpired = false;
    private boolean accountLocked = false;
    private boolean credentialsExpired = false;
    private boolean enabled = true;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<UserRole> userRoles = new HashSet<UserRole>();

    public User() {
    }

    /**
     * Constructs this user with login name and password.
     *
     * @param loginName     login name
     * @param loginPassword password
     */
    public User(String loginName, String loginPassword) {
        this();
        setLoginName(loginName);
        setLoginPassword(loginPassword);
        setRepeatLoginPassword(loginPassword);
    }

    /**
     * Gets login name.
     *
     * @return login name
     */
    @NotBlank
    @NotNull
    @Size(min = 4, max = 16)
    public String getLoginName() {
        return loginName;
    }

    /**
     * Sets login name.
     *
     * @param loginName login name, must be between 4 and 16 characters
     */
    public void setLoginName(String loginName) {
        this.loginName = loginName;
    }

    @Override
    public String getName() {
        return getLoginName();
    }

    public String getLoginPasswordEncrypted() {
        return loginPasswordEncrypted;
    }

    public void setLoginPasswordEncrypted(String loginPasswordEncrypted) {
        this.loginPasswordEncrypted = loginPasswordEncrypted;
    }

    /**
     * Gets password.
     *
     * @return password
     */
    @Size(min = 4, max = 16)
    public String getLoginPassword() {
        return loginPassword;
    }

    /**
     * Sets password.
     *
     * @param loginPassword password, must be between 4 and 16 characters
     */
    public void setLoginPassword(String loginPassword) {
        this.loginPassword = loginPassword;
        if (loginPassword != null) {
            BasicPasswordEncryptor passwordEncryptor = new BasicPasswordEncryptor();
            String encryptedPassword = passwordEncryptor.encryptPassword(loginPassword);
            setLoginPasswordEncrypted(encryptedPassword);
        }
    }

    /**
     * Gets the password the user re-entered to ensure he/she doesn't make a mistake.
     *
     * @return re-entered password
     */
    public String getRepeatLoginPassword() {
        return repeatLoginPassword;
    }

    /**
     * Sets the password the user re-entered to ensure he/she doesn't make a mistake.
     *
     * @param repeatLoginPassword re-entered password
     */
    public void setRepeatLoginPassword(String repeatLoginPassword) {
        this.repeatLoginPassword = repeatLoginPassword;
    }

    /**
     * Validation method to check if loginPassword and repeatLoginPassword match.
     *
     * @return true if passwords match
     */
    @AssertTrueForProperties(errorProperty = "repeatLoginPassword",
            message = "{com.expressui.core.entity.security.User.passwordsDoNotMatch}")
    public boolean isPasswordMatch() {
        return ObjectUtil.isEqual(getLoginPassword(), getRepeatLoginPassword());
    }

    /**
     * Asks if this user's account is expired.
     *
     * @return true if expired
     */
    public boolean isAccountExpired() {
        return accountExpired;
    }

    /**
     * Sets whether or not this user's account is expired.
     *
     * @param accountExpired true if is expired
     */
    public void setAccountExpired(boolean accountExpired) {
        this.accountExpired = accountExpired;
    }

    /**
     * Asks if this user's account is locked.
     *
     * @return true if is locked
     */
    public boolean isAccountLocked() {
        return accountLocked;
    }

    /**
     * Sets whether or not this user's account is locked.
     *
     * @param accountLocked true if locked
     */
    public void setAccountLocked(boolean accountLocked) {
        this.accountLocked = accountLocked;
    }

    /**
     * Asks if this user's credentials have expired.
     *
     * @return true if credentials have expired
     */
    public boolean isCredentialsExpired() {
        return credentialsExpired;
    }

    /**
     * Sets whether or not this user's credentials have expired.
     *
     * @param credentialsExpired true if credentials have expired
     */
    public void setCredentialsExpired(boolean credentialsExpired) {
        this.credentialsExpired = credentialsExpired;
    }

    /**
     * Asks if this user's account is enabled.
     *
     * @return true if is enabled
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Sets whether or not this user's account is enabled.
     *
     * @param enabled true if enabled
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * Gets user-role association entities.
     *
     * @return user-role association entities
     */
    public Set<UserRole> getUserRoles() {
        return userRoles;
    }

    /**
     * Sets user-role association entities.
     *
     * @param userRoles user-role association entities
     */
    public void setUserRoles(Set<UserRole> userRoles) {
        this.userRoles = userRoles;
    }

    /**
     * Gets all roles assigned to this user.
     *
     * @return all roles assigned to this user
     */
    public Set<Role> getRoles() {
        Set<Role> roles = new HashSet<Role>();

        Set<UserRole> userRoles = getUserRoles();
        for (UserRole userRole : userRoles) {
            roles.add(userRole.getRole());
        }

        return roles;
    }

    /**
     * Asks if this user has a given role.
     *
     * @param role to check if it belongs to this user
     * @return true if user has the role
     */
    public boolean hasRole(Role role) {
        Set<Role> roles = getRoles();
        for (Role r : roles) {
            if (role.equals(r)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Asks if this user has a given role.
     *
     * @param role to check if it belongs to this user
     * @return true if user has the role
     */
    public boolean hasRole(String role) {
        Set<Role> roles = getRoles();
        for (Role r : roles) {
            if (role.equals(r.getName())) {
                return true;
            }
        }

        return false;
    }

    /**
     * Asks if view access is allowed for given type.
     *
     * @param type type to check
     * @return true if view access is allowed
     */
    public boolean isViewAllowed(String type) {
        Set<UserRole> roles = getUserRoles();
        for (UserRole role : roles) {
            if (role.getRole().isViewAllowed(type)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Asks if edit access is allowed for given type.
     *
     * @param type type to check
     * @return true if edit access is allowed
     */
    public boolean isEditAllowed(String type) {
        Set<UserRole> roles = getUserRoles();
        for (UserRole role : roles) {
            if (role.getRole().isEditAllowed(type)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Asks if create access is allowed for given type.
     *
     * @param type type to check
     * @return true if create access is allowed
     */
    public boolean isCreateAllowed(String type) {
        Set<UserRole> roles = getUserRoles();
        for (UserRole role : roles) {
            if (role.getRole().isCreateAllowed(type)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Asks if delete access is allowed for given type.
     *
     * @param type type to check
     * @return true if delete access is allowed
     */
    public boolean isDeleteAllowed(String type) {
        Set<UserRole> roles = getUserRoles();
        for (UserRole role : roles) {
            if (role.getRole().isDeleteAllowed(type)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Asks if view access is allowed for given field (property) within given type.  If no permission
     * exists for field, then defaults to permission of the containing type.
     *
     * @param type  type to check
     * @param field field to check
     * @return true if view access is allowed
     */
    public boolean isViewAllowed(String type, String field) {
        Set<UserRole> roles = getUserRoles();
        for (UserRole role : roles) {
            if (role.getRole().isViewAllowed(type, field)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Asks if edit access is allowed for given field (property) within given type.  If no permission
     * exists for field, then defaults to permission of the containing type.
     *
     * @param type  type to check
     * @param field field to check
     * @return true if edit access is allowed
     */
    public boolean isEditAllowed(String type, String field) {
        Set<UserRole> roles = getUserRoles();
        for (UserRole role : roles) {
            if (role.getRole().isEditAllowed(type, field)) {
                return true;
            }
        }

        return false;
    }
}
