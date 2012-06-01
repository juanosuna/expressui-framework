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

package com.expressui.core.view.tomanyrelationship;

import com.expressui.core.dao.EntityDao;
import com.expressui.core.entity.IdentifiableEntity;
import com.expressui.core.util.BeanPropertyType;
import com.expressui.core.util.assertion.Assert;

import java.io.Serializable;

/**
 * Results of related entities in a many-to-many, aggregation relationship.
 *
 * @param <T> type of related entity
 * @param <A> type of association entity that links the two sides of the relationship
 */
public abstract class ManyToManyRelationshipResults<T, A extends IdentifiableEntity> extends ToManyAggregationRelationshipResults<T> {

    /**
     * Get the DAO for accessing entities of the association type.
     *
     * @return association DAO
     */
    public abstract EntityDao<A, ? extends Serializable> getAssociationDao();

    @Override
    public void setReferencesToParentAndPersist(T... values) {
        for (T value : values) {
            BeanPropertyType beanPropertyType = BeanPropertyType.getBeanPropertyType(getType(), getParentPropertyId());
            Assert.PROGRAMMING.isTrue(beanPropertyType.isCollectionType(),
                    "Parent property id (" + getType() + "." + getParentPropertyId() + ") must be a collection type");
            A associationEntity = createAssociationEntity(value);
            if (!getAssociationDao().isPersistent(associationEntity)) {
                getAssociationDao().persist(associationEntity);
            }
            searchImpl(false);
        }
    }

    @Override
    public void removeConfirmed(T... values) {
        for (T value : values) {
            BeanPropertyType beanPropertyType = BeanPropertyType.getBeanPropertyType(getType(), getParentPropertyId());
            Assert.PROGRAMMING.isTrue(beanPropertyType.isCollectionType(),
                    "Parent property id (" + getType() + "." + getParentPropertyId() + ") must be a collection type");

            A associationEntity = createAssociationEntity(value);
            getAssociationDao().remove(associationEntity);
        }
        searchImpl(false);
        removeButton.setEnabled(false);
    }

    /**
     * Implementation should create the appropriate associate entity for linking the given value to the parent
     *
     * @param value entity to add to the relationship
     * @return the newly created association entity
     */
    public abstract A createAssociationEntity(T value);
}
