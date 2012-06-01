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

package com.expressui.sample.view;

import com.expressui.core.security.exception.*;
import com.expressui.core.view.RootComponent;
import com.expressui.core.view.menu.MainMenuBar;
import com.expressui.core.view.page.Page;
import com.expressui.sample.view.dashboard.SampleDashboardPage;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.LoginForm;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Window;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import static com.expressui.core.view.page.Page.SCOPE_PAGE;

@Component
@Scope(SCOPE_PAGE)
@SuppressWarnings({"serial"})
public class LoginPage extends RootComponent implements Page {

    @Resource
    private MainMenuBar mainMenuBar;

    @PostConstruct
    @Override
    public void postConstruct() {
        super.postConstruct();

        useHorizontalLayout();
        setSizeFull();

        LoginForm loginForm = new LoginForm();
        loginForm.addStyleName("border");
        loginForm.setSizeUndefined();
        loginForm.setLoginButtonCaption("Login");
        loginForm.setUsernameCaption("Username");
        loginForm.setPasswordCaption("Password");
        loginForm.addListener(new LoginHandler());

        Panel panel = new Panel();
        panel.addStyleName("loginPage");
        panel.setCaption("Login");
        panel.addStyleName("border");
        panel.setSizeUndefined();
        panel.addComponent(loginForm);

        addComponent(panel);
        setComponentAlignment(panel, Alignment.MIDDLE_CENTER);
    }

    @Override
    public void onDisplay() {
    }

    private class LoginHandler implements LoginForm.LoginListener {
        public void onLogin(LoginForm.LoginEvent event) {
            String userName = event.getLoginParameter("username");
            String password = event.getLoginParameter("password");

            try {
                securityService.login(userName, password);

                getMainApplication().setCodePopupEnabled(true);

                // Once logged in, hide Login and Registration pages
                mainMenuBar.getRightMenuBarRoot().getChild("Login").setVisible(false);
                mainMenuBar.getRightMenuBarRoot().getChild("Register").setVisible(false);
                mainMenuBar.getRightMenuBarRoot().getChild("My Account").setCaption("My Account ("
                        + securityService.getCurrentUser().getLoginName() + ")");

                // Refresh menu bar so that user can now see everything they have access to
                getMainApplication().mainMenuBar.refresh();

                getMainApplication().displayPage(SampleDashboardPage.class);

            } catch (AuthenticationException e) {
                // Show error notification when user enters bad credentials or account is locked, etc.
                Window.Notification notification = new Window.Notification(convertAuthenticationExceptionToMessage(e),
                        Window.Notification.TYPE_ERROR_MESSAGE);
                notification.setPosition(Window.Notification.POSITION_CENTERED_BOTTOM);
                notification.setDelayMsec(2000);
                getMainApplication().showNotification(notification);
            }
        }
    }

    private String convertAuthenticationExceptionToMessage(AuthenticationException e) {
        if (e instanceof IncorrectCredentialsException || e instanceof LoginNameNotFoundException) {
            return "Invalid username or password";
        } else if (e instanceof AccountExpiredException) {
            return "Account expired";
        } else if (e instanceof CredentialsExpiredException) {
            return "Credentials expired";
        } else if (e instanceof AccountExpiredException) {
            return "Account has expired";
        } else if (e instanceof AccountLockedException) {
            return "Account has been locked";
        } else if (e instanceof AccountDisabledException) {
            return "Account has been disabled";
        } else {
            return "Authentication failed";
        }
    }
}

