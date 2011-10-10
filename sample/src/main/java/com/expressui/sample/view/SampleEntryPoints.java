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

package com.expressui.sample.view;

import com.expressui.core.MainApplication;
import com.expressui.core.view.MainEntryPoint;
import com.expressui.core.view.MainEntryPoints;
import com.expressui.domain.ecbfx.EcbfxService;
import com.expressui.sample.view.account.AccountEntryPoint;
import com.expressui.sample.view.contact.ContactEntryPoint;
import com.expressui.sample.view.opportunity.OpportunityEntryPoint;
import com.expressui.sample.view.role.RoleEntryPoint;
import com.expressui.sample.view.user.UserEntryPoint;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.persistence.Transient;
import java.util.ArrayList;
import java.util.List;

@Component
@Scope("prototype")
@SuppressWarnings({"rawtypes", "serial"})
public class SampleEntryPoints extends MainEntryPoints {

    @Resource
    private ContactEntryPoint contactEntryPoint;

    @Resource
    private AccountEntryPoint accountEntryPoint;

    @Resource
    private OpportunityEntryPoint opportunityEntryPoint;

    @Resource
    private UserEntryPoint userEntryPoint;

    @Resource
    private RoleEntryPoint roleEntryPoint;

    @Resource
    @Transient
    private EcbfxService ecbfxService;

    @Override
    public List<MainEntryPoint> getEntryPoints() {
        List<MainEntryPoint> entryPoints = new ArrayList<MainEntryPoint>();
        entryPoints.add(contactEntryPoint);
        entryPoints.add(accountEntryPoint);
        entryPoints.add(opportunityEntryPoint);
        entryPoints.add(userEntryPoint);
        entryPoints.add(roleEntryPoint);

        return entryPoints;
    }

    @Override
    public String getTheme() {
        return "sampleTheme";
    }

    @Override
    public void postWire() {
        super.postWire();

        try {
            ecbfxService.getFXRates();
        } catch (Exception e) {
            MainApplication.getInstance().showWarning("I can't seem to fetch FX rates from an external REST service hosted at European" +
                    " Central Bank. Please see application.properties. You may need to set an HTTP proxy address." +
                    " In the meantime, click this box to make it disappear.");
        }
    }
}
