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
import com.expressui.core.view.TypedComponent;
import com.expressui.core.view.export.ExportForm;
import com.expressui.core.view.export.ExportParameters;
import com.vaadin.addon.tableexport.ExcelExport;
import com.vaadin.data.Property;
import com.vaadin.data.util.MethodProperty;
import com.vaadin.data.validator.IntegerValidator;
import com.vaadin.terminal.Sizeable;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.*;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;

/**
 * Results component that is bound the results of a query.
 * Also provides paging, sorting and adding/removing columns and re-ordering columns.
 *
 * @param <T> type of entity displayed in the results
 */
public abstract class Results<T> extends TypedComponent<T> {

    @Resource
    private ExportForm exportForm;

    @Resource
    private ResultsFieldSet resultsFieldSet;

    private ResultsTable resultsTable;
    private TextField firstResultTextField;
    private Label resultCountLabel;

    private Select pageSizeMenu;
    private Button firstButton;
    private Button previousButton;
    private Button nextButton;
    private Button lastButton;
    private Button excelButton;

    private HorizontalLayout crudButtons;

    protected Results() {
    }

    /**
     * Configure the fields/columns to be displayed in the results
     *
     * @param resultsFields used for configuring fields/columns
     */
    public abstract void init(ResultsFieldSet resultsFields);

    /**
     * Get the query used to create these results
     *
     * @return query used to create these results
     */
    public abstract EntityQuery<T> getEntityQuery();

    /**
     * Get the fields to be displayed in the results.
     *
     * @return fields to be displayed in the results
     */
    public ResultsFieldSet getResultsFieldSet() {
        return resultsFieldSet;
    }

    /**
     * Get the underlying UI table component used to display results.
     *
     * @return UI table component
     */
    public ResultsTable getResultsTable() {
        return resultsTable;
    }

    /**
     * Get horizontal layout of CRUD buttons
     *
     * @return horizontal layout of CRUD buttons, create, edit, view, delete
     */
    public HorizontalLayout getCrudButtons() {
        return crudButtons;
    }

    @PostConstruct
    @Override
    public void postConstruct() {
        super.postConstruct();
        resultsFieldSet.setType(getType());
        init(resultsFieldSet);

        resultsTable = new ResultsTable(this);
        configureTable(resultsTable);

        VerticalLayout rootLayout = new VerticalLayout();
        setDebugId(rootLayout, "rootLayout");
        setCompositionRoot(rootLayout);

        crudButtons = new HorizontalLayout();
        HorizontalLayout navigationLine = createNavigationLine();
        addComponent(crudButtons);
        addComponent(navigationLine);

        addComponent(resultsTable);

        rootLayout.setMargin(false, false, false, true);

        setSizeUndefined();

        labelRegistry.registerLabels(resultsFieldSet);
    }

    @Override
    public void postWire() {
        super.postWire();
        MethodProperty pageProperty = new MethodProperty(this, "pageSize");
        pageSizeMenu.setPropertyDataSource(pageProperty);
        pageSizeMenu.addListener(Property.ValueChangeEvent.class, this, "search");
        getEntityQuery().postWire();
        exportForm.postWire();
    }

    /**
     * Configure the results table. Maybe overridden to make any configuration changes to the Vaadin table
     *
     * @param resultsTable Vaadin table
     */
    public void configureTable(ResultsTable resultsTable) {
    }

    @Override
    public void addComponent(Component c) {
        ((ComponentContainer) getCompositionRoot()).addComponent(c);
    }

    private HorizontalLayout createNavigationLine() {

        HorizontalLayout resultCountDisplay = new HorizontalLayout();
        setDebugId(resultCountDisplay, "resultCountDisplay");
        firstResultTextField = createFirstResultTextField();
        firstResultTextField.addStyleName("small");
        firstResultTextField.setSizeUndefined();
        resultCountDisplay.addComponent(firstResultTextField);
        resultCountLabel = new Label("", Label.CONTENT_XHTML);
        resultCountLabel.setSizeUndefined();
        resultCountLabel.addStyleName("small");
        resultCountDisplay.addComponent(resultCountLabel);

        Label spaceLabel = new Label(" &nbsp; ", Label.CONTENT_XHTML);
        spaceLabel.setSizeUndefined();
        resultCountDisplay.addComponent(spaceLabel);

        Button refreshButton = new Button(null, getResultsTable(), "refresh");
        refreshButton.setDescription(uiMessageSource.getMessage("results.refresh.description"));
        refreshButton.setSizeUndefined();
        refreshButton.addStyleName("borderless");
        refreshButton.setIcon(new ThemeResource("../expressui/icons/16/refresh-blue.png"));
        resultCountDisplay.addComponent(refreshButton);

        HorizontalLayout navigationButtons = new HorizontalLayout();
        setDebugId(navigationButtons, "navigationButtons");
        navigationButtons.setMargin(false, true, false, false);
        navigationButtons.setSpacing(true);

        String perPageText = uiMessageSource.getMessage("results.pageSize");
        pageSizeMenu = new Select();
        pageSizeMenu.addStyleName("small");
        List<Integer> pageSizeOptions = applicationProperties.getPageSizeOptions();
        for (Integer pageSizeOption : pageSizeOptions) {
            pageSizeMenu.addItem(pageSizeOption);
            pageSizeMenu.setItemCaption(pageSizeOption, pageSizeOption + " " + perPageText);
        }
        pageSizeMenu.setFilteringMode(Select.FILTERINGMODE_OFF);
        pageSizeMenu.setNewItemsAllowed(false);
        pageSizeMenu.setNullSelectionAllowed(false);
        pageSizeMenu.setImmediate(true);
        pageSizeMenu.setWidth(8, UNITS_EM);
        navigationButtons.addComponent(pageSizeMenu);

        firstButton = new Button(null, getResultsTable(), "firstPage");
        firstButton.setDescription(uiMessageSource.getMessage("results.first.description"));
        firstButton.setSizeUndefined();
        firstButton.addStyleName("borderless");
        firstButton.setIcon(new ThemeResource("../expressui/icons/16/first.png"));
        navigationButtons.addComponent(firstButton);

        previousButton = new Button(null, getResultsTable(), "previousPage");
        previousButton.setDescription(uiMessageSource.getMessage("results.previous.description"));
        previousButton.setSizeUndefined();
        previousButton.addStyleName("borderless");
        previousButton.setIcon(new ThemeResource("../expressui/icons/16/previous.png"));
        navigationButtons.addComponent(previousButton);

        nextButton = new Button(null, getResultsTable(), "nextPage");
        nextButton.setDescription(uiMessageSource.getMessage("results.next.description"));
        nextButton.setSizeUndefined();
        nextButton.addStyleName("borderless");
        nextButton.setIcon(new ThemeResource("../expressui/icons/16/next.png"));
        navigationButtons.addComponent(nextButton);

        lastButton = new Button(null, getResultsTable(), "lastPage");
        lastButton.setDescription(uiMessageSource.getMessage("results.last.description"));
        lastButton.setSizeUndefined();
        lastButton.addStyleName("borderless");
        lastButton.setIcon(new ThemeResource("../expressui/icons/16/last.png"));
        navigationButtons.addComponent(lastButton);

        excelButton = new Button(null, this, "openExportForm");
        excelButton.setDescription(uiMessageSource.getMessage("results.excel.description"));
        excelButton.setSizeUndefined();
        excelButton.addStyleName("borderless");
        excelButton.setIcon(new ThemeResource("../expressui/icons/16/excel.bmp"));
        navigationButtons.addComponent(excelButton);
        exportForm.setExportButtonListener(this, "exportToExcel");

        HorizontalLayout navigationLine = new HorizontalLayout();
        setDebugId(navigationLine, "navigationLine");
        navigationLine.setSizeUndefined();
//        navigationLine.setWidth("100%");
        navigationLine.setMargin(true, true, true, true);

        navigationLine.addComponent(resultCountDisplay);
        navigationLine.setComponentAlignment(resultCountDisplay, Alignment.BOTTOM_LEFT);

        spaceLabel = new Label("", Label.CONTENT_XHTML);
        spaceLabel.setWidth(2, Sizeable.UNITS_EM);
        navigationLine.addComponent(spaceLabel);

        navigationLine.addComponent(navigationButtons);
        navigationLine.setComponentAlignment(navigationButtons, Alignment.BOTTOM_RIGHT);

        return navigationLine;
    }

    private TextField createFirstResultTextField() {
        TextField firstResultTextField = new TextField();
        firstResultTextField.setImmediate(true);
        firstResultTextField.setInvalidAllowed(true);
        firstResultTextField.setInvalidCommitted(false);
        firstResultTextField.setWriteThrough(true);
        firstResultTextField.addValidator(new IntegerValidator(uiMessageSource.getMessage("results.firstResult.invalid")) {
            @Override
            protected boolean isValidString(String value) {
                boolean isValid = super.isValidString(value);
                if (!isValid) {
                    return false;
                } else {
                    Integer intValue = Integer.parseInt(value);
                    if (getEntityQuery().getResultCount() > 0) {
                        return intValue >= 1 && intValue <= getEntityQuery().getResultCount();
                    } else {
                        return intValue == 0;
                    }
                }
            }
        });
        firstResultTextField.setPropertyDataSource(new MethodProperty(getResultsTable(), "firstResult"));
        firstResultTextField.setWidth(3, Sizeable.UNITS_EM);

        return firstResultTextField;
    }

    void refreshNavigationButtonStates() {
        firstButton.setEnabled(getEntityQuery().hasPreviousPage());
        previousButton.setEnabled(getEntityQuery().hasPreviousPage());
        lastButton.setEnabled(getEntityQuery().hasNextPage());
        nextButton.setEnabled(getEntityQuery().hasNextPage());

        pageSizeMenu.setEnabled(getEntityQuery().getResultCount() > 5);
        firstResultTextField.setEnabled(getEntityQuery().getResultCount() > 5);
        excelButton.setEnabled(getEntityQuery().getResultCount() > 0);
    }

    /**
     * Change the page size selection
     *
     * @param pageSize new page size
     */
    public void selectPageSize(Integer pageSize) {
        pageSizeMenu.select(pageSize);
    }

    /**
     * Get currently selected page size
     *
     * @return currently selected page size
     */
    public int getPageSize() {
        return getEntityQuery().getPageSize();
    }

    /**
     * Set the page size in the entity query
     *
     * @param pageSize new page size
     */
    public void setPageSize(int pageSize) {
        getEntityQuery().setPageSize(pageSize);
    }

    /**
     * Ask if page-size menu is visible.
     *
     * @return true if visible
     */
    public boolean getPageSizeVisible() {
        return pageSizeMenu.isVisible();
    }

    /**
     * Set whether or not the page-size menu is visible, useful if the number of items is known to be small
     * or when layout does not permit larger pages.
     *
     * @param isVisible true if visible
     */
    public void setPageSizeVisible(boolean isVisible) {
        pageSizeMenu.setVisible(isVisible);
    }

    /**
     * Add a listener that detects row-selection changes in the results
     *
     * @param target     target object to invoke listener on
     * @param methodName name of method to invoke when selection occurs
     */
    public void addSelectionChangedListener(Object target, String methodName) {
        resultsTable.addListener(Property.ValueChangeEvent.class, target, methodName);
    }

    /**
     * Get the currently selected value in the results table. Could be a single entity or a collection of entities.
     *
     * @return either single entity or collection of entities
     */
    public Object getSelectedValue() {
        return getResultsTable().getValue();
    }

    /**
     * Get the currently selected values in the results table.
     *
     * @return collection of entities
     */
    public Collection getSelectedValues() {
        return (Collection) getResultsTable().getValue();
    }

    /**
     * Execute current query and refresh results. Any existing selected rows are cleared.
     */
    public void search() {
        searchImpl(true);
    }

    /**
     * Execute current query and refresh results.
     *
     * @param clearSelection true if row selection should be cleared
     */
    protected void searchImpl(boolean clearSelection) {
        getEntityQuery().firstPage();
        getResultsTable().executeCurrentQuery();

        if (clearSelection) {
            getResultsTable().clearSelection();
        }
    }

    /**
     * Refresh the label displaying the result count
     */
    protected void refreshResultCountLabel() {
        EntityQuery query = getEntityQuery();
        String caption = uiMessageSource.getMessage("results.caption",
                new Object[]{
                        query.getResultCount() == 0 ? 0 : query.getLastResult(),
                        query.getResultCount()});
        firstResultTextField.setWidth(Math.max(3, query.getResultCount().toString().length() - 1), Sizeable.UNITS_EM);
        resultCountLabel.setValue(caption);
    }

    /**
     * Open the excel-export popup form.
     */
    public void openExportForm() {
        String entityLabel = labelRegistry.getTypeLabel(getType().getName());
        if (entityLabel == null) {
            entityLabel = getType().getSimpleName();
        }

        entityLabel += " ";
        exportForm.getExportParameters().setWorkbookName(entityLabel + "Export");
        exportForm.getExportParameters().setSheetName(entityLabel + "Export");
        exportForm.getExportParameters().setExportFilename("\"" + entityLabel + "Export.xls\"");

        exportForm.open();
    }

    /**
     * Export the single page of displayed data to Excel, using parameters configured by user in popup form.
     */
    public void exportToExcel() {
        ExportParameters exportParameters = exportForm.getExportParameters();

        ExcelExport excelExport = new ExcelExport(getResultsTable(), exportParameters.getWorkbookName(),
                exportParameters.getSheetName());
        if (exportParameters.getDateFormat() != null) {
            excelExport.setDateDataFormat(exportParameters.getDateFormat());
        }
        if (exportParameters.getDoubleFormat() != null) {
            excelExport.setDoubleDataFormat(exportParameters.getDoubleFormat());
        }

        excelExport.setDisplayTotals(exportParameters.isDisplayTotals());
        excelExport.setExportFileName(exportParameters.getExportFilename());
        excelExport.setRowHeaders(exportParameters.isDisplayRowHeaders());
        excelExport.excludeCollapsedColumns();
        excelExport.export();
    }

    @Override
    public String getTypeCaption() {
        return null;
    }
}
