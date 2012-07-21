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

package com.expressui.core.view.page;

import com.expressui.core.view.TypedComponent;
import com.expressui.core.view.form.SearchForm;
import com.expressui.core.view.results.Results;
import com.vaadin.ui.Alignment;

import javax.annotation.PostConstruct;

/**
 * A page consisting of a SearchForm and Results for working with entities of a particular type.
 *
 * @param <T> type of entity for this page
 */
public abstract class SearchPage<T> extends TypedComponent<T> implements Page {

    /**
     * Gets the search form component of this page.
     *
     * @return search form component
     */
    public abstract SearchForm getSearchForm();

    /**
     * Gets the results component for this page.
     *
     * @return Results component embedded in this page
     */
    public abstract Results<T> getResults();

    @PostConstruct
    @Override
    public void postConstruct() {
        super.postConstruct();

        if (isViewAllowed()) {
            addComponent(getSearchForm());
            addComponent(getResults());
        }

        addCodePopupButtonIfEnabled(Alignment.TOP_LEFT, SearchPage.class);
    }

    @Override
    public void postWire() {
        super.postWire();
        getSearchForm().setResults(getResults());
        getSearchForm().postWire();
        getResults().postWire();
    }

    @Override
    public void onDisplay() {
        getResults().search();

        getSearchForm().onDisplay();
        getResults().onDisplay();
    }

    @Override
    public String getCaption() {
        return null;
    }
}
