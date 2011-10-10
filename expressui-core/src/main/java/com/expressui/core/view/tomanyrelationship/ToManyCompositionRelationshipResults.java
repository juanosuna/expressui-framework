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

package com.expressui.core.view.tomanyrelationship;

import com.expressui.core.entity.WritableEntity;
import com.expressui.core.security.SecurityService;
import com.expressui.core.util.assertion.Assert;
import com.expressui.core.view.EntityForm;
import com.expressui.core.view.EntityFormWindow;
import com.expressui.core.view.ResultsConnectedEntityForm;
import com.expressui.core.view.WalkableResults;
import com.vaadin.data.util.BeanItem;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.Button;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.Collection;

public abstract class ToManyCompositionRelationshipResults<T> extends ToManyRelationshipResults<T> implements WalkableResults {

    public abstract EntityForm<T> getEntityForm();

    private Button editButton;

    private Object currentItemId;

    private ResultsConnectedEntityForm resultsConnectedEntityForm;
    private int previousSelectionCount;

    @Resource
    private SecurityService securityService;

    @PostConstruct
    @Override
    public void postConstruct() {
        super.postConstruct();

        editButton = new Button(uiMessageSource.getMessage("entityResults.edit"), this, "edit");
        editButton.setDescription(uiMessageSource.getMessage("entityResults.edit.description"));
        editButton.setIcon(new ThemeResource("icons/16/edit.png"));
        editButton.setEnabled(false);
        editButton.addStyleName("small default");
        crudButtons.addComponent(editButton, 1);

        actionContextMenu.addAction("entityResults.edit", this, "edit");

        getResultsTable().addListener(new DoubleClickListener());
        resultsConnectedEntityForm = new ResultsConnectedEntityForm(getEntityForm(), this);
    }

    @Override
    public void postWire() {
        super.postWire();

        getEntityForm().postWire();
    }

    @Override
    public void applySecurityIsEditable() {
        super.applySecurityIsEditable();
        boolean isEditable = securityService.getCurrentUser().isEditAllowed(getParentEntityType().getName());
        editButton.setVisible(isEditable);

        selectionChanged();
    }

    @Override
    public void setReadOnly(boolean isReadOnly) {
        super.setReadOnly(isReadOnly);
        editButton.setVisible(!isReadOnly);

        selectionChanged();
    }

    @Override
    public void add() {
        getEntityForm().create();
        EntityFormWindow entityFormWindow = EntityFormWindow.open(getEntityForm());
        entityFormWindow.addCloseListener(this, "search");

        T value = getEntityForm().getEntity();
        setReferenceToParent(value);
    }

    public void edit() {
        Collection itemIds = (Collection) getResultsTable().getValue();
        Assert.PROGRAMMING.assertTrue(itemIds.size() == 1);
        editImpl(itemIds.iterator().next());
    }

    public void editImpl(Object itemId) {
        loadItem(itemId);
        EntityFormWindow entityFormWindow = EntityFormWindow.open(resultsConnectedEntityForm);
        entityFormWindow.addCloseListener(this, "search");
        if (!getEntityForm().getViewableToManyRelationships().isEmpty()) {
            entityFormWindow.setHeight("95%");
        }
    }

    public void loadItem(Object itemId) {
        loadItem(itemId, true);
    }

    public void loadItem(Object itemId, boolean selectFirstTab) {
        currentItemId = itemId;
        BeanItem beanItem = getResultsTable().getContainerDataSource().getItem(itemId);
        getEntityForm().load((WritableEntity) beanItem.getBean(), selectFirstTab);
    }

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

    public boolean hasPreviousItem() {
        Object previousItemId = getResultsTable().getContainerDataSource().prevItemId(currentItemId);
        return previousItemId != null || getEntityQuery().hasPreviousPage();
    }

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

    public boolean hasNextItem() {
        Object nextItemId = getResultsTable().getContainerDataSource().nextItemId(currentItemId);
        return nextItemId != null || getEntityQuery().hasNextPage();
    }

    @Override
    public void valuesRemoved(T... values) {
        for (T value : values) {
            getEntityDao().remove(value);
        }

        searchImpl(false);
        editButton.setEnabled(false);
        removeButton.setEnabled(false);
    }

    @Override
    public void selectionChanged() {
        Collection itemIds = (Collection) getSelectedValue();

        if (itemIds.size() == previousSelectionCount && !itemIds.isEmpty()) {
            return;
        } else {
            previousSelectionCount = itemIds.size();
        }

        boolean isParentPropertyEditable = securityService.getCurrentUser().isEditAllowed(getParentEntityType().getName(),
                getChildPropertyId());
        boolean isEditAllowed = securityService.getCurrentUser().isEditAllowed(getEntityType().getName());

        if (itemIds.size() == 1) {
            actionContextMenu.setActionEnabled("entityResults.edit", isEditAllowed && !isViewMode());
            actionContextMenu.setActionEnabled("entityResults.remove", isParentPropertyEditable && !isViewMode());
            getResultsTable().removeActionHandler(actionContextMenu);
            getResultsTable().addActionHandler(actionContextMenu);
            editButton.setEnabled(true);
            removeButton.setEnabled(true);
        } else if (itemIds.size() > 1) {
            actionContextMenu.setActionEnabled("entityResults.edit", false);
            actionContextMenu.setActionEnabled("entityResults.remove", isParentPropertyEditable && !isViewMode());
            getResultsTable().removeActionHandler(actionContextMenu);
            getResultsTable().addActionHandler(actionContextMenu);
            editButton.setEnabled(false);
            removeButton.setEnabled(true);
        } else {
            actionContextMenu.setActionEnabled("entityResults.edit", false);
            actionContextMenu.setActionEnabled("entityResults.remove", false);
            getResultsTable().removeActionHandler(actionContextMenu);
            editButton.setEnabled(false);
            removeButton.setEnabled(false);
        }
    }

    public class DoubleClickListener implements ItemClickEvent.ItemClickListener {
        public void itemClick(ItemClickEvent event) {
            if (event.isDoubleClick()) {
                editImpl(event.getItemId());
            }
        }
    }
}
