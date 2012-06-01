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

import org.hibernate.AssertionFailure;
import org.hibernate.cfg.DefaultComponentSafeNamingStrategy;

/**
 * Hibernate strategy for generating DLL, making sure that all table and column names
 * are upper-cased and words are separated by underscores.
 */
public class UpperCaseAndUnderscoresNamingStrategy extends DefaultComponentSafeNamingStrategy {

    public static final String TABLE_PREFIX = "";

    public UpperCaseAndUnderscoresNamingStrategy() {
        super();
    }

    protected String insertUnderscores(String name) {
        StringBuffer buf = new StringBuffer(name.replace('.', '_'));
        for (int i = 1; i < buf.length() - 1; i++) {
            if (
                    Character.isLowerCase(buf.charAt(i - 1)) &&
                            Character.isUpperCase(buf.charAt(i)) &&
                            Character.isLowerCase(buf.charAt(i + 1))
                    ) {
                buf.insert(i++, '_');
            }
        }
        return buf.toString().toLowerCase();
    }

    @Override
    public String classToTableName(String className) {
        return TABLE_PREFIX + insertUnderscores(className).toUpperCase();
    }

    @Override
    public String propertyToColumnName(String propertyName) {
        return insertUnderscores(propertyName).toUpperCase();
    }

    @Override
    public String tableName(String tableName) {
        return TABLE_PREFIX + insertUnderscores(tableName).toUpperCase();
    }

    @Override
    public String columnName(String columnName) {
        return insertUnderscores(columnName).toUpperCase();
    }

    @Override
    public String collectionTableName(String ownerEntity, String ownerEntityTable, String associatedEntity, String associatedEntityTable, String propertyName) {
        return tableName(
                new StringBuilder(ownerEntityTable).append("_")
                        .append(
                                associatedEntityTable != null ?
                                        associatedEntityTable :
                                        insertUnderscores(propertyName)
                        ).toString()
        ).toUpperCase();
    }

    @Override
    public String foreignKeyColumnName(String propertyName, String propertyEntityName, String propertyTableName, String referencedColumnName) {
        String header = propertyName != null ? insertUnderscores(propertyName) : propertyTableName;
        if (header == null) throw new AssertionFailure("NamingStrategy not properly filled");
        return columnName(header + "_" + referencedColumnName).toUpperCase();
    }

    @Override
    public String logicalColumnName(String columnName, String propertyName) {
        return insertUnderscores(super.logicalColumnName(columnName, propertyName)).toUpperCase();
    }

    @Override
    public String logicalCollectionTableName(String tableName, String ownerEntityTable, String associatedEntityTable, String propertyName) {
        return (TABLE_PREFIX + super.logicalCollectionTableName(tableName, ownerEntityTable, associatedEntityTable, propertyName)).toUpperCase();
    }

    @Override
    public String logicalCollectionColumnName(String columnName, String propertyName, String referencedColumn) {
        return super.logicalCollectionColumnName(columnName, propertyName, referencedColumn).toUpperCase();
    }


    @Override
    public String joinKeyColumnName(String joinedColumn, String joinedTable) {
        return super.joinKeyColumnName(joinedColumn, joinedTable).toUpperCase();
    }
}
