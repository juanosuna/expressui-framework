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

package com.expressui.core.dao.query;

import com.expressui.core.dao.GenericDao;
import com.expressui.core.util.ReflectionUtil;
import com.expressui.core.util.assertion.Assert;
import org.apache.commons.beanutils.PropertyUtils;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;

/**
 * Query for finding entities, similar to a DAO but adds support for paging and sorting result sets.
 * <p/>
 * Generically typed subclasses should add entity-specific properties, to be passed as parameter values to the query.
 *
 * @param <T> type of entity being queried
 */
public abstract class EntityQuery<T> {

    public static final Integer DEFAULT_PAGE_SIZE = 10;

    private Integer pageSize = DEFAULT_PAGE_SIZE;
    private Integer firstResult = 0;
    private Long resultCount = 0L;
    private String orderByPropertyId;
    private OrderDirection orderDirection = OrderDirection.ASC;

    private PropertyDescriptor[] descriptors;

    /**
     * Generic DAO for actually executing the query.
     */
    @Resource
    public GenericDao genericDao;

    protected EntityQuery() {
        descriptors = PropertyUtils.getPropertyDescriptors(this);
    }

    /**
     * Lifecycle method called after this bean has been constructed.
     */
    @PostConstruct
    public void postConstruct() {
    }

    /**
     * Lifecycle method called after all beans have been wired.
     */
    public void postWire() {
    }

    /**
     * Get the type of entity being queried
     *
     * @return type of entity
     */
    public Class getEntityType() {
        return ReflectionUtil.getGenericArgumentType(getClass());
    }

    /**
     * Execute the query. Implementation should call the appropriate DAO method for
     * executing the query for this entity type.
     *
     * @return list of matching entities for the page range specified by this query
     */
    public abstract List<T> execute();

    /**
     * Get the number of records to display in a page.
     *
     * @return number of records
     */
    public Integer getPageSize() {
        return pageSize;
    }

    /**
     * Set the number of records to display in a page.
     *
     * @param pageSize number of records
     */
    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    /**
     * Get the zero-based index of the first record to display
     *
     * @return index of the first record
     */
    public Integer getFirstResult() {
        return firstResult;
    }

    /**
     * Set the zero-based index of the first record to display
     *
     * @param firstResult index of the first record
     */
    public void setFirstResult(Integer firstResult) {
        this.firstResult = firstResult;
    }

    /**
     * Get the index of the last record to display. This is derived from the first result and the page size.
     *
     * @return index of the last record
     */
    public Integer getLastResult() {
        return Math.min(firstResult + pageSize, resultCount.intValue());
    }

    /**
     * Get a count of the number of results found after executing the query.
     *
     * @return number of results found
     */
    public Long getResultCount() {
        return resultCount;
    }

    /**
     * Set a count of the number of results found after executing the query.
     *
     * @param resultCount number of results found
     */
    public void setResultCount(Long resultCount) {
        this.resultCount = resultCount;
    }

    /**
     * Set index to 0, first page in the result set
     */
    public void firstPage() {
        firstResult = 0;
    }

    /**
     * Increment index to next page in the result set
     */
    public void nextPage() {
        firstResult = Math.min(firstResult + pageSize, Math.max(resultCount.intValue() - pageSize, 0));
    }

    /**
     * Ask if there is a next page.
     *
     * @return true if there are more results after the current page
     */
    public boolean hasNextPage() {
        if (resultCount > 0) {
            return Math.min(firstResult + pageSize, Math.max(resultCount.intValue() - pageSize, 0)) > firstResult;
        } else {
            return false;
        }
    }

    /**
     * Decrement index to previous page.
     */
    public void previousPage() {
        firstResult = Math.max(firstResult - pageSize, 0);
    }

    /**
     * Ask if there is previous page.
     *
     * @return true if there are results before the current page, false if index is 0
     */
    public boolean hasPreviousPage() {
        return Math.max(firstResult - pageSize, 0) < firstResult;
    }

    /**
     * Set index to last page in result set.
     */
    public void lastPage() {
        firstResult = Math.max(resultCount.intValue() - pageSize, 0);
    }

    /**
     * Get the property to be used in the ORDER BY clause of the query
     *
     * @return name of the bean property
     */
    public String getOrderByPropertyId() {
        if (orderByPropertyId == null) {
            orderByPropertyId = "lastModified";
            setOrderDirection(OrderDirection.DESC);
        }

        return orderByPropertyId;
    }

    /**
     * Set the property to be used in the ORDER BY clause of the query.
     *
     * @param orderByPropertyId name of the bean property
     */
    public void setOrderByPropertyId(String orderByPropertyId) {
        this.orderByPropertyId = orderByPropertyId;
    }

    /**
     * Get the ORDER BY direction, i.e. ascending or descending. Default is ascending.
     *
     * @return ORDER BY direction
     */
    public OrderDirection getOrderDirection() {
        return orderDirection;
    }

    /**
     * Set ORDER BY direction, i.e. ascending or descending. Default is ascending.
     *
     * @param orderDirection ORDER BY direction
     */
    public void setOrderDirection(OrderDirection orderDirection) {
        this.orderDirection = orderDirection;
    }

    /**
     * Clear this query so that all filters (query parameters) and sort-criteria are removed. The method uses
     * reflection to clear any filters defined as bean properties by subclasses. Once cleared, re-execution of query
     * results in all records being found.
     * <p/>
     * If a subclass wants to apply a default filter that is always applied,
     * then subclass should override this method and re-apply this filter after calling super.clear().
     */
    public void clear() {
        setOrderByPropertyId(null);
        setOrderDirection(OrderDirection.ASC);

        try {
            for (PropertyDescriptor descriptor : descriptors) {
                Method writeMethod = descriptor.getWriteMethod();
                Method readMethod = descriptor.getReadMethod();
                if (readMethod != null && writeMethod != null
                        && !writeMethod.getDeclaringClass().equals(EntityQuery.class)
                        && !writeMethod.getDeclaringClass().equals(Object.class)) {
                    Class type = descriptor.getPropertyType();
                    if (type.isPrimitive() && !type.isArray()) {
                        if (ReflectionUtil.isNumberType(type)) {
                            writeMethod.invoke(this, 0);
                        } else if (Boolean.class.isAssignableFrom(type)) {
                            writeMethod.invoke(this, false);
                        }
                    } else {
                        writeMethod.invoke(this, new Object[]{null});
                    }
                }
            }
        } catch (IllegalAccessException e) {
            Assert.PROGRAMMING.fail(e);
        } catch (InvocationTargetException e) {
            Assert.PROGRAMMING.fail(e);
        }
    }

    /**
     * Ask if string is not empty.
     *
     * @param s string to check
     * @return true if string has value
     */
    public static boolean hasValue(String s) {
        return !isEmpty(s);
    }

    /**
     * Ask if object is not null
     *
     * @param o object to check
     * @return true if object is not null
     */
    public static boolean hasValue(Object o) {
        return !isEmpty(o);
    }

    /**
     * Ask if collection is not empty, is not null and has at least 1 member.
     *
     * @param c collection to check
     * @return true if not empty
     */
    public static boolean hasValue(Collection c) {
        return !isEmpty(c);
    }

    /**
     * Ask if given string is empty or null. Subclasses may use this convenient, utility method to determine if
     * parameter values are not empty and should be applied to the query.
     *
     * @param s string to check
     * @return true if empty or null
     */
    public static boolean isEmpty(String s) {
        return s == null || s.isEmpty();
    }

    /**
     * Ask if given object is null.
     *
     * @param o object to check
     * @return true if null
     */
    public static boolean isEmpty(Object o) {
        return o == null;
    }

    /**
     * Ask if given collection is empty or null.
     *
     * @param c collection to check
     * @return true if empty or null
     */
    public static boolean isEmpty(Collection c) {
        return c == null || c.isEmpty();
    }

    @Override
    public String toString() {
        return "EntityQuery{" +
                "pageSize=" + pageSize +
                ", firstResult=" + firstResult +
                '}';
    }

    /**
     * Order direction, i.e. ascending or descending
     */
    public enum OrderDirection {
        /**
         * Ascending
         */
        ASC,
        /**
         * Descending
         */
        DESC
    }
}
