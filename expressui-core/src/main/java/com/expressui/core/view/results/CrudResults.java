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

package com.expressui.core.view.results;

import com.expressui.core.util.assertion.Assert;
import com.expressui.core.view.form.EntityForm;
import com.expressui.core.view.form.EntityFormWindow;
import com.expressui.core.view.form.ResultsConnectedEntityForm;
import com.expressui.core.view.menu.ActionContextMenu;
import com.vaadin.data.Property;
import com.vaadin.data.util.BeanItem;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Window;
import org.vaadin.dialogs.ConfirmDialog;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.persistence.EntityNotFoundException;
import java.util.Collection;

/**
 * Results with CRUD buttons to create, view, edit and delete selected items in the results.
 *
 * @param <T> type of entity in the results
 */
public abstract class CrudResults<T> extends Results<T> implements WalkableResults {

    @Resource
    private ActionContextMenu actionContextMenu;

    private Button newButton;
    private Button editButton;
    private Button viewButton;
    private Button deleteButton;

    private Object currentItemId;

    protected CrudResults() {
        super();
    }

    /**
     * Get the entity form used for viewing/editing items in the results.
     *
     * @return entity form
     */
    public abstract EntityForm<T> getEntityForm();

    @PostConstruct
    @Override
    public void postConstruct() {
        super.postConstruct();

        getResultsTable().setMultiSelect(true);
        HorizontalLayout crudButtons = new HorizontalLayout();
        setDebugId(crudButtons, "crudButtons");
        crudButtons.setMargin(false);
        crudButtons.setSpacing(true);

        newButton = new Button(uiMessageSource.getMessage("crudResults.new"), this, "create");
        newButton.setDescription(uiMessageSource.getToolTip("crudResults.new.toolTip"));
        newButton.setIcon(new ThemeResource("../expressui/icons/16/add.png"));
        newButton.addStyleName("small default");
        crudButtons.addComponent(newButton);

        viewButton = new Button(uiMessageSource.getMessage("crudResults.view"), this, "view");
        viewButton.setDescription(uiMessageSource.getToolTip("crudResults.view.toolTip"));
        viewButton.setIcon(new ThemeResource("../expressui/icons/16/view.png"));
        viewButton.setEnabled(false);
        viewButton.addStyleName("small default");
        crudButtons.addComponent(viewButton);

        editButton = new Button(uiMessageSource.getMessage("crudResults.edit"), this, "edit");
        editButton.setDescription(uiMessageSource.getToolTip("crudResults.edit.toolTip"));
        editButton.setIcon(new ThemeResource("../expressui/icons/16/edit.png"));
        editButton.setEnabled(false);
        editButton.addStyleName("small default");
        crudButtons.addComponent(editButton);

        deleteButton = new Button(uiMessageSource.getMessage("crudResults.delete"), this, "delete");
        deleteButton.setDescription(uiMessageSource.getToolTip("crudResults.delete.toolTip"));
        deleteButton.setIcon(new ThemeResource("../expressui/icons/16/delete.png"));
        deleteButton.setEnabled(false);
        deleteButton.addStyleName("small default");
        crudButtons.addComponent(deleteButton);

        getResultsTable().addListener(Property.ValueChangeEvent.class, this, "selectionChanged");
        actionContextMenu.addAction("crudResults.view", this, "view");
        actionContextMenu.addAction("crudResults.edit", this, "edit");
        actionContextMenu.addAction("crudResults.delete", this, "delete");

        applySecurityToCRUDButtons();
        getCrudButtons().addComponent(crudButtons, 0);
        getCrudButtons().setComponentAlignment(crudButtons, Alignment.MIDDLE_LEFT);

        getResultsTable().addListener(new DoubleClickListener());
        getEntityForm().addCancelListener(this, "search");
        getEntityForm().addCloseListener(this, "search");

        addCodePopupButtonIfEnabled(CrudResults.class);
    }

    @Override
    public void postWire() {
        super.postWire();
        getEntityForm().postWire();

    }

    @Override
    public void onDisplay() {
    }

    /**
     * Apply current security permissions to CRUD buttons so that they are enabled if and only if allowed.
     */
    public void applySecurityToCRUDButtons() {
        boolean hasViewableFields = !getEntityForm().getFormFieldSet().getViewableFormFields().isEmpty();

        boolean isViewAllowed = securityService.getCurrentUser().isViewAllowed(getType().getName())
                && hasViewableFields;
        viewButton.setVisible(isViewAllowed);

        boolean hasEditableFields = !getEntityForm().getFormFieldSet().getEditableFormFields().isEmpty();
        boolean isEditAllowed = securityService.getCurrentUser().isEditAllowed(getType().getName())
                && isViewAllowed && hasEditableFields;
        editButton.setVisible(isEditAllowed);

        newButton.setVisible(securityService.getCurrentUser().isCreateAllowed(getType().getName()));

        deleteButton.setVisible(securityService.getCurrentUser().isDeleteAllowed(getType().getName()));
    }

    /**
     * Create a new entity and open edit form to edit new entity
     */
    public void create() {
        getEntityForm().setViewMode(false);
        getEntityForm().applyViewMode();
        getEntityForm().create();
        EntityFormWindow entityFormWindow = EntityFormWindow.open(getEntityForm());
        entityFormWindow.addCloseListener(this, "search");
    }

    /**
     * View an entity and open form in read-only mode.
     */
    public void view() {
        getEntityForm().setViewMode(true);
        editOrView();
    }

    /**
     * Edit the selected entity and open edit form to edit selected entity
     */
    public void edit() {
        getEntityForm().setViewMode(false);
        editOrView();
    }

    private void editOrView() {
        Collection itemIds = (Collection) getResultsTable().getValue();
        Assert.PROGRAMMING.size(itemIds, 1);
        editOrView(itemIds.iterator().next());
    }

    private void editOrView(Object itemId) {
        loadItem(itemId);
        ResultsConnectedEntityForm resultsConnectedEntityForm = new ResultsConnectedEntityForm(getEntityForm(), this);
        EntityFormWindow entityFormWindow = EntityFormWindow.open(resultsConnectedEntityForm);
        entityFormWindow.addCloseListener(this, "search");
        if (!getEntityForm().getViewableToManyRelationships().isEmpty()) {
            entityFormWindow.setHeight("100%");
        }
    }

    private void loadItem(Object itemId) {
        loadItem(itemId, true);
    }

    private void loadItem(Object itemId, boolean selectFirstTab) throws EntityNotFoundException {
        try {
            getEntityForm().restoreIsReadOnly();
            currentItemId = itemId;
            getResultsTable().clearSelection();
            getResultsTable().select(currentItemId);
            BeanItem beanItem = getResultsTable().getContainerDataSource().getItem(itemId);
            getEntityForm().load((T) beanItem.getBean(), selectFirstTab);
        } finally {
            // in case entity is not found and exception occurs, still need to apply view mode
            getEntityForm().applyViewMode();
        }
    }

    @Override
    public void editOrViewPreviousItem() {
        Object previousItemId = getResultsTable().getContainerDataSource().prevItemId(currentItemId);
        if (previousItemId == null) {
            if (getEntityQuery().hasPreviousPage()) {
                getResultsTable().previousPage();
            } else {
                getResultsTable().lastPage();
            }
            previousItemId = getResultsTable().getContainerDataSource().lastItemId();
        }
        if (previousItemId != null) {
            try {
                loadItem(previousItemId, false);
            } catch (EntityNotFoundException e) { // may occur if entity has been deleted by another user
                editOrViewPreviousItem();
            }
        }
    }

    @Override
    public boolean hasPreviousItem() {
        Object previousItemId = getResultsTable().getContainerDataSource().prevItemId(currentItemId);
        return previousItemId != null || getEntityQuery().hasPreviousPage();
    }

    @Override
    public void editOrViewNextItem() {
        Object nextItemId = getResultsTable().getContainerDataSource().nextItemId(currentItemId);
        if (nextItemId == null) {
            if (getEntityQuery().hasNextPage()) {
                getResultsTable().nextPage();
            } else {
                getResultsTable().firstPage();
            }
            nextItemId = getResultsTable().getContainerDataSource().firstItemId();
        }

        if (nextItemId != null) {
            try {
                loadItem(nextItemId, false);
            } catch (EntityNotFoundException e) { // may occur if entity has been deleted by another user
                editOrViewNextItem();
            }
        }
    }

    @Override
    public boolean hasNextItem() {
        Object nextItemId = getResultsTable().getContainerDataSource().nextItemId(currentItemId);
        return nextItemId != null || getEntityQuery().hasNextPage();
    }

    private void deleteImpl() {
        Collection itemIds = (Collection) getResultsTable().getValue();
        for (Object itemId : itemIds) {
            BeanItem<T> beanItem = getResultsTable().getContainerDataSource().getItem(itemId);
            T entity = beanItem.getBean();
            preDelete(entity);
            if (getEntityDao() == null) {
                genericDao.remove(entity);
            } else {
                getEntityDao().remove(entity);
            }
        }

        showDeleteSuccessfulMessage();

        // solves tricky ConcurrentModification bug where ContextMenu handler calls delete
        // but then search removes handler
        searchImpl(false);
        deleteButton.setEnabled(false);
        editButton.setEnabled(false);
        viewButton.setEnabled(false);
    }

    /**
     * Show notification message that delete was successful.
     */
    public void showDeleteSuccessfulMessage() {
        Window.Notification notification = new Window.Notification(uiMessageSource.getMessage("crudResults.deleted"),
                Window.Notification.TYPE_HUMANIZED_MESSAGE);
        notification.setDelayMsec(Window.Notification.DELAY_NONE);
        notification.setPosition(Window.Notification.POSITION_CENTERED);
        getMainApplication().showNotification(notification);
    }

    /**
     * Delete selected entities. First, pops up confirmation dialog.
     */
    public void delete() {
        getMainApplication().showConfirmationDialog(
                new ConfirmDialog.Listener() {
                    public void onClose(ConfirmDialog dialog) {
                        if (dialog.isConfirmed()) {
                            deleteImpl();
                        }
                    }
                });
    }

    /**
     * Lifecycle listener method called before delete.
     *
     * @param entity entity that will be deleted
     */
    public void preDelete(T entity) {
    }

    /**
     * Listener method called when selection of result items changes.
     *
     * @param event ignored
     */
    public void selectionChanged(Property.ValueChangeEvent event) {
        Collection itemIds = (Collection) getSelectedValue();

        getResultsTable().turnOnContentRefreshing();

        boolean hasViewableFields = !getEntityForm().getFormFieldSet().getViewableFormFields().isEmpty();
        boolean isViewAllowed = securityService.getCurrentUser().isViewAllowed(getType().getName())
                && hasViewableFields;
        boolean isEditAllowed = securityService.getCurrentUser().isEditAllowed(getType().getName())
                && isViewAllowed;
        boolean isDeleteAllowed = securityService.getCurrentUser().isDeleteAllowed(getType().getName());

        if (itemIds.size() == 1) {
            actionContextMenu.setActionEnabled("crudResults.view", isViewAllowed);
            actionContextMenu.setActionEnabled("crudResults.edit", isEditAllowed);
            actionContextMenu.setActionEnabled("crudResults.delete", isDeleteAllowed);
            getResultsTable().removeActionHandler(actionContextMenu);
            getResultsTable().addActionHandler(actionContextMenu);
            editButton.setEnabled(isEditAllowed);
            viewButton.setEnabled(isViewAllowed);
            deleteButton.setEnabled(isDeleteAllowed);
        } else if (itemIds.size() > 1) {
            actionContextMenu.setActionEnabled("crudResults.view", false);
            actionContextMenu.setActionEnabled("crudResults.edit", false);
            actionContextMenu.setActionEnabled("crudResults.delete", isDeleteAllowed);
            getResultsTable().removeActionHandler(actionContextMenu);
            getResultsTable().addActionHandler(actionContextMenu);
            editButton.setEnabled(false);
            viewButton.setEnabled(false);
            deleteButton.setEnabled(isDeleteAllowed);
        } else {
            getResultsTable().removeActionHandler(actionContextMenu);
            editButton.setEnabled(false);
            viewButton.setEnabled(false);
            deleteButton.setEnabled(false);
        }
    }


    class DoubleClickListener implements ItemClickEvent.ItemClickListener {
        public void itemClick(ItemClickEvent event) {
            if (event.isDoubleClick()) {
                boolean hasViewableFields = !getEntityForm().getFormFieldSet().getViewableFormFields().isEmpty();
                boolean isViewAllowed = securityService.getCurrentUser().isViewAllowed(getType().getName())
                        && hasViewableFields;
                if (isViewAllowed) {
                    boolean isEditAllowed = securityService.getCurrentUser().isEditAllowed(getType().getName())
                            && isViewAllowed;
                    getEntityForm().setViewMode(!isEditAllowed);
                }

                editOrView(event.getItemId());
            }
        }
    }
}
