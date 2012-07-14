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
import javax.persistence.Query;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;

/**
 * Base class for entity DAOs with a parameter type.
 * <p/>
 * Methods that write to the database are marked @Transactional.
 *
 * @param <T>  entity type
 * @param <ID> entity's primary key type that must implement Serializable
 */
public abstract class EntityDao<T, ID extends Serializable> {

    @Resource
    private GenericDao genericDao;

    private Class<T> entityType;
    private Class<ID> idType;

    protected EntityDao() {
        entityType = ReflectionUtil.getGenericArgumentType(getClass());
        idType = ReflectionUtil.getGenericArgumentType(getClass(), 1);
    }

    /**
     * Gets the entity type declared as the subclass's generic first argument.
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
     * Gets the entity's primary key type declared as the subclass's generic second argument.
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
     * Gets the JPA EntityManager.
     *
     * @return the EntityManager
     */
    public EntityManager getEntityManager() {
        return genericDao.getEntityManager();
    }

    /**
     * Creates an entity.
     *
     * @return newly created entity
     */
    public T create() {
        return genericDao.create(getEntityType());
    }


    /**
     * Removes a managed or detached entity.
     *
     * @param entity either attached or detached
     * @see javax.persistence.EntityManager#remove(Object)
     */
    @Transactional
    public void remove(T entity) {
        genericDao.remove(entity);
    }

    /**
     * Merges an entity.
     *
     * @param entity the entity to merge
     * @return managed entity
     * @see javax.persistence.EntityManager#merge(Object)
     */
    @Transactional
    public T merge(T entity) {
        return genericDao.merge(entity);
    }

    /**
     * Persists an entity.
     *
     * @param entity the entity to persist
     * @see javax.persistence.EntityManager#persist(Object)
     */
    @Transactional
    public void persist(T entity) {
        genericDao.persist(entity);
    }

    /**
     * Persists a collection of entities
     *
     * @param entities the entities to persist
     */
    @Transactional
    public void persist(Collection<T> entities) {
        genericDao.persist(entities);
    }

    /**
     * Saves an entity, persisting it if new and merging it if already persistent.
     *
     * @param entity the entity to save
     * @return merged entity if entity was merged, else same as argument if persisted
     */
    @Transactional
    public T save(T entity) {
        return genericDao.save(entity);
    }

    /**
     * Refreshes an entity.
     *
     * @param entity the entity to refresh
     * @see javax.persistence.EntityManager#refresh(Object)
     */
    public void refresh(T entity) {
        genericDao.refresh(entity);
    }

    /**
     * Gets a managed reference to the given entity, which may have become detached. The managed reference is retrieved
     * using given entity's primary key. This is useful when you need an managed reference to an entity and don't
     * care about merging it's state.
     *
     * @param entity a possibly detached but persistent entity that contains primary key
     * @return managed but hollow entity
     * @see javax.persistence.EntityManager#getReference(Class, Object)
     */
    public T getReference(T entity) {
        return genericDao.getReference(entity);
    }

    /**
     * Gets the id or primary key of a persistent entity.
     *
     * @param entity the persistent entity
     * @return id or primary key
     */
    public Serializable getId(T entity) {
        return genericDao.getId(entity);
    }

    /**
     * Asks if an entity is persistent, that is if it has a primary key.
     *
     * @param entity the entity to check
     * @return true if entity has a primary key
     */
    public boolean isPersistent(T entity) {
        return genericDao.isPersistent(entity);
    }

    /**
     * Flushes the EntityManager.
     *
     * @see javax.persistence.EntityManager#flush()
     */
    public void flush() {
        genericDao.flush();
    }

    /**
     * Clears the EntityManager.
     *
     * @see javax.persistence.EntityManager#clear()
     */
    public void clear() {
        genericDao.clear();
    }

    /**
     * Finds an entity by primary key.
     *
     * @param id the primary key
     * @return found entity
     * @see javax.persistence.EntityManager#find(Class, Object)
     */
    public T find(ID id) {
        return genericDao.find(getEntityType(), id);
    }

    /**
     * Finds an entity again from the database.
     *
     * @param entity the persistent entity with an id
     * @return attached entity found from database, null if none found
     */
    public T reFind(T entity) {
        return genericDao.reFind(entity);
    }

    /**
     * Finds an entity by natural id, or business key. This method only works for entities that have a single property
     * marked as @NaturalId. The benefit of calling this method is that Hibernate will try to look up the entity
     * in the secondary cache.
     *
     * @param propertyName  the name of the property in the entity that is marked @NaturalId
     * @param propertyValue the value to search for
     * @return found entity
     */
    public T findByNaturalId(String propertyName, Object propertyValue) {
        return genericDao.findByNaturalId(getEntityType(), propertyName, propertyValue);
    }

    /**
     * Finds a single entity that is "owned" by a given user, for example a user profile or preferences.
     *
     * @param user the user to query
     * @return found entity, null if none found
     */
    public T findUserOwnedEntity(User user) {
        return genericDao.findUserOwnedEntity(getEntityType(), user);
    }

    /**
     * Finds all entities of given type.
     *
     * @return list of all entities
     */
    public List<T> findAll() {
        return genericDao.findAll(getEntityType());
    }

    /**
     * Gets a count of all entities of given type.
     *
     * @return count of all records in the database
     */
    public Long countAll() {
        return genericDao.countAll(getEntityType());
    }

    /**
     * Utility method for setting Hibernate hints on a query to read-only, thus enabling caching.
     *
     * @param query query to set hints on
     */
    public static void setReadOnly(Query query) {
        GenericDao.setReadOnly(query);
    }

    /**
     * Executes a structured entity query.
     *
     * @param structuredEntityQuery the structured entity query that can be re-executed
     *                              as paging, sort and other criteria vary
     * @return list of found entities
     * @see com.expressui.core.dao.query.StructuredEntityQuery
     */
    public List<T> execute(StructuredEntityQuery structuredEntityQuery) {
        return genericDao.execute(structuredEntityQuery);
    }

    /**
     * Executes a structured entity query that finds child entities that reference a parent entity in a
     * to-many relationship.
     *
     * @param toManyRelationshipQuery the query that can be re-executed as paging, sort and other criteria vary
     * @return list of found entities
     * @see com.expressui.core.dao.query.ToManyRelationshipQuery
     */
    public List<T> execute(ToManyRelationshipQuery toManyRelationshipQuery) {
        return genericDao.execute(toManyRelationshipQuery);
    }
}
