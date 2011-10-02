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

package com.expressui.sample.view.user.related;

import com.expressui.core.dao.ToManyRelationshipQuery;
import com.expressui.core.view.field.DisplayFields;
import com.expressui.core.view.tomanyrelationship.ManyToManyRelationshipResults;
import com.expressui.core.view.tomanyrelationship.ToManyRelationship;
import com.expressui.sample.dao.RoleDao;
import com.expressui.sample.dao.UserRoleDao;
import com.expressui.sample.entity.security.Role;
import com.expressui.sample.entity.security.User;
import com.expressui.sample.entity.security.UserRole;
import com.expressui.sample.view.select.RoleSelect;
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
public class RelatedRoles extends ToManyRelationship<Role> {

    @Resource
    private RelatedRolesResults relatedRolesResults;

    @Override
    public String getEntityCaption() {
        return "Roles";
    }

    @Override
    public RelatedRolesResults getResults() {
        return relatedRolesResults;
    }

    @Component
    @Scope("prototype")
    public static class RelatedRolesResults extends ManyToManyRelationshipResults<Role, UserRole> {

        @Resource
        private RoleDao roleDao;

        @Resource
        private UserRoleDao userRoleDao;

        @Resource
        private RoleSelect roleSelect;

        @Resource
        private RelatedRolesQuery relatedRolesQuery;

        @Override
        public void add() {
            User parentUser = relatedRolesQuery.getParent();
            roleSelect.getResults().getEntityQuery().setDoesNotBelongToUser(parentUser);
            super.add();
        }

        @Override
        public RoleDao getEntityDao() {
            return roleDao;
        }

        @Override
        public UserRoleDao getAssociationDao() {
            return userRoleDao;
        }

        @Override
        public RoleSelect getEntitySelect() {
            return roleSelect;
        }

        @Override
        public ToManyRelationshipQuery getEntityQuery() {
            return relatedRolesQuery;
        }

        @Override
        public void configureFields(DisplayFields displayFields) {
            displayFields.setPropertyIds(new String[]{
                    "name",
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
        public UserRole createAssociationEntity(Role role) {
            return new UserRole(relatedRolesQuery.getParent(), role);
        }

        @Override
        public String getEntityCaption() {
            return "Roles";
        }
    }

    @Component
    @Scope("prototype")
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

