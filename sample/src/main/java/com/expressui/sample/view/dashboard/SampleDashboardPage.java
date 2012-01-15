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

package com.expressui.sample.view.dashboard;

import com.expressui.core.view.page.DashboardPage;
import com.expressui.domain.geocode.MapService;
import com.expressui.sample.dao.OpportunityDao;
import com.expressui.sample.entity.Address;
import com.expressui.sample.entity.Contact;
import com.expressui.sample.entity.derived.TotalSalesStage;
import com.expressui.sample.entity.derived.TotalYearSales;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.vaadin.vaadinvisualizations.ColumnChart;
import org.vaadin.vaadinvisualizations.PieChart;
import org.vaadin.vol.OpenLayersMap;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;

@Component
@Scope("session")
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

        addComponent(createOpportunityChartByYear(), "Sales - Won & Lost", 1, 1);

        recentContactResults.setWidth("625px");
        recentContactResults.setHeight("350px");
        addComponent(recentContactResults, "Recent Contacts", 1, 2);
        recentContactResults.addSelectionChangedListener(this, "contactSelectionChanged");
        recentContactResults.setPageSizeVisible(false);

        if (recentContactResults.getResultsTable().getContainerDataSource().size() > 0) {
            Object firstItem = recentContactResults.getResultsTable().getContainerDataSource().getIdByIndex(0);
            recentContactResults.getResultsTable().select(firstItem);
        }

        addComponent(createOpportunitySalesStageChart(), "Sales Stage Breakdown", 2, 1);
    }

    @Override
    public void postWire() {
        super.postWire();
        recentContactResults.postWire();
    }

    public void contactSelectionChanged() {
        Collection<Contact> contacts = (Collection<Contact>) recentContactResults.getSelectedValue();

        if (contacts.size() == 1) {
            Contact contact = contacts.iterator().next();
            Address mailingAddress = contact.getMailingAddress();
            String address = mailingAddress.getStreet() + ", "
                    + mailingAddress.getCity() + ",";
            if (mailingAddress.getState() != null) {
                address += mailingAddress.getState().getCode() + ", ";
            }
            address += mailingAddress.getZipCode() + ", ";
            address += mailingAddress.getCountry().getDisplayName();

            OpenLayersMap map = mapService.createMap(address, contact.getName(), 16);
            if (map != null) {
                map.setWidth("625px");
                addComponent(map, "Contact Location", 2, 2);
            }
        } else {
            removeComponent(2, 2);
        }
    }

    private ColumnChart createOpportunityChartByYear() {
        List<TotalYearSales> totalYearSalesList = opportunityDao.getSalesByYear();
        List<TotalYearSales> totalYearSalesLostList = opportunityDao.getSalesLostByYear();

        ColumnChart columnChart = new ColumnChart();
        columnChart.setOption("is3D", true);
        columnChart.setOption("isStacked", false);
        columnChart.setOption("width", 525);
        columnChart.setOption("height", 350);

        columnChart.addXAxisLabel("Year");
        columnChart.addColumn("Closed Won");
        columnChart.addColumn("Closed Lost");

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
        columnChart.setWidth("525px");
        columnChart.setHeight("350px");

        return columnChart;
    }

    public PieChart createOpportunitySalesStageChart() {
        PieChart pc = new PieChart();

        pc.setWidth("525px");
        pc.setHeight("350px");

        List<TotalSalesStage> totalSalesStages = opportunityDao.getSalesStageCounts();
        for (TotalSalesStage totalSalesStage : totalSalesStages) {
            pc.add(totalSalesStage.getSalesStage().getDisplayName(), totalSalesStage.getCount());
        }
        pc.setOption("title", "Opportunity Sales Stages");
        pc.setOption("width", 525);
        pc.setOption("height", 350);
        pc.setOption("is3D", true);

        return pc;
    }
}
