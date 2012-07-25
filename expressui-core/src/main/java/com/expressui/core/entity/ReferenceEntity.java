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

package com.expressui.core.entity;

import javax.persistence.Cacheable;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

/**
 * Base class for entities that are read-only by end users and that often represent
 * menu select items like states or countries.
 */
@MappedSuperclass
@Cacheable
public abstract class ReferenceEntity implements IdentifiableEntity, NamedEntity, Comparable {

    /**
     * Name of read-only cache, which should be defined in the application's ehcache.xml.
     */
    public static final String READ_ONLY_CACHE = "ReadOnly";

    /**
     * Property of reference entities that is displayed to users in select menus.
     */
    public static final String DISPLAY_PROPERTY = "name";

    /**
     * Property of reference entities used for sorting reference entities, allowing full control over
     * the sort order of reference entities.
     */
    public static final String ORDER_BY_PROPERTY = "sortOrder";

    @Id
    private String id;

    private String name;

    private Integer sortOrder;

    protected ReferenceEntity() {
    }

    /**
     * Constructor.
     *
     * @param id the primary key for this entity
     */
    protected ReferenceEntity(String id) {
        this.id = id;
    }

    /**
     * Constructor.
     *
     * @param id   the primary key for this entity
     * @param name the friendly name intended to displayed to end user
     */
    protected ReferenceEntity(String id, String name) {
        this.id = id;
        this.name = name;
    }

    @Override
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    /**
     * Gets caption text for displaying to the user in menus. The display name
     * can be different than the id but doesn't have to be.
     *
     * @return friendly name that identifies this entity to an end-user
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * Sets caption text for displaying to the user in menus. The display name
     * can be different than the id but doesn't have to be.
     *
     * @param name friendly name that identifies this entity to an end-user
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets number for controlling sort order.
     *
     * @return number for controlling sort order
     */
    public Integer getSortOrder() {
        return sortOrder;
    }

    /**
     * Sets number for controlling sort order.
     *
     * @param order number for controlling sort order
     */
    public void setSortOrder(Integer order) {
        this.sortOrder = order;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ReferenceEntity)) return false;

        ReferenceEntity that = (ReferenceEntity) o;

        return getId().equals(that.getId());
    }

    @Override
    public int hashCode() {
        return getId().hashCode();
    }

    @Override
    public String toString() {
        return getId();
    }

    @Override
    public int compareTo(Object o) {
        return id.compareTo(((ReferenceEntity) o).id);
    }
}
