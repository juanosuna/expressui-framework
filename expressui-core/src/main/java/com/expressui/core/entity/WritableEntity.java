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

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import java.util.UUID;

/**
 * Base class for entities that are writable by end users.
 * <p/>
 * This class requires that these entities use a generated Long id as the primary key. This is a surrogate key
 * that should have no business meaning.
 * <p/>
 * It also generates a unique UUID that is used in the default equals and hashcode logic. Developers are free
 * to override this logic and use their own logic based on business keys, which is the "ideal" best practice.
 * However, the UUID approach also correctly solves the equality problem where transient and non-transient entities
 * can be compared and/or added to collections.
 * <p/>
 * Even though UUIDs seem like clutter in the database, they pragmatically relieve developers
 * from having to properly implement equals/hashcode by identifying business keys for every entity. So,
 * the UUID approach offers a good default strategy.
 * <p/>
 * Of course, even with the UUIDs, developers should make sure to annotate business keys so that unique
 * constraints are generated in the DDL, even if these business keys are not used in equals/hashcode.
 */
@MappedSuperclass
public abstract class WritableEntity extends AuditableEntity {

    @Id
    @GeneratedValue(generator = "sequence")
    @GenericGenerator(name = "sequence", strategy = "com.expressui.core.util.TableNameSequenceGenerator")
    private Long id;

    @Column(unique = true, nullable = false, updatable = false)
    private String uuid;

    protected WritableEntity() {
        super();
        uuid = UUID.randomUUID().toString();
    }

    /**
     * Get the Id or primary key for this entity.
     *
     * @return id or primary key for this entity
     */
    public Long getId() {
        return id;
    }

    /**
     * Get the randomly generated UUID that was created when this entity was constructed in memory
     *
     * @return UUID that was generated from UUID.randomUUID()
     */
    public String getUuid() {
        return uuid;
    }

    /**
     * Implements equals based on randomly generated UUID
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof WritableEntity)) return false;

        WritableEntity that = (WritableEntity) o;

        if (!getUuid().equals(that.getUuid())) return false;

        return true;
    }

    /**
     * Implements hashCode based on randomly generated UUID
     */
    @Override
    public int hashCode() {
        return getUuid().hashCode();
    }

    @Override
    public String toString() {
        return "WritableEntity{" +
                "uuid=" + getUuid() +
                '}';
    }
}
