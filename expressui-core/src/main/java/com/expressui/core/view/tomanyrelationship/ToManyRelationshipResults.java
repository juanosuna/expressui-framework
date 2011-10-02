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

import com.expressui.core.MainApplication;
import com.expressui.core.dao.ToManyRelationshipQuery;
import com.expressui.core.security.SecurityService;
import com.expressui.core.util.BeanPropertyType;
import com.expressui.core.util.assertion.Assert;
import com.expressui.core.view.util.MessageSource;
import com.expressui.core.view.Results;
import com.expressui.core.view.menu.ActionContextMenu;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import org.apache.commons.beanutils.PropertyUtils;
import org.vaadin.dialogs.ConfirmDialog;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;

public abstract class ToManyRelationshipResults<T> extends Results<T> {

    @Resource(name = "uiMessageSource")
    protected MessageSource uiMessageSource;

    @Resource
    protected ActionContextMenu actionContextMenu;

    @Resource
    private SecurityService securityService;

    protected HorizontalLayout crudButtons;

    private Button addButton;
    protected Button removeButton;

    protected ToManyRelationshipResults() {
        super();
    }

    public abstract String getEntityCaption();

    public abstract String getChildPropertyId();

    public abstract String getParentPropertyId();

    @Override
    public abstract ToManyRelationshipQuery getEntityQuery();

    @PostConstruct
    @Override
    public void postConstruct() {
        super.postConstruct();

        crudButtons = new HorizontalLayout();
        crudButtons.setMargin(false);
        crudButtons.setSpacing(true);

        addButton = new Button(uiMessageSource.getMessage("entityResults.add"), this, "add");
        addButton.setDescription(uiMessageSource.getMessage("entityResults.add.description"));
        addButton.setIcon(new ThemeResource("icons/16/add.png"));
        addButton.addStyleName("small default");
        crudButtons.addComponent(addButton);

        removeButton = new Button(uiMessageSource.getMessage("entityResults.remove"), this, "remove");
        removeButton.setDescription(uiMessageSource.getMessage("entityResults.remove.description"));
        removeButton.setIcon(new ThemeResource("icons/16/delete.png"));
        removeButton.setEnabled(false);
        removeButton.addStyleName("small default");
        crudButtons.addComponent(removeButton);

        getCrudButtons().addComponent(crudButtons, 0);
        getCrudButtons().setComponentAlignment(crudButtons, Alignment.MIDDLE_LEFT);

        getResultsTable().setMultiSelect(true);

        actionContextMenu.addAction("entityResults.remove", this, "remove");
        actionContextMenu.setActionEnabled("entityResults.remove", true);
        addSelectionChangedListener(this, "selectionChanged");
    }

    public abstract void add();

    public void setReferencesToParentAndPersist(T... values) {
        for (T value : values) {
            T referenceValue = getEntityDao().getReference(value);
            setReferenceToParent(referenceValue);
            getEntityDao().persist(referenceValue);
        }
        searchImpl(false);
    }

    public void setReferenceToParent(T value) {
        try {
            BeanPropertyType beanPropertyType = BeanPropertyType.getBeanPropertyType(getEntityType(), getParentPropertyId());
            Assert.PROGRAMMING.assertTrue(!beanPropertyType.isCollectionType(),
                    "Parent property id (" + getEntityType() + "." + getParentPropertyId() + ") must not be a collection type");
            PropertyUtils.setProperty(value, getParentPropertyId(), getEntityQuery().getParent());
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    public void setReadOnly(boolean isReadOnly) {
        addButton.setVisible(!isReadOnly);
        removeButton.setVisible(!isReadOnly);
    }

    public void applySecurityIsEditable() {
        boolean isEditable = securityService.getCurrentUser().isEditAllowed(getEntityType().getName(), getChildPropertyId());
        addButton.setVisible(isEditable);
        removeButton.setVisible(isEditable);

        if (isEditable) {
            getResultsTable().addActionHandler(actionContextMenu);
        } else {
            getResultsTable().removeActionHandler(actionContextMenu);
        }
    }

    public void valuesRemoved(T... values) {
        for (T value : values) {
            value = getEntityDao().getReference(value);
            try {
                PropertyUtils.setProperty(value, getParentPropertyId(), null);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            } catch (InvocationTargetException e) {
                throw new RuntimeException(e);
            } catch (NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
            getEntityDao().persist(value);
        }
        searchImpl(false);
        removeButton.setEnabled(false);
    }

    public void removeImpl() {
        Collection<T> selectedValues = getSelectedValues();
        valuesRemoved((T[]) selectedValues.toArray());
    }

    public void remove() {
        ConfirmDialog.show(MainApplication.getInstance().getMainWindow(),
                uiMessageSource.getMessage("entityResults.confirmationCaption"),
                uiMessageSource.getMessage("entityResults.confirmationPrompt"),
                uiMessageSource.getMessage("entityResults.confirmationYes"),
                uiMessageSource.getMessage("entityResults.confirmationNo"),
                new ConfirmDialog.Listener() {
                    public void onClose(ConfirmDialog dialog) {
                        if (dialog.isConfirmed()) {
                            removeImpl();
                        }
                    }
                });
    }

    public void selectionChanged() {
        Collection itemIds = (Collection) getResultsTable().getValue();
        if (itemIds.size() > 0) {
            getResultsTable().addActionHandler(actionContextMenu);
            removeButton.setEnabled(true);
        } else {
            getResultsTable().removeActionHandler(actionContextMenu);
            removeButton.setEnabled(false);
        }
    }
}
