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

import com.expressui.core.dao.query.StructuredEntityQuery;
import com.expressui.core.dao.query.ToManyRelationshipQuery;
import com.expressui.core.entity.security.User;
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
 * Base class for entity DAOs with parameter type.
 * <p/>
 * Methods that write to the database are marked @Transactional.
 *
 * @param <T>  entity type
 * @param <ID> entity's primary key type that must implement Serializable
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

    /**
     * Get the entity type declared as the subclass's generic first argument.
     *
     * @return the entity type declared as the subclass's generic first argument
     */
    public Class<T> getEntityType() {
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
    public Class<ID> getIdType() {
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
     * Get the id or primary key of the given entity.
     *
     * @param entity persistent entity
     * @return id or primary key
     */
    public <T> Serializable getId(T entity) {
        return genericDao.getId(entity);
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
    public T getReference(T entity) {
        return genericDao.getReference(entity);
    }

    /**
     * Create new entity
     *
     * @return newly created entity
     */
    public T create() {
        return genericDao.create(getEntityType());
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
     * Save a given entity, i.e. persist if new and merge if already persistent
     *
     * @param entity to save
     */
    public <T> void save(T entity) {
        genericDao.save(entity);
    }

    /**
     * Refresh given entity.
     *
     * @param entity to refresh
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
     * Find single entity owned by a given user, e.g Profile
     *
     * @param user user to query
     * @return found entity
     */
    public T findUserOwnedEntity(User user) {
        return genericDao.findUserOwnedEntity(getEntityType(), user);
    }

    /**
     * Utility method for setting hints on given query to read-only, thus enabling caching
     *
     * @param query query to set hints on
     */
    public static void setReadOnly(Query query) {
        GenericDao.setReadOnly(query);
    }

    /**
     * Execute the given structured entity query.
     *
     * @param structuredEntityQuery query that can be re-executed as paging, sort and other criteria change
     * @return list of found entities
     * @see com.expressui.core.dao.query.StructuredEntityQuery
     */
    public List<T> execute(StructuredEntityQuery structuredEntityQuery) {
        return genericDao.execute(structuredEntityQuery);
    }

    /**
     * Execute the given structured entity query that finds child entities that reference a parent entity in a
     * to-many relationship.
     *
     * @param toManyRelationshipQuery query that can be re-executed as paging, sort and other criteria change
     * @return list of found entities
     * @see com.expressui.core.dao.query.ToManyRelationshipQuery
     */
    public List<T> execute(ToManyRelationshipQuery toManyRelationshipQuery) {
        return genericDao.execute(toManyRelationshipQuery);
    }
}
