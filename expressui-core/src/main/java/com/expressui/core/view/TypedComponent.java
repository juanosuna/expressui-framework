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

package com.expressui.core.view;

import com.expressui.core.dao.EntityDao;
import com.expressui.core.entity.security.User;
import com.expressui.core.util.ReflectionUtil;
import com.expressui.core.util.SpringApplicationContext;
import com.expressui.core.util.assertion.Assert;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;

import java.io.Serializable;

/**
 * A visual component associated with of a generic type, often an entity class.
 *
 * @param <T> type this component is associated with
 */
public abstract class TypedComponent<T> extends RootComponent {

    protected TypedComponent() {
        super();
    }

    /**
     * Gets type of this component.
     *
     * @return type of domain entity for this page
     */
    public Class<T> getType() {
        Class type = ReflectionUtil.getGenericArgumentType(getClass());
        Assert.PROGRAMMING.notNull(type, "This component must specify a generic type");

        return type;
    }

    /**
     * Gets the caption that describes the type of this component.
     *
     * @return caption that describes the type of this component
     */
    /**
     * Gets null. However, overriding methods often lookup the caption from domainMessages/.
     *
     * @return caption to tell tje user what type of component this is
     */
    public String getTypeCaption() {
        return null;
    }

    @Override
    public String getCaption() {
        return getTypeCaption();
    }

    /**
     * Gets an entityDao associated with the type of this component, if one exists.
     *
     * @return entityDao associated with type, null if none exists
     */
    public EntityDao<T, Serializable> getEntityDao() {
        try {
            return (EntityDao<T, Serializable>) SpringApplicationContext.getBean(getType());
        } catch (NoSuchBeanDefinitionException e) {
            return null;
        }
    }

    /**
     * Asks if user has permission to create this component's type.
     *
     * @return true if user has permission
     */
    public boolean isCreateAllowed() {
        return getCurrentUser().isCreateAllowed(getType().getName());
    }

    /**
     * Asks if user has permission to view this component's type.
     *
     * @return true if user has permission
     */
    public boolean isViewAllowed() {
        return getCurrentUser().isViewAllowed(getType().getName());
    }

    /**
     * Asks if user has permission to edit this component's type.
     *
     * @return true if user has permission
     */
    public boolean isEditAllowed() {
        return getCurrentUser().isEditAllowed(getType().getName()) && isViewAllowed();
    }

    /**
     * Asks if user has permission to delete this component's type.
     *
     * @return true if user has permission
     */
    public boolean isDeleteAllowed() {
        return getCurrentUser().isDeleteAllowed(getType().getName());
    }

    @Override
    protected Class[] autoAddCodeClasses(Class... classes) {
        Class[] allClasses = new Class[classes.length + 2];
        allClasses[0] = getClass();
        allClasses[1] = getType();
        System.arraycopy(classes, 0, allClasses, 2, classes.length);

        return allClasses;
    }
}
