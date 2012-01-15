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

package com.expressui.core.security;


import com.expressui.core.dao.security.UserDao;
import com.expressui.core.entity.security.Permission;
import com.expressui.core.entity.security.Role;
import com.expressui.core.entity.security.User;
import com.expressui.core.util.assertion.Assert;
import org.hibernate.Hibernate;
import org.springframework.context.annotation.Scope;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Set;

/**
 * Service for getting the current user and logging out. The current user entity
 * provides access to roles and permissions.
 */
@Service
@Scope("session")
public class SecurityService {

    public static final String SYSTEM_USER = "system";

    @Resource
    private AuthenticationManager authenticationManager;

    @Resource
    private UserDao userDao;

    private User user;

    /**
     * Get the login name of the currently logged in user.
     *
     * @return login name
     */
    private static String getCurrentLoginName() {
        if (SecurityContextHolder.getContext() != null && SecurityContextHolder.getContext().getAuthentication() != null
                && SecurityContextHolder.getContext().getAuthentication().getPrincipal() != null) {
            UserDetails user = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            return user.getUsername();
        } else {
            return SYSTEM_USER;
        }
    }

    /**
     * Get the user entity for the currently logged in user. Uses a cache
     * to improve performance.
     *
     * @return user entity with roles and permissions
     */
    public User getCurrentUser() {
        if (user == null) {
            String loginName = getCurrentLoginName();
            user = findUser(loginName);

            Hibernate.initialize(user);
            Set<Role> roles = user.getRoles();
            for (Role role : roles) {
                Hibernate.initialize(role);
                Set<Permission> permissions = role.getPermissions();
                for (Permission permission : permissions) {
                    Hibernate.initialize(permission);
                }
            }
        }

        return user;
    }

    public void setCurrentUser(User user) {
        this.user = user;
    }

    /**
     * Find user entity for the given login name. Does not use cache.
     *
     * @param loginName login name of the user entity to find
     * @return user entity
     */
    public User findUser(String loginName) {

        Assert.PROGRAMMING.assertTrue(loginName != null, "Current loginName is null");

        return userDao.findByNaturalId("loginName", loginName);
    }

    /**
     * Clear the Authentication object from Spring Security's context.
     */
    public void logout() {
        SecurityContextHolder.getContext().setAuthentication(null);
        user = null;
    }

    public boolean login(String username, String password) {
        logout();
        if (username == null) username = "";
        if (password == null) password = "";
        username = username.trim();

        UsernamePasswordAuthenticationToken authRequest = new UsernamePasswordAuthenticationToken(username, password);

        Authentication authResult;
        try {
            authResult = authenticationManager.authenticate(authRequest);
        } catch (AuthenticationException failed) {
            unsuccessfulAuthentication(failed);
            return false;
        }
        successfulAuthentication(authResult);

        return true;
    }

    protected void successfulAuthentication(Authentication authResult) {
        SecurityContextHolder.getContext().setAuthentication(authResult);
        getCurrentUser();

        // optionally getRememberMeServices().loginSuccess(request, response, authResult);
        // optionally getEventPublisher().publishEvent(new InteractiveAuthenticationSuccessEvent(authResult, this.getClass()));
        // optionally getSuccessHandler().onAuthenticationSuccess(request, response, authResult);

        // TODO redirect to the correct application URL
    }

    protected void unsuccessfulAuthentication(AuthenticationException failed) {
        // optionally getRememberMeServices().loginFail(request, response);
        // optionally getFailureHandler().onAuthenticationFailure(request, response, failed);
    }
}
