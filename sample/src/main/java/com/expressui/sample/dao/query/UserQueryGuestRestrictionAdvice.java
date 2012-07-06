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

package com.expressui.sample.dao.query;

import com.expressui.core.dao.security.RoleDao;
import com.expressui.core.dao.security.query.RelatedUsersQuery;
import com.expressui.core.dao.security.query.UserQuery;
import com.expressui.core.entity.security.Role;
import com.expressui.core.security.SecurityService;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

import static org.springframework.web.context.WebApplicationContext.SCOPE_SESSION;

/**
 * This aspect intercepts calls to execute UserQuery and prevents guest users from seeing other guest users.
 * This protects the privacy of guests registering for the online demo application.
 * </p>
 * Developers running the demo application locally should login as admin/admin to see all features.
 */
@Aspect
@Component
@Scope(SCOPE_SESSION)
public class UserQueryGuestRestrictionAdvice {
    @Resource
    private RoleDao roleDao;

    @Resource
    private SecurityService securityService;

    /*
        Using bean pointcuts is a workaround for a Spring bug, which occurs when AOP is used with 3.1 in various JEE
        servers. See https://jira.springsource.org/browse/SPR-9335
     */
    @Before("bean(userQuery) && execution(* *.execute(..))")
    public void restrictUserQuery(JoinPoint joinPoint) {
        if (securityService.getCurrentUser().hasRole("ROLE_GUEST")) {
            UserQuery userQuery = (UserQuery) joinPoint.getThis();
            Role role = roleDao.findByName("ROLE_GUEST");
            userQuery.setDoesNotBelongToRole(role);
        }
    }

    /*
        Using bean pointcuts is a workaround for a Spring bug, which occurs when AOP is used with 3.1 in various JEE
        servers. See https://jira.springsource.org/browse/SPR-9335
     */
    @Before("bean(relatedUsersQuery) && execution(* *.execute(..))")
    public void restrictRelatedUsersQuery(JoinPoint joinPoint) {
        if (securityService.getCurrentUser().hasRole("ROLE_GUEST")) {
            RelatedUsersQuery userQuery = (RelatedUsersQuery) joinPoint.getThis();
            Role role = roleDao.findByName("ROLE_GUEST");
            userQuery.setDoesNotBelongToRole(role);
        }
    }
}
