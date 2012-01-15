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

package com.expressui.core.view.page;

import com.expressui.core.entity.security.User;
import com.expressui.core.security.SecurityService;
import com.expressui.core.view.EntityComponent;
import com.expressui.core.view.field.LabelRegistry;
import com.expressui.core.view.form.SearchForm;
import com.expressui.core.view.results.Results;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

/**
 * A main page for the user to work with entities of a particular type.
 * <p/>
 * The difference between main page and a regular page: a main page is presented to the user
 * as a Vaadin Tab in the initial "home page" of the application, a TabSheet. A regular page can be
 * presented anywhere in the application, e.g. a pop-up EntitySelect  also provides a search form
 * and results for selecting a entity in a many-to-one relationship.
 *
 * @param <T> type of business entity for this page
 */
public abstract class SearchPage<T> extends EntityComponent<T> implements Page {

    @Resource
    private SecurityService securityService;

    @Resource
    private LabelRegistry labelRegistry;

    /**
     * Get the search form component of this page
     *
     * @return search form component
     */
    public abstract SearchForm getSearchForm();

    /**
     * Get the results component for this page.
     *
     * @return
     */
    public abstract Results<T> getResults();

    @PostConstruct
    @Override
    public void postConstruct() {
        super.postConstruct();

        addStyleName("p-page");

        labelRegistry.putTypeLabel(getEntityType().getName(), getEntityCaption());

        addComponent(getSearchForm());
        addComponent(getResults());
    }

    @Override
    public void postWire() {
        super.postWire();
        getSearchForm().setResults(getResults());
        getSearchForm().postWire();
        getResults().postWire();
    }

    @Override
    public void onLoad() {
    }

    public String getCaption() {
        return null;
    }

    @Override
    public boolean isViewAllowed() {
        User user = securityService.getCurrentUser();

        return user.isViewAllowed(getEntityType().getName())
                && !getResults().getDisplayFields().getViewablePropertyIds().isEmpty();
    }
}
