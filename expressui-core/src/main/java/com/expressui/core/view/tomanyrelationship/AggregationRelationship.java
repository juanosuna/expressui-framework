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

import com.expressui.core.util.CollectionsUtil;
import com.expressui.core.view.entityselect.EntitySelect;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import org.apache.commons.beanutils.PropertyUtils;
import org.vaadin.dialogs.ConfirmDialog;

import javax.annotation.PostConstruct;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;

/**
 * Results containing entities in a to-many aggregation relationship.
 *
 * @param <T> type of the entities in the results
 */
public abstract class AggregationRelationship<T> extends ToManyRelationship<T> {

    private HorizontalLayout crudButtons;
    private Button addButton;
    private Button removeButton;

    /**
     * Gets the EntitySelect component used to select an entity to add to the relationship.
     *
     * @return EntitySelect component
     */
    public abstract EntitySelect<T> getEntitySelect();

    @PostConstruct
    @Override
    public void postConstruct() {
        super.postConstruct();

        crudButtons = new HorizontalLayout();
        setDebugId(crudButtons, "crudButtons");
        crudButtons.setMargin(false, true, false, false);
        crudButtons.setSpacing(true);

        addButton = new Button(uiMessageSource.getMessage("aggregationRelationship.add"), this, "add");
        addButton.setDescription(uiMessageSource.getToolTip("aggregationRelationship.add.toolTip"));
        addButton.setIcon(new ThemeResource("../expressui/icons/16/add.png"));
        addButton.addStyleName("small default");
        crudButtons.addComponent(addButton);

        removeButton = new Button(uiMessageSource.getMessage("aggregationRelationship.remove"), this, "remove");
        removeButton.setDescription(uiMessageSource.getToolTip("aggregationRelationship.remove.toolTip"));
        removeButton.setIcon(new ThemeResource("../expressui/icons/16/delete.png"));
        removeButton.addStyleName("small default");
        crudButtons.addComponent(removeButton);

        getCrudButtons().addComponent(crudButtons, 0);
        getCrudButtons().setComponentAlignment(crudButtons, Alignment.MIDDLE_LEFT);

        actionContextMenu.addAction("aggregationRelationship.remove", this, "remove");
    }

    @Override
    protected void addCodePopupButtonIfEnabledForCrudResults() {
        addCodePopupButtonIfEnabled(AggregationRelationship.class);
    }

    @Override
    public void postWire() {
        super.postWire();
        getEntitySelect().postWire();
        getEntitySelect().getResults().addSelectActionListener(this, "itemsSelected");
    }

    /**
     * Automatically invoked when user clicks to add an entity to the to-many relationship.
     */
    public void add() {
        preAdd();
        EntitySelect<T> entitySelect = getEntitySelect();
        entitySelect.setMultiSelect(true);
        getEntitySelect().open();
    }

    protected void preAdd() {
    }

    /**
     * Automatically invoked when user clicks action to remove selected entities.
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
            preRemove(value);
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
        showRemoveSuccessful();
        searchImpl(false);

        clearSelection();
        syncCrudActions();
    }

    /**
     * Shows confirmation message the items were successfully removed.
     */
    public void showRemoveSuccessful() {
        getMainApplication().showMessage(uiMessageSource.getMessage("aggregationRelationship.removed"));
    }

    /**
     * Lifecycle listener method called before remove.
     *
     * @param entity entity that will be removed
     */
    public void preRemove(T entity) {
    }

    @Override
    public void syncCrudActions() {
        super.syncCrudActions();

        crudButtons.setVisible(!isViewMode());

        addButton.setEnabled(
                getCurrentUser().isEditAllowed(getParentEntityType().getName(), getChildPropertyId()));

        Collection itemIds = (Collection) getSelectedValue();
        getResultsTable().turnOnContentRefreshing();
        int selectionItemsCount = itemIds.size();

        if (selectionItemsCount > 0) {
            actionContextMenu.setActionEnabled("aggregationRelationship.remove",
                    getCurrentUser().isEditAllowed(getParentEntityType().getName(), getChildPropertyId())
                            && !isViewMode());
            getResultsTable().addActionHandler(actionContextMenu);
            removeButton.setEnabled(
                    getCurrentUser().isEditAllowed(getParentEntityType().getName(), getChildPropertyId()));
        } else {
            actionContextMenu.setActionEnabled("aggregationRelationship.remove", false);
            getResultsTable().removeActionHandler(actionContextMenu);
            removeButton.setEnabled(false);
        }
    }

    @Override
    public boolean isViewAllowed() {
        return super.isViewAllowed() &&
                getCurrentUser().isViewAllowed(getParentEntityType().getName(), getChildPropertyId());
    }

    @Override
    public boolean isEditAllowed() {
        return super.isEditAllowed() &&
                getCurrentUser().isEditAllowed(getParentEntityType().getName(), getChildPropertyId());
    }

    @Override
    public boolean isCreateAllowed() {
        return super.isCreateAllowed()
                && getCurrentUser().isEditAllowed(getParentEntityType().getName(), getChildPropertyId());
    }

    @Override
    public boolean isDeleteAllowed() {
        return super.isDeleteAllowed()
                && getCurrentUser().isEditAllowed(getParentEntityType().getName(), getChildPropertyId());
    }

    /**
     * Invoked when user selects entities to be added to the relationship.
     */
    public void itemsSelected() {
        getEntitySelect().close();
        Collection<T> selectedValues = getEntitySelect().getResults().getSelectedValues();
        setReferencesToParentAndPersist((T[]) selectedValues.toArray());
        showAddSuccessful();
    }

    /**
     * Shows message indicating that add was successful.
     */
    public void showAddSuccessful() {
        getMainApplication().showMessage(uiMessageSource.getMessage("aggregationRelationship.added"));
    }
}
