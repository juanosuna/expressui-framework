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
import com.expressui.core.view.util.MessageSource;
import com.github.wolfie.sessionguard.SessionGuard;
import com.vaadin.Application;
import com.vaadin.terminal.ExternalResource;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.terminal.gwt.server.HttpServletRequestListener;
import com.vaadin.terminal.gwt.server.WebApplicationContext;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ChameleonTheme;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.context.annotation.Scope;
import org.springframework.dao.DataIntegrityViolationException;
import org.vaadin.dialogs.ConfirmDialog;
import org.vaadin.dialogs.DefaultConfirmDialogFactory;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.persistence.EntityNotFoundException;
import javax.servlet.http.Cookie;
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

    private static ThreadLocal<HttpServletRequest> currentRequest = new ThreadLocal<HttpServletRequest>();

    private static ThreadLocal<HttpServletResponse> currentResponse = new ThreadLocal<HttpServletResponse>();

    private static ThreadLocal<MainApplication> currentInstance = new ThreadLocal<MainApplication>();

    private final Logger log = Logger.getLogger(getClass());

    /**
     * Application properties defined in application.properties
     */
    @Resource
    public ApplicationProperties applicationProperties;

    /**
     * Service for logging in/out and getting the current user. The current user entity
     * provides access to roles and permissions.
     */
    @Resource
    public SecurityService securityService;

    /**
     * Provides access to internationalized UI messages.
     */
    @Resource
    public MessageSource uiMessageSource;

    /**
     * Provides messages (display labels) associated with domain-level entities.
     */
    @Resource
    public MessageSource domainMessageSource;

    /**
     * Provides validation error messages.
     */
    @Resource
    public MessageSource validationMessageSource;

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

    private TabSheet pageLayoutTabSheet;

    private Class<? extends Page> currentPageClass;
    private Class<? extends Page> previousPageClass;

    private PageConversation currentPageConversation;

    public abstract void configureLeftMenuBar(MenuBarNode rootNode);

    public abstract void configureRightMenuBar(MenuBarNode rootNode);

    /**
     * Gets the custom style theme for this application. Normally, applications would want to override this.
     * For example, a custom theme called "sample" would activate a style sheet found at
     * VAADIN/themes/sample/styles.css. Any custom theme should also import
     * ../chameleon/styles.css, ../chameleon-blue/styles.css, ../expressui/styles.css, as shown in sample app.
     *
     * @return name of custom theme
     */
    public String getCustomTheme() {
        return "expressui";
    }

    /**
     * Gets domain message associated with this component and given code.
     *
     * @param code code to prepend class name
     * @return internationalized domain message
     */
    public String getDomainMessage(String code) {
        return domainMessageSource.getMessage(getClass().getName() + "." + code);
    }

    /**
     * Gets domain message associated with this component and given code.
     *
     * @param code code to prepend class name
     * @param args used to interpolate the message
     * @return internationalized domain message
     */
    public String getDomainMessage(String code, Object... args) {
        return domainMessageSource.getMessage(getClass().getName() + "." + code, args);
    }

    /**
     * Gets the current page conversation.
     *
     * @return current page conversation
     */
    public PageConversation getCurrentPageConversation() {
        return currentPageConversation;
    }

    /**
     * Begins a new page conversation.
     *
     * @param id unique id of conversation
     * @return newly created page conversation
     */
    public PageConversation beginPageConversation(String id) {
        currentPageConversation = new PageConversation(id);

        return currentPageConversation;
    }

    /**
     * Ends the current page conversation. This doesn't need to be called, since a new page conversation automatically
     * ends the previous conversation.
     */
    public void endPageConversation() {
        currentPageConversation = null;
    }

    @PostConstruct
    @Override
    public void postConstruct() {
        currentInstance.set(this);
    }

    @Override
    public void postWire() {
    }

    @Override
    public void onDisplay() {
        mainMenuBar.onDisplay();
    }

    /**
     * Gets instance of MainApplication associated with the current HTTP session.
     * The user's MainApplication is always tied to the current thread and can be looked up by calling getInstance().
     *
     * @return MainApplication associated with user's session
     */
    public static MainApplication getInstance() {
        return currentInstance.get();
    }

    /**
     * Sets instance of MainApplication associated with the current HTTP session.
     * The user's MainApplication is always tied to the current thread and can be looked up by calling getInstance().
     *
     * @param mainApplication MainApplication associated with user's session
     */
    public static void setInstance(MainApplication mainApplication) {
        currentInstance.set(mainApplication);
    }

    /**
     * Gets the current HTTP servlet request.
     *
     * @return current HTTP service request
     */
    public static HttpServletRequest getRequest() {
        return currentRequest.get();
    }

    /**
     * Gets the current HTTP servlet response.
     *
     * @return current HTTP service response
     */
    public static HttpServletResponse getResponse() {
        return currentResponse.get();
    }

    @Override
    public void onRequestStart(HttpServletRequest request, HttpServletResponse response) {
        currentInstance.set(this);
        currentRequest.set(request);
        currentResponse.set(response);
        if (securityService.getCurrentUser() != null) {
            SecurityService.setCurrentLoginName(securityService.getCurrentUser().getLoginName());
        }
    }

    @Override
    public void onRequestEnd(HttpServletRequest request, HttpServletResponse response) {
        currentResponse.remove();
        currentRequest.remove();
        currentInstance.remove();
        SecurityService.removeCurrentLoginName();
    }

    /**
     * Gets cookie associated with given name.
     *
     * @param name name of the cookie
     * @return cookie
     */
    public Cookie getCookie(String name) {
        Cookie[] cookies = getRequest().getCookies();
        for (Cookie cookie : cookies) {
            if (cookie.getName().equals(name)) {
                return cookie;
            }
        }

        return null;
    }

    /**
     * Adds a cookie to the HTTP response.
     *
     * @param name  name of the cookie
     * @param value value
     */
    public void addCookie(String name, String value) {
        Cookie cookie = new Cookie(name, value);
        cookie.setPath("/");
        getResponse().addCookie(cookie);
    }

    /**
     * Adds a cookie to the HTTP response.
     *
     * @param name   name of the cookie
     * @param value  value
     * @param maxAge max age
     * @see Cookie#Cookie(String, String)
     * @see Cookie#setMaxAge(int)
     */
    public void addCookie(String name, String value, int maxAge) {
        Cookie cookie = new Cookie(name, value);
        cookie.setMaxAge(maxAge);
        cookie.setPath("/");
        getResponse().addCookie(cookie);
    }

    /**
     * Adds a cookie to the HTTP response.
     *
     * @see Cookie
     */
    public void addCookie(Cookie cookie) {
        getResponse().addCookie(cookie);
    }

    @Override
    public void init() {
        currentInstance.set(this);

        setTheme(getCustomTheme());
        customizeConfirmDialogStyle();

        Window mainWindow = new Window();
        setMainWindow(mainWindow);
        mainWindow.addStyleName("e-main-window");
        mainWindow.setCaption(getTypeCaption());

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
        expressUILink.setTargetName("_blank");
        expressUILink.setIcon(new ThemeResource("../expressui/favicon.png"));
        expressUILink.setSizeUndefined();
        mainLayout.addComponent(expressUILink);
        mainLayout.setComponentAlignment(expressUILink, Alignment.TOP_CENTER);

        configureSessionTimeout(mainWindow);
        postWire();
        onDisplay();
    }

    /**
     * Gets the caption that describes the type of this component. Looks up the caption from domainMessages/,
     * using the class name of this component as the key, for example, com.expressui.sample.SampleApplication.
     *
     * @return caption that describes type of this component
     */
    public String getTypeCaption() {
        return domainMessageSource.getMessage(getClass().getName());
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
     * Selects a new page based on page's class. Does nothing if user does not have permission to access the page.
     * If the previous page is {@link Page#SCOPE_PAGE}, then it is removed from session storage and the UI tabsheet.
     * If the previous page is WebApplicationContext.SESSION_SCOPE, then it is retained
     * and in the UI as a hidden component in an unselected tab.
     * </P>
     * {@link org.springframework.web.context.WebApplicationContext#SCOPE_SESSION} pages display more quickly at the
     * cost of using more memory.
     *
     * @param pageClass class of the new page to select
     */
    public void displayPage(Class<? extends Page> pageClass) {
        User currentUser = securityService.getCurrentUser();

        if (!currentUser.isViewAllowed(pageClass.getName())) {
            showError(uiMessageSource.getMessage("mainApplication.notAllowed", new Object[]{pageClass.getName()}));
            return;
        }

        Page page = loadPageBean(pageClass);

        if (page instanceof TypedComponent && !((TypedComponent) page).isViewAllowed()) {
            showError(uiMessageSource.getMessage("mainApplication.notAllowed",
                    new Object[]{((TypedComponent) page).getType().getName()}));
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

        page.onDisplay();
    }

    /**
     * Asks if user is allowed to view page of given type.
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
        Throwable rootThrowable = event.getThrowable();
        if (rootThrowable == null) return;

        Exception cause;
        if ((cause = ExceptionUtil.findThrowableInChain(rootThrowable, DataIntegrityViolationException.class)) != null) {
            log.warn("Terminal error: ", rootThrowable);
            getMainWindow().showNotification(
                    uiMessageSource.getMessage("mainApplication.dataConstraintViolation"),
                    cause.getMessage(),
                    Window.Notification.TYPE_ERROR_MESSAGE);
        } else if ((cause = ExceptionUtil.findThrowableInChain(rootThrowable, ConstraintViolationException.class)) != null) {
            log.warn("Terminal error: ", rootThrowable);
            ConstraintViolationException violationException = (ConstraintViolationException) cause;
            getMainWindow().showNotification(
                    uiMessageSource.getMessage("mainApplication.dataConstraintViolation"),
                    violationException.getMessage(),
                    Window.Notification.TYPE_ERROR_MESSAGE);
        } else if ((cause = ExceptionUtil.findThrowableInChain(rootThrowable, EntityNotFoundException.class)) != null) {
            log.warn("Terminal error: ", rootThrowable);
            getMainWindow().showNotification(
                    uiMessageSource.getMessage("mainApplication.entityNotFound"),
                    Window.Notification.TYPE_ERROR_MESSAGE);
        } else {
            super.terminalError(event);
            log.error("Terminal error: ", rootThrowable);
            openErrorWindow(rootThrowable);
        }
    }

    /**
     * Shows big error box to user.
     *
     * @param errorMessage message to display
     */
    public void showError(String errorMessage) {
        getMainWindow().showNotification(errorMessage, Window.Notification.TYPE_ERROR_MESSAGE);
    }

    /**
     * Shows big warning box to user.
     *
     * @param warningMessage message to display
     */
    public void showWarning(String warningMessage) {
        getMainWindow().showNotification(warningMessage, Window.Notification.TYPE_WARNING_MESSAGE);
    }

    /**
     * Shows message box to user.
     *
     * @param humanizedMessage message to display
     */
    public void showMessage(String humanizedMessage) {
        getMainWindow().showNotification(humanizedMessage, Window.Notification.TYPE_HUMANIZED_MESSAGE);
    }

    /**
     * Shows notification to user, more customizable that other show* methods.
     *
     * @param notification customized notification
     */
    public void showNotification(Window.Notification notification) {
        getMainWindow().showNotification(notification);
    }

    /**
     * Shows notification to user, more customizable that other show* methods.
     *
     * @param caption   the message to show
     * @param type      type of message
     * @param position  desired notification position
     * @param delayMsec desired delay in msec, -1 to require the user to click the message
     */
    public void showNotification(String caption, int type, int position, int delayMsec) {
        Window.Notification notification = new Window.Notification(caption, type);
        notification.setPosition(position);
        notification.setDelayMsec(delayMsec);
        getMainWindow().showNotification(notification);
    }

    /**
     * Shows message in tray area, bottom right with 2000 milliseconds delay.
     *
     * @param message message to display
     */
    public void showTrayMessage(String message) {
        showTrayMessage(2000, message);
    }

    /**
     * Shows message in tray area.
     *
     * @param delayMSec delay in milliseconds to display message
     * @param message   message to display
     */
    public void showTrayMessage(int delayMSec, String message) {
        Window.Notification notification = new Window.Notification(message, Window.Notification.TYPE_TRAY_NOTIFICATION);
        notification.setPosition(Window.Notification.POSITION_BOTTOM_RIGHT);
        notification.setDelayMsec(delayMSec);
        notification.setHtmlContentAllowed(true);
        getInstance().showNotification(notification);
    }

    /**
     * Shows a yes/no confirmation dialog box.
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
     * Configures various messages to embed {@link ApplicationProperties#restartApplicationUrl}
     * if anything goes wrong, for example if session expires, communication error, out of sync, etc.
     * <p/>
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
     * Opens a separate error Window with the full stack trace of a throwable
     * and error box highlighting the root cause message.
     *
     * @param throwable full stack trace of this throwable is displayed in error Window
     */
    public void openErrorWindow(Throwable throwable) {
        showError(ExceptionUtils.getRootCauseMessage(throwable));

        String message = ExceptionUtils.getFullStackTrace(throwable);
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
     * Logs out of application, clear credentials and ends user session.
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
     * Checks if application has access to external Internet, if there is no firewall or proxy interference.
     *
     * @param testUrl      url to test
     * @param errorMessage error message if test fails
     */
    public void checkInternetConnectivity(String testUrl, String errorMessage) {
        try {
            UrlUtil.getContents(testUrl);
        } catch (Exception e) {
            getInstance().showError(errorMessage);
        }
    }
}
