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

package com.expressui.core.view.form;

import com.expressui.core.entity.WritableEntity;
import com.expressui.core.util.MethodDelegate;
import com.expressui.core.util.StringUtil;
import com.expressui.core.view.results.WalkableResults;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.*;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * An entity form connected to results, allowing user to walk through the current
 * results using next or previous buttons.
 *
 * @param <T> type of entity in the entity form and results
 */
public class ResultsConnectedEntityForm<T> extends CustomComponent {

    private EntityForm<T> entityForm;
    private WalkableResults results;

    private Button nextButton;
    private Button previousButton;

    private Set<MethodDelegate> walkListeners = new LinkedHashSet<MethodDelegate>();

    public ResultsConnectedEntityForm(EntityForm<T> entityForm, WalkableResults results) {
        this.entityForm = entityForm;
        this.results = results;

        initialize();
    }

    private void initialize() {
        setCompositionRoot(createNavigationFormLayout());
        setSizeUndefined();
    }

    private HorizontalLayout createNavigationFormLayout() {
        HorizontalLayout navigationFormLayout = new HorizontalLayout();
        String id = StringUtil.generateDebugId("e", this, navigationFormLayout, "navigationFormLayout");
        navigationFormLayout.setDebugId(id);
        navigationFormLayout.setSizeUndefined();

        VerticalLayout previousButtonLayout = new VerticalLayout();
        id = StringUtil.generateDebugId("e", this, previousButtonLayout, "previousButtonLayout");
        previousButtonLayout.setDebugId(id);

        previousButtonLayout.setSizeUndefined();
        previousButtonLayout.setMargin(false);
        previousButtonLayout.setSpacing(false);
        Label spaceLabel = new Label("</br></br></br>", Label.CONTENT_XHTML);
        spaceLabel.setSizeUndefined();
        previousButtonLayout.addComponent(spaceLabel);

        previousButton = new Button(null, this, "previousItem");
        previousButton.setDescription(entityForm.uiMessageSource.getMessage("entityForm.previous.description"));
        previousButton.setSizeUndefined();
        previousButton.addStyleName("borderless");
        previousButton.setIcon(new ThemeResource("../expressui/icons/16/previous.png"));

        if (entityForm.getViewableToManyRelationships().size() == 0) {
            HorizontalLayout previousButtonHorizontalLayout = new HorizontalLayout();
            id = StringUtil.generateDebugId("e", this, previousButtonHorizontalLayout, "previousButtonHorizontalLayout");
            previousButtonHorizontalLayout.setDebugId(id);
            previousButtonHorizontalLayout.setSizeUndefined();
            Label horizontalSpaceLabel = new Label("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;", Label.CONTENT_XHTML);
            horizontalSpaceLabel.setSizeUndefined();
            previousButtonHorizontalLayout.addComponent(previousButton);
            previousButtonHorizontalLayout.addComponent(horizontalSpaceLabel);
            previousButtonLayout.addComponent(previousButtonHorizontalLayout);
        } else {
            previousButtonLayout.addComponent(previousButton);
        }

        navigationFormLayout.addComponent(previousButtonLayout);
        navigationFormLayout.setComponentAlignment(previousButtonLayout, Alignment.TOP_LEFT);

        navigationFormLayout.addComponent(entityForm);

        VerticalLayout nextButtonLayout = new VerticalLayout();
        id = StringUtil.generateDebugId("e", this, nextButtonLayout, "nextButtonLayout");
        nextButtonLayout.setDebugId(id);
        nextButtonLayout.setSizeUndefined();
        nextButtonLayout.setMargin(false);
        nextButtonLayout.setSpacing(false);
        spaceLabel = new Label("</br></br></br>", Label.CONTENT_XHTML);
        spaceLabel.setSizeUndefined();
        previousButtonLayout.addComponent(spaceLabel);
        nextButtonLayout.addComponent(spaceLabel);

        nextButton = new Button(null, this, "nextItem");
        nextButton.setDescription(entityForm.uiMessageSource.getMessage("entityForm.next.description"));
        nextButton.setSizeUndefined();
        nextButton.addStyleName("borderless");
        nextButton.setIcon(new ThemeResource("../expressui/icons/16/next.png"));

        HorizontalLayout nextButtonHorizontalLayout = new HorizontalLayout();
        id = StringUtil.generateDebugId("e", this, nextButtonHorizontalLayout, "nextButtonHorizontalLayout");
        nextButtonHorizontalLayout.setDebugId(id);
        nextButtonHorizontalLayout.setSizeUndefined();
        Label horizontalSpaceLabel = new Label("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;", Label.CONTENT_XHTML);
        horizontalSpaceLabel.setSizeUndefined();
        nextButtonHorizontalLayout.addComponent(horizontalSpaceLabel);
        nextButtonHorizontalLayout.addComponent(nextButton);

        nextButtonLayout.addComponent(nextButtonHorizontalLayout);
        navigationFormLayout.addComponent(nextButtonLayout);
        navigationFormLayout.setComponentAlignment(nextButtonLayout, Alignment.TOP_RIGHT);

        navigationFormLayout.setSpacing(false);
        navigationFormLayout.setMargin(false);

        return navigationFormLayout;
    }

    /**
     * Refresh navigation buttons so that they are enabled if entity is persistent and disabled if transient
     */
    public void refreshNavigationButtonStates() {
        if (entityForm.isEntityPersistent()) {
            previousButton.setEnabled(results.hasPreviousItem());
            nextButton.setEnabled(results.hasNextItem());
        } else {
            previousButton.setEnabled(false);
            nextButton.setEnabled(false);
        }
    }

    /**
     * Go to previous item in the current results
     */
    public void previousItem() {
        results.editOrViewPreviousItem();
        refreshNavigationButtonStates();
        onWalk();
    }

    /**
     * Go to next item in the current results
     */
    public void nextItem() {
        results.editOrViewNextItem();
        refreshNavigationButtonStates();
        onWalk();
    }

    /**
     * Get entity form that is connected to results
     */
    public EntityForm<T> getEntityForm() {
        return entityForm;
    }

    private void onWalk() {
        for (MethodDelegate walkListener : walkListeners) {
            walkListener.execute();
        }
    }

    /**
     * Add a listener to detect any time user goes to next or previous records in results
     *
     * @param target     target object to invoke
     * @param methodName name of method to invoke
     */
    public void addWalkListener(Object target, String methodName) {
        walkListeners.add(new MethodDelegate(target, methodName));
    }

    public void removeListeners(Object target) {
        Set<MethodDelegate> listenersToRemove;

        listenersToRemove = new HashSet<MethodDelegate>();
        for (MethodDelegate walkListener : walkListeners) {
            if (walkListener.getTarget().equals(target)) {
                listenersToRemove.add(walkListener);
            }
        }
        for (MethodDelegate listener : listenersToRemove) {
            walkListeners.remove(listener);
        }
    }
}
