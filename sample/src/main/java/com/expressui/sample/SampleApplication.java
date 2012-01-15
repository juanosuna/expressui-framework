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

package com.expressui.sample;

import com.expressui.core.MainApplication;
import com.expressui.core.security.SecurityService;
import com.expressui.core.view.menu.MainMenuBar;
import com.expressui.core.view.security.role.RolePage;
import com.expressui.core.view.security.user.UserPage;
import com.expressui.domain.ecbfx.EcbfxService;
import com.expressui.sample.view.HomePage;
import com.expressui.sample.view.LoginPage;
import com.expressui.sample.view.account.AccountPage;
import com.expressui.sample.view.contact.ContactPage;
import com.expressui.sample.view.dashboard.SampleDashboardPage;
import com.expressui.sample.view.opportunity.OpportunityPage;
import com.expressui.sample.view.profile.ProfilePage;

import javax.annotation.Resource;
import javax.persistence.Transient;

public class SampleApplication extends MainApplication {

    @Resource
    private SecurityService securityService;

    @Resource
    @Transient
    private EcbfxService ecbfxService;

    @Override
    public void configureLeftMenuBar(MainMenuBar mainMenuBar) {
        mainMenuBar.addPage("Home", HomePage.class);
        mainMenuBar.addPage("Dashboard", SampleDashboardPage.class);
        mainMenuBar.addPage("Accounts", AccountPage.class).addPage("Opportunities", OpportunityPage.class);
        mainMenuBar.addPage("Contacts", ContactPage.class);
        mainMenuBar.addPage("Users", UserPage.class).addPage("Roles", RolePage.class);

    }

    @Override
    public void configureRightMenuBar(MainMenuBar mainMenuBar) {
        mainMenuBar.addPage("Profile", ProfilePage.class);

        if (securityService.getCurrentUser().getLoginName().equals("anonymous")) {
            mainMenuBar.addPage("Login", LoginPage.class);
        } else {
            mainMenuBar.addCommand("Logout", this, "logout");
        }
    }

    @Override
    public String getCustomTheme() {
        return "sampleTheme";
    }

    @Override
    public void postWire() {
        securityService.login("anonymous", "anonymous");

        super.postWire();

        try {
            ecbfxService.getFXRates();
        } catch (Exception e) {
            MainApplication.getInstance().showWarning("I can't seem to fetch FX rates from an external REST service hosted at European" +
                    " Central Bank. Please see application.properties. You may need to set an HTTP proxy address." +
                    " In the meantime, click this box to make it disappear.");
        }

        selectPage(HomePage.class);
    }
}
