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

package com.expressui.core.view.security.role.related;

import com.expressui.core.dao.query.ToManyRelationshipQuery;
import com.expressui.core.dao.security.PermissionDao;
import com.expressui.core.dao.security.query.RelatedPermissionsQuery;
import com.expressui.core.entity.security.Permission;
import com.expressui.core.entity.security.Role;
import com.expressui.core.util.StringUtil;
import com.expressui.core.view.form.EntityForm;
import com.expressui.core.view.form.FormFieldSet;
import com.expressui.core.view.results.ResultsFieldSet;
import com.expressui.core.view.tomanyrelationship.ToManyCompositionRelationshipResults;
import com.expressui.core.view.tomanyrelationship.ToManyRelationship;
import com.vaadin.data.Property;
import com.vaadin.ui.Field;
import com.vaadin.ui.Select;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.beans.factory.config.BeanDefinition.SCOPE_PROTOTYPE;

/**
 * Component for viewing/editing permissions related to a role.
 */
@Component
@Scope(SCOPE_PROTOTYPE)
@SuppressWarnings({"rawtypes", "serial"})
public class RelatedPermissions extends ToManyRelationship<Permission> {

    @Resource
    private RelatedPermissionsResults relatedPermissionsResults;

    @Override
    public String getTypeCaption() {
        return "Permissions";
    }

    @Override
    public RelatedPermissionsResults getResults() {
        return relatedPermissionsResults;
    }

    @Component
    @Scope(SCOPE_PROTOTYPE)
    public static class RelatedPermissionsResults extends ToManyCompositionRelationshipResults<Permission> {

        @Resource
        private PermissionForm permissionForm;

        @Resource
        private RelatedPermissionsQuery relatedPermissionsQuery;

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
        public void init(ResultsFieldSet resultsFields) {
            resultsFields.setPropertyIds(
                    "targetType",
                    "field",
                    "permissions",
                    "lastModified",
                    "modifiedBy"
            );
        }

        @Override
        public String getChildPropertyId() {
            return "permissions";
        }

        @Override
        public String getParentPropertyId() {
            return "role";
        }
    }

    @Component
    @Scope(SCOPE_PROTOTYPE)
    public static class PermissionForm extends EntityForm<Permission> {

        @Resource
        private PermissionDao permissionDao;

        private RelatedPermissionsQuery relatedPermissionsQuery;

        @Override
        public void init(FormFieldSet formFields) {
            formFields.setCoordinates("targetType", 1, 1);
            formFields.setCoordinates("targetTypeLabel", 1, 2);

            formFields.setCoordinates("field", 2, 1);
            formFields.setCoordinates("fieldLabel", 2, 2);

            formFields.setCoordinates("viewAllowed", 3, 1);
            formFields.setCoordinates("editAllowed", 3, 2);

            formFields.setCoordinates("createAllowed", 4, 1);
            formFields.setCoordinates("deleteAllowed", 4, 2);

            formFields.setField("targetType", new Select());
            formFields.addValueChangeListener("targetType", this, "targetTypeChanged");

            formFields.setField("field", new Select());

            formFields.addValueChangeListener("viewAllowed", this, "syncCRUDCheckboxes");
            formFields.addValueChangeListener("field", this, "syncCRUDCheckboxes");
            formFields.addValueChangeListener("field", this, "syncIsRequiredIndicator");
        }

        public void syncCRUDCheckboxes(Property.ValueChangeEvent event) {
            Field viewField = getFormFieldSet().getFormField("viewAllowed").getField();
            Boolean isViewChecked = (Boolean) viewField.getValue();

            Field fieldField = getFormFieldSet().getFormField("field").getField();
            Boolean isFieldSelected = fieldField.getValue() != null;

            String targetType = (String) getFormFieldSet().getFormField("targetType").getField().getValue();

            boolean hasProperties = !StringUtil.isEmpty(targetType) && !labelRegistry.getPropertyIds(targetType).isEmpty();

            getFormFieldSet().setEnabled("field", hasProperties);

            getFormFieldSet().setEnabled("createAllowed", hasProperties && isViewChecked && !isFieldSelected);
            getFormFieldSet().setEnabled("editAllowed", hasProperties && isViewChecked);
            getFormFieldSet().setEnabled("deleteAllowed", hasProperties && isViewChecked && !isFieldSelected);
        }

        @Override
        public void postWire() {
            super.postWire();

            getFormFieldSet().setSelectItems("targetType", getTargetTypeItems());
        }

        @Override
        public void create() {
            super.create();

            getFormFieldSet().setSelectItems("targetType", getTargetTypeItems());
            syncIsRequiredIndicator(null);
//            getFormFields().setRequired("field", false);
//            syncTabAndSaveButtonErrors();
        }


        @Override
        public void refresh() {
            super.refresh();

            getFormFieldSet().setSelectItems("targetType", getTargetTypeItems());
            syncIsRequiredIndicator(null);
        }

        @Override
        public void load(Permission entity, boolean selectFirstTab) {
            super.load(entity, selectFirstTab);

            getFormFieldSet().setSelectItems("targetType", getTargetTypeItems());
            syncIsRequiredIndicator(null);
        }

        private Map<Object, String> getTargetTypeItems() {
            Map<Object, String> targetTypeItems = new LinkedHashMap<Object, String>();
            List<Permission> existingPermissions = getExistingPermissionsForParentRole();

            Map<String, String> targetTypes = labelRegistry.getTypeLabels();
            for (String targetType : targetTypes.keySet()) {
                boolean isTargetTypeWithNullFieldAvailable = true;
                for (Permission existingPermission : existingPermissions) {
                    if (!existingPermission.equals(getEntity()) && existingPermission.getTargetType().equals(targetType)
                            && existingPermission.getField() == null) {
                        isTargetTypeWithNullFieldAvailable = false;
                        break;
                    }
                }

                Map<Object, String> fieldItems = getFieldItems(targetType);
                if (!fieldItems.isEmpty() || isTargetTypeWithNullFieldAvailable) {
                    targetTypeItems.put(targetType, targetType);
                }
            }

            return targetTypeItems;
        }

        public void targetTypeChanged(Property.ValueChangeEvent event) {
            String newTargetType = (String) event.getProperty().getValue();

            if (newTargetType != null) {
                Map<Object, String> fieldItems = getFieldItems(newTargetType);
                getFormFieldSet().setSelectItems("field", fieldItems, "None");

                syncCRUDCheckboxes(null);

                syncIsRequiredIndicator(null);
            }
        }

        public void syncIsRequiredIndicator(Property.ValueChangeEvent event) {
            getFormFieldSet().setRequired("field", anotherPermissionHasNullField(getEntity().getTargetType()));
            syncTabAndSaveButtonErrors();
        }

        private boolean anotherPermissionHasNullField(String targetType) {
            List<Permission> existingPermissions = getExistingPermissionsForParentRole();
            for (Permission existingPermission : existingPermissions) {
                if (existingPermission.getTargetType().equals(targetType) && existingPermission.getField() == null
                        && !existingPermission.equals(getEntity())) {
                    return true;
                }
            }

            return false;
        }

        private Map<Object, String> getFieldItems(String targetType) {
            List<Permission> existingPermissions = getExistingPermissionsForParentRole();

            Map<Object, String> fieldItems = labelRegistry.getPropertyIds(targetType);
            for (Permission existingPermission : existingPermissions) {
                if (existingPermission.getTargetType().equals(targetType) && existingPermission.getField() != null
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
            Role role = relatedPermissionsQuery.getParent();

            return permissionDao.findByRole(role);
        }

        @Override
        public String getTypeCaption() {
            if (getEntity().getTargetType() == null) {
                return "Permission Form - New";
            } else {
                if (getEntity().getField() == null) {
                    return "Permission Form - " + getEntity().getTargetTypeLabel();
                } else {
                    return "Permission Form - " + getEntity().getTargetTypeLabel() + "." + getEntity().getFieldLabel();
                }
            }
        }
    }
}

