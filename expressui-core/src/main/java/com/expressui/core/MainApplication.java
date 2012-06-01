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

package com.expressui.core;

import com.expressui.core.entity.security.User;
import com.expressui.core.security.SecurityService;
import com.expressui.core.util.*;
import com.expressui.core.view.TypedComponent;
import com.expressui.core.view.ViewBean;
import com.expressui.core.view.field.LabelRegistry;
import com.expressui.core.view.menu.MainMenuBar;
import com.expressui.core.view.menu.MenuBarNode;
import com.expressui.core.view.page.Page;
import com.expressui.core.view.page.PageConversation;
import com.expressui.core.view.page.SearchPage;
import com.expressui.core.view.results.CrudResults;
import com.expressui.core.view.util.MessageSource;
import com.github.wolfie.sessionguard.SessionGuard;
import com.vaadin.Application;
import com.vaadin.terminal.ExternalResource;
import com.vaadin.terminal.gwt.server.HttpServletRequestListener;
import com.vaadin.terminal.gwt.server.WebApplicationContext;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ChameleonTheme;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.context.annotation.Scope;
import org.springframework.dao.DataIntegrityViolationException;
import org.vaadin.dialogs.ConfirmDialog;
import org.vaadin.dialogs.DefaultConfirmDialogFactory;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Main Vaadin Application, which is tied to the user's session. The user's MainApplication
 * is also tied to the current thread and can be looked up by calling getInstance().
 */
public abstract class MainApplication extends Application implements ViewBean, HttpServletRequestListener {

    private static ThreadLocal<MainApplication> currentInstance = new ThreadLocal<MainApplication>();

    /**
     * Service for logging in/out and getting the current user. The current user entity
     * provides access to roles and permissions.
     */
    @Resource
    public SecurityService securityService;

    /**
     * Provides access to internationalized messages.
     */
    @Resource
    public MessageSource uiMessageSource;

    /**
     * A registry for managing UI display labels.
     */
    @Resource
    public LabelRegistry labelRegistry;

    /**
     * Main menu bar displayed for navigation.
     */
    @Resource
    public MainMenuBar mainMenuBar;

    private boolean codePopupEnabled = false;

    private TabSheet pageLayoutTabSheet;

    private Class<? extends Page> currentPageClass;
    private Class<? extends Page> previousPageClass;

    private PageConversation currentPageConversation;

    public abstract void configureLeftMenuBar(MenuBarNode rootNode);

    public abstract void configureRightMenuBar(MenuBarNode rootNode);

    public String getCustomTheme() {
        return "expressui";
    }

    /**
     * Get the current page conversation.
     *
     * @return current page conversation
     */
    public PageConversation getCurrentPageConversation() {
        return currentPageConversation;
    }

    /**
     * Begin a new page conversation.
     *
     * @param id unique id of conversation
     * @return newly created page conversation
     */
    public PageConversation beginPageConversation(String id) {
        currentPageConversation = new PageConversation(id);

        return currentPageConversation;
    }

    /**
     * End the current page conversation.
     */
    public void endPageConversation() {
        currentPageConversation = null;
    }

    @PostConstruct
    @Override
    public void postConstruct() {
        MainApplication.setInstance(this);
    }

    @Override
    public void postWire() {
    }

    @Override
    public void onDisplay() {
        mainMenuBar.onDisplay();
    }

    /**
     * Get instance of MainApplication associated with current session.
     * The user's MainApplication is always tied to the current thread and can be looked up by calling getInstance().
     *
     * @return MainApplication associated with user's session
     */
    public static MainApplication getInstance() {
        return currentInstance.get();
    }

    private static void setInstance(MainApplication application) {
        currentInstance.set(application);
    }

    @Override
    public void onRequestStart(HttpServletRequest request, HttpServletResponse response) {
        MainApplication.setInstance(this);
        if (securityService.getCurrentUser() != null) {
            SecurityService.setCurrentLoginName(securityService.getCurrentUser().getLoginName());
        }
    }

    @Override
    public void onRequestEnd(HttpServletRequest request, HttpServletResponse response) {
        currentInstance.remove();
        SecurityService.removeCurrentLoginName();
    }

    @Override
    public void init() {
        setInstance(this);

        setTheme(getCustomTheme());
        customizeConfirmDialogStyle();

        Window mainWindow = new Window(uiMessageSource.getMessage("mainApplication.caption"));
        mainWindow.addStyleName("e-main-window");
        setMainWindow(mainWindow);

        VerticalLayout mainLayout = new VerticalLayout();
        String id = StringUtil.generateDebugId("e", this, mainLayout, "mainLayout");
        mainLayout.setDebugId(id);

        mainWindow.setSizeFull();
        mainLayout.setSizeFull();
        mainWindow.setContent(mainLayout);

        setLogoutURL(getApplicationProperties().getRestartApplicationUrl());

        configureLeftMenuBar(mainMenuBar.getLeftMenuBarRoot());
        configureRightMenuBar(mainMenuBar.getRightMenuBarRoot());
        mainLayout.addComponent(mainMenuBar);

        pageLayoutTabSheet = new TabSheet();
        id = StringUtil.generateDebugId("e", this, pageLayoutTabSheet, "pageLayoutTabSheet");
        pageLayoutTabSheet.setDebugId(id);

        pageLayoutTabSheet.addStyleName("e-main-page-layout");
        pageLayoutTabSheet.setSizeFull();
        mainLayout.addComponent(pageLayoutTabSheet);
        mainLayout.setExpandRatio(pageLayoutTabSheet, 1.0f);

        Link expressUILink = new Link(uiMessageSource.getMessage("mainApplication.footerMessage"),
                new ExternalResource(uiMessageSource.getMessage("mainApplication.footerLink")));
//        expressUILink.setIcon(new ThemeResource("../expressui/img/expressui_logo.png"));
        expressUILink.setSizeUndefined();
        mainLayout.addComponent(expressUILink);
        mainLayout.setComponentAlignment(expressUILink, Alignment.TOP_CENTER);

        configureSessionTimeout(mainWindow);
        postWire();
        onDisplay();
    }

    private void configureSessionTimeout(Window mainWindow) {
        ((WebApplicationContext) getContext())
                .getHttpSession().setMaxInactiveInterval(getApplicationProperties().getSessionTimeout() * 60);

        SessionGuard sessionGuard = new SessionGuard();
        Integer timeoutWarningPeriod = getApplicationProperties().getSessionTimeoutWarning();
        sessionGuard.setTimeoutWarningPeriod(timeoutWarningPeriod);
        sessionGuard.setTimeoutWarningXHTML(uiMessageSource.getMessage("mainApplication.timeoutWarning",
                new Object[]{timeoutWarningPeriod}));
        mainWindow.addComponent(sessionGuard);
    }

    /**
     * Select a new page base on page's class. Does nothing if user does not have permission to access the page.
     * If the previous page is Page.SCOPE_PAGE, then it is removed from Spring's ApplicationContext and the UI.
     * If the previous page is WebApplicationContext.SESSION_SCOPE, then it is retained in Spring's ApplicationContext
     * and in the UI as a hidden component in an unselected tab.
     * </P>
     * WebApplicationContext.SESSION_SCOPE pages display more quickly at the cost of using more memory.
     *
     * @param pageClass class of the new page to select
     */
    public void displayPage(Class<? extends Page> pageClass) {
        User currentUser = securityService.getCurrentUser();

        if (!currentUser.isViewAllowed(pageClass.getName())) {
            showError(uiMessageSource.getMessage("mainApplication.notAllowed"));
            return;
        }

        Page page = loadPageBean(pageClass);

        if (page instanceof TypedComponent && !((TypedComponent) page).isViewAllowed()) {
            showError(uiMessageSource.getMessage("mainApplication.notAllowed"));
            return;
        }

        if (!ObjectUtil.isEqual(currentPageClass, pageClass)) {
            previousPageClass = currentPageClass;
            currentPageClass = pageClass;
        }

        Page previousPage = (Page) pageLayoutTabSheet.getSelectedTab();
        if (previousPage != null) {
            Scope scope = previousPage.getClass().getAnnotation(Scope.class);
            if (scope != null && scope.value().equals(Page.SCOPE_PAGE)) {
                pageLayoutTabSheet.removeComponent(previousPage);
            }
        }

        boolean componentFound = false;
        Iterator<Component> components = pageLayoutTabSheet.getComponentIterator();
        while (components.hasNext()) {
            Component component = components.next();
            if (page.equals(component)) {
                pageLayoutTabSheet.setSelectedTab(component);
                componentFound = true;
            }
        }

        if (!componentFound) {
            page.postWire();
            pageLayoutTabSheet.addTab(page);
            pageLayoutTabSheet.setSelectedTab(page);
        }

        if (page instanceof SearchPage) {
            SearchPage searchPage = (SearchPage) page;
            searchPage.getResults().search();
            if (searchPage.getResults() instanceof CrudResults) {
                ((CrudResults) searchPage.getResults()).applySecurityToCRUDButtons();
            }
        }

        page.onDisplay();
    }

    /**
     * Ask of user is allowed to view page of given type.
     *
     * @param pageClass class of type Page
     * @return true if user is allowed.
     */
    public boolean isPageViewAllowed(Class<? extends Page> pageClass) {

        User currentUser = securityService.getCurrentUser();
        Class genericType = ReflectionUtil.getGenericArgumentType(pageClass);
        return currentUser.isViewAllowed(pageClass.getName()) && currentUser.isViewAllowed(genericType.getName());
    }

    /**
     * Navigates to the previously selected page, sort of like using back button, except only keeps a history of one
     * page.
     */
    public void selectPreviousPage() {
        if (previousPageClass != null) {
            displayPage(previousPageClass);
        }
    }

    /**
     * Forces all page beans to be loaded. This is useful for admin managing security permissions. In this case,
     * all pages must be loaded so that security becomes aware of the components whose permissions
     * can be altered.
     */
    public void loadAllPageBeans() {
        Map<String, String> typeLabels = labelRegistry.getTypeLabels();
        Set<Class> classes = new HashSet<Class>();
        for (String type : typeLabels.keySet()) {
            try {
                Class clazz = Class.forName(type);
                classes.add(clazz);
            } catch (ClassNotFoundException e) {
                // ignore for menu bar labels that have NullCommand
            }
        }

        for (Class clazz : classes) {
            if (Page.class.isAssignableFrom(clazz)) {
                loadPageBean(clazz);
            }
        }
    }

    private Page loadPageBean(Class<? extends Page> pageClass) {
        beginPageConversation(pageClass.getName());

        return SpringApplicationContext.getBean(pageClass);
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

        if (cause instanceof DataIntegrityViolationException) {
            DataIntegrityViolationException violationException = (DataIntegrityViolationException) cause;
            getMainWindow().showNotification(
                    uiMessageSource.getMessage("mainApplication.dataConstraintViolation"),
                    violationException.getMessage(),
                    Window.Notification.TYPE_ERROR_MESSAGE);
        } else if (cause instanceof ConstraintViolationException) {
            ConstraintViolationException violationException = (ConstraintViolationException) cause;
            getMainWindow().showNotification(
                    uiMessageSource.getMessage("mainApplication.dataConstraintViolation"),
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

    /**
     * Should message box to user.
     *
     * @param humanizedMessage
     */
    public void showMessage(String humanizedMessage) {
        getMainWindow().showNotification(humanizedMessage, Window.Notification.TYPE_HUMANIZED_MESSAGE);
    }

    /**
     * Show notification to user, more customizable that other show* methods.
     *
     * @param notification customized notification
     */
    public void showNotification(Window.Notification notification) {
        getMainWindow().showNotification(notification);
    }

    /**
     * Show a yes/no confirmation dialog box.
     *
     * @param listener listener to capture whether user chooses yes or no
     */
    public void showConfirmationDialog(ConfirmDialog.Listener listener) {
        ConfirmDialog.show(getMainWindow(),
                uiMessageSource.getMessage("mainApplication.confirmationCaption"),
                uiMessageSource.getMessage("mainApplication.confirmationPrompt"),
                uiMessageSource.getMessage("mainApplication.confirmationYes"),
                uiMessageSource.getMessage("mainApplication.confirmationNo"),
                listener);
    }

    /**
     * Configures various messages to embed ApplicationProperties.restartApplicationUrl when anything goes wrong,
     * e.g. session expires, communication error, out of sync, etc.
     * </P>
     * Vaadin automatically calls this method.
     *
     * @return configured SystemMessages
     */
    public static SystemMessages getSystemMessages() {
        String restartUrl = getApplicationProperties().getRestartApplicationUrl();
        CustomizedSystemMessages customizedSystemMessages = new CustomizedSystemMessages();
        customizedSystemMessages.setSessionExpiredURL(restartUrl);
        customizedSystemMessages.setCommunicationErrorURL(restartUrl);
        customizedSystemMessages.setOutOfSyncURL(restartUrl);
        return customizedSystemMessages;
    }

    private static ApplicationProperties getApplicationProperties() {
        return (ApplicationProperties) SpringApplicationContext.getBean("applicationProperties");
    }

    /**
     * Open separate error Window, useful for showing long stacktraces.
     *
     * @param message to display in error Window
     */
    public void openErrorWindow(String message) {
        Window errorWindow = new Window(uiMessageSource.getMessage("mainApplication.errorWindowCaption"));
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
        getMainWindow().addWindow(errorWindow);
    }

    /**
     * Logout of application, clear credentials and end user session.
     */
    public void logout() {
        securityService.logout();
        close();
        invalidateSession();
    }

    private void invalidateSession() {
        WebApplicationContext context = (WebApplicationContext) getContext();
        HttpSession httpSession = context.getHttpSession();
        httpSession.invalidate();
    }

    /**
     * Ask if code popups are enabled, useful for demo apps only.
     *
     * @return true if code popups are enabled
     */
    public boolean isCodePopupEnabled() {
        return codePopupEnabled;
    }

    /**
     * Set if code popups are enabled.
     *
     * @param codePopupEnabled true if code popups are enabled
     */
    public void setCodePopupEnabled(boolean codePopupEnabled) {
        this.codePopupEnabled = codePopupEnabled;
    }

    /**
     * Check if application has access to external Internet, e.g. there is no firewall or proxy interference.
     *
     * @param testUrl      url to test
     * @param errorMessage error message if test fails
     */
    public void checkInternetConnectivity(String testUrl, String errorMessage) {
        try {
            UrlUtil.getContents(testUrl);
        } catch (Exception e) {
            MainApplication.getInstance().showError(errorMessage);
        }
    }
}
