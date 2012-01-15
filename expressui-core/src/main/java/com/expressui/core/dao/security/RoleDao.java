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

import javax.annotation.Resource;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.beans.factory.config.BeanDefinition.SCOPE_PROTOTYPE;

@Repository
@SuppressWarnings("unchecked")
public class RoleDao extends EntityDao<Role, Long> {

    @Override
    public List<Role> findAll() {
        Query query = getEntityManager().createQuery("SELECT r FROM Role r ORDER BY r.name");
        setReadOnly(query);

        return query.getResultList();
    }

    public Role findByName(String name) {
        Query query = getEntityManager().createQuery("SELECT r FROM Role r WHERE r.name = :name");

        query.setParameter("name", name);

        return (Role) query.getSingleResult();
    }

    @Component
    @Scope(SCOPE_PROTOTYPE)
    public static class RoleQuery extends StructuredEntityQuery<Role> {

        @Resource
        private RoleDao roleDao;

        private String name;
        private User doesNotBelongToUser;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public User getDoesNotBelongToUser() {
            return doesNotBelongToUser;
        }

        public void setDoesNotBelongToUser(User doesNotBelongToUser) {
            this.doesNotBelongToUser = doesNotBelongToUser;
        }

        @Override
        public List<Role> execute() {
            return roleDao.execute(this);
        }

        @Override
        public List<Predicate> buildCriteria(CriteriaBuilder builder, Root<Role> rootEntity) {
            List<Predicate> criteria = new ArrayList<Predicate>();

            if (!isEmpty(name)) {
                ParameterExpression<String> p = builder.parameter(String.class, "name");
                criteria.add(builder.like(builder.upper(rootEntity.<String>get("name")), p));
            }

            if (!isEmpty(doesNotBelongToUser)) {
                ParameterExpression<User> p = builder.parameter(User.class, "doesNotBelongToUser");
                Join join = rootEntity.join("userRoles", JoinType.LEFT);
                criteria.add(builder.or(
                        builder.notEqual(join.get("user"), p),
                        builder.isNull(join.get("user"))
                ));
            }

            return criteria;
        }

        @Override
        public void setParameters(TypedQuery typedQuery) {
            if (!isEmpty(name)) {
                typedQuery.setParameter("name", "%" + name.toUpperCase() + "%");
            }
            if (!isEmpty(doesNotBelongToUser)) {
                typedQuery.setParameter("doesNotBelongToUser", doesNotBelongToUser);
            }
        }

        @Override
        public void clear() {
            User doesNotBelongToUser = this.doesNotBelongToUser;
            super.clear();
            this.doesNotBelongToUser = doesNotBelongToUser;
        }

        @Override
        public String toString() {
            return "RoleQuery{" +
                    "name='" + name + '\'' +
                    '}';
        }
    }

    @Component
    @Scope(SCOPE_PROTOTYPE)
    public static class RelatedRolesQuery extends ToManyRelationshipQuery<Role, User> {

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
        public List<Predicate> buildCriteria(CriteriaBuilder builder, Root<Role> rootEntity) {
            List<Predicate> criteria = new ArrayList<Predicate>();

            if (!isEmpty(user)) {
                ParameterExpression<User> p = builder.parameter(User.class, "user");
                criteria.add(builder.equal(rootEntity.join("userRoles").get("user"), p));
            }

            return criteria;
        }

        @Override
        public void setParameters(TypedQuery typedQuery) {
            if (!isEmpty(user)) {
                typedQuery.setParameter("user", user);
            }
        }

        @Override
        public void addFetchJoins(Root<Role> rootEntity) {
            rootEntity.fetch("userRoles", JoinType.INNER).fetch("user", JoinType.INNER);
        }

        @Override
        public String toString() {
            return "RelatedRoles{" +
                    "user='" + user + '\'' +
                    '}';
        }

    }
}
