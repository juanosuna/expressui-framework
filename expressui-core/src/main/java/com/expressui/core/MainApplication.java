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

package com.expressui.core;

import com.expressui.core.security.SecurityService;
import com.expressui.core.view.CrudResults;
import com.expressui.core.view.Dashboard;
import com.expressui.core.view.MainEntryPoint;
import com.expressui.core.view.MainEntryPoints;
import com.expressui.core.view.util.MessageSource;
import com.vaadin.Application;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.terminal.gwt.server.HttpServletRequestListener;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ChameleonTheme;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.AccessDeniedException;
import org.vaadin.dialogs.ConfirmDialog;
import org.vaadin.dialogs.DefaultConfirmDialogFactory;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * Main Vaadin Application that is tied to the user's session. The user's MainApplication
 * is always tied to the current thread and can be looked up by calling getInstance().
 * Instance of MainEntryPoints is injected into the MainApplication and used to launch
 * the application.
 */
public class MainApplication extends Application implements HttpServletRequestListener {

    private static final int WARNING_PERIOD_MINS = 2;

    private static ThreadLocal<MainApplication> threadLocal = new ThreadLocal<MainApplication>();

    @Resource(name = "uiMessageSource")
    private MessageSource messageSource;

    @Resource
    private Dashboard dashboard;

    @Resource
    private MainEntryPoints mainEntryPoints;

    @Resource
    private SecurityService securityService;

    @Resource
    protected MessageSource uiMessageSource;

    private Button logoutButton;

    /**
     * Get instance of MainApplication associated with current session.
     * The user's MainApplication is always tied to the current thread and can be looked up by calling getInstance().
     *
     * @return MainApplication associated with user's session
     */
    public static MainApplication getInstance() {
        return threadLocal.get();
    }

    private static void setInstance(MainApplication application) {
        if (getInstance() == null) {
            threadLocal.set(application);
        }
    }

    /**
     * Get the main entry points to the application that comprise the "home page"
     *
     * @return MainEntryPoints
     */
    public MainEntryPoints getMainEntryPoints() {
        return mainEntryPoints;
    }

    @Override
    public void onRequestStart(HttpServletRequest request, HttpServletResponse response) {
        MainApplication.setInstance(this);
    }

    @Override
    public void onRequestEnd(HttpServletRequest request, HttpServletResponse response) {
        threadLocal.remove();
    }

    @Override
    public void init() {
        setInstance(this);

        setTheme(mainEntryPoints.getTheme());
        customizeConfirmDialogStyle();

        Window mainWindow = new Window(messageSource.getMessage("mainApplication.caption"));
        mainWindow.addStyleName("p-main-window");
        mainWindow.getContent().setSizeUndefined();
        setMainWindow(mainWindow);

        VerticalLayout verticalLayout = new VerticalLayout();
        verticalLayout.setSizeUndefined();
        mainWindow.setContent(verticalLayout);

        logoutButton = new Button(null);
        logoutButton.setDescription(uiMessageSource.getMessage("mainApplication.logout"));
        logoutButton.setSizeUndefined();
        logoutButton.addStyleName("borderless");
        logoutButton.setIcon(new ThemeResource("icons/16/logout.png"));
        setLogoutURL("mvc/login.do");
        logoutButton.addListener(Button.ClickEvent.class, MainApplication.getInstance(), "logout");

        verticalLayout.addComponent(logoutButton);
        verticalLayout.setComponentAlignment(logoutButton, Alignment.TOP_RIGHT);

        verticalLayout.addComponent(createTabSheet());

//        SessionGuard sessionGuard = new SessionGuard();
//        sessionGuard.setTimeoutWarningPeriod(WARNING_PERIOD_MINS);
//        mainWindow.addComponent(sessionGuard);

        dashboard.postWire();
        mainEntryPoints.postWire();
    }

    private TabSheet createTabSheet() {
        TabSheet tabSheet = new TabSheet();
        tabSheet.addStyleName("p-main-entry-points");
        tabSheet.setSizeUndefined();

        tabSheet.addTab(dashboard);
        List<MainEntryPoint> entryPoints = mainEntryPoints.getViewableEntryPoints();
        for (MainEntryPoint entryPoint : entryPoints) {
            tabSheet.addTab(entryPoint);
        }

        tabSheet.addListener(new TabChangeListener());
        if (entryPoints.size() > 0) {
            entryPoints.get(0).getResults().search();
        }

        return tabSheet;
    }

    private class TabChangeListener implements TabSheet.SelectedTabChangeListener {

        @Override
        public void selectedTabChange(TabSheet.SelectedTabChangeEvent event) {
            if (event.getTabSheet().getSelectedTab() instanceof MainEntryPoint) {
                MainEntryPoint entryPoint = (MainEntryPoint) event.getTabSheet().getSelectedTab();
                entryPoint.getResults().search();
                if (entryPoint.getResults() instanceof CrudResults) {
                    ((CrudResults) entryPoint.getResults()).applySecurityToCRUDButtons();
                }
            }
        }
    }

    private void customizeConfirmDialogStyle() {
        ConfirmDialog.Factory confirmDialogFactory = new DefaultConfirmDialogFactory() {
            @Override
            public ConfirmDialog create(String caption, String message,
                                        String okCaption, String cancelCaption) {
                ConfirmDialog confirmDialog;
                confirmDialog = super.create(caption, message, okCaption, cancelCaption);
                confirmDialog.setStyleName(ChameleonTheme.WINDOW_OPAQUE);
                confirmDialog.getOkButton().addStyleName("small default");
                confirmDialog.getCancelButton().addStyleName("small default");

                return confirmDialog;
            }
        };
        ConfirmDialog.setFactory(confirmDialogFactory);
    }

    @Override
    public void terminalError(com.vaadin.terminal.Terminal.ErrorEvent event) {
        super.terminalError(event);
        Throwable cause = event.getThrowable().getCause();

        if (cause instanceof AccessDeniedException) {
            getMainWindow().showNotification(
                    messageSource.getMessage("mainApplication.accessDenied"),
                    Window.Notification.TYPE_ERROR_MESSAGE);
        } else if (cause instanceof DataIntegrityViolationException) {
            DataIntegrityViolationException violationException = (DataIntegrityViolationException) cause;
            getMainWindow().showNotification(
                    messageSource.getMessage("mainApplication.dataConstraintViolation"),
                    violationException.getMessage(),
                    Window.Notification.TYPE_ERROR_MESSAGE);
        } else if (cause instanceof ConstraintViolationException) {
            ConstraintViolationException violationException = (ConstraintViolationException) cause;
            getMainWindow().showNotification(
                    messageSource.getMessage("mainApplication.dataConstraintViolation"),
                    violationException.getMessage(),
                    Window.Notification.TYPE_ERROR_MESSAGE);
        } else {
            String fullStackTrace = ExceptionUtils.getFullStackTrace(event.getThrowable());
            openErrorWindow(fullStackTrace);
        }
    }

    /**
     * Show big error box to user.
     *
     * @param errorMessage
     */
    public void showError(String errorMessage) {
        getMainWindow().showNotification(errorMessage, Window.Notification.TYPE_ERROR_MESSAGE);
    }

    /**
     * Show big warning box to user.
     *
     * @param warningMessage
     */
    public void showWarning(String warningMessage) {
        getMainWindow().showNotification(warningMessage, Window.Notification.TYPE_WARNING_MESSAGE);
    }

    public static SystemMessages getSystemMessages() {
        CustomizedSystemMessages customizedSystemMessages = new CustomizedSystemMessages();
        customizedSystemMessages.setSessionExpiredURL("mvc/login.do");
        customizedSystemMessages.setCommunicationErrorURL("mvc/login.do");
        customizedSystemMessages.setOutOfSyncURL("mvc/login.do");
        return customizedSystemMessages;
    }

    /**
     * Open separate error Window, useful for showing stacktraces.
     *
     * @param message
     */
    public void openErrorWindow(String message) {
        Window errorWindow = new Window("Error");
        errorWindow.addStyleName("opaque");
        VerticalLayout layout = (VerticalLayout) errorWindow.getContent();
        layout.setSpacing(true);
        layout.setWidth("100%");
        errorWindow.setWidth("100%");
        errorWindow.setModal(true);
        Label label = new Label(message);
        label.setContentMode(Label.CONTENT_PREFORMATTED);
        layout.addComponent(label);
        errorWindow.setClosable(true);
        errorWindow.setScrollable(true);
        MainApplication.getInstance().getMainWindow().addWindow(errorWindow);
    }

    /**
     * Logout of application, clear credentials and end user session.
     */
    public void logout() {
        securityService.logout();
        close();
    }
}
