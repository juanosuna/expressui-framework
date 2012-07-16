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

import com.expressui.core.dao.query.ToManyRelationshipQuery;
import com.expressui.core.util.BeanPropertyType;
import com.expressui.core.util.ReflectionUtil;
import com.expressui.core.util.assertion.Assert;
import com.expressui.core.view.results.CrudResults;
import org.apache.commons.beanutils.PropertyUtils;

import java.lang.reflect.InvocationTargetException;

/**
 * Results containing entities in a to-many relationship.
 *
 * @param <T> type of the entities in the results
 */
public abstract class ToManyRelationship<T> extends CrudResults<T> {

    private boolean isViewMode;

    protected ToManyRelationship() {
        super();
    }

    /**
     * Gets the property id in the parent entity for referencing the child in this to-many relationship.
     *
     * @return child property id
     */
    public abstract String getChildPropertyId();

    /**
     * Gets the property id in the child entity for referencing the parent entity in this to-many relationship.
     *
     * @return parent property id
     */
    public abstract String getParentPropertyId();

    /**
     * Gets the entity query that generates these results.
     *
     * @return entity query
     */
    @Override
    public abstract ToManyRelationshipQuery getEntityQuery();

    @Override
    public void create() {
        super.create();

        T value = getEntityForm().getBean();
        setReferenceToParent(value);
    }

    /**
     * Sets references inside given values to the parent and then persists all values.
     *
     * @param values values in which to set reference
     */
    public void setReferencesToParentAndPersist(T... values) {
        for (T value : values) {
            T referenceValue = genericDao.getReference(value);
            setReferenceToParent(referenceValue);
            if (getEntityDao() == null) {
                genericDao.persist(referenceValue);
            } else {
                getEntityDao().persist(referenceValue);
            }
        }
        searchImpl(false);
    }

    /**
     * Sets references inside the given value to the parent.
     *
     * @param value value in which to set reference
     */
    public void setReferenceToParent(T value) {
        try {
            BeanPropertyType beanPropertyType = BeanPropertyType.getBeanPropertyType(getType(), getParentPropertyId());
            Assert.PROGRAMMING.isTrue(!beanPropertyType.isCollectionType(),
                    "Parent property id (" + getType() + "." + getParentPropertyId() + ") must not be a collection type");
            PropertyUtils.setProperty(value, getParentPropertyId(), getEntityQuery().getParent());
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Gets the type of the parent entity in this relationship.
     *
     * @return type of parent entity
     */
    public Class getParentEntityType() {
        BeanPropertyType beanPropertyType = BeanPropertyType.getBeanPropertyType(getType(), getParentPropertyId());
        return beanPropertyType.getType();
    }


    /**
     * Asks if this component is in view-only mode.
     *
     * @return true if in view-only mode
     */
    public boolean isViewMode() {
        return isViewMode;
    }

    /**
     * Sets whether or not this component is in view-only mode.
     *
     * @param viewMode true to set in view-only mode
     */
    public void setViewMode(boolean viewMode) {
        isViewMode = viewMode;
    }

    @Override
    public String getTypeCaption() {
        String parentType = getParentEntityType().getName();
        String childId = getChildPropertyId();

        return domainMessageSource.getMessage(parentType + "." + childId, getType().getSimpleName());
    }
}
