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

package com.expressui.sample.view.role.related;

import com.expressui.core.dao.ToManyRelationshipQuery;
import com.expressui.core.view.field.DisplayFields;
import com.expressui.core.view.tomanyrelationship.ManyToManyRelationshipResults;
import com.expressui.core.view.tomanyrelationship.ToManyRelationship;
import com.expressui.sample.dao.UserDao;
import com.expressui.sample.dao.UserRoleDao;
import com.expressui.sample.entity.security.Role;
import com.expressui.sample.entity.security.User;
import com.expressui.sample.entity.security.UserRole;
import com.expressui.sample.view.select.UserSelect;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.util.ArrayList;
import java.util.List;

@Component
@Scope("prototype")
@SuppressWarnings({"rawtypes", "serial"})
public class RelatedUsers extends ToManyRelationship<User> {

    @Resource
    private RelatedUsersResults relatedUsersResults;

    @Override
    public String getEntityCaption() {
        return "Users";
    }

    @Override
    public RelatedUsersResults getResults() {
        return relatedUsersResults;
    }

    @Component
    @Scope("prototype")
    public static class RelatedUsersResults extends ManyToManyRelationshipResults<User, UserRole> {

        @Resource
        private UserDao userDao;

        @Resource
        private UserRoleDao userRoleDao;

        @Resource
        private UserSelect userSelect;

        @Resource
        private RelatedUsersQuery relatedUsersQuery;

        @Override
        public UserDao getEntityDao() {
            return userDao;
        }

        @Override
        public UserRoleDao getAssociationDao() {
            return userRoleDao;
        }

        @Override
        public UserSelect getEntitySelect() {
            return userSelect;
        }

        @Override
        public ToManyRelationshipQuery getEntityQuery() {
            return relatedUsersQuery;
        }

        @Override
        public void add() {
            Role parentRole = relatedUsersQuery.getParent();
            userSelect.getResults().getEntityQuery().setDoesNotBelongToRole(parentRole);
            super.add();
        }

        @Override
        public void configureFields(DisplayFields displayFields) {
            displayFields.setPropertyIds(new String[]{
                    "loginName",
                    "lastModified",
                    "modifiedBy"
            });
        }

        @Override
        public String getChildPropertyId() {
            return "userRoles";
        }

        @Override
        public String getParentPropertyId() {
            return "userRoles";
        }

        @Override
        public UserRole createAssociationEntity(User user) {
            return new UserRole(user, relatedUsersQuery.getParent());
        }

        @Override
        public String getEntityCaption() {
            return "Users";
        }
    }

    @Component
    @Scope("prototype")
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

