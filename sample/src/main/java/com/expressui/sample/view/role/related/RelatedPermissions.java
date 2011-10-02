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

package com.expressui.sample.view.role.related;

import com.expressui.core.dao.ToManyRelationshipQuery;
import com.expressui.core.view.EntityForm;
import com.expressui.core.view.field.DisplayFields;
import com.expressui.core.view.field.FormFields;
import com.expressui.core.view.tomanyrelationship.ToManyCompositionRelationshipResults;
import com.expressui.core.view.tomanyrelationship.ToManyRelationship;
import com.expressui.sample.dao.PermissionDao;
import com.expressui.sample.entity.security.Permission;
import com.expressui.sample.entity.security.Role;
import com.vaadin.data.Property;
import com.vaadin.ui.Field;
import com.vaadin.ui.Select;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
@Scope("prototype")
@SuppressWarnings({"rawtypes", "serial"})
public class RelatedPermissions extends ToManyRelationship<Permission> {

    @Resource
    private RelatedPermissionsResults relatedPermissionsResults;

    @Override
    public String getEntityCaption() {
        return "Permissions";
    }

    @Override
    public RelatedPermissionsResults getResults() {
        return relatedPermissionsResults;
    }

    @Component
    @Scope("prototype")
    public static class RelatedPermissionsResults extends ToManyCompositionRelationshipResults<Permission> {

        @Resource
        private PermissionDao permissionDao;

        @Resource
        private PermissionForm permissionForm;

        @Resource
        private RelatedPermissionsQuery relatedPermissionsQuery;

        @Override
        public PermissionDao getEntityDao() {
            return permissionDao;
        }

        @Override
        public PermissionForm getEntityForm() {
            return permissionForm;
        }

        @Override
        public ToManyRelationshipQuery getEntityQuery() {
            return relatedPermissionsQuery;
        }

        @Override
        public void postWire() {
            permissionForm.setRelatedPermissionsQuery(relatedPermissionsQuery);
            super.postWire();
        }

        @Override
        public void configureFields(DisplayFields displayFields) {
            displayFields.setPropertyIds(new String[]{
                    "entityType",
                    "field",
                    "permissions",
                    "lastModified",
                    "modifiedBy"
            });
        }

        @Override
        public String getChildPropertyId() {
            return "permissions";
        }

        @Override
        public String getParentPropertyId() {
            return "role";
        }

        @Override
        public String getEntityCaption() {
            return "Permissions";
        }
    }

    @Component
    @Scope("prototype")
    public static class RelatedPermissionsQuery extends ToManyRelationshipQuery<Permission, Role> {

        @Resource
        private PermissionDao permissionDao;

        private Role role;

        @Override
        public void setParent(Role parent) {
            this.role = parent;
        }

        @Override
        public Role getParent() {
            return role;
        }

        @Override
        public List<Permission> execute() {
            return permissionDao.execute(this);
        }

        @Override
        public List<Predicate> buildCriteria(CriteriaBuilder builder, Root<Permission> rootEntity) {
            List<Predicate> criteria = new ArrayList<Predicate>();

            if (!isEmpty(role)) {
                ParameterExpression<Role> p = builder.parameter(Role.class, "role");
                criteria.add(builder.equal(rootEntity.get("role"), p));
            }

            return criteria;
        }

        @Override
        public void setParameters(TypedQuery typedQuery) {
            if (!isEmpty(role)) {
                typedQuery.setParameter("role", role);
            }
        }

        @Override
        public void addFetchJoins(Root<Permission> rootEntity) {
            rootEntity.fetch("role", JoinType.LEFT);
        }

        @Override
        public String toString() {
            return "RelatedPermissions{" +
                    "role='" + role + '\'' +
                    '}';
        }
    }

    @Component
    @Scope("prototype")
    public static class PermissionForm extends EntityForm<Permission> {

        @Resource
        private PermissionDao permissionDao;

        private RelatedPermissionsQuery relatedPermissionsQuery;

        @Override
        public void configureFields(FormFields formFields) {
            formFields.setPosition("entityType", 1, 1);
            formFields.setPosition("entityTypeLabel", 1, 2);

            formFields.setPosition("field", 2, 1);
            formFields.setPosition("fieldLabel", 2, 2);

            formFields.setPosition("view", 3, 1);
            formFields.setPosition("edit", 3, 2);

            formFields.setPosition("create", 4, 1);
            formFields.setPosition("delete", 4, 2);

            formFields.setField("entityType", new Select());
            formFields.addValueChangeListener("entityType", this, "entityTypeChanged");

            formFields.setField("field", new Select());

            formFields.addValueChangeListener("view", this, "syncCRUDCheckboxes");
            formFields.addValueChangeListener("field", this, "syncCRUDCheckboxes");
            formFields.addValueChangeListener("field", this, "syncIsRequiredIndicator");
        }

        public void syncCRUDCheckboxes(Property.ValueChangeEvent event) {
            Field viewField = getFormFields().getFormField("view").getField();
            Boolean isViewChecked = (Boolean) viewField.getValue();

            Field fieldField = getFormFields().getFormField("field").getField();
            Boolean isFieldSelected = fieldField.getValue() != null;

            getFormFields().setEnabled("create", isViewChecked && !isFieldSelected);
            getFormFields().setEnabled("edit", isViewChecked);
            getFormFields().setEnabled("delete", isViewChecked && !isFieldSelected);
        }

        @Override
        public void postWire() {
            super.postWire();

            getFormFields().setSelectItems("entityType", getEntityTypeItems());
        }

        @Override
        public void create() {
            super.create();

            getFormFields().setSelectItems("entityType", getEntityTypeItems());
            syncIsRequiredIndicator(null);
//            getFormFields().setRequired("field", false);
//            syncTabAndSaveButtonErrors();
        }


        @Override
        public void refresh() {
            super.refresh();
            syncIsRequiredIndicator(null);
        }

        private Map<Object, String> getEntityTypeItems() {
            Map<Object, String> entityTypeItems = new LinkedHashMap<Object, String>();
            List<Permission> existingPermissions = getExistingPermissionsForParentRole();

            Map<String, String> entityTypes = labelDepot.getEntityTypeLabels();
            for (String entityType : entityTypes.keySet()) {
                boolean isEntityTypeWithNullFieldAvailable = true;
                for (Permission existingPermission : existingPermissions) {
                    if (!existingPermission.equals(getEntity()) && existingPermission.getEntityType().equals(entityType)
                            && existingPermission.getField() == null) {
                        isEntityTypeWithNullFieldAvailable = false;
                        break;
                    }
                }

                Map<Object, String> fieldItems = getFieldItems(entityType);
                if (!fieldItems.isEmpty() || isEntityTypeWithNullFieldAvailable) {
                    entityTypeItems.put(entityType, entityType);
                }
            }

            return entityTypeItems;
        }

        public void entityTypeChanged(Property.ValueChangeEvent event) {
            String newEntityType = (String) event.getProperty().getValue();

            if (newEntityType != null) {
                Map<Object, String> fieldItems = getFieldItems(newEntityType);
                getFormFields().setSelectItems("field", fieldItems, "None");

                syncIsRequiredIndicator(null);
            }
        }

        public void syncIsRequiredIndicator(Property.ValueChangeEvent event) {
            getFormFields().setRequired("field", anotherPermissionHasNullField(getEntity().getEntityType()));
            syncTabAndSaveButtonErrors();
        }

        private boolean anotherPermissionHasNullField(String entityType) {
            List<Permission> existingPermissions = getExistingPermissionsForParentRole();
            for (Permission existingPermission : existingPermissions) {
                if (existingPermission.getEntityType().equals(entityType) && existingPermission.getField() == null
                        && !existingPermission.equals(getEntity())) {
                    return true;
                }
            }

            return false;
        }

        private Map<Object, String> getFieldItems(String entityType) {
            List<Permission> existingPermissions = getExistingPermissionsForParentRole();

            Map<Object, String> fieldItems = labelDepot.getPropertyIds(entityType);
            for (Permission existingPermission : existingPermissions) {
                if (existingPermission.getEntityType().equals(entityType) && existingPermission.getField() != null
                        && !existingPermission.equals(getEntity())) {
                    fieldItems.remove(existingPermission.getField());
                }
            }

            return fieldItems;
        }

        public RelatedPermissionsQuery getRelatedPermissionsQuery() {
            return relatedPermissionsQuery;
        }

        public void setRelatedPermissionsQuery(RelatedPermissionsQuery relatedPermissionsQuery) {
            this.relatedPermissionsQuery = relatedPermissionsQuery;
        }

        private List<Permission> getExistingPermissionsForParentRole() {
            Role role = (Role) relatedPermissionsQuery.getParent();

            return permissionDao.findByRole(role);
        }

        @Override
        public String getEntityCaption() {
            if (getEntity().getEntityType() == null) {
                return "Permission Form - New";
            } else {
                if (getEntity().getField() == null) {
                    return "Permission Form - " + getEntity().getEntityTypeLabel();
                } else {
                    return "Permission Form - " + getEntity().getEntityTypeLabel() + "." + getEntity().getFieldLabel();
                }
            }
        }
    }
}

