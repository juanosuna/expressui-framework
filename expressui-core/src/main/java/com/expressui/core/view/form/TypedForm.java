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

import com.expressui.core.util.assertion.Assert;
import com.expressui.core.view.TypedComponent;
import com.expressui.core.view.field.FormField;
import com.expressui.core.view.field.SelectField;
import com.expressui.core.view.field.format.DefaultFormats;
import com.expressui.core.view.form.layout.FormGridLayout;
import com.expressui.core.view.menu.LayoutContextMenu;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
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
import java.util.*;

/**
 * A form that can be data-bound to any domain class.
 *
 * @param <T> type of domain class
 */
public abstract class TypedForm<T> extends TypedComponent<T> {

    @Resource
    private DefaultFormats defaultFormat;

    @Resource
    private FormFieldSet formFieldSet;

    private ConfigurableForm form;
    private TabSheet formTabSheet;
    private Map<String, Integer> tabPositions = new HashMap<String, Integer>();
    protected LayoutContextMenu menu;
    private Button toggleFormCollapseButton;
    private Animator formAnimator;

    /**
     * Configure the form fields, e.g. positioning, custom field components, listeners, etc.
     *
     * @param formFields to configure
     */
    protected abstract void init(FormFieldSet formFields);

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
    public FormFieldSet getFormFieldSet() {
        return formFieldSet;
    }

    @Override
    public String getCaption() {
        return null;
    }

    /**
     * Get the Vaadin form that is wrapped by this form component, useful in case Vaadin features are needed that
     * ExpressUI does not expose.
     *
     * @return Vaadin form
     */
    public Form getForm() {
        return form;
    }

    @PostConstruct
    @Override
    public void postConstruct() {
        super.postConstruct();

        form = new ConfigurableForm();
        form.setSizeUndefined();

        form.setWriteThrough(true);
        form.setInvalidCommitted(true);
        form.setImmediate(true);
        form.setValidationVisibleOnCommit(true);

        formFieldSet.setForm(this);
        init(formFieldSet);
        formFieldSet.assertValid();
        form.setFormFieldFactory(new TypedFormFieldFactory(getFormFieldSet()));

        final Layout gridLayout = getFormFieldSet().createGridLayout();
        form.setLayout(gridLayout);

        form.getFooter().addStyleName("e-typed-form-footer");
        createFooterButtons((HorizontalLayout) form.getFooter());

        VerticalLayout tabsAndForm = new VerticalLayout();
        setDebugId(tabsAndForm, "tabsAndForm");
        tabsAndForm.setSizeUndefined();
        if (getFormFieldSet().hasTabs()) {
            initializeTabs(tabsAndForm);
            if (getFormFieldSet().hasOptionalTabs()) {
                initializeOptionalTabs(tabsAndForm);
            }
        }
        tabsAndForm.addComponent(form);

        Label spaceLabel = new Label("</br>", Label.CONTENT_XHTML);
        spaceLabel.setSizeUndefined();
        tabsAndForm.addComponent(spaceLabel);

        addComponent(makeCollapsible(getTypeCaption(), tabsAndForm));
        labelRegistry.registerLabels(getFormFieldSet());
    }

    @Override
    public void postWire() {
        super.postWire();

        Collection<FormField> formFields = getFormFieldSet().getFormFields();
        for (FormField formField : formFields) {
            Field field = formField.getField();
            if (field instanceof SelectField) {
                ((SelectField) field).getEntitySelect().postWire();
            }
        }
    }

    private void initializeTabs(VerticalLayout layout) {
        final Set<String> tabNames = getFormFieldSet().getTabNames();

        formTabSheet = new TabSheet();
        setDebugId(formTabSheet, "formTabSheet");
        formTabSheet.addStyleName("borderless");
        formTabSheet.setSizeUndefined();
        int tabPosition = 0;
        for (String tabName : tabNames) {
            Assert.PROGRAMMING.isTrue(!tabName.isEmpty(), "Tab name may not be empty string");
            Label emptyLabel = new Label();
            emptyLabel.setSizeUndefined();
            formTabSheet.addTab(emptyLabel, tabName, null);
            tabPositions.put(tabName, tabPosition++);
        }

        layout.addComponent(formTabSheet);

        formTabSheet.addListener(new TabSheet.SelectedTabChangeListener() {
            @Override
            public void selectedTabChange(TabSheet.SelectedTabChangeEvent event) {
                String tabName = getCurrentlySelectedTabName();
                form.getLayout().removeAllComponents();
                FormGridLayout gridLayout = (FormGridLayout) form.getLayout();
                gridLayout.setFormColumns(getFormFieldSet().getColumns(tabName));
                gridLayout.setRows(getFormFieldSet().getRows(tabName));
                Set<FormField> formFields = getFormFieldSet().getFormFields(tabName);
                for (FormField formField : formFields) {
                    String propertyId = formField.getPropertyId();
                    Field field = formField.getField();
                    form.attachField(propertyId, field);
                }
                gridLayout.requestRepaint();
            }
        });
    }

    private void initializeOptionalTabs(VerticalLayout layout) {
        final Set<String> tabNames = getFormFieldSet().getTabNames();
        menu = new LayoutContextMenu(layout);
        for (String tabName : tabNames) {
            TabSheet.Tab tab = getTabByName(tabName);
            if (getFormFieldSet().isTabOptional(tabName)) {
                tab.setClosable(true);
                menu.addAction(uiMessageSource.getMessage("typedForm.add") + " " + tabName,
                        this, "executeContextAction").setVisible(true);
                menu.addAction(uiMessageSource.getMessage("typedForm.remove") + " " + tabName,
                        this, "executeContextAction").setVisible(false);
                setIsRequiredEnable(tabName, false);
                tab.setVisible(false);
            }
            tab.setDescription(uiMessageSource.getToolTip("typedForm.tab.toolTip"));
        }

        formTabSheet.setCloseHandler(new TabSheet.CloseHandler() {
            @Override
            public void onTabClose(TabSheet tabsheet, Component tabContent) {
                String tabName = tabsheet.getTab(tabContent).getCaption();
                String actionName = uiMessageSource.getMessage("typedForm.remove") + " " + tabName;
                executeContextAction(actionName);
            }
        });
    }

    /**
     * Make the component collapsible by wrapping it with a layout and button for toggling
     * the component's visibility. This allows the user to expand/collapse the given component in order
     * to free space for viewing other components.
     * <p/>
     * Uses horizontal layout for placing toggle button and animated component.
     *
     * @param caption to display as a header above collapsible component
     * @param component component to show/hide
     * @return the newly created layout that contains the toggle button and animated component
     */
    protected Component makeCollapsible(String caption, Component component) {
        return makeCollapsible(caption, component, true);
    }

    /**
     * Animate the component by wrapping it with a layout and button for toggling
     * the component's visibility. This allows the user to expand/collapse the given component in order
     * to free space for viewing other components.
     *
     * @param component         component to show/hide
     * @param useVerticalLayout true if toggle button should be laid out vertically next to animated component
     * @return the newly created layout that contains the toggle button and animated component
     */
    protected Component makeCollapsible(String caption, Component component, boolean useVerticalLayout) {
        formAnimator = new Animator(component);
        formAnimator.setSizeUndefined();

        AbstractOrderedLayout animatorLayout;
        if (useVerticalLayout) {
            animatorLayout = new VerticalLayout();
        } else {
            animatorLayout = new HorizontalLayout();
        }
        setDebugId(animatorLayout, "animatorLayout");

        animatorLayout.setMargin(false, false, false, false);
        animatorLayout.setSpacing(false);

        toggleFormCollapseButton = new Button(null, new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                setFormVisible(!isFormVisible());
            }
        });
        toggleFormCollapseButton.setDescription(uiMessageSource.getToolTip("typedForm.toggleSearchForm.toolTip"));
        toggleFormCollapseButton.setIcon(new ThemeResource("../expressui/icons/collapse-icon.png"));
        toggleFormCollapseButton.addStyleName("borderless");

        if (useVerticalLayout) {
            HorizontalLayout toggleFormButtonAndCaption = new HorizontalLayout();
            setDebugId(toggleFormButtonAndCaption, "toggleFormButtonAndCaption");
            toggleFormButtonAndCaption.setSizeUndefined();
            toggleFormButtonAndCaption.addComponent(toggleFormCollapseButton);
            Label label = new Label(caption);
            label.setSizeUndefined();
            toggleFormButtonAndCaption.addComponent(label);
            animatorLayout.addComponent(toggleFormButtonAndCaption);
            animatorLayout.addComponent(formAnimator);
        } else {
            animatorLayout.addComponent(toggleFormCollapseButton);
            animatorLayout.addComponent(formAnimator);
        }

        return animatorLayout;
    }

    public boolean isFormVisible() {
        return formAnimator == null || !formAnimator.isRolledUp();
    }

    public void setFormVisible(boolean isVisible) {
        if (formAnimator != null) {
            formAnimator.setRolledUp(!isVisible);
            if (formAnimator.isRolledUp()) {
                toggleFormCollapseButton.setIcon(new ThemeResource("../expressui/icons/expand-icon.png"));
            } else {
                toggleFormCollapseButton.setIcon(new ThemeResource("../expressui/icons/collapse-icon.png"));
            }
        }
    }

    /**
     * Set visibility of the collapse toggle button. Sometimes it is useful to hide the toggle button
     * when hiding the collapsible component provides no benefit in terms of free space, e.g. when editing new entity,
     * there are no to-many tabs and no point in hiding the form.
     *
     * @param isVisible true to hide visibility of toggle button
     */
    public void setFormAnimatorToggleButtonVisible(boolean isVisible) {
        toggleFormCollapseButton.setVisible(isVisible);
    }

    void executeContextAction(ContextMenu.ContextMenuItem item) {
        executeContextAction(item.getName());
    }

    void executeContextAction(String name) {
        if (name.startsWith(uiMessageSource.getMessage("typedForm.add") + " ")) {
            String tabName = name.substring(uiMessageSource.getMessage("typedForm.add").length() + 1);
            FormFieldSet.AddRemoveTabMethodDelegate addRemoveTabMethodDelegate = getFormFieldSet().getTabAddRemoveDelegate(tabName);
            addRemoveTabMethodDelegate.getAddTabMethodDelegate().execute();
            TabSheet.Tab tab = getTabByName(tabName);
            setIsRequiredEnable(tabName, true);
            tab.setVisible(true);
            formTabSheet.setSelectedTab(tab.getComponent());
        } else if (name.startsWith(uiMessageSource.getMessage("typedForm.remove") + " ")) {
            String tabName = name.substring(uiMessageSource.getMessage("typedForm.remove").length() + 1);
            FormFieldSet.AddRemoveTabMethodDelegate addRemoveTabMethodDelegate = getFormFieldSet().getTabAddRemoveDelegate(tabName);
            addRemoveTabMethodDelegate.getRemoveTabMethodDelegate().execute();
            TabSheet.Tab tab = getTabByName(tabName);
            setIsRequiredEnable(tabName, false);
            tab.setVisible(false);
        }

        BeanItem beanItem = createBeanItem(getBean());
        getForm().setItemDataSource(beanItem, getFormFieldSet().getPropertyIds());

        resetContextMenu();
    }

    /**
     * Reset the context menu for the tabs so that optional tabs can be added/removed, if there are any.
     */
    protected void resetContextMenu() {
        if (getFormFieldSet().hasOptionalTabs()) {
            Set<String> tabNames = getFormFieldSet().getViewableTabNames();
            for (String tabName : tabNames) {
                TabSheet.Tab tab = getTabByName(tabName);

                String caption = uiMessageSource.getMessage("typedForm.add") + " " + tabName;
                if (menu.containsItem(caption)) {
                    menu.getContextMenuItem(caption).setVisible(!tab.isVisible());
                }
                caption = uiMessageSource.getMessage("typedForm.remove") + " " + tabName;
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
     * @param tabName   tab for enabling/disabling fields
     * @param isEnabled true to restore is-required settings to originally configured values
     */
    protected void setIsRequiredEnable(String tabName, boolean isEnabled) {
        Set<FormField> fields = getFormFieldSet().getFormFields(tabName);
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
     * @return Vaadin tab
     */
    public TabSheet.Tab getTabByName(String tabName) {
        if (formTabSheet == null) {
            return null;
        } else {
            Integer position = tabPositions.get(tabName);
            Assert.PROGRAMMING.notNull(position);
            return formTabSheet.getTab(position);
        }
    }

    /**
     * Get currently selected tab name.
     *
     * @return currently selected tab name
     */
    public String getCurrentlySelectedTabName() {
        if (formTabSheet == null || formTabSheet.getSelectedTab() == null) {
            return getFormFieldSet().getFirstTabName();
        } else {
            return formTabSheet.getTab(formTabSheet.getSelectedTab()).getCaption();
        }
    }

    /**
     * Select the first tab.
     */
    public void selectFirstTab() {
        if (formTabSheet != null && getFormFieldSet().getTabNames().iterator().hasNext()) {
            String firstTabName = getFormFieldSet().getTabNames().iterator().next();
            formTabSheet.setSelectedTab(getTabByName(firstTabName).getComponent());
        }
    }

    /**
     * Ask if this form has tabs, i.e. more than one. Note that one logical tab is actually displayed as no tabs to
     * the user.
     *
     * @return true if form has more than one tab
     */
    public boolean hasTabs() {
        return formTabSheet != null;
    }

    /**
     * Get the domain object that is bound to this form.
     *
     * @return data-bound domain object
     */
    public T getBean() {
        BeanItem beanItem = (BeanItem) getForm().getItemDataSource();
        if (beanItem == null) {
            return null;
        } else {
            return (T) beanItem.getBean();
        }
    }

    protected BeanItem createBeanItem(Object entity) {
        Assert.PROGRAMMING.notNull(entity);
        List<String> propertyIds = getFormFieldSet().getPropertyIds();
        Map<String, VaadinPropertyDescriptor> descriptors = new HashMap<String, VaadinPropertyDescriptor>();
        for (String propertyId : propertyIds) {
            VaadinPropertyDescriptor descriptor = new EnhancedNestedPropertyDescriptor(propertyId, getType(),
                    getFormFieldSet().getField(propertyId));
            descriptors.put(propertyId, descriptor);
        }
        return new EnhancedBeanItem(entity, descriptors);
    }

    private static class TypedFormFieldFactory implements FormFieldFactory {

        private FormFieldSet formFieldSet;

        public TypedFormFieldFactory(FormFieldSet formFieldSet) {
            this.formFieldSet = formFieldSet;
        }

        @Override
        public Field createField(Item item, Object propertyId, Component uiContext) {
            FormField formField = formFieldSet.getFormField(propertyId.toString());

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
            FormFieldSet formFieldSet = getFormFieldSet();
            String currentTabName = getCurrentlySelectedTabName();
            if (formFieldSet.containsPropertyId(currentTabName, propertyId.toString())) {
                if (TypedForm.this instanceof SearchForm
                        || securityService.getCurrentUser().isViewAllowed(TypedForm.this.getType().getName(),
                        propertyId.toString())) {
                    gridLayout.addField(getFormFieldSet().getFormField(propertyId.toString()));
                }
            }
        }

        @Override
        protected void detachField(final Field field) {
            FormGridLayout formGridLayout = (FormGridLayout) form.getLayout();
            FormField formField = getFormFieldSet().findByField(field);
            formGridLayout.removeField(formField);
        }

        // Fixes bug in Vaadin where clear action does not refresh the PropertyFormatter DataSources
        @Override
        protected void bindPropertyToField(final Object propertyId,
                                           final Property property, final Field field) {
            // check if field has a property that is Viewer set. In that case we
            // expect developer has e.g. PropertyFormatter that he wishes to use and
            // assign the property to the Viewer instead.
            boolean hasFilterProperty = field.getPropertyDataSource() != null
                    && (field.getPropertyDataSource() instanceof Property.Viewer);
            if (hasFilterProperty) {
                ((Property.Viewer) field.getPropertyDataSource())
                        .setPropertyDataSource(property);
                // need to reset DataSource property to itself to trigger refresh of field's value
                field.setPropertyDataSource(field.getPropertyDataSource());
            } else {
                field.setPropertyDataSource(property);
            }
        }
    }
}
