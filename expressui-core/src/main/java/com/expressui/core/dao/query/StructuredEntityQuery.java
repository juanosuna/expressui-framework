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

import org.apache.commons.lang.StringUtils;
import org.hibernate.ejb.criteria.BasicPathUsageException;

import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * A query designed to be re-executed as the user pages through results and applies different sort criteria. A subclass
 * must implement methods for translating query parameters into JPA criteria, sort property to an order-by clause
 * and specifying any fetch-joins used to eagerly fetch nested entities.
 * <p/>
 * Although ExpressUI supports a traditional DAO approach as well, developers are encouraged to subclass
 * StructuredEntityQuery wherever possible, because it solves a difficult challenge in using Hibernate efficiently.
 * <p/>
 * Hibernate has a limitation where paging does not work as expected with fetch-join clauses. When fetch-join
 * clauses are added to a query, Hibernate pulls all the results into memory and pages through results in memory.
 * Obviously, this is not practical for large result sets!
 * <p/>
 * A StructuredEntityQuery works around this limitation by breaking the query into 3 stages:
 * <ol><li>Execute query and get count of all results</li><li>Execute query and fetch only the primary keys of the
 * current page</li><li>Fetch records matching previously found primary keys but with added fetch-join clauses</li></ol>
 * This approach essentially breaks paging apart from fetching nested entities.
 * <p/>
 * <strong>Design Hint:</strong> Annotate JPA properties using a lazy rather than an eager fetch strategy and override
 * addFetchJoins to specify any nested entities whose properties are referenced in the results. This design approach
 * offers the best performance by avoiding the N+1 select problem, where additional cascading queries are generated for
 * each nested entity referenced in the results. If you see 10 extra queries being generated in the log when displaying
 * a page of 10 results, then you know you have the N+1 select problem!
 * <p/>
 * Subclassing StructuredEntityQuery requires knowledge of the JPA criteria API, which is not
 * terribly friendly. However, this approach not only brings significant performance benefits but also reduces query
 * code to a minimum by allowing query logic to be reused in the different scenarios, i.e. with different query
 * parameters and sort criteria. Simple string-based JPQL or HQL queries initially look easier to read but become
 * messy once you have to restructure them dynamically to handle different query parameters, sort criteria as well as
 * paging and join strategies.
 *
 * @param <T> type of entity being queried
 * @see com.expressui.core.dao.EntityDao#execute(StructuredEntityQuery)
 */
public abstract class StructuredEntityQuery<T> extends EntityQuery<T> {

    /**
     * Builds query criteria.
     *
     * @param builder    should be used by implementation to build criteria
     * @param rootEntity root entity in the from clause
     * @return a list of predicates, one for every part of the criteria
     */
    public List<Predicate> buildCriteria(CriteriaBuilder builder, CriteriaQuery<T> query, Root<T> rootEntity) {
        return new ArrayList<Predicate>();
    }

    /**
     * Sets the parameter values for the query. The parameter names should match those defined by the buildCriteria
     * implementation. The values can be stored in bean properties defined on this object.
     *
     * @param typedQuery interface for setting parameters
     */
    public void setParameters(TypedQuery<Serializable> typedQuery) {
    }

    /**
     * Builds the Path used for sorting.
     *
     * @param rootEntity root entity in the from clause
     * @return path used for sorting
     */
    public Path buildOrderBy(Root<T> rootEntity) {
        return null;
    }

    /**
     * Adds any fetch joins required to improve performance, i.e. to avoid N+1 select problem
     *
     * @param rootEntity root entity in the from clause
     */
    public void addFetchJoins(Root<T> rootEntity) {
    }

    public <Y> Path<Y> path(Root<T> rootEntity, Object beanNode) {
        String propertyPath = id(beanNode);

        Path currentPath = rootEntity;
        String[] properties = StringUtils.split(propertyPath, ".");
        for (String property : properties) {
            currentPath = currentPath.get(property);
        }

        return currentPath;
    }

    public <Y> Path<Y> orderByPath(Root<T> rootEntity, Object beanNode) {
        String propertyPath = id(beanNode);

        Path currentPath = rootEntity;
        String[] properties = StringUtils.split(propertyPath, ".");
        for (String property : properties) {
            if (currentPath instanceof From) {
                try {
                    currentPath = ((From) currentPath).join(property, JoinType.LEFT);
                } catch (BasicPathUsageException e) {
                    currentPath = currentPath.get(property);
                }
            } else {
                currentPath = currentPath.get(property);
            }
        }

        return currentPath;
    }

    public <X, Y> FetchParent<X, Y> fetch(Root<T> rootEntity, JoinType joinType, Object beanNode) {
        String propertyPath = id(beanNode);

        FetchParent currentPath = rootEntity;
        String[] properties = StringUtils.split(propertyPath, ".");
        for (String property : properties) {
            currentPath = currentPath.fetch(property, joinType);
        }

        return (FetchParent<X, Y>) currentPath;
    }

    /**
     * Executes this query.
     *
     * @return list of results
     */
    @Override
    public List<T> execute() {
        return genericDao.execute(this);
    }
}
