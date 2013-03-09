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

package com.expressui.core.validation;

import com.expressui.core.AbstractCoreTest;
import com.expressui.core.MainApplication;
import com.expressui.core.test.NestedBean;
import com.expressui.core.test.RootBean;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.test.context.ContextConfiguration;

import javax.annotation.Resource;
import javax.validation.ConstraintViolation;
import java.util.Locale;
import java.util.Set;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ContextConfiguration(locations = {
        "classpath:/spring/applicationContext-test-validation.xml"
})
public class ValidationTest extends AbstractCoreTest {

    @Resource
    private Validation validation;

    @BeforeClass
    public static void beforeClass() {
        MainApplication mainApplication = mock(MainApplication.class);
        MainApplication.setInstance(mainApplication);
        when(mainApplication.getLocale()).thenReturn(Locale.getDefault());
    }

    @Test
    public void validateInvalidRoot() {
        RootBean rootBean = new RootBean();
        NestedBean nestedBean = new NestedBean();
        nestedBean.setProperty("12");
        rootBean.setNestedBean(nestedBean);

        Set<ConstraintViolation<RootBean>> violations = validation.validate(rootBean);
        Assert.assertTrue(violations.size() > 0);
    }

    @Test
    public void validateRootProperty() {
        RootBean rootBean = new RootBean();
        NestedBean nestedBean = new NestedBean();
        nestedBean.setProperty("12");
        rootBean.setNestedBean(nestedBean);

        Set<ConstraintViolation<RootBean>> violations = validation.validateProperty(rootBean, "nestedBean");
        Assert.assertTrue(violations.isEmpty());
    }

    @Test
    public void validateInvalidRootNotNullProperty() {
        RootBean rootBean = new RootBean();

        Set<ConstraintViolation<RootBean>> violations = validation.validateProperty(rootBean, "notNullNestedBean");
        Assert.assertTrue(violations.size() == 1);
    }

    @Test
    public void validateInvalidNestedProperty() {
        RootBean rootBean = new RootBean();
        NestedBean nestedBean = new NestedBean();
        nestedBean.setProperty("12");
        rootBean.setNestedBean(nestedBean);

        Set<ConstraintViolation<RootBean>> violations = validation.validateProperty(rootBean, "nestedBean.property");
        Assert.assertTrue(violations.size() == 1);
    }

    @Test
    public void validateInvalidNestedNullProperty() {
        RootBean rootBean = new RootBean();
        NestedBean nestedBean = new NestedBean();
        rootBean.setNestedBean(nestedBean);

        Set<ConstraintViolation<RootBean>> violations = validation.validateProperty(rootBean, "nestedBean.property");
        Assert.assertTrue(violations.size() == 1);
    }

    @Test
    public void validateInvalidNotNullNullNestedProperty() {
        RootBean rootBean = new RootBean();

        Set<ConstraintViolation<RootBean>> violations = validation.validateProperty(rootBean, "notNullNestedBean.property");
        Assert.assertTrue(violations.size() == 1);
    }

    @Test
    public void validateInvalidOptionalNullNestedProperty() {
        RootBean rootBean = new RootBean();

        Set<ConstraintViolation<RootBean>> violations = validation.validateProperty(rootBean,
                "notNullNestedBean.optionalProperty");
        Assert.assertTrue(violations.size() == 1);
    }

    @Test
    public void validateNestedPropertyWithNull() {
        RootBean rootBean = new RootBean();

        Set<ConstraintViolation<RootBean>> violations = validation.validateProperty(rootBean, "nestedBean.property");
        Assert.assertTrue(violations.isEmpty());
    }

    @Test
    public void validateIgnoredNestedProperty() {
        RootBean rootBean = new RootBean();
        NestedBean nestedBean = new NestedBean();
        nestedBean.setProperty("12");
        rootBean.setIgnoredNestedBean(nestedBean);

        Set<ConstraintViolation<RootBean>> violations = validation.validateProperty(rootBean, "ignoredNestedBean.property");
        Assert.assertTrue(violations.isEmpty());
    }

}
