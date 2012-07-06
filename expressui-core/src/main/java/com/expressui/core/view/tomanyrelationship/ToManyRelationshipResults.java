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

package com.expressui.core.view.tomanyrelationship;

import com.expressui.core.dao.query.ToManyRelationshipQuery;
import com.expressui.core.util.BeanPropertyType;
import com.expressui.core.util.CollectionsUtil;
import com.expressui.core.util.assertion.Assert;
import com.expressui.core.view.menu.ActionContextMenu;
import com.expressui.core.view.results.Results;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Window;
import org.apache.commons.beanutils.PropertyUtils;
import org.vaadin.dialogs.ConfirmDialog;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;

/**
 * Results containing entities in a to-many relationship.
 *
 * @param <T> type of the entities in the results
 */
public abstract class ToManyRelationshipResults<T> extends Results<T> {

    @Resource
    ActionContextMenu actionContextMenu;

    HorizontalLayout crudButtons;

    private Button addButton;
    Button removeButton;

    private boolean isViewMode;

    ToManyRelationshipResults() {
        super();
    }

    @Override
    public String getTypeCaption() {
        return null;
    }

    /**
     * Get the property id in the parent entity for referencing the child entity
     *
     * @return child property id
     */
    public abstract String getChildPropertyId();

    /**
     * Get the property id in the child entity for referencing the parent entity
     *
     * @return parent property id
     */
    public abstract String getParentPropertyId();

    /**
     * Get the entity query that generates these results.
     *
     * @return entity query
     */
    @Override
    public abstract ToManyRelationshipQuery getEntityQuery();

    @PostConstruct
    @Override
    public void postConstruct() {
        super.postConstruct();

        crudButtons = new HorizontalLayout();
        setDebugId(crudButtons, "crudButtons");
        crudButtons.setMargin(false);
        crudButtons.setSpacing(true);

        addButton = new Button(uiMessageSource.getMessage("toManyRelationshipResults.add"), this, "add");
        addButton.setDescription(uiMessageSource.getToolTip("toManyRelationshipResults.add.toolTip"));
        addButton.setIcon(new ThemeResource("../expressui/icons/16/add.png"));
        addButton.addStyleName("small default");
        crudButtons.addComponent(addButton);

        removeButton = new Button(uiMessageSource.getMessage("toManyRelationshipResults.remove"), this, "remove");
        removeButton.setDescription(uiMessageSource.getToolTip("toManyRelationshipResults.remove.toolTip"));
        removeButton.setIcon(new ThemeResource("../expressui/icons/16/delete.png"));
        removeButton.setEnabled(false);
        removeButton.addStyleName("small default");
        crudButtons.addComponent(removeButton);

        getCrudButtons().addComponent(crudButtons, 0);
        getCrudButtons().setComponentAlignment(crudButtons, Alignment.MIDDLE_LEFT);

        getResultsTable().setMultiSelect(true);

        actionContextMenu.addAction("toManyRelationshipResults.remove", this, "remove");
        addSelectionChangedListener(this, "selectionChanged");

        addCodePopupButtonIfEnabled(ToManyRelationshipResults.class);
    }

    @Override
    public void onDisplay() {
    }

    /**
     * Invoked when user clicks to add an entity to the to-many relationship. Implementation should
     * take appropriate action, depending on type of relationships, aggregation, composition.
     */
    public abstract void add();

    /**
     * Set references in given values to the parent and then persists all values.
     *
     * @param values values in which to set reference
     */
    public void setReferencesToParentAndPersist(T... values) {
        for (T value : values) {
            T referenceValue = genericDao.getReference(value);
            setReferenceToParent(referenceValue);
            if (getEntityDao() == null) {
                genericDao.persist(referenceValue);
            } else {
                getEntityDao().persist(referenceValue);
            }
        }
        searchImpl(false);
    }

    /**
     * Set reference inside single given value to the parent in the to-many relationship.
     *
     * @param value value in which to set reference
     */
    public void setReferenceToParent(T value) {
        try {
            BeanPropertyType beanPropertyType = BeanPropertyType.getBeanPropertyType(getType(), getParentPropertyId());
            Assert.PROGRAMMING.isTrue(!beanPropertyType.isCollectionType(),
                    "Parent property id (" + getType() + "." + getParentPropertyId() + ") must not be a collection type");
            PropertyUtils.setProperty(value, getParentPropertyId(), getEntityQuery().getParent());
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Get the type of the parent entity in this relationship
     *
     * @return type of parent entity
     */
    public Class getParentEntityType() {
        BeanPropertyType beanPropertyType = BeanPropertyType.getBeanPropertyType(getType(), getParentPropertyId());
        return beanPropertyType.getType();
    }

    /**
     * Set this results component to read-only or writable.
     *
     * @param isReadOnly true if read only
     */
    public void setReadOnly(boolean isReadOnly) {
        super.setReadOnly(isReadOnly);
        addButton.setVisible(!isReadOnly);
        removeButton.setVisible(!isReadOnly);
        if (isReadOnly) {
            actionContextMenu.setActionEnabled("toManyRelationshipResults.remove", false);
            getResultsTable().removeActionHandler(actionContextMenu);
        } else {
            actionContextMenu.setActionEnabled("toManyRelationshipResults.remove", true);
            getResultsTable().addActionHandler(actionContextMenu);
        }
    }

    /**
     * Reads current security rules and applies them to this results component, i.e. making CRUD buttons (in)visible
     */
    public void applySecurityIsEditable() {
        boolean isEditable = securityService.getCurrentUser().isEditAllowed(getParentEntityType().getName(), getChildPropertyId());
        addButton.setVisible(isEditable);
        removeButton.setVisible(isEditable);

        if (isEditable) {
            actionContextMenu.setActionEnabled("toManyRelationshipResults.remove", true);
            getResultsTable().addActionHandler(actionContextMenu);
        } else {
            actionContextMenu.setActionEnabled("toManyRelationshipResults.remove", false);
            getResultsTable().removeActionHandler(actionContextMenu);
        }

        selectionChanged();
    }

    /**
     * Invoked when user clicks action to remove selected entities.
     */
    public void remove() {
        getMainApplication().showConfirmationDialog(
                new ConfirmDialog.Listener() {
                    public void onClose(ConfirmDialog dialog) {
                        if (dialog.isConfirmed()) {
                            Collection<T> selectedValues = getSelectedValues();
                            removeConfirmed(CollectionsUtil.toArray(getType(), selectedValues));
                        }
                    }
                });
    }

    /**
     * Invoked when user confirms that she really wants to remove values.
     *
     * @param values values to be removed
     */
    public void removeConfirmed(T... values) {
        for (T value : values) {
            value = genericDao.getReference(value);
            try {
                PropertyUtils.setProperty(value, getParentPropertyId(), null);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            } catch (InvocationTargetException e) {
                throw new RuntimeException(e);
            } catch (NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
            if (getEntityDao() == null) {
                genericDao.persist(value);
            } else {
                getEntityDao().persist(value);
            }
        }
        showRemoveSuccessfulMessage();
        searchImpl(false);
        removeButton.setEnabled(false);
    }

    protected void showAddSuccessfulMessage() {
        Window.Notification notification = new Window.Notification(
                uiMessageSource.getMessage("toManyRelationshipResults.added"),
                Window.Notification.TYPE_HUMANIZED_MESSAGE);
        notification.setDelayMsec(Window.Notification.DELAY_NONE);
        notification.setPosition(Window.Notification.POSITION_CENTERED);
        getMainApplication().showNotification(notification);
    }

    protected void showRemoveSuccessfulMessage() {
        Window.Notification notification = new Window.Notification(
                uiMessageSource.getMessage("toManyRelationshipResults.removed"),
                Window.Notification.TYPE_HUMANIZED_MESSAGE);
        notification.setDelayMsec(Window.Notification.DELAY_NONE);
        notification.setPosition(Window.Notification.POSITION_CENTERED);
        getMainApplication().showNotification(notification);
    }

    /**
     * Invoked whenever user changes selection of rows in results table.
     */
    public void selectionChanged() {
        boolean isEditable = securityService.getCurrentUser().isEditAllowed(getParentEntityType().getName(), getChildPropertyId());

        getResultsTable().turnOnContentRefreshing();

        Collection itemIds = (Collection) getResultsTable().getValue();
        if (itemIds.size() > 0 && isEditable && !isViewMode()) {
            actionContextMenu.setActionEnabled("toManyRelationshipResults.remove", true);
            getResultsTable().addActionHandler(actionContextMenu);
            removeButton.setEnabled(true);
        } else {
            actionContextMenu.setActionEnabled("toManyRelationshipResults.remove", false);
            getResultsTable().removeActionHandler(actionContextMenu);
            removeButton.setEnabled(false);
        }
    }

    /**
     * Ask if this component is in view-only mode.
     *
     * @return true if in view-only mode
     */
    public boolean isViewMode() {
        return isViewMode;
    }

    /**
     * Set whether or not this component is in view-only mode.
     *
     * @param viewMode true to set in view-only mode
     */
    public void setViewMode(boolean viewMode) {
        isViewMode = viewMode;
    }
}
