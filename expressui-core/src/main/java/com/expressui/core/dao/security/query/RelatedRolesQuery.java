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
import com.expressui.core.dao.security.RoleDao;
import com.expressui.core.entity.security.Role;
import com.expressui.core.entity.security.User;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.beans.factory.config.BeanDefinition.SCOPE_PROTOTYPE;

/**
 * Query for finding roles related to a user.
 */
@Component
@Scope(SCOPE_PROTOTYPE)
public class RelatedRolesQuery extends ToManyRelationshipQuery<Role, User> {

    @Resource
    private RoleDao roleDao;

    private User user;

    @Override
    public void setParent(User parent) {
        this.user = parent;
    }

    @Override
    public User getParent() {
        return user;
    }

    @Override
    public List<Role> execute() {
        return roleDao.execute(this);
    }

    @Override
    public List<Predicate> buildCriteria(CriteriaBuilder builder, CriteriaQuery query, Root<Role> role) {
        List<Predicate> predicates = new ArrayList<Predicate>();

        if (hasValue(user)) {
            ParameterExpression<User> userExp = builder.parameter(User.class, "user");
            predicates.add(builder.equal(role.join("userRoles").get("user"), userExp));
        }

        return predicates;
    }

    @Override
    public void setParameters(TypedQuery typedQuery) {
        if (hasValue(user)) {
            typedQuery.setParameter("user", user);
        }
    }

    @Override
    public void addFetchJoins(Root<Role> role) {
        role.fetch("userRoles", JoinType.INNER).fetch("user", JoinType.INNER);
    }

    @Override
    public String toString() {
        return "RelatedRolesQuery{" +
                "user='" + user + '\'' +
                '}';
    }

}
