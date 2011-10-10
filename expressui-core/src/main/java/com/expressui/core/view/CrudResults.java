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

package com.expressui.core.view;

import com.expressui.core.MainApplication;
import com.expressui.core.entity.WritableEntity;
import com.expressui.core.security.SecurityService;
import com.expressui.core.util.assertion.Assert;
import com.expressui.core.view.util.MessageSource;
import com.expressui.core.view.menu.ActionContextMenu;
import com.vaadin.data.Property;
import com.vaadin.data.util.BeanItem;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import org.vaadin.dialogs.ConfirmDialog;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.Collection;

/**
 * Results with CRUD buttons to create, view, edit and delete items in the results.
 *
 * @param <T> type of entity in the results
 */
public abstract class CrudResults<T> extends Results<T> implements WalkableResults {

    @Resource(name = "uiMessageSource")
    private MessageSource uiMessageSource;

    @Resource
    private ActionContextMenu actionContextMenu;

    @Resource
    private SecurityService securityService;

    private Button newButton;
    private Button editButton;
    private Button viewButton;
    private Button deleteButton;

    private Object currentItemId;
    private int previousSelectionCount;

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
        crudButtons.setMargin(false);
        crudButtons.setSpacing(true);

        newButton = new Button(uiMessageSource.getMessage("entityResults.new"), this, "create");
        newButton.setDescription(uiMessageSource.getMessage("entityResults.new.description"));
        newButton.setIcon(new ThemeResource("icons/16/add.png"));
        newButton.addStyleName("small default");
        crudButtons.addComponent(newButton);

        viewButton = new Button(uiMessageSource.getMessage("entityResults.view"), this, "view");
        viewButton.setDescription(uiMessageSource.getMessage("entityResults.view.description"));
        viewButton.setIcon(new ThemeResource("icons/16/view.png"));
        viewButton.setEnabled(false);
        viewButton.addStyleName("small default");
        crudButtons.addComponent(viewButton);

        editButton = new Button(uiMessageSource.getMessage("entityResults.edit"), this, "edit");
        editButton.setDescription(uiMessageSource.getMessage("entityResults.edit.description"));
        editButton.setIcon(new ThemeResource("icons/16/edit.png"));
        editButton.setEnabled(false);
        editButton.addStyleName("small default");
        crudButtons.addComponent(editButton);

        deleteButton = new Button(uiMessageSource.getMessage("entityResults.delete"), this, "delete");
        deleteButton.setDescription(uiMessageSource.getMessage("entityResults.delete.description"));
        deleteButton.setIcon(new ThemeResource("icons/16/delete.png"));
        deleteButton.setEnabled(false);
        deleteButton.addStyleName("small default");
        crudButtons.addComponent(deleteButton);

        getResultsTable().addListener(Property.ValueChangeEvent.class, this, "selectionChanged");
//        addSelectionChangedListener(this, "selectionChanged");
        actionContextMenu.addAction("entityResults.view", this, "view");
        actionContextMenu.addAction("entityResults.edit", this, "edit");
        actionContextMenu.addAction("entityResults.delete", this, "delete");

        applySecurityToCRUDButtons();
        getCrudButtons().addComponent(crudButtons, 0);
        getCrudButtons().setComponentAlignment(crudButtons, Alignment.MIDDLE_LEFT);

        getResultsTable().addListener(new DoubleClickListener());
        getEntityForm().addCancelListener(this, "search");
        getEntityForm().addCloseListener(this, "search");
    }

    @Override
    public void postWire() {
        super.postWire();
        getEntityForm().postWire();

    }

    /**
     * Apply current security permissions to CRUD buttons so that they are enabled if and only if allowed.
     */
    public void applySecurityToCRUDButtons() {
        boolean hasViewableFields = !getEntityForm().getFormFields().getViewableFormFields().isEmpty();

        boolean isViewAllowed = securityService.getCurrentUser().isViewAllowed(getEntityType().getName())
                && hasViewableFields;
        viewButton.setVisible(isViewAllowed);

        boolean hasEditableFields = !getEntityForm().getFormFields().getEditableFormFields().isEmpty();
        boolean isEditAllowed = securityService.getCurrentUser().isEditAllowed(getEntityType().getName())
                && isViewAllowed && hasEditableFields;
        editButton.setVisible(isEditAllowed);

        newButton.setVisible(securityService.getCurrentUser().isCreateAllowed(getEntityType().getName())
            && isEditAllowed);

        deleteButton.setVisible(securityService.getCurrentUser().isDeleteAllowed(getEntityType().getName()));
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
        Assert.PROGRAMMING.assertTrue(itemIds.size() == 1);
        editOrView(itemIds.iterator().next());
    }

    private void editOrView(Object itemId) {
        loadItem(itemId);
        ResultsConnectedEntityForm resultsConnectedEntityForm = new ResultsConnectedEntityForm(getEntityForm(), this);
        EntityFormWindow entityFormWindow = EntityFormWindow.open(resultsConnectedEntityForm);
        entityFormWindow.addCloseListener(this, "search");
        if (!getEntityForm().getViewableToManyRelationships().isEmpty()) {
            entityFormWindow.setHeight("95%");
        }
    }

    private void loadItem(Object itemId) {
        loadItem(itemId, true);
    }

    private void loadItem(Object itemId, boolean selectFirstTab) {

        getEntityForm().restoreIsReadOnly();

        currentItemId = itemId;
        BeanItem beanItem = getResultsTable().getContainerDataSource().getItem(itemId);
        getEntityForm().load((WritableEntity) beanItem.getBean(), selectFirstTab);

        getEntityForm().applyViewMode();
    }

    @Override
    public void editOrViewPreviousItem() {
        Object previousItemId = getResultsTable().getContainerDataSource().prevItemId(currentItemId);
        if (previousItemId == null && getEntityQuery().hasPreviousPage()) {
            getResultsTable().previousPage();
            previousItemId = getResultsTable().getContainerDataSource().lastItemId();
        }
        if (previousItemId != null) {
            loadItem(previousItemId, false);
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
        if (nextItemId == null && getEntityQuery().hasNextPage()) {
            getResultsTable().nextPage();
            nextItemId = getResultsTable().getContainerDataSource().firstItemId();
        }

        if (nextItemId != null) {
            loadItem(nextItemId, false);
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
            getEntityDao().remove(entity);
        }

        // solves tricky ConcurrentModification bug where ContextMenu handler calls delete
        // but then search removes handler
        searchImpl(false);
        deleteButton.setEnabled(false);
        editButton.setEnabled(false);
        viewButton.setEnabled(false);
    }

    /**
     * Delete selected entities. First, pops up confirmation dialog.
     */
    public void delete() {
        ConfirmDialog.show(MainApplication.getInstance().getMainWindow(),
                uiMessageSource.getMessage("entityResults.confirmationCaption"),
                uiMessageSource.getMessage("entityResults.confirmationPrompt"),
                uiMessageSource.getMessage("entityResults.confirmationYes"),
                uiMessageSource.getMessage("entityResults.confirmationNo"),
                new ConfirmDialog.Listener() {
                    public void onClose(ConfirmDialog dialog) {
                        if (dialog.isConfirmed()) {
                            deleteImpl();
                        }
                    }
                });
    }

    public void selectionChanged(Property.ValueChangeEvent event) {
        Collection itemIds = (Collection) getSelectedValue();

        if (itemIds.size() == previousSelectionCount) {
            return;
        } else {
            previousSelectionCount = itemIds.size();
        }

        boolean hasViewableFields = !getEntityForm().getFormFields().getViewableFormFields().isEmpty();
        boolean isViewAllowed = securityService.getCurrentUser().isViewAllowed(getEntityType().getName())
                && hasViewableFields;
        boolean isEditAllowed = securityService.getCurrentUser().isEditAllowed(getEntityType().getName())
                && isViewAllowed;
        boolean isDeleteAllowed = securityService.getCurrentUser().isDeleteAllowed(getEntityType().getName());

        if (itemIds.size() == 1) {
            actionContextMenu.setActionEnabled("entityResults.view", isViewAllowed);
            actionContextMenu.setActionEnabled("entityResults.edit", isEditAllowed);
            actionContextMenu.setActionEnabled("entityResults.delete", isDeleteAllowed);
            getResultsTable().removeActionHandler(actionContextMenu);
            getResultsTable().addActionHandler(actionContextMenu);
            editButton.setEnabled(isEditAllowed);
            viewButton.setEnabled(isViewAllowed);
            deleteButton.setEnabled(isDeleteAllowed);
        } else if (itemIds.size() > 1) {
            actionContextMenu.setActionEnabled("entityResults.view", false);
            actionContextMenu.setActionEnabled("entityResults.edit", false);
            actionContextMenu.setActionEnabled("entityResults.delete", isDeleteAllowed);
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
                getEntityForm().setViewMode(false);
                editOrView(event.getItemId());
            }
        }
    }
}
