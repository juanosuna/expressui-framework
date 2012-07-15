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

package com.expressui.core.util.assertion;

import java.util.Collection;

/**
 * Utility class for making assertions based on the classification of the error.
 */
public class Assert {

    /**
     * Used to assert programming correctness. Failure indicates programming error and should
     * never happen in production. ProgrammingExceptions should be reported as bugs.
     */
    public static final Assert PROGRAMMING = new Assert(AssertionExceptionType.PROGRAMMING_EXCEPTION);

    /**
     * Used to assert configuration correctness. Failure indicates configuration error and should
     * not happen in a running production system. ConfigurationExceptions should be reported
     * to administrator or developer, depending on nature of configuration error.
     */
    public static final Assert CONFIGURATION = new Assert(AssertionExceptionType.CONFIGURATION_EXCEPTION);

    /**
     * Used to assert system correctness. Failure indicates system failure, e.g. database or network down,
     * and may happen in production. SystemException should be reported to an administrator.
     */
    public static final Assert SYSTEM = new Assert(AssertionExceptionType.SYSTEM_EXCEPTION);

    /**
     * Used to enforce contracts with an external, client system. Failure indicates client
     * has violated a contract. ClientException should be reported to client-side developers.
     */
    public static final Assert BUSINESS = new Assert(AssertionExceptionType.BUSINESS_EXCEPTION);

    /**
     * Used to assert database correctness. Failure indicates database integrity issue.
     * DatabaseException should be reported to database administrator or developer.
     */
    public static final Assert DATABASE = new Assert(AssertionExceptionType.DATABASE_EXCEPTION);

    private AssertionExceptionType assertionExceptionType;

    private Assert(AssertionExceptionType assertionExceptionType) {
        this.assertionExceptionType = assertionExceptionType;
    }

    /**
     * Asserts condition and throws exception if condition is not true.
     *
     * @param condition boolean expression
     */
    public void isTrue(boolean condition) {
        if (!condition) {
            throw assertionExceptionType.create();
        }
    }

    /**
     * Asserts condition and throws exception if condition is not true.
     *
     * @param condition boolean expression
     * @param message   to be embedded in thrown exception
     */
    public void isTrue(boolean condition, String message) {
        if (!condition) {
            throw assertionExceptionType.create(message);
        }
    }

    /**
     * Asserts object is null.
     *
     * @param object  object that must be null
     * @param message to be embedded in thrown exception
     */
    public void isNull(Object object, String message) {
        isTrue(object == null, message);
    }

    /**
     * Asserts object is  null.
     *
     * @param object object that must be null
     */
    public void isNull(Object object) {
        isNull(object, "argument must be null");
    }

    /**
     * Asserts object is not null.
     *
     * @param object  object that must not be null
     * @param message to be embedded in thrown exception
     */
    public void notNull(Object object, String message) {
        isTrue(object != null, message);
    }

    /**
     * Asserts object is not null.
     *
     * @param object object that must not be null
     */
    public void notNull(Object object) {
        notNull(object, "argument must not be null");
    }

    /**
     * Asserts collection must be of a certain size.
     *
     * @param collection collection that must be of a certain size
     * @param size       size being asserted
     * @param message    to be embedded in thrown exception
     */
    public void size(Collection collection, int size, String message) {
        isTrue(collection.size() == size, message);
    }

    /**
     * Asserts collection must be of a certain size.
     *
     * @param collection collection that must be of a certain size
     * @param size       size being asserted
     */
    public void size(Collection collection, int size) {
        isTrue(collection.size() == size, "collection argument must be size " + size);
    }

    /**
     * Asserts collection must be empty.
     *
     * @param collection collection that must be empty
     * @param message    to be embedded in thrown exception
     */
    public void empty(Collection collection, String message) {
        size(collection, 0, message);
    }

    /**
     * Asserts collection must be empty.
     *
     * @param collection collection that must be empty
     */
    public void empty(Collection collection) {
        size(collection, 0);
    }

    /**
     * Asserts collection must not be empty.
     *
     * @param collection collection that must not be empty
     */
    public void notEmpty(Collection collection) {
        isTrue(collection.size() > 0, "collection must not be empty");
    }

    /**
     * Asserts object must of a given type.
     *
     * @param object  object to check
     * @param type    type to check
     * @param message to be embedded in thrown exception
     */
    public void instanceOf(Object object, Class type, String message) {
        isTrue(type.isInstance(object), message);
    }

    /**
     * Asserts object must of a given type.
     *
     * @param object object to check
     * @param type   type to check
     */
    public void instanceOf(Object object, Class type) {
        instanceOf(object, type, "object " + (object != null ? object.getClass().getName() : "null") +
                " must be an instance of " + type);
    }

    /**
     * Forces the throwing of an exception.
     *
     * @param message to embed in the exception
     * @throws RuntimeException Always thrown exception containing given description
     */
    public void fail(String message) throws RuntimeException {
        throw assertionExceptionType.create(message);
    }

    /**
     * Forces the throwing of an exception.
     *
     * @param message to embed in the exception
     * @param cause   root cause for chaining to thrown exception
     * @throws RuntimeException Always thrown exception containing given description
     */
    public void fail(String message, Throwable cause) throws RuntimeException {
        throw assertionExceptionType.create(message, cause);
    }

    /**
     * Forces throw throwing of an exception.
     *
     * @param cause root cause for chaining to thrown exception
     * @throws RuntimeException Always thrown exception containing given description
     */
    public void fail(Throwable cause) throws RuntimeException {
        throw assertionExceptionType.create(cause);
    }
}
