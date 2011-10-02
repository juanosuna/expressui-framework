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

import com.vaadin.data.util.BeanItem;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;

import javax.annotation.PostConstruct;

/**
 * Search form bound to a POJO that contains query criteria.
 *
 * @param <T> type of POJO
 */
public abstract class SearchForm<T> extends FormComponent<T> {

    private Results results;

    @PostConstruct
    @Override
    public void postConstruct() {
        super.postConstruct();

        getForm().addStyleName("p-search-form");
    }

    @Override
    public void postWire() {
        super.postWire();
        BeanItem beanItem = createBeanItem(getResults().getEntityQuery());
        getForm().setItemDataSource(beanItem, getFormFields().getPropertyIds());
    }

    /**
     * Get results UI component connected to this search form.
     *
     * @return results UI component
     */
    public Results getResults() {
        return results;
    }

    void setResults(Results results) {
        this.results = results;
    }

    @Override
    protected void createFooterButtons(HorizontalLayout footerLayout) {
        footerLayout.setSpacing(true);
        footerLayout.setMargin(true);

        Button clearButton = new Button(uiMessageSource.getMessage("entitySearchForm.clear"), this, "clear");
        clearButton.setDescription(uiMessageSource.getMessage("entitySearchForm.clear.description"));
        clearButton.setIcon(new ThemeResource("icons/16/clear.png"));
        clearButton.addStyleName("small default");
        footerLayout.addComponent(clearButton);
        // alignment doesn't work
//        footerLayout.setComponentAlignment(clearButton, Alignment.MIDDLE_RIGHT);

        Button searchButton = new Button(uiMessageSource.getMessage("entitySearchForm.search"), this, "search");
        searchButton.setDescription(uiMessageSource.getMessage("entitySearchForm.search.description"));
        searchButton.setIcon(new ThemeResource("icons/16/search.png"));
        searchButton.addStyleName("small default");
        footerLayout.addComponent(searchButton);
//        footerLayout.setComponentAlignment(searchButton, Alignment.MIDDLE_RIGHT);
    }

    /**
     * Clear the contains of the search form and execute empty query, thus
     * returning all results.
     */
    public void clear() {
        getResults().getEntityQuery().clear();
        getResults().getResultsTable().setSortContainerPropertyId(null);
        BeanItem beanItem = createBeanItem(getResults().getEntityQuery());
        getForm().setItemDataSource(beanItem, getFormFields().getPropertyIds());

        getResults().search();
        requestRepaintAll();
    }

    /**
     * Execute search.
     */
    public void search() {
        getForm().commit();
        getResults().search();
    }

    @Override
    protected Component animate(Component component) {
        return super.animate(component, true);
    }
}
