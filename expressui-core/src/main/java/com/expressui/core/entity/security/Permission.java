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
import com.expressui.core.view.field.LabelRegistry;
import org.hibernate.annotations.ForeignKey;
import org.hibernate.annotations.Index;
import org.hibernate.validator.constraints.NotBlank;

import javax.annotation.Resource;
import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * A permission for controlling view, create, edit or delete actions against an
 * type or a field/property within an type.
 */
@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@ValidPermission
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"TARGET_TYPE", "FIELD"}))
public class Permission extends WritableEntity {

    @Transient
    @Resource
    private LabelRegistry labelRegistry;

    private String targetType;
    private String field;

    private boolean view;
    private boolean create;
    private boolean edit;
    private boolean delete;

    @Index(name = "IDX_PERMISSION_ROLE")
    @ForeignKey(name = "FK_PERMISSION_ROLE")
    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.REFRESH)
    private Role role;

    public Permission() {
    }

    public Permission(String targetType) {
        this.targetType = targetType;
    }

    /**
     * Get type for entity this permission applies to.
     *
     * @return name of the entity class type
     */
    @NotBlank
    @NotNull
    @Size(min = 1, max = 64)
    public String getTargetType() {
        return targetType;
    }

    /**
     * Set the type of entity this permission applies to.
     *
     * @param targetType name of the entity class type
     */
    public void setTargetType(String targetType) {
        this.targetType = targetType;
    }

    /**
     * Get the field name this permission applies to.
     *
     * @return name of the field (bean property), null if this permission applies to type only
     */
    public String getField() {
        return field;
    }

    /**
     * Set the field name this permission applies to.
     *
     * @param field name of the field (bean property), null if this permission applies to type only
     */
    public void setField(String field) {
        this.field = field;
    }

    /**
     * Ask if this permission grants view access
     *
     * @return true to grant view access
     */
    public boolean isView() {
        return view;
    }

    /**
     * Set if this permission grants view access
     *
     * @param view true to grant view access
     */
    public void setView(boolean view) {
        this.view = view;
    }

    /**
     * Ask if this permission grants create access
     *
     * @return true to grant create access
     */
    public boolean isCreate() {
        return create;
    }

    /**
     * Set if this permission grants create access
     *
     * @param create true to grant create access
     */
    public void setCreate(boolean create) {
        this.create = create;
    }

    /**
     * Ask if this permission grants edit access
     *
     * @return true to grant edit access
     */
    public boolean isEdit() {
        return edit;
    }

    /**
     * Set if this permission grants edit access
     *
     * @param edit true to grant edit access
     */
    public void setEdit(boolean edit) {
        this.edit = edit;
    }

    /**
     * Ask if this permission grants delete access
     *
     * @return true to grant delete access
     */
    public boolean isDelete() {
        return delete;
    }

    /**
     * Set if this permission grants delete access
     *
     * @param delete true to grant delete access
     */
    public void setDelete(boolean delete) {
        this.delete = delete;
    }

    /**
     * Get display-friend list of create, view, edit, delete permissions
     *
     * @return comma-delimited list for display to end user
     */
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

    /**
     * Get the user-friendly display label for references the type
     *
     * @return user-friendly label, displayed in UI
     */
    public String getTargetTypeLabel() {
        if (getTargetType() == null) {
            return null;
        } else {
            return labelRegistry.getTypeLabel(getTargetType());
        }
    }

    /**
     * Get the user-friendly display label for references the field
     *
     * @return user-friendly label, displayed in UI
     */
    public String getFieldLabel() {
        if (getTargetType() == null || getField() == null) {
            return null;
        } else {
            return labelRegistry.getFieldLabel(getTargetType(), getField());
        }
    }

    /**
     * Get the role that this permission compositionally belong to
     *
     * @return parent Role
     */
    public Role getRole() {
        return role;
    }

    /**
     * Set the role that this permission compositionally belong to
     *
     * @param role parent Role
     */
    public void setRole(Role role) {
        this.role = role;
    }
}
