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

import com.expressui.core.entity.AuditableEntity;
import org.hibernate.annotations.ForeignKey;
import org.hibernate.annotations.Index;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Association entity that relates user and role
 */
@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@Table
public class UserRole extends AuditableEntity {

    @EmbeddedId
    private Id id = new Id();

    @Index(name = "IDX_USER_ROLE_USER")
    @ForeignKey(name = "FK_USER_ROLE_USER")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(insertable = false, updatable = false)
    private User user;

    @Index(name = "IDX_USER_ROLE_ROLE")
    @ForeignKey(name = "FK_USER_ROLE_ROLE")
    @JoinColumn(insertable = false, updatable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private Role role;

    /**
     * Construct relationship between user and role
     *
     * @param userId primary key of related user
     * @param roleId primary key of related role
     */
    public UserRole(Long userId, Long roleId) {
        id.userId = userId;
        id.roleId = roleId;
    }

    public UserRole() {
    }

    /**
     * Construct relationship between user and role
     *
     * @param user related user
     * @param role related role
     */
    public UserRole(User user, Role role) {
        this(user.getId(), role.getId());
        this.user = user;
        this.role = role;
    }

    /**
     * Get primary composition key
     *
     * @return composition key comprised of user id and role id
     */
    public Id getId() {
        return id;
    }

    /**
     * Get related user.
     *
     * @return related user
     */
    public User getUser() {
        return user;
    }

    /**
     * Get related role.
     *
     * @return related role
     */
    public Role getRole() {
        return role;
    }

    /**
     * Composition primary key, comprised of related user id and role id
     */
    @Embeddable
    public static class Id implements Serializable {
        @Column(name = "USER_ID")
        private Long userId;
        @Column(name = "ROLE_ID")
        private Long roleId;

        public Id() {
        }

        /**
         * Construct composition key with related user id and role id
         *
         * @param userId related user id
         * @param roleId related role id
         */
        public Id(Long userId, Long roleId) {
            this.userId = userId;
            this.roleId = roleId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Id)) return false;

            Id id = (Id) o;

            return roleId.equals(id.roleId) && userId.equals(id.userId);
        }

        @Override
        public int hashCode() {
            int result = userId.hashCode();
            result = 31 * result + roleId.hashCode();
            return result;
        }
    }
}
