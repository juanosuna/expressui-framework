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

package com.expressui.core.dao;

import com.expressui.core.dao.query.StructuredEntityQuery;
import com.expressui.core.dao.query.ToManyRelationshipQuery;
import com.expressui.core.entity.IdentifiableEntity;
import com.expressui.core.util.ReflectionUtil;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;

/**
 * Base class for entity DAOs. All methods that write to the database are marked @Transactional.
 *
 * @param <T>  entity type that must implement IdentifiableEntity
 * @param <ID> entity's primary key type that must implement Serializable
 * @see IdentifiableEntity
 */
public abstract class EntityDao<T, ID extends Serializable> {

    @Resource
    private GenericDao genericDao;

    @PersistenceContext
    private EntityManager entityManager;

    private Class<T> entityType;
    private Class<ID> idType;

    protected EntityDao() {
        entityType = ReflectionUtil.getGenericArgumentType(getClass());
        idType = ReflectionUtil.getGenericArgumentType(getClass(), 1);
    }

    public void setEntityType(Class<T> entityType) {
        this.entityType = entityType;
    }

    public void setIdType(Class<ID> idType) {
        this.idType = idType;
    }

    /**
     * Get the entity type declared as the subclass's generic first argument.
     *
     * @return the entity type declared as the subclass's generic first argument
     */
    protected Class<T> getEntityType() {
        if (entityType == null) {
            throw new UnsupportedOperationException();
        }

        return entityType;
    }

    /**
     * Get the entity's primary key type declared as the subclass's generic second argument.
     *
     * @return the entity's primary key type declared as the subclass's generic second argument
     */
    protected Class<ID> getIdType() {
        if (idType == null) {
            throw new UnsupportedOperationException();
        }

        return idType;
    }

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
     * @see EntityManager#getReference(Class, Object)
     */
    public T getReference(T entity) {
        return genericDao.getReference(entity);
    }

    /**
     * Remove a managed or detached entity.
     *
     * @param entity either attached or detached
     */
    @Transactional
    public void remove(T entity) {
        genericDao.remove(entity);
    }

    /**
     * Merge given entity.
     *
     * @param entity to merge
     * @return managed entity
     * @see EntityManager#merge(Object)
     */
    @Transactional
    public T merge(T entity) {
        return genericDao.merge(entity);
    }

    /**
     * Persist given entity.
     *
     * @param entity to persist
     * @see EntityManager#persist(Object)
     */
    @Transactional
    public void persist(T entity) {
        genericDao.persist(entity);
    }

    /**
     * Persist a collection of entities
     *
     * @param entities to persist
     */
    @Transactional
    public void persist(Collection<T> entities) {
        genericDao.persist(entities);
    }

    /**
     * Refresh given entity.
     *
     * @param entity
     * @see EntityManager#refresh(Object)
     */
    public void refresh(T entity) {
        genericDao.refresh(entity);
    }

    /**
     * Ask if given entity is persistent, i.e. if it has a primary key
     *
     * @param entity to check
     * @return true if entity has primary key
     */
    public boolean isPersistent(T entity) {
        return genericDao.isPersistent(entity);
    }

    /**
     * Flush the entityManager.
     *
     * @see EntityManager#flush()
     */
    public void flush() {
        genericDao.flush();
    }

    /**
     * Clear the entityManager.
     *
     * @see EntityManager#clear()
     */
    public void clear() {
        genericDao.clear();
    }

    /**
     * Find entity by primary key.
     *
     * @param id primary key
     * @return initialized entity
     * @see EntityManager#find(Class, Object)
     */
    public T find(ID id) {
        return genericDao.find(getEntityType(), id);
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
    public T findByNaturalId(String propertyName, Object propertyValue) {
        return genericDao.findByNaturalId(getEntityType(), propertyName, propertyValue);
    }

    /**
     * Find all entities of this DAO's type.
     *
     * @return list of all entities
     */
    public List<T> findAll() {
        return genericDao.findAll(getEntityType());
    }

    /**
     * Get a count of all entities of this DAO's type.
     *
     * @return count of all records in the database
     */
    public Long countAll() {
        return genericDao.countAll(getEntityType());
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
    public List<T> execute(StructuredEntityQuery structuredEntityQuery) {
        return genericDao.execute(structuredEntityQuery);
    }

    /**
     * Execute the given structured query that finds child entities that reference a parent entity in a
     * to-many relationship. Provides same benefits as StructuredEntityQuery.
     *
     * @param toManyRelationshipQuery query that can be re-executed as paging, sort and other criteria change
     * @return list of entities of this DAO's type
     */
    public List<T> execute(ToManyRelationshipQuery toManyRelationshipQuery) {
        return genericDao.execute(toManyRelationshipQuery);
    }

    public static void setReadOnly(Query query) {
        GenericDao.setReadOnly(query);
    }
}
