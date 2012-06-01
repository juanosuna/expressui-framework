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

import java.util.Collection;

/**
 * Utility class for managing objects.
 */
public class ObjectUtil {

    /**
     * Ask if two args are equal. Differs from Object.equals in that method returns true if
     * both args are null or returns false if only one is null (without throwing a
     * NullPointerException).
     *
     * @param a first object to compare
     * @param b second object to compare
     * @return true if they are equal
     */
    public static boolean isEqual(Object a, Object b) {
        if (a == null && b == null) return true;
        if (a == null && b != null) return false;
        if (a != null && b == null) return false;

        return a.equals(b);
    }

    /**
     * Ask if two collections are equal by comparing them deeply.
     * Returns true if both args are null or returns false if only one is null
     * (without throwing a NullPointerException).
     *
     * @param a first collection to compare
     * @param b second collection to compare
     * @return true if they are equal
     */
    public static boolean isEqualDeep(Collection a, Collection b) {
        if (a == null && b == null) return true;
        if (a == null && b != null) return false;
        if (a != null && b == null) return false;

        for (Object o : a) {
            if (!b.contains(a)) return false;
        }
        for (Object o : b) {
            if (!a.contains(b)) return false;
        }
        return true;
    }

    /**
     * Calculate the hashCode of a collection by summing up the hashcodes of all its members.
     * This assures that any two collections with exactly the same members have the same hashCode.
     *
     * @param collection collection from which to extract hashCode
     * @return sum of all member hashCodes
     */
    public static int hashCodeDeep(Collection collection) {
        int hashCode = 0;

        for (Object o : collection) {
            hashCode += o.hashCode();
        }

        return hashCode;
    }
}
