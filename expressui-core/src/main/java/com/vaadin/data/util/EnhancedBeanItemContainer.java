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

import com.expressui.core.view.field.DisplayFields;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

public class EnhancedBeanItemContainer<BEANTYPE> extends BeanItemContainer<BEANTYPE> {
    private Class beanType;
    private Set<String> nonSortablePropertyIds = new HashSet<String>();
    private DisplayFields displayFields;

    public EnhancedBeanItemContainer(Class<? super BEANTYPE> type, DisplayFields displayFields)
            throws IllegalArgumentException {
        super(type);
        beanType = type;
        this.displayFields = displayFields;
    }

    public EnhancedBeanItemContainer(Class<? super BEANTYPE> type, Collection<? extends BEANTYPE> beantypes, DisplayFields displayFields)
            throws IllegalArgumentException {
        super(type, beantypes);
        beanType = type;
        this.displayFields = displayFields;
    }

    @Override
    public boolean addNestedContainerProperty(String propertyId) {
        return addContainerProperty(propertyId, new EnhancedNestedPropertyDescriptor(
                propertyId, beanType, displayFields.getField(propertyId)));
    }

    public Set<String> getNonSortablePropertyIds() {
        return nonSortablePropertyIds;
    }

    public void setNonSortablePropertyIds(Set<String> nonSortablePropertyIds) {
        this.nonSortablePropertyIds = nonSortablePropertyIds;
    }

    @Override
    public Collection<?> getSortableContainerPropertyIds() {
        LinkedList<Object> sortables = new LinkedList<Object>();
        for (Object propertyId : getContainerPropertyIds()) {
            if (!nonSortablePropertyIds.contains(propertyId)) {
                sortables.add(propertyId);
            }
        }
        return sortables;
    }
}
