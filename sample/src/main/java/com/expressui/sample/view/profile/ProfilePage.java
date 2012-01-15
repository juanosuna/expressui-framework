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

package com.expressui.sample.view.profile;

import com.expressui.core.entity.security.User;
import com.expressui.core.security.SecurityService;
import com.expressui.core.view.page.Page;
import com.expressui.sample.dao.ProfileDao;
import com.expressui.sample.entity.Profile;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.VerticalLayout;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

@Component
@Scope("session")
@SuppressWarnings({"serial"})
public class ProfilePage extends CustomComponent implements Page {

    @Resource
    private SecurityService securityService;

    @Resource
    private ProfileDao profileDao;

    @Resource
    private ProfileForm profileForm;

    @PostConstruct
    @Override
    public void postConstruct() {
        VerticalLayout layout = new VerticalLayout();
        layout.setMargin(true);
        layout.setSpacing(true);
        setCompositionRoot(layout);

        setCustomSizeUndefined();

        layout.addComponent(profileForm);
        layout.setComponentAlignment(profileForm, Alignment.MIDDLE_CENTER);
    }

    private void setCustomSizeUndefined() {
        setSizeUndefined();
        getCompositionRoot().setSizeUndefined();
    }

    @Override
    public void postWire() {
    }

    @Override
    public void onLoad() {
        User user = securityService.getCurrentUser();
        Profile profile = profileDao.findOneByUser(user);

        if (profile == null) {
            profileForm.create();
            profileForm.getEntity().setUser(user);
        } else {
            profileForm.load(profile);
        }
    }

    @Override
    public boolean isViewAllowed() {
        return true;
    }
}

