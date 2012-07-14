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

package com.expressui.core.dao.security;

import com.expressui.core.dao.EntityDao;
import com.expressui.core.entity.security.Permission;
import com.expressui.core.entity.security.Role;
import org.springframework.stereotype.Repository;

import javax.persistence.FlushModeType;
import javax.persistence.Query;
import java.util.List;

/**
 * Permissions DAO.
 */
@Repository
@SuppressWarnings("unchecked")
public class PermissionDao extends EntityDao<Permission, Long> {

    /**
     * Finds all permissions ordered by targetType and field.
     *
     * @return all permissions
     */
    @Override
    public List<Permission> findAll() {
        Query query = getEntityManager().createQuery("SELECT p FROM Permission p ORDER BY p.targetType, p.field");
        setReadOnly(query);

        return query.getResultList();
    }

    /**
     * Finds all permissions for a given role.
     *
     * @param role role to query
     * @return all permissions for role
     */
    public List<Permission> findByRole(Role role) {
        Query query = getEntityManager().createQuery("SELECT p FROM Permission p WHERE p.role = :role");
        query.setParameter("role", role);

        return query.getResultList();
    }

    /**
     * Finds permissions for a given role, entity type and field.
     *
     * @param role       the role to query
     * @param entityType the entity type to query
     * @param field      the field to query
     * @return found permissions
     */
    public List<Permission> findByRoleEntityTypeAndField(Role role, String entityType, String field) {
        Query query = getEntityManager().createQuery("SELECT p FROM Permission p WHERE p.role = :role" +
                " AND p.targetType = :entityType AND p.field = :field");
        query.setParameter("role", role);
        query.setParameter("entityType", entityType);
        query.setParameter("field", field);

        query.setFlushMode(FlushModeType.COMMIT);

        return query.getResultList();
    }
}
