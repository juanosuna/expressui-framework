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
import com.expressui.core.util.UrlUtil;
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

import javax.servlet.http.Cookie;
import java.util.Locale;

@SuppressWarnings({"serial"})
public class SampleApplication extends MainApplication {

    public static int LOCALE_COOKIE_EXPIRATION_SECS = 60 * 60 * 24 * 365 * 5; // approximately 5 years

    @Override
    public void configureLeftMenuBar(MenuBarNode rootNode) {
        rootNode.addPage(HomePage.class);
        rootNode.addPage(SampleDashboardPage.class);
        rootNode.addPage(AccountPage.class);
        rootNode.addPage(OpportunityPage.class);
        rootNode.addPage(ContactPage.class);

        MenuBarNode securityNode = rootNode.addCaption(getClass().getName() + "." + "security");
        securityNode.addPage(UserPage.class);
        securityNode.addPage(RolePage.class);
        securityNode.addPage(ProfilePage.class);
    }

    @Override
    public void configureRightMenuBar(MenuBarNode rootNode) {
        MenuBarNode languageNode = rootNode.addCaption(getClass().getName() + "." + "language");
        languageNode.addCommand(this, "setEnglish");
        languageNode.addCommand(this, "setGerman");

        MenuBarNode myAccountNode = rootNode.addPage(MyProfilePage.class);
        myAccountNode.addCommand(this, "logout");

        rootNode.addPage(LoginPage.class);
        rootNode.addPage(RegistrationPage.class);
    }

    public void setEnglish() {
        addCookie("locale", Locale.US.toString(), LOCALE_COOKIE_EXPIRATION_SECS);
        logout();
    }

    public void setGerman() {
        addCookie("locale", Locale.GERMANY.toString(), LOCALE_COOKIE_EXPIRATION_SECS);
        logout();
    }

    @Override
    public String getCustomTheme() {
        return "sample";
    }

    @Override
    public void init() {
        Cookie localeCookie = getCookie("locale");
        if (localeCookie != null) {
            String[] localeParts = localeCookie.getValue().split("_");
            if (localeParts.length == 2) {
                setLocale(new Locale(localeParts[0], localeParts[1]));
            }
        }

        super.init();

        try {
            securityService.login("anonymous", "anonymous");
        } catch (AuthenticationException e) {
            throw new RuntimeException("Anonymous account login failed", e);
        }
        displayPage(LoginPage.class);
        mainMenuBar.refresh();

        checkInternetConnectivity(UrlUtil.EXPRESSUI_TEST_PAGE,
                uiMessageSource.getMessage("sampleApplication.internetConnectivityError"));
    }
}
