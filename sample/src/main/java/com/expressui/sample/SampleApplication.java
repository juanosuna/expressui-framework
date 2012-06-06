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

package com.expressui.sample;

import com.expressui.core.MainApplication;
import com.expressui.core.security.exception.AuthenticationException;
import com.expressui.core.view.menu.MenuBarNode;
import com.expressui.core.view.security.role.RolePage;
import com.expressui.core.view.security.user.UserPage;
import com.expressui.sample.view.HomePage;
import com.expressui.sample.view.LoginPage;
import com.expressui.sample.view.account.AccountPage;
import com.expressui.sample.view.contact.ContactPage;
import com.expressui.sample.view.dashboard.SampleDashboardPage;
import com.expressui.sample.view.myprofile.MyProfilePage;
import com.expressui.sample.view.opportunity.OpportunityPage;
import com.expressui.sample.view.profile.ProfilePage;
import com.expressui.sample.view.registration.RegistrationPage;

public class SampleApplication extends MainApplication {

    @Override
    public void configureLeftMenuBar(MenuBarNode rootNode) {
        rootNode.addPage("Home", HomePage.class);
        rootNode.addPage("Dashboard", SampleDashboardPage.class);
        rootNode.addPage("Accounts", AccountPage.class);
        rootNode.addPage("Opportunities", OpportunityPage.class);
        rootNode.addPage("Contacts", ContactPage.class);

        MenuBarNode securityNode = rootNode.addCaption("Security");
        securityNode.addPage("Users", UserPage.class);
        securityNode.addPage("Roles", RolePage.class);
        securityNode.addPage("Profiles", ProfilePage.class);
    }

    @Override
    public void configureRightMenuBar(MenuBarNode rootNode) {
        MenuBarNode myAccountNode = rootNode.addPage("My Account", MyProfilePage.class);
        myAccountNode.addCommand("Logout", this, "logout");

        rootNode.addPage("Login", LoginPage.class);
        rootNode.addPage("Register", RegistrationPage.class);
    }

    @Override
    public String getCustomTheme() {
        return "sample";
    }

    @Override
    public void init() {
        super.init();

        try {
            securityService.login("anonymous", "anonymous");
        } catch (AuthenticationException e) {
            throw new RuntimeException("Anonymous account login failed");
        }
        displayPage(LoginPage.class);
        mainMenuBar.refresh();

        checkInternetConnectivity("http://www.google.com",
                "The sample application requires an Internet connection.</br>If it is running behind a proxy," +
                        " please configure http.proxyHost and http.proxyPort in application.properties.");
    }
}
