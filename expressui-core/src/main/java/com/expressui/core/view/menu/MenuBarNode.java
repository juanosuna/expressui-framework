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

import com.expressui.core.security.SecurityService;
import com.expressui.core.util.MethodDelegate;
import com.expressui.core.util.ReflectionUtil;
import com.expressui.core.util.SpringApplicationContext;
import com.expressui.core.util.assertion.Assert;
import com.expressui.core.view.field.LabelRegistry;
import com.expressui.core.view.page.Page;
import com.vaadin.ui.MenuBar;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * A node in the menu bar tree. Each node has a caption and is linked to a Page or action/listener method.
 */
public class MenuBarNode {

    @Resource
    private LabelRegistry labelRegistry;

    @Resource
    private SecurityService securityService;

    private String caption;
    private MenuBar.Command command;
    private boolean visible = true;
    private LinkedHashMap<String, MenuBarNode> children = new LinkedHashMap<String, MenuBarNode>();

    /**
     * Construct a root node without caption or page/action or nested node where caption and page/action
     * will be set later.
     */
    public MenuBarNode() {
        SpringApplicationContext.autowire(this);
    }

    /**
     * Construct nested node with caption and no page or action.
     *
     * @param caption caption to display to user as menu item
     */
    public MenuBarNode(String caption) {
        this();
        this.caption = caption;
        command = new NullCommand();
        labelRegistry.putTypeLabel(caption, caption); // todo fixme
    }

    /**
     * Construct nested node with caption and page.
     *
     * @param caption   caption to display to user as menu item
     * @param pageClass page to display when user chooses menu item
     */
    public MenuBarNode(String caption, Class<? extends Page> pageClass) {
        this();
        this.caption = caption;
        command = new PageCommand(pageClass);
        labelRegistry.putTypeLabel(pageClass.getName(), caption);
    }

    /**
     * Get the command that is attached to this node.
     *
     * @return command
     */
    public MenuBar.Command getCommand() {
        return command;
    }

    /**
     * Construct nested node with caption and action/listener
     *
     * @param caption    caption to display to user as menu item
     * @param target     target listener object to be invoked when user chooses menu item
     * @param methodName listener method to be invoked when user chooses menu item
     */
    public MenuBarNode(String caption, Object target, String methodName) {
        this();
        this.caption = caption;
        command = new MethodCommand(target, methodName);
    }

    /**
     * Ask if this node/menu item is visible.
     *
     * @return true if visible
     */
    public boolean isVisible() {
        return visible;
    }

    /**
     * Set whether or not this node/menu item is visible.
     *
     * @param visible true if visible
     */
    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    /**
     * Get child node associated with given name
     *
     * @param name key to look up child
     * @return child node
     */
    public MenuBarNode getChild(String name) {
        return children.get(name);
    }

    /**
     * Get all immediate child nodes.
     *
     * @return all child nodes
     */
    public List<MenuBarNode> getChildren() {
        return Collections.unmodifiableList(new ArrayList<MenuBarNode>(children.values()));
    }

    /**
     * Add a caption to this node without any page or action associated with caption
     *
     * @param caption caption to display in menu item
     * @return newly created node, to which nested nodes may be added
     */
    public MenuBarNode addCaption(String caption) {
        MenuBarNode menuBarNode = new MenuBarNode(caption);
        children.put(caption, menuBarNode);

        return menuBarNode;
    }

    /**
     * Add a page to this node so that the page is loaded
     *
     * @param caption   caption to display in menu item
     * @param pageClass page to display when menu item is chosen
     * @return newly created node, to which nested nodes may be added
     */
    public MenuBarNode addPage(String caption, Class<? extends Page> pageClass) {
        MenuBarNode menuBarNode = new MenuBarNode(caption, pageClass);
        children.put(caption, menuBarNode);

        return menuBarNode;
    }

    /**
     * Add a page to this node so that the page is loaded
     *
     * @param caption    caption to display in menu item
     * @param target     target listener object to be invoked when user chooses menu item
     * @param methodName listener method to be invoked when user chooses menu item
     * @return newly created node, to which nested nodes may be added
     */
    public MenuBarNode addCommand(String caption, Object target, String methodName) {
        MenuBarNode menuBarNode = new MenuBarNode(caption, target, methodName);
        children.put(caption, menuBarNode);

        return menuBarNode;
    }

    MenuBar createMenuBar() {
        MenuBar menuBar = new MenuBar();
        menuBar.setSizeUndefined();
        menuBar.setAutoOpen(true);
        menuBar.setHtmlContentAllowed(true);

        for (MenuBarNode child : children.values()) {
            if (child.isViewAllowed() && child.isVisible()) {
                MenuBar.MenuItem childMenuItem = menuBar.addItem(child.caption, child.command);
                child.populate(childMenuItem);
            }
        }

        return menuBar;
    }

    private void populate(MenuBar.MenuItem menuItem) {
        for (MenuBarNode child : children.values()) {
            if (child.isViewAllowed() && child.isVisible()) {
                MenuBar.MenuItem childMenuItem = menuItem.addItem(child.caption, child.command);
                child.populate(childMenuItem);
            }
        }
    }

    private boolean isViewAllowed() {
        if (command instanceof PageCommand) {
            Class pageClass = ((PageCommand) command).getPageClass();
            Class genericType = ReflectionUtil.getGenericArgumentType(pageClass);
            if (genericType == null) {
                return securityService.getCurrentUser().isViewAllowed(pageClass.getName());
            } else {
                return securityService.getCurrentUser().isViewAllowed(pageClass.getName())
                        && securityService.getCurrentUser().isViewAllowed(genericType.getName());
            }
        } else if (command instanceof MethodCommand) {
            MethodDelegate methodDelegate = ((MethodCommand) command).getMethodDelegate();
            String name = methodDelegate.getTarget().getClass().getName() + "." + methodDelegate.getMethod().getName();
            return securityService.getCurrentUser().isViewAllowed(name);
        } else if (command instanceof NullCommand) {
            return securityService.getCurrentUser().isViewAllowed(caption);
        } else {
            Assert.PROGRAMMING.fail("Command must be either of type PageCommand,  MethodCommand or NullCommand. " +
                    "Instead it is of type " + command.getClass());
        }

        return false;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }
}
