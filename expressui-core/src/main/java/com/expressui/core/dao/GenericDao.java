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
import com.expressui.core.entity.UserOwnedEntity;
import com.expressui.core.entity.security.User;
import com.expressui.core.util.ReflectionUtil;
import com.expressui.core.util.assertion.Assert;
import org.hibernate.Criteria;
import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.*;
import javax.persistence.criteria.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * A generic Data Access Object, which can be used directly without subclassing with type parameters.
 * Instead, type parameters are available at the method level.
 * This is useful for performing standard database operations where no type-specific
 * implementation is needed, thus avoiding empty type-specific DAOs.
 * <p/>
 * Methods that write to the database are marked @Transactional.
 */
@Repository
public class GenericDao {

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Get the JPA EntityManager.
     *
     * @return the EntityManager
     */
    public EntityManager getEntityManager() {
        return entityManager;
    }

    /**
     * Get a managed reference to the given entity, which may have been detached. The managed reference is retrieved
     * using given entity's primary key. This is useful when you need an managed reference to an entity and don't
     * care about merging it's state.
     *
     * @param entity for getting the primary key
     * @return attached but hollow entity
     * @see javax.persistence.EntityManager#getReference(Class, Object)
     */
    public <T> T getReference(T entity) {
        Object primaryKey = getId(entity);
        Assert.PROGRAMMING.notNull(primaryKey, "entity argument must be persistent and have a primary key");
        return getEntityManager().getReference(getEntityType(entity), primaryKey);
    }

    /**
     * Get the type of the given entity.
     *
     * @param entity entity to check
     * @param <T>    entity
     * @return type of the given entity
     */
    private <T> Class<? extends T> getEntityType(T entity) {
        return Hibernate.getClass(entity);
    }


    /**
     * Create entity of given type
     *
     * @param entityType type of entity to create
     * @param <T>        type of entity
     * @return newly created entity
     */
    public <T> T create(Class<? extends T> entityType) {
        try {
            return entityType.newInstance();
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
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
     * Save a given entity, i.e. persist if new and merge if already persistent
     *
     * @param entity to save
     * @param <T>    managed instance
     * @return merged entity if entity was merged, else same as argument
     */
    @Transactional
    public <T> T save(T entity) {
        if (isPersistent(entity)) {
            return merge(entity);
        } else {
            persist(entity);
            return entity;
        }
    }

    /**
     * Refresh given entity.
     *
     * @param entity to refresh
     * @see javax.persistence.EntityManager#refresh(Object)
     */
    public <T> void refresh(T entity) {
        getEntityManager().refresh(entity);
    }

    /**
     * Get the id or primary key of the given entity.
     *
     * @param entity persistent entity
     * @param <T>    type of given entity
     * @return id or primary key
     */
    public <T> Serializable getId(T entity) {
        Serializable id = (Serializable) getEntityManager().getEntityManagerFactory().getPersistenceUnitUtil().getIdentifier(entity);
        if (id == null && entity instanceof IdentifiableEntity) {
            id = ((IdentifiableEntity) entity).getId();
        }

        return id;
    }

    /**
     * Ask if given entity is persistent, i.e. if it has a primary key
     *
     * @param entity to check
     * @return true if entity has primary key
     */
    public <T> boolean isPersistent(T entity) {
        Serializable id = getId(entity);
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
     * @param entityType type of entity
     * @param id         primary key
     * @return found entity
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
     * @param entityType    type of entity
     * @param propertyName  name of the property in the entity that is marked @NaturalId
     * @param propertyValue value to search for
     * @return found entity
     */
    public <T> T findByNaturalId(Class<? extends T> entityType, String propertyName, Object propertyValue) {
        Session session = (Session) getEntityManager().getDelegate();

        Criteria criteria = session.createCriteria(entityType);
        criteria.add(Restrictions.naturalId().set(propertyName, propertyValue));
        criteria.setCacheable(true);

        return (T) criteria.uniqueResult();
    }

    /**
     * Find single entity owned by a given user, e.g Profile
     *
     * @param entityType type of entity
     * @param user       user to query
     * @param <T>        type of entity
     * @return found entity
     */
    public <T> T findUserOwnedEntity(Class<? extends T> entityType, User user) {
        Assert.PROGRAMMING.isTrue(UserOwnedEntity.class.isAssignableFrom(entityType),
                "This entityType " + entityType + " must implement " + UserOwnedEntity.class.getName());

        Query query = getEntityManager().createQuery("SELECT e FROM " + entityType.getSimpleName()
                + " e where e.user = :user");

        query.setParameter("user", user);

        try {
            return (T) query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    /**
     * Find all entities of given type.
     *
     * @param entityType type of entity
     * @return list of all entities
     */
    public <T> List<T> findAll(Class<? extends T> entityType) {
        Query query = getEntityManager().createQuery("SELECT e FROM " + entityType.getSimpleName() + " e");

        return query.getResultList();
    }

    /**
     * Get a count of all entities of given type.
     *
     * @param entityType type of entity
     * @return count of all records in the database
     */
    public <T> Long countAll(Class<? extends T> entityType) {
        Query query = getEntityManager().createQuery("SELECT COUNT(e) from " + entityType.getSimpleName() + " e");

        return (Long) query.getSingleResult();
    }

    /**
     * Utility method for setting hints on given query to read-only, thus enabling caching
     *
     * @param query query to set hints on
     */
    public static void setReadOnly(Query query) {
        query.setHint("org.hibernate.cacheable", true);
        query.setHint("org.hibernate.cacheRegion", "ReadOnlyQuery");
        query.setHint("org.hibernate.readOnly", true);
    }

    /**
     * Execute the given structured entity query.
     *
     * @param structuredEntityQuery query that can be re-executed as paging, sort and other criteria change
     * @return list of found entities
     * @see com.expressui.core.dao.query.StructuredEntityQuery
     */
    public <T> List<T> execute(StructuredEntityQuery<T> structuredEntityQuery) {
        return new StructuredQueryExecutor(
                ReflectionUtil.getGenericArgumentType(structuredEntityQuery.getClass()),
                structuredEntityQuery).execute();
    }

    /**
     * Execute the given structured entity query that finds child entities that reference a parent entity in a
     * to-many relationship.
     *
     * @param toManyRelationshipQuery query that can be re-executed as paging, sort and other criteria change
     * @return list of found entities
     * @see com.expressui.core.dao.query.ToManyRelationshipQuery
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
            CriteriaQuery query = builder.createQuery();
            Root rootEntity = query.from(getEntityType());

            if (isCount) {
                query.select(builder.count(rootEntity));
            } else {
                query.select(rootEntity.get("id"));
            }

            List<Predicate> criteria = structuredQuery.buildCriteria(builder, query, rootEntity);
            query.where(builder.and(criteria.toArray(new Predicate[0])));

            if (!isCount && structuredQuery.getOrderByPropertyId() != null) {
                Path path = structuredQuery.buildOrderBy(rootEntity);
                if (path == null) {
                    path = rootEntity.get(structuredQuery.getOrderByPropertyId());
                }
                if (structuredQuery.getOrderDirection().equals(EntityQuery.OrderDirection.ASC)) {
                    query.orderBy(builder.asc(path));
                } else {
                    query.orderBy(builder.desc(path));
                }
            }

            TypedQuery<Serializable> typedQuery = getEntityManager().createQuery(query);
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
