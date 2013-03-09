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

import com.expressui.core.security.exception.AuthenticationException;
import com.expressui.core.view.RootComponent;
import com.expressui.core.view.menu.MainMenuBar;
import com.expressui.core.view.page.Page;
import com.expressui.sample.view.dashboard.SampleDashboardPage;
import com.expressui.sample.view.myprofile.MyProfilePage;
import com.expressui.sample.view.registration.RegistrationPage;
import com.vaadin.ui.*;
import org.apache.log4j.Logger;
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

        setSizeFull();

        LoginForm loginForm = new LoginForm();
        loginForm.addStyleName("border");
        loginForm.setSizeUndefined();
        loginForm.setLoginButtonCaption(uiMessageSource.getMessage("loginPage.button"));
        loginForm.setUsernameCaption(uiMessageSource.getMessage("loginPage.username"));
        loginForm.setPasswordCaption(uiMessageSource.getMessage("loginPage.password"));
        loginForm.addListener(new LoginHandler());

        Panel panel = new Panel();
        panel.addStyleName("loginPage");
        panel.addStyleName("border");
        panel.setSizeUndefined();
        panel.setCaption(uiMessageSource.getMessage("loginPage.caption"));
        panel.addComponent(loginForm);
        panel.addComponent(new Label(uiMessageSource.getMessage("loginPage.tip")));

        addComponent(panel);
        setComponentAlignment(panel, Alignment.MIDDLE_CENTER);
    }

    @Override
    public AbstractOrderedLayout createOrderedLayout() {
        return createHorizontalLayout();
    }

    @Override
    public void onDisplay() {
        getMainApplication().showTrayMessage(5000,
                "<h3>Feature Tips:</h3>" +
                        "<ul>" +
                        "<li>You may also register as another guest user" +
                        "<li>Download this <a href=\"https://github.com/juanosuna/expressui-framework/downloads\" " +
                        "target=\"_blank\" style=\"color:yellow\">sample app</a> to play with security permissions as an admin user" +
                        "</ul>"
        );
    }

    private class LoginHandler implements LoginForm.LoginListener {
        private final Logger log = Logger.getLogger(getClass());

        public void onLogin(LoginForm.LoginEvent event) {
            String userName = event.getLoginParameter("username");
            String password = event.getLoginParameter("password");

            try {
                securityService.login(userName, password);

                // Once logged in, hide Login and Registration pages
                mainMenuBar.getRightMenuBarRoot().getChild(LoginPage.class.getName()).setVisible(false);

                mainMenuBar.getRightMenuBarRoot().getChild(RegistrationPage.class.getName()).setVisible(false);

                // Embed login name next to My Account caption
                mainMenuBar.getRightMenuBarRoot().getChild(MyProfilePage.class.getName()).setCaption(
                        domainMessageSource.getMessage(MyProfilePage.class.getName(),
                                new Object[]{"(" + getCurrentUser().getLoginName() + ")"}));

                // Refresh menu bar so that user can now see everything they have access to
                getMainApplication().mainMenuBar.refresh();

                getMainApplication().displayPage(SampleDashboardPage.class);

            } catch (AuthenticationException e) {
                String message = uiMessageSource.getMessage("loginPage." + e.getClass().getSimpleName());
                // Show error notification when user enters bad credentials or account is locked, etc.
                getMainApplication().showNotification(message, Window.Notification.TYPE_ERROR_MESSAGE,
                        Window.Notification.POSITION_CENTERED_BOTTOM, 2000);
            } catch (Exception e) {
                log.error("Error initializing application", e);
                getMainApplication().openErrorWindow(e);
            }
        }
    }
}

