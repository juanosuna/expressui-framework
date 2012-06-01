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

package com.expressui.sample.dao;

import com.expressui.core.entity.security.User;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

/**
 * Provides advice to GenericDao.remove by clearing out
 * all references to the user so that user can be deleted without resulting in referential
 * constraint violations.
 */
@Aspect
@Repository
public class UserReferenceCleanupAdvice {
    @PersistenceContext
    private EntityManager entityManager;

    @Before("execution(* com.expressui.core.dao.GenericDao.remove(Object)) && args(user)")
    @Transactional
    public void remove(User user) {
        Query query = entityManager.createQuery(
                "UPDATE Account a SET a.assignedTo = null WHERE a.assignedTo = :user");
        query.setParameter("user", user);
        query.executeUpdate();

        query = entityManager.createQuery(
                "UPDATE Contact c SET c.assignedTo = null WHERE c.assignedTo = :user");
        query.setParameter("user", user);
        query.executeUpdate();

        query = entityManager.createQuery(
                "UPDATE Opportunity o SET o.assignedTo = null WHERE o.assignedTo = :user");
        query.setParameter("user", user);
        query.executeUpdate();
    }
}
