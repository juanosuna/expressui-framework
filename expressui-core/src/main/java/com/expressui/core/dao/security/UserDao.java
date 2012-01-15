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

package com.expressui.core.dao.security;

import com.expressui.core.dao.EntityDao;
import com.expressui.core.dao.query.StructuredEntityQuery;
import com.expressui.core.dao.query.ToManyRelationshipQuery;
import com.expressui.core.entity.security.Role;
import com.expressui.core.entity.security.User;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.beans.factory.config.BeanDefinition.SCOPE_PROTOTYPE;

@Repository
@SuppressWarnings({"rawtypes", "unchecked"})
public class UserDao extends EntityDao<User, Long> {

    @Override
    public List<User> findAll() {
        Query query = getEntityManager().createQuery("SELECT u FROM User u ORDER BY u.loginName");
        setReadOnly(query);

        return query.getResultList();
    }

    @Transactional
    @Override
    public void remove(User user) {
        Query query = getEntityManager().createQuery(
                "UPDATE Account a SET a.assignedTo = null WHERE a.assignedTo = :user");
        query.setParameter("user", user);
        query.executeUpdate();

        query = getEntityManager().createQuery(
                "UPDATE Contact c SET c.assignedTo = null WHERE c.assignedTo = :user");
        query.setParameter("user", user);
        query.executeUpdate();

        query = getEntityManager().createQuery(
                "UPDATE Opportunity o SET o.assignedTo = null WHERE o.assignedTo = :user");
        query.setParameter("user", user);
        query.executeUpdate();

        super.remove(user);
    }

    @Component
    @Scope(SCOPE_PROTOTYPE)
    public static class UserQuery extends StructuredEntityQuery<User> {

        @Resource
        private UserDao userDao;

        private String loginName;
        private Role doesNotBelongToRole;


        public String getLoginName() {
            return loginName;
        }

        public void setLoginName(String loginName) {
            this.loginName = loginName;
        }

        public Role getDoesNotBelongToRole() {
            return doesNotBelongToRole;
        }

        public void setDoesNotBelongToRole(Role doesNotBelongToRole) {
            this.doesNotBelongToRole = doesNotBelongToRole;
        }

        @Override
        public List<User> execute() {
            return userDao.execute(this);
        }

        @Override
        public List<Predicate> buildCriteria(CriteriaBuilder builder, Root<User> rootEntity) {
            List<Predicate> criteria = new ArrayList<Predicate>();

            if (!isEmpty(loginName)) {
                ParameterExpression<String> p = builder.parameter(String.class, "loginName");
                criteria.add(builder.like(builder.upper(rootEntity.<String>get("loginName")), p));
            }

            if (!isEmpty(doesNotBelongToRole)) {
                ParameterExpression<Role> p = builder.parameter(Role.class, "doesNotBelongToRole");
                Join join = rootEntity.join("userRoles", JoinType.LEFT);
                criteria.add(builder.or(
                        builder.notEqual(join.get("role"), p),
                        builder.isNull(join.get("role"))
                ));
            }

            return criteria;
        }

        @Override
        public void setParameters(TypedQuery typedQuery) {
            if (!isEmpty(loginName)) {
                typedQuery.setParameter("loginName", "%" + loginName.toUpperCase() + "%");
            }
            if (!isEmpty(doesNotBelongToRole)) {
                typedQuery.setParameter("doesNotBelongToRole", doesNotBelongToRole);
            }
        }

        @Override
        public void clear() {
            Role doesNotBelongToRole = this.doesNotBelongToRole;
            super.clear();
            this.doesNotBelongToRole = doesNotBelongToRole;
        }

        @Override
        public String toString() {
            return "UserQuery{" +
                    "loginName='" + loginName + '\'' +
                    '}';
        }
    }

    @Component
    @Scope(SCOPE_PROTOTYPE)
    public static class RelatedUsersQuery extends ToManyRelationshipQuery<User, Role> {

        @Resource
        private UserDao userDao;

        private Role role;

        @Override
        public void setParent(Role parent) {
            this.role = parent;
        }

        @Override
        public Role getParent() {
            return role;
        }

        @Override
        public List<User> execute() {
            return userDao.execute(this);
        }

        @Override
        public List<Predicate> buildCriteria(CriteriaBuilder builder, Root<User> rootEntity) {
            List<Predicate> criteria = new ArrayList<Predicate>();

            if (!isEmpty(role)) {
                ParameterExpression<Role> p = builder.parameter(Role.class, "role");
                criteria.add(builder.equal(rootEntity.join("userRoles").get("role"), p));
            }

            return criteria;
        }

        @Override
        public void setParameters(TypedQuery typedQuery) {
            if (!isEmpty(role)) {
                typedQuery.setParameter("role", role);
            }
        }

        @Override
        public void addFetchJoins(Root<User> rootEntity) {
            rootEntity.fetch("userRoles", JoinType.INNER).fetch("user", JoinType.INNER);
        }

        @Override
        public String toString() {
            return "RelatedUsers{" +
                    "role='" + role + '\'' +
                    '}';
        }
    }
}
