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

package com.expressui.core.dao.security.query;

import com.expressui.core.dao.query.ToManyRelationshipQuery;
import com.expressui.core.dao.security.UserDao;
import com.expressui.core.entity.security.Role;
import com.expressui.core.entity.security.User;
import com.expressui.core.entity.security.UserRole;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.beans.factory.config.BeanDefinition.SCOPE_PROTOTYPE;

/**
 * User: Juan
 * Date: 5/30/12
 */
@Component
@Scope(SCOPE_PROTOTYPE)
public class RelatedUsersQuery extends ToManyRelationshipQuery<User, Role> {

    @Resource
    private UserDao userDao;

    private Role role;

    private Role doesNotBelongToRole;

    @Override
    public void setParent(Role parent) {
        this.role = parent;
    }

    @Override
    public Role getParent() {
        return role;
    }

    /**
     * Get Role that matching users must not belong to.
     *
     * @return role
     */
    public Role getDoesNotBelongToRole() {
        return doesNotBelongToRole;
    }

    /**
     * Set Role that matching users must not belong to.
     *
     * @param doesNotBelongToRole role
     */
    public void setDoesNotBelongToRole(Role doesNotBelongToRole) {
        this.doesNotBelongToRole = doesNotBelongToRole;
    }

    @Override
    public List<User> execute() {
        return userDao.execute(this);
    }

    @Override
    public List<Predicate> buildCriteria(CriteriaBuilder builder, CriteriaQuery<User> query, Root<User> user) {
        List<Predicate> predicates = new ArrayList<Predicate>();

        if (hasValue(role)) {
            ParameterExpression<Role> roleExp = builder.parameter(Role.class, "role");
            predicates.add(builder.equal(user.join("userRoles").get("role"), roleExp));
        }

        if (hasValue(doesNotBelongToRole)) {
            Subquery<User> subquery = query.subquery(User.class);
            Root userRole = subquery.from(UserRole.class);
            ParameterExpression<Role> roleExp = builder.parameter(Role.class, "doesNotBelongToRole");
            subquery.select(userRole.get("user")).where(builder.equal(userRole.get("role"), roleExp));
            predicates.add(builder.not(user.in(subquery)));
        }

        return predicates;
    }

    @Override
    public void setParameters(TypedQuery<Serializable> typedQuery) {
        if (hasValue(role)) {
            typedQuery.setParameter("role", role);
        }
        if (hasValue(doesNotBelongToRole)) {
            typedQuery.setParameter("doesNotBelongToRole", doesNotBelongToRole);
        }
    }

    @Override
    public void addFetchJoins(Root<User> user) {
        user.fetch("userRoles", JoinType.INNER).fetch("user", JoinType.INNER);
    }

    @Override
    public void clear() {
        Role doesNotBelongToRole = this.doesNotBelongToRole;
        super.clear();
        this.doesNotBelongToRole = doesNotBelongToRole;
    }

    @Override
    public String toString() {
        return "RelatedUsers{" +
                "role='" + role + '\'' +
                '}';
    }
}
