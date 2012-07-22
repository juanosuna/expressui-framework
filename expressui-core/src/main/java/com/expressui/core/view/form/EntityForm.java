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

package com.expressui.core.view.form;

import com.expressui.core.entity.NameableEntity;
import com.expressui.core.entity.security.User;
import com.expressui.core.util.MethodDelegate;
import com.expressui.core.validation.AssertTrueForProperties;
import com.expressui.core.validation.Validation;
import com.expressui.core.view.field.FormField;
import com.expressui.core.view.field.SelectField;
import com.expressui.core.view.tomanyrelationship.ToManyRelationship;
import com.vaadin.data.Item;
import com.vaadin.data.Validator;
import com.vaadin.data.util.BeanItem;
import com.vaadin.terminal.ErrorMessage;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.terminal.UserError;
import com.vaadin.ui.*;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.persistence.EntityNotFoundException;
import javax.persistence.OptimisticLockException;
import javax.validation.ConstraintViolation;
import javax.validation.metadata.ConstraintDescriptor;
import java.lang.annotation.Annotation;
import java.util.*;

/**
 * A form bound to a JPA entity, providing save, refresh and cancel actions.
 * The main area of the form can have tabs for different sections. Underneath the form
 * are more tabs representing to-many relationships, allowing related entities to be added and removed.
 * <p/>
 * Other features include displaying in view-only mode, handling security permissions, validating fields and
 * keeping track of which tabs contain validation errors.
 *
 * @param <T> type of entity
 */
public abstract class EntityForm<T> extends TypedForm<T> {

    @Resource
    private Validation validation;

    private boolean isViewMode;

    private TabSheet toManyRelationshipTabs;

    private Button cancelButton;
    private Button refreshButton;
    private Button saveAndCloseButton;
    private Button saveAndStayOpenButton;
    private boolean isValidationEnabled = true;

    private Boolean isPopupWindowHeightFull;

    private com.vaadin.terminal.Resource saveAndCloseButtonIconBackup;
    private com.vaadin.terminal.Resource saveAndStayOpenButtonIconBackup;

    private Set<MethodDelegate> closeListeners = new LinkedHashSet<MethodDelegate>();
    private Set<MethodDelegate> cancelListeners = new LinkedHashSet<MethodDelegate>();
    private Set<MethodDelegate> saveListeners = new LinkedHashSet<MethodDelegate>();

    @PostConstruct
    @Override
    public void postConstruct() {
        super.postConstruct();

        labelRegistry.putTypeLabel(getType().getName(), getTypeCaption());

        List<ToManyRelationship> toManyRelationships = getViewableToManyRelationships();
        if (toManyRelationships.size() > 0) {
            toManyRelationshipTabs = new TabSheet();
            setDebugId(toManyRelationshipTabs, "toManyRelationshipTabs");
            toManyRelationshipTabs.setSizeUndefined();
            for (ToManyRelationship toManyRelationship : toManyRelationships) {
                toManyRelationshipTabs.addTab(toManyRelationship);
                toManyRelationship.getResultsTable().addExecuteQueryListener(this, "requestRepaintAll");
                labelRegistry.putFieldLabel(getType().getName(), toManyRelationship.getChildPropertyId(),
                        "Relationship", toManyRelationship.getTypeCaption());
            }

            HorizontalLayout toManyRelationshipLayout = new HorizontalLayout();
            setDebugId(toManyRelationshipLayout, "toManyRelationshipLayout");
            toManyRelationshipLayout.setSizeUndefined();
            Label label = new Label("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;", Label.CONTENT_XHTML);
            toManyRelationshipLayout.addComponent(label);
            toManyRelationshipLayout.addComponent(toManyRelationshipTabs);
            addComponent(toManyRelationshipLayout);
        }

        addCodePopupButtonIfEnabled(Alignment.MIDDLE_RIGHT, EntityForm.class);
    }

    @Override
    public void postWire() {
        super.postWire();

        List<ToManyRelationship> toManyRelationships = getViewableToManyRelationships();
        for (ToManyRelationship toManyRelationship : toManyRelationships) {
            toManyRelationship.postWire();
        }
    }

    @Override
    public void onDisplay() {
        List<ToManyRelationship> toManyRelationships = getToManyRelationships();
        for (ToManyRelationship toManyRelationship : toManyRelationships) {
            toManyRelationship.onDisplay();
        }
    }

    /**
     * Gets caption that describes the entity bean bound to this form. If the bean
     * implements {@link NameableEntity}, then {@link NameableEntity#getName} is
     * used. Otherwise, the bean's toString is used as a caption. Override
     * this method to customize the caption.
     *
     * @return caption that describes entity
     */
    public String getEntityCaption() {
        String typeName = domainMessageSource.getMessage(getType().getName(), getType().getSimpleName());
        T bean = getBean();
        if (genericDao.isPersistent(bean)) {
            if (bean instanceof NameableEntity) {
                return uiMessageSource.getMessage("entityForm.entityCaption.existing",
                        new Object[]{typeName, ((NameableEntity) bean).getName()});
            } else {
                return uiMessageSource.getMessage("entityForm.entityCaption.existing",
                        new Object[]{typeName, bean.toString()});
            }
        } else {
            return uiMessageSource.getMessage("entityForm.entityCaption.new",
                    new Object[]{typeName});
        }
    }

    @Override
    public String getTypeCaption() {
        String typeName = domainMessageSource.getMessage(getType().getName(), getType().getSimpleName());
        return uiMessageSource.getMessage("entityForm.typeCaption", new Object[]{typeName});
    }

    /**
     * Gets all to-many relationships, displayed as tabs below the form.
     *
     * @return list of to-many relationships
     */
    public List<ToManyRelationship> getToManyRelationships() {
        return new ArrayList<ToManyRelationship>();
    }

    /**
     * Gets all viewable to-many relationships, based on security permissions.
     *
     * @return all viewable to-many relationships
     */
    public List<ToManyRelationship> getViewableToManyRelationships() {
        List<ToManyRelationship> viewableToManyRelationships = new ArrayList<ToManyRelationship>();
        List<ToManyRelationship> toManyRelationships = getToManyRelationships();

        for (ToManyRelationship toManyRelationship : toManyRelationships) {
            User user = getCurrentUser();

            if (user.isViewAllowed(toManyRelationship.getType().getName())
                    && !toManyRelationship.getResultsFieldSet().getViewablePropertyIds().isEmpty()
                    && user.isViewAllowed(getType().getName(), toManyRelationship.getChildPropertyId())) {
                viewableToManyRelationships.add(toManyRelationship);
            }
        }

        return viewableToManyRelationships;
    }

    /**
     * Makes the component collapsible if and only if there are viewable to-many tabs, toggling component's
     * visibility.
     *
     * @param component component to show/hide
     * @return the newly created wrapper layout that contains the toggle button and collapsible component
     */
    @Override
    protected Component makeCollapsible(String caption, Component component) {
        if (getViewableToManyRelationships().size() > 0) {
            return super.makeCollapsible(caption, component, false);
        } else {
            return component;
        }
    }

    /**
     * Creates the footer buttons: cancel, refresh, save.
     *
     * @param footerLayout horizontal layout containing buttons
     */
    @Override
    protected void createFooterButtons(HorizontalLayout footerLayout) {
        footerLayout.setSpacing(true);
        footerLayout.setMargin(true);

        cancelButton = new Button(uiMessageSource.getMessage("entityForm.cancel"), this, "cancel");
        cancelButton.setDescription(uiMessageSource.getToolTip("entityForm.cancel.toolTip"));
        cancelButton.setIcon(new ThemeResource("../expressui/icons/16/cancel.png"));
        cancelButton.addStyleName("small default");
        footerLayout.addComponent(cancelButton);

        refreshButton = new Button(uiMessageSource.getMessage("entityForm.refresh"), this, "refresh");
        refreshButton.setDescription(uiMessageSource.getToolTip("entityForm.refresh.toolTip"));
        refreshButton.setIcon(new ThemeResource("../expressui/icons/16/refresh.png"));
        refreshButton.addStyleName("small default");
        footerLayout.addComponent(refreshButton);

        saveAndStayOpenButton = new Button(uiMessageSource.getMessage("entityForm.saveAndStayOpen"), this, "saveAndStayOpen");
        saveAndStayOpenButton.setDescription(uiMessageSource.getToolTip("entityForm.saveAndStayOpen.toolTip"));
        saveAndStayOpenButton.setIcon(new ThemeResource("../expressui/icons/16/save.png"));
        saveAndStayOpenButton.addStyleName("small default");
        footerLayout.addComponent(saveAndStayOpenButton);

        saveAndCloseButton = new Button(uiMessageSource.getMessage("entityForm.saveAndClose"), this, "saveAndClose");
        saveAndCloseButton.setDescription(uiMessageSource.getToolTip("entityForm.saveAndClose.toolTip"));
        saveAndCloseButton.setIcon(new ThemeResource("../expressui/icons/16/save.png"));
        saveAndCloseButton.addStyleName("small default");
        footerLayout.addComponent(saveAndCloseButton);

        backupSaveButtonIcons();
    }

    /**
     * Asks if this form is in read/view-only mode.
     *
     * @return true if in view-only mode
     */
    public boolean isViewMode() {
        return isViewMode;
    }

    /**
     * Sets whether or not this form is in read/view-only mode. Note that this action does not immediately change
     * fields to read-only or restore them to writable. It just sets the mode for the next time
     * an entity is loaded or {@link #syncCrudActions} is called.
     *
     * @param viewMode true if in view-only mode
     */
    public void setViewMode(boolean viewMode) {
        isViewMode = viewMode;
        List<ToManyRelationship> toManyRelationships = getToManyRelationships();
        for (ToManyRelationship toManyRelationship : toManyRelationships) {
            toManyRelationship.setViewMode(viewMode);
        }
    }

    /**
     * Synchronizes the states of CRUD action buttons and context menu items so that they are consistent
     * with security permissions, the number of rows selected (if any) and whether or not this component
     * is in view mode. Synchronization is performed on form buttons and any to-many relatonships.
     */
    public void syncCrudActions() {
        getFormFieldSet().setReadOnly(isViewMode());
        if (!isViewMode()) {
            getFormFieldSet().applySecurity();
        }

        saveAndCloseButton.setVisible(!isViewMode());
        saveAndStayOpenButton.setVisible(!isViewMode());
        refreshButton.setVisible(!isViewMode());

        List<ToManyRelationship> toManyRelationships = getToManyRelationships();
        for (ToManyRelationship toManyRelationship : toManyRelationships) {
            toManyRelationship.syncCrudActions();
        }
    }

    /**
     * If this entity form is displayed in popup window, asks if the window's height is full.
     * If this is not set (is null), default behavior is as follows:
     * an entity form with to-many relationships uses full height and those without are undefined
     * and automatically adjust their size according to contents.
     *
     * @return true if popup window's height is full or null for default behavior
     */
    public Boolean isPopupWindowHeightFull() {
        return isPopupWindowHeightFull;
    }

    /**
     * If this entity form is displayed in popup window, set if the window's height is full.
     * If this is not set (is null), default behavior is as follows:
     * an entity form with to-many relationships uses full height and those without are undefined
     * and automatically adjust their size according to contents.
     *
     * @param popupWindowHeightFull true if popup window's height is full or null for default behavior
     */
    public void setPopupWindowHeightFull(Boolean popupWindowHeightFull) {
        isPopupWindowHeightFull = popupWindowHeightFull;
    }

    /**
     * Asks if validation is currently enabled for this form. Sometimes it is useful to temporarily turn off
     * validation, for example when setting a new data-source.
     *
     * @return true if validation is currently enabled
     */
    public boolean isValidationEnabled() {
        return isValidationEnabled;
    }

    private void setItemDataSource(Item newDataSource, Collection<?> propertyIds) {
        isValidationEnabled = false;
        getForm().setItemDataSource(newDataSource, propertyIds);
        isValidationEnabled = true;
    }

    /**
     * Loads and binds a new entity to the form. Automatically selects the first tab (if tabs exist), whenever
     * a new entity is loaded.
     *
     * @param entity entity to load
     */
    public void load(T entity) {
        load(entity, true);
    }

    /**
     * Loads and binds a new entity to the form.
     *
     * @param entity         entity to load
     * @param selectFirstTab true to select the first tab, once the entity is loaded, otherwise maintains the
     *                       tab selection of any previously shown entity
     * @throws EntityNotFoundException
     */
    public void load(T entity, boolean selectFirstTab) throws EntityNotFoundException {
        T loadedEntity = genericDao.find(getType(), genericDao.getId(entity));
        if (loadedEntity == null) {
            throw new EntityNotFoundException(entity.toString());
        }
        postLoad(loadedEntity);
        BeanItem beanItem = createBeanItem(loadedEntity);
        setItemDataSource(beanItem, getFormFieldSet().getPropertyIds());
        getFormFieldSet().autoAdjustWidths();

        validate(true);

        loadToManyRelationships();
        resetTabs(selectFirstTab);

        refreshButton.setCaption(uiMessageSource.getMessage("entityForm.refresh"));
        requestRepaintAll();
    }

    @Override
    protected BeanItem createBeanItem(Object entity) {
        isValidationEnabled = false;
        BeanItem beanItem = super.createBeanItem(entity);
        isValidationEnabled = true;

        return beanItem;
    }

    private void loadToManyRelationships() {
        List<ToManyRelationship> toManyRelationships = getViewableToManyRelationships();
        if (toManyRelationships.size() > 0) {
            for (ToManyRelationship toManyRelationship : toManyRelationships) {
                Object parent = getBean();
                toManyRelationship.getEntityQuery().setParent(parent);
                toManyRelationship.search();
                toManyRelationship.syncCrudActions();

            }
            toManyRelationshipTabs.setVisible(true);
        }
    }

    /**
     * Clears the form of data and all errors.
     */
    public void clear() {
        clearAllErrors(true);
        setItemDataSource(null, getFormFieldSet().getPropertyIds());
    }

    /**
     * Creates an empty form with an empty entity. Makes to-many relationship tabs invisible. User must
     * save an entity and then to-many relationships will appear.
     */
    public void create() {
        createImpl();

        if (toManyRelationshipTabs != null) {
            toManyRelationshipTabs.setVisible(false);
        }
    }

    private void createImpl() {
        T newEntity = createEntity();
        postCreate(newEntity);
        BeanItem beanItem = createBeanItem(newEntity);
        setItemDataSource(beanItem, getFormFieldSet().getPropertyIds());

        validate(true);

        resetTabs();

        refreshButton.setCaption(uiMessageSource.getMessage("entityForm.clear"));
    }

    private T createEntity() {
        if (getEntityDao() == null) {
            return (T) genericDao.create(getType());
        } else {
            return getEntityDao().create();
        }
    }

    private void resetTabs() {
        resetTabs(true);
    }

    private void resetTabs(boolean selectFirstTab) {

        if (selectFirstTab) {
            selectFirstToManyTab();
        }

        if (!hasTabs()) return;

        Set<String> viewableTabNames = getFormFieldSet().getViewableTabNames();
        Set<String> tabNames = getFormFieldSet().getTabNames();
        for (String tabName : tabNames) {
            TabSheet.Tab tab = getTabByName(tabName);

            Set<FormField> fields = getFormFieldSet().getFormFields(tabName);

            if (getFormFieldSet().isTabOptional(tabName)) {
                boolean isTabEmpty = true;
                for (FormField field : fields) {
                    if (field.getField().getValue() != null) {
                        isTabEmpty = false;
                        break;
                    }
                }

                setIsRequiredEnable(tabName, !isTabEmpty);
                tab.setClosable(!isViewMode());
                tab.setVisible(!isTabEmpty && viewableTabNames.contains(tabName));
            } else {
                tab.setVisible(viewableTabNames.contains(tabName));
            }
        }

        resetContextMenu();

        if (selectFirstTab || !getTabByName(getCurrentlySelectedTabName()).isVisible()) {
            selectFirstTab();
        }

        syncTabAndSaveButtonErrors();
    }

    private void selectFirstToManyTab() {
        if (toManyRelationshipTabs != null) {
            toManyRelationshipTabs.setSelectedTab(toManyRelationshipTabs.getTab(0).getComponent());
        }
    }

    @Override
    protected void resetContextMenu() {
        if (getFormFieldSet().hasOptionalTabs()) {
            Set<String> tabNames = getFormFieldSet().getViewableTabNames();
            for (String tabName : tabNames) {
                TabSheet.Tab tab = getTabByName(tabName);

                String caption = uiMessageSource.getMessage("typedForm.add") + " " + tabName;
                if (menu.containsItem(caption)) {
                    menu.getContextMenuItem(caption).setVisible(!tab.isVisible() && !isViewMode());
                }
                caption = uiMessageSource.getMessage("typedForm.remove") + " " + tabName;
                if (menu.containsItem(caption)) {
                    menu.getContextMenuItem(caption).setVisible(tab.isVisible() && !isViewMode());
                }
            }
        }
    }

    /**
     * Adds cancel listener. Listener is invoked when form is canceled.
     *
     * @param target     target object
     * @param methodName name of method to invoke
     */
    public void addCancelListener(Object target, String methodName) {
        cancelListeners.add(new MethodDelegate(target, methodName));
    }

    /**
     * Adds close listener. Listener is invoked when form is closed.
     *
     * @param target     target object
     * @param methodName name of method to invoke
     */
    public void addCloseListener(Object target, String methodName) {
        closeListeners.add(new MethodDelegate(target, methodName));
    }

    /**
     * Adds save listener. Listener is invoked when form is saved.
     *
     * @param target     target object
     * @param methodName name of method to invoke
     */
    public void addSaveListener(Object target, String methodName) {
        saveListeners.add(new MethodDelegate(target, methodName));
    }

    /**
     * Removes all listeners from the given target.
     *
     * @param target all listeners defined on this target are removed
     */
    public void removeListeners(Object target) {
        Set<MethodDelegate> listenersToRemove;

        listenersToRemove = new HashSet<MethodDelegate>();
        for (MethodDelegate closeListener : closeListeners) {
            if (closeListener.getTarget().equals(target)) {
                listenersToRemove.add(closeListener);
            }
        }
        for (MethodDelegate listener : listenersToRemove) {
            closeListeners.remove(listener);
        }

        listenersToRemove = new HashSet<MethodDelegate>();
        for (MethodDelegate cancelListener : cancelListeners) {
            if (cancelListener.getTarget().equals(target)) {
                listenersToRemove.add(cancelListener);
            }
        }
        for (MethodDelegate listener : listenersToRemove) {
            cancelListeners.remove(listener);
        }

        listenersToRemove = new HashSet<MethodDelegate>();
        for (MethodDelegate saveListener : saveListeners) {
            if (saveListener.getTarget().equals(target)) {
                listenersToRemove.add(saveListener);
            }
        }
        for (MethodDelegate listener : listenersToRemove) {
            saveListeners.remove(listener);
        }
    }

    /**
     * Cancels and closes the form, discarding any changes.
     */
    public void cancel() {
        clearAllErrors(true);
        getForm().discard();
        setViewMode(false);
        syncCrudActions();
        BeanItem beanItem = (BeanItem) getForm().getItemDataSource();
        if (beanItem == null) {
            clear();
        } else {
            T entity = (T) beanItem.getBean();
            if (genericDao.getId(entity) == null) {
                create();
            } else {
                try {
                    load(entity);
                } catch (EntityNotFoundException e) {
                    // ignore if user cancels when viewing/editing entity that another user deleted
                }
            }
        }

        Set<MethodDelegate> listenersToExecute;
        listenersToExecute = (Set<MethodDelegate>) ((LinkedHashSet) cancelListeners).clone();
        for (MethodDelegate listener : listenersToExecute) {
            listener.execute();
        }
    }

    /**
     * Saves changes to the entity, either persisting a transient entity or updating existing one, then closes form.
     *
     * @return true if save was successful
     */
    public boolean saveAndClose() {
        return save(true);
    }

    /**
     * Saves changes to the entity, either persisting a transient entity or updating existing one, while
     * keeping form open.
     *
     * @return true if save was successful
     */
    public boolean saveAndStayOpen() {
        boolean successful = save(false);

        if (successful) {
            loadToManyRelationships();
        }

        return successful;
    }

    /**
     * Saves changes to the entity, either persisting a transient entity or updating existing one.
     *
     * @param executeCloseListeners whether or not to execute close listeners
     * @return true if save was successful
     */
    public boolean save(boolean executeCloseListeners) {
        try {
            return saveImpl(executeCloseListeners);
        } catch (OptimisticLockException e) {
            showSaveConflictMessage();
            return false;
        }
    }

    private boolean saveImpl(boolean executeCloseListeners) throws OptimisticLockException {
        boolean isValid = validate(false);
        if (getForm().isValid() && isValid) {
            getForm().commit();

            preSave(getBean());

            T entity = getBean();
            checkThatToOneSelectionsExist();
            if (genericDao.getId(entity) != null) {
                T foundEntity = genericDao.find(getType(), genericDao.getId(entity));
                if (foundEntity == null) {
                    throw new EntityNotFoundException(entity.toString());
                }

                T mergedEntity;
                if (getEntityDao() == null) {
                    mergedEntity = genericDao.merge(entity);
                } else {
                    mergedEntity = getEntityDao().merge(entity);
                }

                postSave(mergedEntity);
                load(mergedEntity, false);
            } else {
                if (getEntityDao() == null) {
                    genericDao.persist(entity);
                } else {
                    getEntityDao().persist(entity);
                }
                postSave(entity);
                load(entity, false);

                if (!executeCloseListeners) {
                    loadToManyRelationships();
                }
            }

            showSaveSuccessfulMessage();

            Set<MethodDelegate> listenersToExecute = (Set<MethodDelegate>) ((LinkedHashSet) saveListeners).clone();
            for (MethodDelegate listener : listenersToExecute) {
                listener.execute();
            }

            if (executeCloseListeners) {
                listenersToExecute = (Set<MethodDelegate>) ((LinkedHashSet) closeListeners).clone();
                for (MethodDelegate listener : listenersToExecute) {
                    listener.execute();
                }
            }
            return true;
        } else {
            showSaveValidationErrorMessage();
            return false;
        }
    }

    private void checkThatToOneSelectionsExist() {
        Set<FormField> formFields = getFormFieldSet().getFormFields();
        for (FormField formField : formFields) {
            if (formField.getField() instanceof SelectField) {
                SelectField selectField = (SelectField) formField.getField();
                Object selectedValue = selectField.getBean();
                if (selectedValue != null) {
                    Object reFoundValue = genericDao.reFind(selectedValue);
                    if (reFoundValue == null) {
                        throw new EntityNotFoundException(selectedValue.toString());
                    }
                }
            }
        }
    }

    /**
     * Shows notification message that save was successful.
     */
    public void showSaveSuccessfulMessage() {
        getMainApplication().showMessage(
                "\"" + getEntityCaption()
                        + "\" " + uiMessageSource.getMessage("entityForm.saved")
        );
    }

    /**
     * Shows notification message that save was unsuccessful because of a conflict with another user's changes.
     */
    public void showSaveConflictMessage() {
        Window.Notification notification = new Window.Notification(
                uiMessageSource.getMessage("entityForm.saveConflictError"),
                Window.Notification.TYPE_ERROR_MESSAGE);
        notification.setDelayMsec(Window.Notification.DELAY_NONE);
        notification.setPosition(Window.Notification.POSITION_CENTERED);
        getMainApplication().showNotification(notification);
    }

    /**
     * Shows notification message that save was unsuccessful because of validation error.
     */
    public void showSaveValidationErrorMessage() {
        Window.Notification notification = new Window.Notification("\"" + getEntityCaption()
                + "\" " + uiMessageSource.getMessage("entityForm.saveValidationError"),
                Window.Notification.TYPE_ERROR_MESSAGE);
        notification.setDelayMsec(Window.Notification.DELAY_NONE);
        notification.setPosition(Window.Notification.POSITION_CENTERED);
        getMainApplication().showNotification(notification);
    }

    /**
     * Reloads entity from the database, discarding any changes and clearing any errors.
     */
    public void refresh() {
        clearAllErrors(true);
        BeanItem beanItem = (BeanItem) getForm().getItemDataSource();
        if (beanItem == null) {
            createImpl();
        } else {
            T entity = (T) beanItem.getBean();
            if (genericDao.getId(entity) == null) {
                createImpl();
            } else {
                getForm().discard();
                load(entity, false);
            }
        }
    }

    @Override
    void executeContextAction(String name) {
        super.executeContextAction(name);
        validate(false);
    }

    /**
     * Validates this form by validating the bound JPA entity, annotated with JSR 303 annotations.
     *
     * @param clearConversionErrors true if any existing type-conversion errors should be cleared
     * @return true if no validation errors were found
     */
    public boolean validate(boolean clearConversionErrors) {
        Object entity = getBean();

        clearAllErrors(clearConversionErrors);

        Set<ConstraintViolation<Object>> constraintViolations = validation.validate(entity);
        for (ConstraintViolation constraintViolation : constraintViolations) {
            String propertyPath = constraintViolation.getPropertyPath().toString();

            ConstraintDescriptor descriptor = constraintViolation.getConstraintDescriptor();
            Annotation annotation = descriptor.getAnnotation();

            if (propertyPath.isEmpty()) {
                Validator.InvalidValueException error = new Validator.InvalidValueException(constraintViolation.getMessage());
                getForm().setComponentError(error);
            } else {
                FormField field;
                if (annotation instanceof AssertTrueForProperties) {
                    if (propertyPath.lastIndexOf(".") > 0) {
                        propertyPath = propertyPath.substring(0, propertyPath.lastIndexOf(".") + 1);
                    } else {
                        propertyPath = "";
                    }
                    AssertTrueForProperties assertTrueForProperties = (AssertTrueForProperties) annotation;
                    propertyPath += assertTrueForProperties.errorProperty();
                }
                if (getFormFieldSet().containsPropertyId(propertyPath)) {
                    field = getFormFieldSet().getFormField(propertyPath);
                    if (!field.hasIsRequiredError()) {
                        Validator.InvalidValueException error = new Validator.InvalidValueException(constraintViolation.getMessage());
                        field.addError(error);
                    }
                }
            }
        }

        syncTabAndSaveButtonErrors();

        return constraintViolations.isEmpty();
    }

    private void clearAllErrors(boolean clearConversionErrors) {
        getFormFieldSet().clearErrors(clearConversionErrors);
        getForm().setComponentError(null);
        saveAndCloseButton.setComponentError(null);
        saveAndStayOpenButton.setComponentError(null);

        Set<String> tabNames = getFormFieldSet().getViewableTabNames();
        for (String tabName : tabNames) {
            setTabError(tabName, null);
        }
    }

    private void setTabError(String tabName, ErrorMessage error) {
        TabSheet.Tab tab = getTabByName(tabName);
        if (tab != null) {
            tab.setComponentError(error);
        }
    }

    /**
     * Resets validation error indicators on tabs and save buttons, according to whether
     * any validation errors currently exist in any fields.
     */
    public void syncTabAndSaveButtonErrors() {
        Set<String> tabNames = getFormFieldSet().getViewableTabNames();
        boolean formHasErrors = false;
        for (String tabName : tabNames) {
            if (getFormFieldSet().hasError(tabName)) {
                setTabError(tabName, new UserError(uiMessageSource.getMessage("entityForm.tabWithInvalidValues")));
                formHasErrors = true;
            } else {
                setTabError(tabName, null);
            }
        }

        if (getForm().getComponentError() != null) {
            formHasErrors = true;
        }

        if (formHasErrors) {
            saveAndCloseButton.setIcon(null);
            saveAndStayOpenButton.setIcon(null);
            String errorMsg = uiMessageSource.getMessage("entityForm.formWithInvalidValues");
            saveAndCloseButton.setComponentError(new UserError(errorMsg));
            saveAndStayOpenButton.setComponentError(new UserError(errorMsg));
        } else {
            saveAndCloseButton.setComponentError(null);
            saveAndStayOpenButton.setComponentError(null);
            restoreSaveButtonIcons();
        }
    }

    private void backupSaveButtonIcons() {
        saveAndCloseButtonIconBackup = saveAndCloseButton.getIcon();
        saveAndStayOpenButtonIconBackup = saveAndStayOpenButton.getIcon();
    }

    private void restoreSaveButtonIcons() {
        saveAndCloseButton.setIcon(saveAndCloseButtonIconBackup);
        saveAndStayOpenButton.setIcon(saveAndStayOpenButtonIconBackup);
    }


    /**
     * Lifecycle method called after new entity is created for this form
     *
     * @param entity newly created entity
     */
    protected void postCreate(T entity) {
    }

    /**
     * Lifecycle method called after an entity is loaded from database
     *
     * @param entity loaded entity
     */
    protected void postLoad(T entity) {
    }

    /**
     * Lifecycle method called before entity is saved to database.
     *
     * @param entity entity to be saved
     */
    protected void preSave(T entity) {
    }

    /**
     * Lifecycle method called  after entity is saved to databased.
     *
     * @param entity saved entity
     */
    protected void postSave(T entity) {
    }

    /**
     * Gets cancel button.
     *
     * @return cancel button
     */
    public Button getCancelButton() {
        return cancelButton;
    }

    /**
     * Gets refresh button.
     *
     * @return refresh button
     */
    public Button getRefreshButton() {
        return refreshButton;
    }

    /**
     * Gets save-and-close button.
     *
     * @return save-and-close button
     */
    public Button getSaveAndCloseButton() {
        return saveAndCloseButton;
    }

    /**
     * Gets save-and-stay-open button.
     *
     * @return save-and-stay-open button
     */
    public Button getSaveAndStayOpenButton() {
        return saveAndStayOpenButton;
    }
}
