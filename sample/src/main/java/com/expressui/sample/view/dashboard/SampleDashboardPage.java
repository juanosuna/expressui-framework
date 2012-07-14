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

package com.expressui.sample.view.dashboard;

import com.expressui.core.MainApplication;
import com.expressui.core.util.UrlUtil;
import com.expressui.core.view.page.DashboardPage;
import com.expressui.domain.geocode.MapService;
import com.expressui.sample.dao.OpportunityDao;
import com.expressui.sample.entity.Contact;
import com.expressui.sample.entity.derived.TotalSalesStage;
import com.expressui.sample.entity.derived.TotalYearSales;
import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.AbstractComponentContainer;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.vaadin.vaadinvisualizations.ColumnChart;
import org.vaadin.vaadinvisualizations.PieChart;
import org.vaadin.vol.OpenLayersMap;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;

import static org.springframework.web.context.WebApplicationContext.SCOPE_SESSION;

@Component
@Scope(SCOPE_SESSION)
@SuppressWarnings({"serial"})
public class SampleDashboardPage extends DashboardPage {

    @Resource
    private OpportunityDao opportunityDao;

    @Resource
    private RecentContactResults recentContactResults;

    @Resource
    private MapService mapService;

    @PostConstruct
    @Override
    public void postConstruct() {
        super.postConstruct();

        setCellPixelWidth(670);
        setCellPixelHeight(380);

        configureRecentContactResults();
        addComponent(recentContactResults, uiMessageSource.getMessage("sampleDashBoard.recentContacts"),
                1, 1);

        // Map is shown at coordinates 1, 2 when user selects a contact, see contactSelectionChanged

        addComponent(createOpportunityChartByYear(),
                uiMessageSource.getMessage("sampleDashBoard.opportunityChartByYear"),
                2, 1);

        addComponent(createOpportunitySalesStageChart(),
                uiMessageSource.getMessage("sampleDashBoard.opportunityChartBySalesStage"),
                2, 2);

        // Used to track usage statistics only for sample application
        UrlUtil.addTrackingUrl((AbstractComponentContainer) getCompositionRoot(), "sample");
    }

    private ColumnChart createOpportunityChartByYear() {
        ColumnChart columnChart = new ColumnChart();

        columnChart.setOption("is3D", true);
        columnChart.setOption("isStacked", false);
        columnChart.addXAxisLabel(uiMessageSource.getMessage("sampleDashBoard.opportunityChartByYear.axisLabel"));
        columnChart.addColumn(uiMessageSource.getMessage("sampleDashBoard.opportunityChartByYear.closedWon"));
        columnChart.addColumn(uiMessageSource.getMessage("sampleDashBoard.opportunityChartByYear.closedLost"));

        List<TotalYearSales> totalYearSalesList = opportunityDao.getSalesByYear();
        List<TotalYearSales> totalYearSalesLostList = opportunityDao.getSalesLostByYear();
        for (TotalYearSales totalYearSales : totalYearSalesList) {
            double salesLost = 0;
            for (TotalYearSales yearSalesLost : totalYearSalesLostList) {
                if (yearSalesLost.getYear() == totalYearSales.getYear()) {
                    salesLost = yearSalesLost.getTotalSales().doubleValue();
                }
            }

            columnChart.add(String.valueOf(totalYearSales.getYear()),
                    new double[]{totalYearSales.getTotalSales().doubleValue(), salesLost});
        }

        return columnChart;
    }

    public PieChart createOpportunitySalesStageChart() {
        PieChart pieChart = new PieChart();

        pieChart.setOption("title", uiMessageSource.getMessage("sampleDashBoard.opportunitySalesStages"));
        pieChart.setOption("is3D", true);

        List<TotalSalesStage> totalSalesStages = opportunityDao.getSalesStageCounts();
        for (TotalSalesStage totalSalesStage : totalSalesStages) {
            pieChart.add(totalSalesStage.getSalesStage().getName(), totalSalesStage.getCount());
        }

        return pieChart;
    }

    private void configureRecentContactResults() {
        recentContactResults.getResultsTable().setWidth(600, Sizeable.UNITS_PIXELS);
        recentContactResults.addSelectionChangedListener(this, "contactSelectionChanged");
        recentContactResults.setPageSizeVisible(false); // restrict page size, since dashboard cells are fixed size
    }

    @Override
    public void onDisplay() {
        super.onDisplay();

        recentContactResults.search();
        if (recentContactResults.getResultsTable().getContainerDataSource().size() > 0) {
            Object firstItem = recentContactResults.getResultsTable().getContainerDataSource().getIdByIndex(0);
            recentContactResults.getResultsTable().select(firstItem);
        }

        MainApplication.getInstance().showTrayMessage(
                "<h3>Feature Tips:</h3>" +
                        "<ul>" +
                        "<li>Click on Java buttons to show code and Javadoc behind each UI component" +
                        "<li>Select different contact to update location map" +
                        "<li>Click on My Account to update your profile" +
                        "</ul>"
        );
    }

    @SuppressWarnings("unchecked")
    public void contactSelectionChanged() {
        Collection<Contact> contacts = (Collection<Contact>) recentContactResults.getSelectedValue();

        if (contacts.size() == 1) {
            Contact contact = contacts.iterator().next();
            String formattedAddress = contact.getMailingAddress().getFormatted();
            addContactLocationMap(contact.getName(), formattedAddress);
        } else {
            removeContactLocationMap(); // remove map if user selects more than one contact
        }
    }

    private void addContactLocationMap(String contactName, String formattedAddress) {
        OpenLayersMap map = mapService.createMap(formattedAddress, contactName, 16);
        if (map != null) {
            addComponent(map, uiMessageSource.getMessage("sampleDashBoard.contactLocation"), 1, 2);
        }
    }

    private void removeContactLocationMap() {
        removeComponent(1, 2);
    }
}
