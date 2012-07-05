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

package com.expressui.core.view.results;

import com.expressui.core.dao.query.EntityQuery;
import com.expressui.core.util.MethodDelegate;
import com.expressui.core.view.field.DisplayField;
import com.expressui.core.view.field.ResultsField;
import com.expressui.core.view.field.format.EmptyPropertyFormatter;
import com.expressui.core.view.form.EntityForm;
import com.expressui.core.view.form.EntityFormWindow;
import com.vaadin.data.Container;
import com.vaadin.data.Property;
import com.vaadin.data.util.BeanItem;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.data.util.EnhancedBeanItemContainer;
import com.vaadin.data.util.PropertyFormatter;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.BaseTheme;
import org.apache.commons.beanutils.PropertyUtils;

import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Table for displaying results.
 */
public class ResultsTable extends Table {

    private Results results;
    private Set<MethodDelegate> executeQueryListeners = new LinkedHashSet<MethodDelegate>();


    protected ResultsTable(Results results) {
        this.results = results;
        addStyleName("strong striped");
        initialize();
    }

    private void initialize() {
        setSizeUndefined();
        alwaysRecalculateColumnWidths = true;
        setEditable(true); // set to enable Form links
        setTableFieldFactory(new TableButtonLinkFactory());

        EnhancedBeanItemContainer dataSource = new EnhancedBeanItemContainer(results.getType(),
                results.getResultsFieldSet());
        dataSource.setNonSortablePropertyIds(results.getResultsFieldSet().getNonSortablePropertyIds());
        String[] propertyIds = results.getResultsFieldSet().getViewablePropertyIdsAsArray();
        for (String propertyId : propertyIds) {
            dataSource.addNestedContainerProperty(propertyId);
        }
        setContainerDataSource(dataSource);

        setSelectable(true);
        setImmediate(true);
        setColumnReorderingAllowed(true);
        setColumnCollapsingAllowed(true);
        setCacheRate(1);

        setVisibleColumns(results.getResultsFieldSet().getViewablePropertyIdsAsArray());
        setColumnHeaders(results.getResultsFieldSet().getViewableLabelsAsArray());

        for (String propertyId : propertyIds) {
            ResultsField resultsField = results.getResultsFieldSet().getResultsField(propertyId);
            if (resultsField.getWidth() != null) {
                setColumnWidth(propertyId, resultsField.getWidth());
            }
            if (resultsField.getAlignment() != null) {
                setColumnAlignment(propertyId, resultsField.getAlignment());
            }
        }
    }

    /**
     * EXPERIMENTAL Vaadin feature: will tell the client to re-calculate column widths
     * if set to true. Currently no setter: extend to enable.
     */
    public boolean getAlwaysRecalculateColumnWidths() {
        return alwaysRecalculateColumnWidths;
    }

    /**
     * EXPERIMENTAL Vaadin feature: tells the client to re-calculate column widths
     * if set to true.
     */
    public void setAlwaysRecalculateColumnWidths(boolean alwaysRecalculateColumnWidths) {
        this.alwaysRecalculateColumnWidths = alwaysRecalculateColumnWidths;
    }

    /**
     * @see com.vaadin.ui.Table.enableContentRefreshing
     */
    public void turnOnContentRefreshing() {
        enableContentRefreshing(true);
    }

    @Override
    public BeanItemContainer getContainerDataSource() {
        return (BeanItemContainer) super.getContainerDataSource();
    }

    @Override
    public void sort(Object[] propertyId, boolean[] ascending) throws UnsupportedOperationException {
        if (propertyId.length > 1) {
            throw new RuntimeException("Cannot sort on more than one column");
        } else if (propertyId.length == 1) {
            if (results.getResultsFieldSet().getResultsField(propertyId[0].toString()).isSortable()) {
                results.getEntityQuery().setOrderByPropertyId(propertyId[0].toString());
                if (ascending[0]) {
                    results.getEntityQuery().setOrderDirection(EntityQuery.OrderDirection.ASC);
                } else {
                    results.getEntityQuery().setOrderDirection(EntityQuery.OrderDirection.DESC);
                }
                firstPage();
            } else {
                throw new UnsupportedOperationException("No sorting on this column");
            }
        }
    }

    /**
     * Add execute-query listener. Listener is invoked when query is executed or re-executed during
     * paging, sorting, etc.
     *
     * @param target     target object
     * @param methodName name of method to invoke
     */
    public void addExecuteQueryListener(Object target, String methodName) {
        executeQueryListeners.add(new MethodDelegate(target, methodName));
    }

    /**
     * Get the offset of the first result displayed, starting with 1.
     *
     * @return offset of first result, or 0 if there are no results
     */
    public int getFirstResult() {
        EntityQuery query = results.getEntityQuery();
        return query.getResultCount() == 0 ? 0 : query.getFirstResult() + 1;
    }

    /**
     * Set the offset of the first result displayed, starting with 1.
     *
     * @param firstResult offset of first result, or 0 if there are no results
     */
    public void setFirstResult(int firstResult) {
        clearSelection();
        results.getEntityQuery().setFirstResult(firstResult - 1);
        executeCurrentQuery();
        selectFirstItemInCurrentPage();
    }

    /**
     * Re-execute query.
     */
    public void refresh() {
        clearSelection();
        executeCurrentQuery();
        selectFirstItemInCurrentPage();
    }

    /**
     * Go to first page and re-execute query.
     */
    public void firstPage() {
        clearSelection();
        results.getEntityQuery().firstPage();
        executeCurrentQuery();
        selectFirstItemInCurrentPage();
    }

    /**
     * Go to previous page and re-execute query.
     */
    public void previousPage() {
        clearSelection();
        results.getEntityQuery().previousPage();
        executeCurrentQuery();
        selectFirstItemInCurrentPage();
    }

    /**
     * Go to next page and re-execute query.
     */
    public void nextPage() {
        clearSelection();
        results.getEntityQuery().nextPage();
        executeCurrentQuery();
        selectFirstItemInCurrentPage();
    }

    /**
     * Go to last page and re-execute query.
     */
    public void lastPage() {
        clearSelection();
        results.getEntityQuery().lastPage();
        executeCurrentQuery();
        selectFirstItemInCurrentPage();
    }

    /**
     * Execute the current query.
     */
    public void executeCurrentQuery() {
        List entities = results.getEntityQuery().execute();
        getContainerDataSource().removeAllItems();
        getContainerDataSource().addAll(entities);

        results.refreshFirstResultAndCount();
        results.refreshNavigationButtonStates();
        setPageLength(Math.min(entities.size(), results.getPageSize()));

        Set<MethodDelegate> listenersToExecute = (Set<MethodDelegate>) ((LinkedHashSet) executeQueryListeners).clone();
        for (MethodDelegate listener : listenersToExecute) {
            listener.execute();
        }
    }

    /**
     * Clear any selected rows.
     */
    public void clearSelection() {
        if (isMultiSelect()) {
            setValue(new HashSet());
        } else {
            setValue(null);
        }
    }

    public void selectFirstItemInCurrentPage() {
        if (firstItemId() != null) {
            select(firstItemId());
        }
    }

    @Override
    protected String formatPropertyValue(Object rowId, Object colId, Property property) {

        if (property.getValue() != null) {
            DisplayField displayField = results.getResultsFieldSet().getField(colId.toString());
            PropertyFormatter propertyFormatter = displayField.getPropertyFormatter();

            if (EmptyPropertyFormatter.class.equals(propertyFormatter.getClass())) {
                return super.formatPropertyValue(rowId, colId, property);
            } else {
                return propertyFormatter.format(property.getValue());
            }
        } else {
            return super.formatPropertyValue(rowId, colId, property);
        }
    }

    private class TableButtonLinkFactory implements TableFieldFactory {
        @Override
        public Field createField(Container container, Object itemId,
                                 Object propertyId, Component uiContext) {

            DisplayField.FormLink formLink = results.getResultsFieldSet().getField(propertyId.toString()).getFormLink();

            if (formLink != null) {
                BeanItem item = getContainerDataSource().getItem(itemId);
                Button button = new ButtonLink(item.getItemProperty(propertyId));
                button.addListener(new ButtonLinkClickListener(formLink, item));
                return button;
            }

            return null;
        }
    }

    private class ButtonLinkClickListener implements Button.ClickListener {
        private DisplayField.FormLink formLink;
        private BeanItem item;

        public ButtonLinkClickListener(DisplayField.FormLink formLink, BeanItem item) {
            this.formLink = formLink;
            this.item = item;
        }

        @Override
        public void buttonClick(Button.ClickEvent event) {
            Object parentBean = item.getBean();
            try {
                Object propertyBean = PropertyUtils.getProperty(parentBean,
                        formLink.getPropertyId());
                EntityForm entityForm = formLink.getEntityForm();
                entityForm.addCancelListener(results, "search");
                entityForm.addCloseListener(results, "search");
                entityForm.load(propertyBean);
                EntityFormWindow entityFormWindow = EntityFormWindow.open(entityForm);
                entityFormWindow.addCloseListener(results, "search");
                if (!entityForm.getViewableToManyRelationships().isEmpty()) {
                    entityFormWindow.setHeight("95%");
                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            } catch (InvocationTargetException e) {
                throw new RuntimeException(e);
            } catch (NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static class ButtonLink extends Button {
        private Property itemProperty;

        public ButtonLink(Property itemProperty) {
            this.itemProperty = itemProperty;
            setStyleName(BaseTheme.BUTTON_LINK);
        }

        @Override
        public String getCaption() {
            if (itemProperty.getValue() == null) {
                return null;
            } else {
                return itemProperty.getValue().toString();
            }
        }

        @Override
        protected void setInternalValue(Object newValue) {
            super.setInternalValue(false);
        }

        @Override
        protected void setValue(Object newValue, boolean repaintIsNotNeeded) throws ReadOnlyException, ConversionException {
            super.setValue(false, repaintIsNotNeeded);
        }

        @Override
        public void setValue(Object newValue) throws ReadOnlyException, ConversionException {
            super.setValue(false);
        }

        @Override
        public void setPropertyDataSource(Property newDataSource) {
            itemProperty = newDataSource;
        }
    }
}
