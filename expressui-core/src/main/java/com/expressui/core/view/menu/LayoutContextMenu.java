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

package com.expressui.core.view.menu;

import com.expressui.core.util.MethodDelegate;
import com.vaadin.event.LayoutEvents;
import com.vaadin.ui.AbstractOrderedLayout;
import org.vaadin.peter.contextmenu.ContextMenu;

import java.util.LinkedHashMap;
import java.util.Map;

public class LayoutContextMenu extends ContextMenu implements LayoutEvents.LayoutClickListener, ContextMenu.ClickListener {

    private Map<String, ContextMenuAction> actions = new LinkedHashMap<String, ContextMenuAction>();

    public LayoutContextMenu(AbstractOrderedLayout layout) {
        super();
        layout.addListener(this);
        layout.addComponent(this);
        addListener(this);
    }

    public ContextMenu.ContextMenuItem addAction(String caption, Object target, String methodName) {
        ContextMenu.ContextMenuItem item = super.addItem(caption);

        MethodDelegate methodDelegate = new MethodDelegate(target, methodName, ContextMenu.ContextMenuItem.class);
        ContextMenuAction contextMenuAction = new ContextMenuAction(item, methodDelegate);
        actions.put(caption, contextMenuAction);

        return item;
    }

    public ContextMenu.ContextMenuItem getContextMenuItem(String caption) {
        return actions.get(caption).getItem();
    }

    public boolean containsItem(String caption) {
        return actions.containsKey(caption);
    }

    @Override
    public void contextItemClick(ContextMenu.ClickEvent clickEvent) {
        ContextMenu.ContextMenuItem clickedItem = clickEvent.getClickedItem();
        ContextMenuAction action = actions.get(clickedItem.getName());
        action.getMethodDelegate().execute(clickedItem);
    }

    @Override
    public void layoutClick(LayoutEvents.LayoutClickEvent event) {
        if (LayoutEvents.LayoutClickEvent.BUTTON_RIGHT == event.getButton()) {
            show(event.getClientX(), event.getClientY());
        }
    }

    public static class ContextMenuAction {
        private ContextMenu.ContextMenuItem item;
        private MethodDelegate methodDelegate;

        public ContextMenuAction(ContextMenu.ContextMenuItem item, MethodDelegate methodDelegate) {
            this.item = item;
            this.methodDelegate = methodDelegate;
        }

        public MethodDelegate getMethodDelegate() {
            return methodDelegate;
        }

        public ContextMenuItem getItem() {
            return item;
        }

        public Object execute() {
            return methodDelegate.execute(item);
        }
    }
}

