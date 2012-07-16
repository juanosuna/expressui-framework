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

package com.expressui.core.view.menu;

import com.expressui.core.MainApplication;
import com.expressui.core.view.RootComponent;
import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.*;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import static org.springframework.web.context.WebApplicationContext.SCOPE_SESSION;

/**
 * Main menu bar displayed for navigating the application. Bar consists of left and right root nodes. Each root node
 * can contain a tree of nested menu nodes, where each node is a caption, a link to a page or an action.
 */
@Component
@Scope(SCOPE_SESSION)
public class MainMenuBar extends RootComponent {

    private MenuBarNode leftMenuBarRoot;
    private MenuBarNode rightMenuBarRoot;

    @Override
    public void postConstruct() {
        super.postConstruct();

        HorizontalLayout menuBarLayout = new HorizontalLayout();
        setDebugId(menuBarLayout, "menuBarLayout");
        menuBarLayout.setWidth(100, Sizeable.UNITS_PERCENTAGE);
        setCompositionRoot(menuBarLayout);

        leftMenuBarRoot = new MenuBarNode();
        rightMenuBarRoot = new MenuBarNode();
    }

    @Override
    public void postWire() {
        super.postWire();
    }

    @Override
    public void onDisplay() {
    }

    /**
     * Gets the root node displayed on the left.
     *
     * @return left root node
     */
    public MenuBarNode getLeftMenuBarRoot() {
        return leftMenuBarRoot;
    }

    /**
     * Gets the root node displayed on the right.
     *
     * @return right root node
     */
    public MenuBarNode getRightMenuBarRoot() {
        return rightMenuBarRoot;
    }

    /**
     * Refreshes the menu bar, useful when security permissions change, for example when user logs in and
     * is no longer anonymous.
     */
    public void refresh() {
        removeAllComponents();
        MenuBar leftMenuBar = leftMenuBarRoot.createMenuBar();
        addComponent(leftMenuBar);

        MenuBar rightMenuBar = rightMenuBarRoot.createMenuBar();
        addComponent(rightMenuBar);
        setComponentAlignment(rightMenuBar, Alignment.MIDDLE_RIGHT);

        if (leftMenuBar.getSize() == 0 && rightMenuBar.getSize() == 0) {
            if (securityService.getCurrentUser().getRoles().isEmpty()) {
                getMainApplication().showError("Menu bar contains no viewable items because current user is not assigned any roles.");
            } else {
                getMainApplication().showError("Menu bar contains no viewable items, either because none have been coded"
                        + " or assigned roles do not allow any to be viewed.");
            }
        }

        addPopupCodeIfEnabled();
    }

    private void addPopupCodeIfEnabled() {
        if (isCodePopupEnabled()) {
            Button codePopupButton = codePopup.createPopupCodeButton(getMainApplication().getClass(),
                    MainApplication.class);
            AbstractOrderedLayout layout = ((AbstractOrderedLayout) getCompositionRoot());
            layout.addComponent(codePopupButton, 1);
        }
    }
}
