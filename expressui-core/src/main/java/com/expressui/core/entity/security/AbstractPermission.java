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

package com.expressui.core.entity.security;

import com.expressui.core.entity.WritableEntity;
import com.expressui.core.view.field.LabelDepot;
import org.hibernate.annotations.ForeignKey;
import org.hibernate.annotations.Index;
import org.hibernate.validator.constraints.NotBlank;

import javax.annotation.Resource;
import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * A permission for controlling view, create, edit or delete actions against an
 * entity type or a field/property within an entity type.
 */
@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public abstract class AbstractPermission extends WritableEntity {

    @Transient
    @Resource
    private LabelDepot labelDepot;

    private String entityType;
    private String field;

    private boolean view;
    private boolean create;
    private boolean edit;
    private boolean delete;

    @Index(name = "IDX_PERMISSION_ROLE")
    @ForeignKey(name = "FK_PERMISSION_ROLE")
    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.REFRESH)
    private AbstractRole role;


    public AbstractPermission() {
    }

    public AbstractPermission(String entityType) {
        this.entityType = entityType;
    }

    @NotBlank
    @NotNull
    @Size(min = 1, max = 64)
    public String getEntityType() {
        return entityType;
    }

    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public boolean isView() {
        return view;
    }

    public void setView(boolean view) {
        this.view = view;
    }

    public boolean isCreate() {
        return create;
    }

    public void setCreate(boolean create) {
        this.create = create;
    }

    public boolean isEdit() {
        return edit;
    }

    public void setEdit(boolean edit) {
        this.edit = edit;
    }

    public boolean isDelete() {
        return delete;
    }

    public void setDelete(boolean delete) {
        this.delete = delete;
    }

    public String getPermissions() {
        StringBuilder permissions = new StringBuilder();
        if (isCreate()) {
            permissions.append("Create");
        }
        if (isView()) {
            if (permissions.length() > 0) {
                permissions.append(", ");
            }
            permissions.append("View");
        }
        if (isEdit()) {
            if (permissions.length() > 0) {
                permissions.append(", ");
            }
            permissions.append("Edit");
        }
        if (isDelete()) {
            if (permissions.length() > 0) {
                permissions.append(", ");
            }
            permissions.append("Delete");
        }

        return permissions.toString();
    }

    public String getEntityTypeLabel() {
        if (getEntityType() == null) {
            return null;
        } else {
            return labelDepot.getEntityLabel(getEntityType());
        }
    }

    public String getFieldLabel() {
        if (getEntityType() == null || getField() == null) {
            return null;
        } else {
            return labelDepot.getFieldLabel(getEntityType(), getField());
        }
    }

    public AbstractRole getRole() {
        return role;
    }

    public void setRole(AbstractRole role) {
        this.role = role;
    }
}
