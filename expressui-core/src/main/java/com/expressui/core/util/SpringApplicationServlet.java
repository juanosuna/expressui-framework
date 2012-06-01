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

package com.expressui.core.util;

import com.vaadin.Application;
import com.vaadin.terminal.gwt.server.ApplicationServlet;
import org.apache.log4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import java.net.URL;

/**
 * A servlet for launching Vaadin application with Spring dependency injection.
 */
public class SpringApplicationServlet extends ApplicationServlet {

    private final Logger log = Logger.getLogger(getClass());

    private WebApplicationContext webApplicationContext;

    @Override
    public void init(ServletConfig servletConfig) throws ServletException {
        super.init(servletConfig);
        log.debug("initializing SpringApplicationServlet");
        try {
            webApplicationContext = WebApplicationContextUtils.getRequiredWebApplicationContext(servletConfig.getServletContext());
        } catch (IllegalStateException e) {
            throw new ServletException(e);
        }
    }

    protected final WebApplicationContext getWebApplicationContext() throws ServletException {
        if (webApplicationContext == null) {
            throw new ServletException("init() must be invoked before WebApplicationContext can be retrieved");
        }
        return webApplicationContext;
    }

    protected final AutowireCapableBeanFactory getAutowireCapableBeanFactory() throws ServletException {
        try {
            return getWebApplicationContext().getAutowireCapableBeanFactory();
        } catch (IllegalStateException e) {
            throw new ServletException(e);
        }
    }

    @Override
    protected Application getNewApplication(HttpServletRequest request) throws ServletException {
        Class<? extends Application> applicationClass;
        try {
            applicationClass = getApplicationClass();
        } catch (ClassNotFoundException e) {
            throw new ServletException(e);
        }
        AutowireCapableBeanFactory beanFactory = getAutowireCapableBeanFactory();
        try {
            Application application = beanFactory.createBean(applicationClass);
            log.debug("Created new Application of type: " + applicationClass);
            return application;
        } catch (BeansException e) {
            throw new ServletException(e);
        }
    }

    @Override
    protected boolean isAllowedVAADINResourceUrl(HttpServletRequest request, URL resourceUrl) {
        return true;
    }
}

