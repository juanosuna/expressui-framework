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

package com.expressui.core.view;

import com.expressui.core.security.SecurityService;
import com.expressui.core.util.ReflectionUtil;
import com.expressui.core.view.field.FormField;
import com.expressui.core.view.field.FormFields;
import com.expressui.core.view.field.LabelDepot;
import com.expressui.core.view.field.format.DefaultFormats;
import com.expressui.core.view.layout.FormGridLayout;
import com.expressui.core.view.menu.LayoutContextMenu;
import com.expressui.core.view.util.MessageSource;
import com.vaadin.data.Item;
import com.vaadin.data.Validator;
import com.vaadin.data.util.BeanItem;
import com.vaadin.data.util.EnhancedBeanItem;
import com.vaadin.data.util.EnhancedNestedPropertyDescriptor;
import com.vaadin.data.util.VaadinPropertyDescriptor;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.*;
import org.vaadin.jouni.animator.Animator;
import org.vaadin.peter.contextmenu.ContextMenu;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A form that can be data-bound to a POJO.
 *
 * @param <T> type of POJO
 */
public abstract class FormComponent<T> extends CustomComponent {

    @Resource
    protected MessageSource uiMessageSource;

    @Resource
    protected MessageSource entityMessageSource;

    @Resource
    private DefaultFormats defaultFormat;

    @Resource
    protected LabelDepot labelDepot;

    @Resource
    private SecurityService securityService;

    private FormFields formFields;

    private ConfigurableForm form;
    private TabSheet tabSheet;
    private Map<String, Integer> tabPositions = new HashMap<String, Integer>();
    protected LayoutContextMenu menu;
    private Button toggleFormVisibilityButton;

    /**
     * Get the user-friendly display caption for the form. This is called whenever a new POJO is bound
     * to this form. So, the caption can be dynamic derived/refreshed from property data in the POJO.
     *
     * @return display caption
     */
    public abstract String getEntityCaption();

    /**
     * Configure the form fields, e.g. positioning, custom field components, listeners, etc.
     *
     * @param formFields to configure
     */
    protected abstract void configureFields(FormFields formFields);

    /**
     * Create the layout for holding footer buttons.
     *
     * @param footerButtons layout for holding footer buttons
     */
    protected abstract void createFooterButtons(HorizontalLayout footerButtons);

    /**
     * Get the form fields for this form.
     *
     * @return form fields
     */
    public FormFields getFormFields() {
        return formFields;
    }

    @Resource
    public void setFormFields(FormFields formFields) {
        this.formFields = formFields;
        formFields.setForm(this);
    }

    public MessageSource getUiMessageSource() {
        return uiMessageSource;
    }

    public MessageSource getEntityMessageSource() {
        return entityMessageSource;
    }

    public DefaultFormats getDefaultFormat() {
        return defaultFormat;
    }

    /**
     * Get the type of business entity for this entry point.
     *
     * @return type of business entity for this entry point
     */
    public Class getEntityType() {
        return ReflectionUtil.getGenericArgumentType(getClass());
    }

    /**
     * Get the Vaadin form that is wrapped by this form component.
     *
     * @return Vaadin form
     */
    public Form getForm() {
        return form;
    }

    /**
     * Called after Spring constructs this bean. Overriding methods should call super.
     */
    @PostConstruct
    public void postConstruct() {
        setSizeUndefined();
        form = new ConfigurableForm();
        form.setSizeUndefined();

        form.setWriteThrough(true);
        form.setInvalidCommitted(true);
        form.setImmediate(true);
        form.setValidationVisibleOnCommit(true);
        form.addStyleName("p-form-component");

        configureFields(getFormFields());
        form.setFormFieldFactory(new EntityFieldFactory(getFormFields()));

        final GridLayout gridLayout = getFormFields().createGridLayout();
        form.setLayout(gridLayout);

        form.getFooter().addStyleName("p-form-component-footer");
        createFooterButtons((HorizontalLayout) form.getFooter());

        VerticalLayout tabsAndForm = new VerticalLayout();
        tabsAndForm.setSizeUndefined();
        if (getFormFields().hasTabs()) {
            initializeTabs(tabsAndForm);
            if (getFormFields().hasOptionalTabs()) {
                initializeOptionalTabs(tabsAndForm);
            }
        }
        tabsAndForm.addComponent(form);

        Label spaceLabel = new Label("</br>", Label.CONTENT_XHTML);
        spaceLabel.setSizeUndefined();
        tabsAndForm.addComponent(spaceLabel);

        VerticalLayout formComponentLayout = new VerticalLayout();
        formComponentLayout.addComponent(animate(tabsAndForm));

        setCompositionRoot(formComponentLayout);
        setCustomSizeUndefined();

        labelDepot.trackLabels(getFormFields());
    }

    private void initializeTabs(VerticalLayout layout) {
        final Set<String> tabNames = getFormFields().getTabNames();

        tabSheet = new TabSheet();
        tabSheet.addStyleName("borderless");
        tabSheet.setSizeUndefined();
        int tabPosition = 0;
        for (String tabName : tabNames) {
            Label emptyLabel = new Label();
            emptyLabel.setSizeUndefined();
            tabSheet.addTab(emptyLabel, tabName, null);
            tabPositions.put(tabName, tabPosition++);
        }

        layout.addComponent(tabSheet);

        tabSheet.addListener(new TabSheet.SelectedTabChangeListener() {
            @Override
            public void selectedTabChange(TabSheet.SelectedTabChangeEvent event) {
                String tabName = getCurrentlySelectedTabName();
                form.getLayout().removeAllComponents();
                FormGridLayout gridLayout = (FormGridLayout) form.getLayout();
                gridLayout.setFormColumns(getFormFields().getColumns(tabName));
                gridLayout.setRows(getFormFields().getRows(tabName));
                Set<FormField> formFields = getFormFields().getFormFields(tabName);
                for (FormField formField : formFields) {
                    String propertyId = formField.getPropertyId();
                    Field field = formField.getField();
                    form.attachField(propertyId, field);
                }
            }
        });
    }

    private void initializeOptionalTabs(VerticalLayout layout) {
        final Set<String> tabNames = getFormFields().getTabNames();
        menu = new LayoutContextMenu(layout);
        for (String tabName : tabNames) {
            TabSheet.Tab tab = getTabByName(tabName);
            if (getFormFields().isTabOptional(tabName)) {
                tab.setClosable(true);
                menu.addAction(uiMessageSource.getMessage("formComponent.add") + " " + tabName,
                        this, "executeContextAction").setVisible(true);
                menu.addAction(uiMessageSource.getMessage("formComponent.remove") + " " + tabName,
                        this, "executeContextAction").setVisible(false);
                setIsRequiredEnable(tabName, false);
                tab.setVisible(false);
            }
            tab.setDescription(uiMessageSource.getMessage("formComponent.tab.description"));
        }

        tabSheet.setCloseHandler(new TabSheet.CloseHandler() {
            @Override
            public void onTabClose(TabSheet tabsheet, Component tabContent) {
                String tabName = tabsheet.getTab(tabContent).getCaption();
                String actionName = uiMessageSource.getMessage("formComponent.remove") + " " + tabName;
                executeContextAction(actionName);
            }
        });
    }

    /**
     * Animate the component by visually wrapping it with a layout and button for toggling
     * the component's visibility. This allows the user to expand/collapse the given component in order
     * to free space for viewing other components.
     *
     * Uses horizontal layout for placing toggle button and animated component.
     *
     * @param component component to show/hide
     *
     * @return the newly created layout that contains the toggle button and animated component
     */
    protected Component animate(Component component) {
        return animate(component, false);
    }

    /**
     * Animate the component by visually wrapping it with a layout and button for toggling
     * the component's visibility. This allows the user to expand/collapse the given component in order
     * to free space for viewing other components.
     *
     * @param component component to show/hide
     * @param useVerticalLayout true if toggle button should be laid out vertically next to animated component
     *
     * @return the newly created layout that contains the toggle button and animated component
     */
    protected Component animate(Component component, boolean useVerticalLayout) {
        final Animator formAnimator = new Animator(component);
        formAnimator.setSizeUndefined();

        AbstractOrderedLayout animatorLayout;
        if (useVerticalLayout) {
            animatorLayout = new VerticalLayout();
        } else {
            animatorLayout = new HorizontalLayout();
        }

        animatorLayout.setMargin(false, false, false, false);
        animatorLayout.setSpacing(false);

        toggleFormVisibilityButton = new Button(null, new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                formAnimator.setRolledUp(!formAnimator.isRolledUp());
                if (formAnimator.isRolledUp()) {
                    event.getButton().setIcon(new ThemeResource("../expressUiTheme/icons/expand-icon.png"));
                } else {
                    event.getButton().setIcon(new ThemeResource("../expressUiTheme/icons/collapse-icon.png"));
                }
            }
        });
        toggleFormVisibilityButton.setDescription(uiMessageSource.getMessage("entryPoint.toggleSearchForm.description"));
        toggleFormVisibilityButton.setIcon(new ThemeResource("../expressUiTheme/icons/collapse-icon.png"));
        toggleFormVisibilityButton.addStyleName("borderless");

        if (useVerticalLayout) {
            HorizontalLayout toggleFormButtonAndCaption = new HorizontalLayout();
            toggleFormButtonAndCaption.setSizeUndefined();
            toggleFormButtonAndCaption.addComponent(toggleFormVisibilityButton);
            toggleFormButtonAndCaption.addComponent(new Label(getEntityCaption()));
            animatorLayout.addComponent(toggleFormButtonAndCaption);
            animatorLayout.addComponent(formAnimator);
        } else {
            animatorLayout.addComponent(toggleFormVisibilityButton);
            animatorLayout.addComponent(formAnimator);
        }

        return animatorLayout;
    }

    /**
     * Set visibility of the animator toggle button. Sometimes it is useful to hide the toggle button
     * when hiding the animated component provides no benefit in terms of free space, e.g. when editing new entity,
     * there are no to-many tabs and no point in hiding the form.
     *
     * @param isVisible true to hide visibility of toggle button
     */
    public void setFormAnimatorToggleButtonVisible(boolean isVisible) {
        toggleFormVisibilityButton.setVisible(isVisible);
    }

    void executeContextAction(ContextMenu.ContextMenuItem item) {
        executeContextAction(item.getName());
    }

    void executeContextAction(String name) {
        if (name.startsWith(uiMessageSource.getMessage("formComponent.add") + " ")) {
            String tabName = name.substring(4);
            FormFields.AddRemoveMethodDelegate addRemoveMethodDelegate = getFormFields().getTabAddRemoveDelegate(tabName);
            addRemoveMethodDelegate.getAddMethodDelegate().execute();
            TabSheet.Tab tab = getTabByName(tabName);
            setIsRequiredEnable(tabName, true);
            tab.setVisible(true);
            tabSheet.setSelectedTab(tab.getComponent());
        } else if (name.startsWith(uiMessageSource.getMessage("formComponent.remove") + " ")) {
            String tabName = name.substring(7);
            FormFields.AddRemoveMethodDelegate addRemoveMethodDelegate = getFormFields().getTabAddRemoveDelegate(tabName);
            addRemoveMethodDelegate.getRemoveMethodDelegate().execute();
            TabSheet.Tab tab = getTabByName(tabName);
            setIsRequiredEnable(tabName, false);
            tab.setVisible(false);
        }

        BeanItem beanItem = createBeanItem(getEntity());
        getForm().setItemDataSource(beanItem, getFormFields().getPropertyIds());

        resetContextMenu();
    }

    /**
     * Reset the context menu for the tabs so that optional tabs can be added/removed, if there are any.
     */
    protected void resetContextMenu() {
        if (getFormFields().hasOptionalTabs()) {
            Set<String> tabNames = getFormFields().getViewableTabNames();
            for (String tabName : tabNames) {
                TabSheet.Tab tab = getTabByName(tabName);

                String caption = uiMessageSource.getMessage("formComponent.add") + " " + tabName;
                if (menu.containsItem(caption)) {
                    menu.getContextMenuItem(caption).setVisible(!tab.isVisible());
                }
                caption = uiMessageSource.getMessage("formComponent.remove") + " " + tabName;
                if (menu.containsItem(caption)) {
                    menu.getContextMenuItem(caption).setVisible(tab.isVisible());
                }
            }
        }
    }

    /**
     * Enables or disables whether fields in this form's tab are required. Enabling restores is-required settings
     * to originally configured values.
     *
     * @param tabName tab for enabling/disabling fields
     * @param isEnabled true to restore is-required settings to originally configured values
     */
    protected void setIsRequiredEnable(String tabName, boolean isEnabled) {
        Set<FormField> fields = getFormFields().getFormFields(tabName);
        for (FormField field : fields) {
            if (isEnabled) {
                field.restoreIsRequired();
            } else {
                field.setRequired(false);
            }
        }
    }

    /**
     * Get tab by its name.
     *
     * @param tabName name of the tab to find
     *
     * @return Vaadin tab
     */
    public TabSheet.Tab getTabByName(String tabName) {
        if (tabSheet == null) {
            return null;
        } else {
            Integer position = tabPositions.get(tabName);
            return tabSheet.getTab(position);
        }
    }

    /**
     * Get currently selected tab.
     *
     * @return currently selected tab
     */
    public String getCurrentlySelectedTabName() {
        if (tabSheet == null || tabSheet.getSelectedTab() == null) {
            return getFormFields().getFirstTabName();
        } else {
            return tabSheet.getTab(tabSheet.getSelectedTab()).getCaption();
        }
    }

    /**
     * Select the first tab.
     */
    public void selectFirstTab() {
        if (tabSheet != null && getFormFields().getTabNames().iterator().hasNext()) {
            String firstTabName = getFormFields().getTabNames().iterator().next();
            tabSheet.setSelectedTab(getTabByName(firstTabName).getComponent());
        }
    }

    /**
     * Ask if this form has tabs, i.e. more than one. Note that one logical tab is actually displayed as no tabs to
     * the user.
     *
     * @return true if form has more than one tab
     */
    public boolean hasTabs() {
        return tabSheet != null;
    }

    /**
     * Add component to Vaadin composition root
     *
     * @param component component to add
     */
    @Override
    public void addComponent(Component component) {
        ((ComponentContainer) getCompositionRoot()).addComponent(component);
    }

    private void setCustomSizeUndefined() {
        setSizeUndefined();
        getCompositionRoot().setSizeUndefined();
    }

    /**
     * Get the entity (POJO) that is data-bound to this form.
     *
     * @return data-bound entity
     */
    public T getEntity() {
        BeanItem beanItem = (BeanItem) getForm().getItemDataSource();
        return (T) beanItem.getBean();
    }

    protected BeanItem createBeanItem(Object entity) {
        List<String> propertyIds = getFormFields().getPropertyIds();
        Map<String, VaadinPropertyDescriptor> descriptors = new HashMap<String, VaadinPropertyDescriptor>();
        for (String propertyId : propertyIds) {
            VaadinPropertyDescriptor descriptor = new EnhancedNestedPropertyDescriptor(propertyId, getEntityType(),
                    getFormFields().getField(propertyId));
            descriptors.put(propertyId, descriptor);
        }
        return new EnhancedBeanItem(entity, descriptors);
    }

    /**
     * Can be overridden if any initialization is required after all Spring beans have been wired.
     * Overriding methods should call super.
     */
    public void postWire() {
    }

    private static class EntityFieldFactory implements FormFieldFactory {

        private FormFields formFields;

        public EntityFieldFactory(FormFields formFields) {
            this.formFields = formFields;
        }

        @Override
        public Field createField(Item item, Object propertyId, Component uiContext) {
            FormField formField = formFields.getFormField(propertyId.toString());

            return formField.getField();
        }
    }

    private class ConfigurableForm extends Form {

        @Override
        public void commit() throws SourceException, Validator.InvalidValueException {
            super.commit();
        }

        @Override
        protected void attachField(Object propertyId, Field field) {
            FormGridLayout gridLayout = (FormGridLayout) form.getLayout();
            FormFields formFields = getFormFields();
            String currentTabName = getCurrentlySelectedTabName();
            if (formFields.containsPropertyId(currentTabName, propertyId.toString())) {
                if (FormComponent.this instanceof SearchForm
                        || securityService.getCurrentUser().isViewAllowed(getEntityType().getName(), propertyId.toString())) {
                    gridLayout.addField(getFormFields().getFormField(propertyId.toString()));
                }
            }
        }

        @Override
        protected void detachField(final Field field) {
            FormGridLayout formGridLayout = (FormGridLayout) form.getLayout();
            FormField formField = getFormFields().findByField(field);
            formGridLayout.removeField(formField);
        }
    }
}
