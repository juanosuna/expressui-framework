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

package com.vaadin.data.util;

import com.expressui.core.view.field.DisplayField;
import com.expressui.core.view.field.format.EmptyPropertyFormatter;
import com.vaadin.data.Property;

public class EnhancedNestedPropertyDescriptor<BT> implements VaadinPropertyDescriptor<BT> {
    private final String name;
    private final Class<?> propertyType;
    private DisplayField displayField;

    public EnhancedNestedPropertyDescriptor(String name, Class<BT> beanType, DisplayField displayField)
            throws IllegalArgumentException {
        this.name = name;
        EnhancedNestedMethodProperty property = new EnhancedNestedMethodProperty(beanType, name);
        this.propertyType = property.getType();
        this.displayField = displayField;
    }

    public String getName() {
        return name;
    }

    public Class<?> getPropertyType() {
        return propertyType;
    }

    public Property createProperty(BT bean) {
        Property property = new EnhancedNestedMethodProperty(bean, name);
        PropertyFormatter propertyFormatter = displayField.getPropertyFormatter();
        if (propertyFormatter.getClass().equals(EmptyPropertyFormatter.class)) {
            return property;
        } else {
            propertyFormatter.setPropertyDataSource(property);
            return propertyFormatter;
        }
    }
}
