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

package com.expressui.core.view.export;

import com.expressui.core.view.form.FormFieldSet;
import com.expressui.core.view.form.TypedForm;
import com.vaadin.data.util.BeanItem;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

import static org.springframework.beans.factory.config.BeanDefinition.SCOPE_PROTOTYPE;

/**
 * Form for configuring excel export parameters.
 */
@Component
@Scope(SCOPE_PROTOTYPE)
public class ExportForm extends TypedForm<ExportParameters> {

    private Window popupWindow;

    private Button exportButton;

    @Resource
    private ExportParameters exportParameters;

    @Override
    public void postConstruct() {
        super.postConstruct();

        addCodePopupButtonIfEnabled();
    }

    @Override
    public void postWire() {
        super.postWire();
        BeanItem beanItem = createBeanItem(exportParameters);
        getForm().setItemDataSource(beanItem, getFormFieldSet().getPropertyIds());
    }

    @Override
    public void onDisplay() {
    }

    @Override
    protected com.vaadin.ui.Component makeCollapsible(String caption, com.vaadin.ui.Component component) {
        return component;
    }

    @Override
    public String getTypeCaption() {
        return uiMessageSource.getMessage("exportForm.typeCaption");
    }

    /**
     * Gets export parameters.
     *
     * @return export parameters
     */
    public ExportParameters getExportParameters() {
        return exportParameters;
    }

    @Override
    protected void init(FormFieldSet formFields) {
        formFields.setCoordinates("exportFilename", 1, 1);
        formFields.setCoordinates("workbookName", 1, 2);
        formFields.setCoordinates("sheetName", 1, 3);

        formFields.setCoordinates("dateFormat", 2, 1);
        formFields.setCoordinates("doubleFormat", 2, 2);

        formFields.setCoordinates("displayRowHeaders", 3, 1);
        formFields.setCoordinates("displayTotals", 3, 2);

        formFields.setLabel("exportFilename", uiMessageSource.getMessage("exportForm.exportFilename"));
        formFields.setLabel("workbookName", uiMessageSource.getMessage("exportForm.workbookName"));
        formFields.setLabel("sheetName", uiMessageSource.getMessage("exportForm.sheetName"));
        formFields.setLabel("dateFormat", uiMessageSource.getMessage("exportForm.dateFormat"));
        formFields.setLabel("doubleFormat", uiMessageSource.getMessage("exportForm.doubleFormat"));
        formFields.setLabel("displayRowHeaders", uiMessageSource.getMessage("exportForm.displayRowHeaders"));
        formFields.setLabel("displayTotals", uiMessageSource.getMessage("exportForm.displayTotals"));
    }

    @Override
    protected void createFooterButtons(HorizontalLayout footerLayout) {
        footerLayout.setSpacing(true);
        footerLayout.setMargin(true);

        Button closeButton = new Button(uiMessageSource.getMessage("exportForm.close"), this, "close");
        closeButton.setDescription(uiMessageSource.getToolTip("exportForm.close.toolTip"));
        closeButton.setIcon(new ThemeResource("../expressui/icons/16/delete.png"));
        closeButton.addStyleName("small default");
        footerLayout.addComponent(closeButton);

        exportButton = new Button(uiMessageSource.getMessage("exportForm.export"));
        exportButton.setDescription(uiMessageSource.getToolTip("exportForm.export.toolTip"));
        exportButton.setIcon(new ThemeResource("../expressui/icons/16/excel.bmp"));
        exportButton.addStyleName("small default");
        footerLayout.addComponent(exportButton);
    }

    /**
     * Sets listener invoked when user clicks export button.
     *
     * @param target     target object
     * @param methodName name of listener method
     */
    public void setExportButtonListener(Object target, String methodName) {
        exportButton.removeListener(Button.ClickEvent.class, target, methodName);
        exportButton.addListener(Button.ClickEvent.class, target, methodName);
    }

    /**
     * Pops up this export form.
     */
    public void open() {
        popupWindow = new Window(getTypeCaption());
        popupWindow.addStyleName("e-exportForm-window");
        popupWindow.addStyleName("opaque");
        VerticalLayout layout = (VerticalLayout) popupWindow.getContent();
        layout.setMargin(true);
        layout.setSpacing(true);
        layout.setSizeUndefined();
        popupWindow.setSizeUndefined();
        popupWindow.setModal(false);
        popupWindow.setClosable(true);

        popupWindow.addComponent(this);
        getMainApplication().getMainWindow().addWindow(popupWindow);

        onDisplay();
    }

    /**
     * Closes/cancels this export form.
     */
    public void close() {
        getMainApplication().getMainWindow().removeWindow(popupWindow);
    }
}
