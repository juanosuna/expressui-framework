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

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.Cacheable;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

import static com.expressui.core.entity.ReferenceEntity.READ_ONLY_CACHE;

/**
 * Base class for entities that are read-only by end users.  and that represent
 * things like menu selects states or countries.
 */
@MappedSuperclass
@Cacheable
public abstract class ReferenceEntity implements IdentifiableEntity, Comparable {

    /**
     * Name of read-only cache, which should be defined in the application's ehcache.xml.
     */
    public static final String READ_ONLY_CACHE = "ReadOnly";

    /**
     * Property of reference entities that is displayed to users in select boxes.
     */
    public static final String DISPLAY_PROPERTY = "displayName";

    /**
     * Property of reference entities used for sorting reference entities, allowing full control over
     * the sort order of reference entities.
     */
    public static final String ORDER_BY_PROPERTY = "sortOrder";

    @Id
    private String id;

    private String displayName;

    private Integer sortOrder;

    protected ReferenceEntity() {
    }

    protected ReferenceEntity(String id) {
        this.id = id;
    }

    protected ReferenceEntity(String id, String displayName) {
        this.id = id;
        this.displayName = displayName;
    }

    @Override
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    /**
     * Get caption text for displaying to the user in menus. The display name
     * can be different than the id but doesn't have to be.
     *
     * @return friendly name that identifies this entity to an end-user
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Set caption text for displaying to the user in menus. The display name
     * can be different than the id but doesn't have to be.
     *
     * @param displayName friendly name that identifies this entity to an end-user
     */
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Get number for controlling sort order.
     *
     * @return number for controlling sort order
     */
    public Integer getSortOrder() {
        return sortOrder;
    }

    /**
     * Set number for controlling sort order.
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

        if (!getId().equals(that.getId())) return false;

        return true;
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
