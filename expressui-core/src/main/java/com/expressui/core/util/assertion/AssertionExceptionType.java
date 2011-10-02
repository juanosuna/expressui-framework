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

package com.expressui.core.util.assertion;


import java.lang.reflect.InvocationTargetException;

public enum AssertionExceptionType {
    CONFIGURATION_EXCEPTION(ConfigurationException.class),
    PROGRAMMING_EXCEPTION(ProgrammingException.class),
    BUSINESS_EXCEPTION(BusinessException.class),
    DATABASE_EXCEPTION(DatabaseException.class),
    SYSTEM_EXCEPTION(SystemException.class);

    private Class<AssertionException> exceptionType;

    private AssertionExceptionType(Class exceptionType) {
        this.exceptionType = exceptionType;
    }

    /**
     * Reflectively instantiates exception based on type
     *
     * @return exception instance based on type
     */
    public AssertionException create() {
        try {
            return exceptionType.getConstructor().newInstance();
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Reflectively instantiates exception based on type
     *
     * @param message to pass to exception's constructor
     * @return instance of this exception's type
     */
    public AssertionException create(String message) {
        try {
            return exceptionType.getConstructor(String.class).newInstance(message);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Reflectively instantiates exception based on type
     *
     * @param cause to pass to exception's constructor
     * @return instance of this exception's type
     */
    public AssertionException create(Throwable cause) {
        try {
            return exceptionType.getConstructor(Throwable.class).newInstance(cause);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * Reflectively instantiates exception based on type
     *
     * @param message to pass to exception's constructor
     * @param cause   to pass to exception's constructor
     * @return instance of this exception's type
     */
    public AssertionException create(String message, Throwable cause) {
        try {
            return exceptionType.getConstructor(String.class, Throwable.class).newInstance(message, cause);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        }
    }
}
