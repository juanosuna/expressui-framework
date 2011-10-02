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

import com.expressui.core.entity.security.AbstractUser;
import com.expressui.core.security.SecurityService;
import com.vaadin.ui.TabSheet;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * Main entry points into the ExpressUI application, presented as a Vaadin Tabsheet.
 *
 * Each entry point is presented as a Vaadin Tab.
 */
public abstract class MainEntryPoints extends TabSheet {

    @Resource
    private SecurityService securityService;

    /**
     * Get all the entry points of the application, including those the user doesn't
     * have permission to view.
     *
     * Implementer should return all entry points and let ExpressUI take care of
     * security handling.
     *
     * @return all entry points into the application
     */
    public abstract List<MainEntryPoint> getEntryPoints();

    /**
     * Get all entry points that the user has security permission to view.
     *
     * @return all entry points user is permitted to view
     */
    public final List<MainEntryPoint> getViewableEntryPoints() {
        List<MainEntryPoint> entryPoints = getEntryPoints();
        List<MainEntryPoint> viewableEntryPoints = new ArrayList<MainEntryPoint>();

        for (MainEntryPoint entryPoint : entryPoints) {
            AbstractUser user = securityService.getCurrentUser();

            if (user.isViewAllowed(entryPoint.getEntityType().getName())
                    && !entryPoint.getResults().getDisplayFields().getViewablePropertyIds().isEmpty()) {

                viewableEntryPoints.add(entryPoint);
            }
        }

        return viewableEntryPoints;
    }

    /**
     * Name of the Vaadin theme used to style this application.
     * Default is "expressUiTheme." Implementer can override this name and provide
     * their custom theme.
     *
     * @return name of Vaadin theme
     */
    public String getTheme() {
        return "expressUiTheme";
    }

    /**
     * Called after Spring constructs this bean. Overriding methods should call super.
     */
    @PostConstruct
    protected void postConstruct() {
        setSizeUndefined();
        List<MainEntryPoint> entryPoints = getViewableEntryPoints();
        for (MainEntryPoint entryPoint : entryPoints) {
            addTab(entryPoint);
        }

        addListener(new TabChangeListener());
        if (entryPoints.size() > 0) {
            entryPoints.get(0).getResults().search();
        }
    }

    /**
     * Can be overridden if any initialization is required after all Spring beans have been wired.
     * Overriding methods should call super.
     */
    public void postWire() {
        List<MainEntryPoint> entryPoints = getViewableEntryPoints();
        for (EntryPoint entryPoint : entryPoints) {
            entryPoint.postWire();
        }
    }

    private class TabChangeListener implements SelectedTabChangeListener {

        @Override
        public void selectedTabChange(SelectedTabChangeEvent event) {
            MainEntryPoint entryPoint = (MainEntryPoint) getSelectedTab();
            entryPoint.getResults().search();
            if (entryPoint.getResults() instanceof CrudResults) {
                ((CrudResults) entryPoint.getResults()).applySecurityToCRUDButtons();
            }
        }
    }
}
