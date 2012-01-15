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

package com.expressui.core.dao;

import com.expressui.core.dao.query.EntityQuery;
import com.expressui.core.dao.query.StructuredEntityQuery;
import com.expressui.core.dao.query.ToManyRelationshipQuery;
import com.expressui.core.entity.IdentifiableEntity;
import com.expressui.core.util.ReflectionUtil;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Base class for entity DAOs. All methods that write to the database are marked @Transactional.
 *
 * @see com.expressui.core.entity.IdentifiableEntity
 */
@Repository
public class GenericDao {

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Get the EntityManager.
     *
     * @return the EntityManager
     */
    public EntityManager getEntityManager() {
        return entityManager;
    }

    /**
     * Get a managed reference to the given entity, which may be detached. The managed reference is retrieved
     * using given entity's primary key. This is useful when you need an managed reference to an entity and don't
     * care about merging it's state.
     *
     * @param entity for getting the primary key
     * @return attached but hollow entity
     * @see javax.persistence.EntityManager#getReference(Class, Object)
     */
    public <T> T getReference(T entity) {
        Object primaryKey = ((IdentifiableEntity) entity).getId();
        return getEntityManager().getReference(getEntityType(entity), primaryKey);
    }

    private <T> Class<? extends T> getEntityType(T entity) {
        return (Class<? extends T>) entity.getClass();
    }

    /**
     * Remove a managed or detached entity.
     *
     * @param entity either attached or detached
     */
    @Transactional
    public <T> void remove(T entity) {
        T attachedEntity = getReference(entity);
        getEntityManager().remove(attachedEntity);
    }

    /**
     * Merge given entity.
     *
     * @param entity to merge
     * @return managed entity
     * @see javax.persistence.EntityManager#merge(Object)
     */
    @Transactional
    public <T> T merge(T entity) {
        return getEntityManager().merge(entity);
    }

    /**
     * Persist given entity.
     *
     * @param entity to persist
     * @see javax.persistence.EntityManager#persist(Object)
     */
    @Transactional
    public <T> void persist(T entity) {
        getEntityManager().persist(entity);
    }

    /**
     * Persist a collection of entities
     *
     * @param entities to persist
     */
    @Transactional
    public <T> void persist(Collection<T> entities) {
        for (T entity : entities) {
            persist(entity);
        }
    }

    /**
     * Refresh given entity.
     *
     * @param entity
     * @see javax.persistence.EntityManager#refresh(Object)
     */
    public <T> void refresh(T entity) {
        getEntityManager().refresh(entity);
    }

    /**
     * Ask if given entity is persistent, i.e. if it has a primary key
     *
     * @param entity to check
     * @return true if entity has primary key
     */
    public <T> boolean isPersistent(T entity) {
        Serializable id = (Serializable) getEntityManager().getEntityManagerFactory().getPersistenceUnitUtil().getIdentifier(entity);
        if (id == null) {
            return false;
        } else {
            T existingEntity = find(getEntityType(entity), id);
            return existingEntity != null;
        }
    }

    /**
     * Flush the entityManager.
     *
     * @see javax.persistence.EntityManager#flush()
     */
    public void flush() {
        getEntityManager().flush();
    }

    /**
     * Clear the entityManager.
     *
     * @see javax.persistence.EntityManager#clear()
     */
    public void clear() {
        getEntityManager().clear();
    }

    /**
     * Find entity by primary key.
     *
     * @param id primary key
     * @return initialized entity
     * @see javax.persistence.EntityManager#find(Class, Object)
     */
    public <T> T find(Class<? extends T> entityType, Serializable id) {
        return getEntityManager().find(entityType, id);
    }

    /**
     * Find entity by natural id, or business key. This method only works for entities that have a single property
     * marked as @NaturalId. The benefit of calling this method is that Hibernate will try to look up the entity
     * in the secondary cache.
     *
     * @param propertyName  name of the property in the entity that is marked @NaturalId
     * @param propertyValue value to search for
     * @return entity
     */
    public <T> T findByNaturalId(Class<? extends T> entityType, String propertyName, Object propertyValue) {
        Session session = (Session) getEntityManager().getDelegate();

        Criteria criteria = session.createCriteria(entityType);
        criteria.add(Restrictions.naturalId().set(propertyName, propertyValue));
        criteria.setCacheable(true);

        return (T) criteria.uniqueResult();
    }

    /**
     * Find all entities of this DAO's type.
     *
     * @return list of all entities
     */
    public <T> List<T> findAll(Class<? extends T> entityType) {
        Query query = getEntityManager().createQuery("SELECT e FROM " + entityType.getSimpleName() + " e");

        return query.getResultList();
    }

    /**
     * Get a count of all entities of this DAO's type.
     *
     * @return count of all records in the database
     */
    public <T> Long countAll(Class<? extends T> entityType) {
        Query query = getEntityManager().createQuery("SELECT COUNT(e) from " + entityType.getSimpleName() + " e");

        return (Long) query.getSingleResult();
    }

    public static void setReadOnly(Query query) {
        query.setHint("org.hibernate.cacheable", true);
        query.setHint("org.hibernate.cacheRegion", "ReadOnlyQuery");
        query.setHint("org.hibernate.readOnly", true);
    }

    /**
     * Execute the given structured query. The main benefits of a structured query are that it can be
     * re-executed to fetch different pages in the result set, to sort on different properties, and to
     * keep track of the result set count.
     * Another benefit is performance. Normally, Hibernate does not support fetch joins with paging.
     * However, a structure query works around this limitation by breaking the query into multiple queries
     * executed in stages. First, it fetches the count. Second, it fetches
     * the ids matching the query and paging criteria. Lastly, it fetches the full entities using fetch joins
     * using the ids returned from the previous query. Using this staged approach, fetch joins are not
     * executed in the same query as paging.
     *
     * @param structuredEntityQuery query that can be re-executed as paging, sort and other criteria change
     * @return list of entities of this DAO's type
     */
    public <T> List<T> execute(StructuredEntityQuery<T> structuredEntityQuery) {
        return new StructuredQueryExecutor(
                ReflectionUtil.getGenericArgumentType(structuredEntityQuery.getClass()),
                structuredEntityQuery).execute();
    }

    /**
     * Execute the given structured query that finds child entities that reference a parent entity in a
     * to-many relationship. Provides same benefits as StructuredEntityQuery.
     *
     * @param toManyRelationshipQuery query that can be re-executed as paging, sort and other criteria change
     * @return list of entities of this DAO's type
     */
    public <T, P> List<T> execute(ToManyRelationshipQuery<T, P> toManyRelationshipQuery) {
        return new ToManyRelationshipQueryExecutor(
                ReflectionUtil.getGenericArgumentType(toManyRelationshipQuery.getClass()),
                toManyRelationshipQuery).execute();
    }

    private class StructuredQueryExecutor {

        private Class entityType;

        private StructuredEntityQuery structuredQuery;

        public StructuredQueryExecutor(Class entityType, StructuredEntityQuery structuredQuery) {
            this.structuredQuery = structuredQuery;
            this.entityType = entityType;
        }

        public StructuredEntityQuery getStructuredQuery() {
            return structuredQuery;
        }

        public Class getEntityType() {
            return entityType;
        }

        public List execute() {
            List<Serializable> count = executeImpl(true);
            structuredQuery.setResultCount((Long) count.get(0));

            if (structuredQuery.getResultCount() > 0) {
                List<Serializable> ids = executeImpl(false);
                return findByIds(ids);
            } else {
                return new ArrayList();
            }
        }

        private List<Serializable> executeImpl(boolean isCount) {
            CriteriaBuilder builder = getEntityManager().getCriteriaBuilder();
            CriteriaQuery c = builder.createQuery();
            Root rootEntity = c.from(getEntityType());

            if (isCount) {
                c.select(builder.count(rootEntity));
            } else {
                c.select(rootEntity.get("id"));
            }

            List<Predicate> criteria = structuredQuery.buildCriteria(builder, rootEntity);
            c.where(builder.and(criteria.toArray(new Predicate[0])));

            if (!isCount && structuredQuery.getOrderByPropertyId() != null) {
                Path path = structuredQuery.buildOrderBy(rootEntity);
                if (path == null) {
                    path = rootEntity.get(structuredQuery.getOrderByPropertyId());
                }
                if (structuredQuery.getOrderDirection().equals(EntityQuery.OrderDirection.ASC)) {
                    c.orderBy(builder.asc(path));
                } else {
                    c.orderBy(builder.desc(path));
                }
            }

            TypedQuery<Serializable> typedQuery = getEntityManager().createQuery(c);
            structuredQuery.setParameters(typedQuery);

            if (!isCount) {
                typedQuery.setFirstResult(structuredQuery.getFirstResult());
                typedQuery.setMaxResults(structuredQuery.getPageSize());
            }

            return typedQuery.getResultList();
        }

        private List findByIds(List<Serializable> ids) {
            CriteriaBuilder builder = getEntityManager().getCriteriaBuilder();
            CriteriaQuery c = builder.createQuery(getEntityType());
            Root rootEntity = c.from(getEntityType());
            c.select(rootEntity);

            structuredQuery.addFetchJoins(rootEntity);

            List<Predicate> criteria = new ArrayList<Predicate>();
            ParameterExpression<List> p = builder.parameter(List.class, "ids");
            criteria.add(builder.in(rootEntity.get("id")).value(p));

            c.where(builder.and(criteria.toArray(new Predicate[0])));

            if (structuredQuery.getOrderByPropertyId() != null) {
                Path path = structuredQuery.buildOrderBy(rootEntity);
                if (path == null) {
                    path = rootEntity.get(structuredQuery.getOrderByPropertyId());
                }
                if (structuredQuery.getOrderDirection().equals(EntityQuery.OrderDirection.ASC)) {
                    c.orderBy(builder.asc(path));
                } else {
                    c.orderBy(builder.desc(path));
                }
            }

            TypedQuery q = getEntityManager().createQuery(c);
            q.setParameter("ids", ids);

            return q.getResultList();
        }
    }

    private class ToManyRelationshipQueryExecutor extends StructuredQueryExecutor {
        public ToManyRelationshipQueryExecutor(Class entityType, ToManyRelationshipQuery toManyRelationshipQuery) {
            super(entityType, toManyRelationshipQuery);
        }

        @Override
        public ToManyRelationshipQuery getStructuredQuery() {
            return (ToManyRelationshipQuery) super.getStructuredQuery();
        }

        @Override
        public List execute() {
            if (getStructuredQuery().getParent() == null) {
                return new ArrayList();
            } else {
                return super.execute();
            }
        }
    }

}
