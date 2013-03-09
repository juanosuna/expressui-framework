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

package com.expressui.core.util;

import com.expressui.core.test.NestedBean;
import com.expressui.core.test.RootBean;
import org.junit.Assert;
import org.junit.Test;

import java.util.Set;

public class BeanInvocationInterceptorTest {

    @Test
    public void getNestedBean() {

        RootBean rootBean = BeanInvocationInterceptor.newBeanRoot(RootBean.class);
        BeanRoot beanInvocationTracker = (BeanRoot) rootBean;

        NestedBean nestedBean = rootBean.getNestedBean();
        Assert.assertNotNull(nestedBean);
        Assert.assertEquals("nestedBean", beanInvocationTracker.lastInvokedPropertyPath());

        String property = nestedBean.getProperty();
        Assert.assertNotNull(property);
        Assert.assertNotNull(beanInvocationTracker.lastInvokedPropertyPath());
        Assert.assertEquals("nestedBean.property", beanInvocationTracker.lastInvokedPropertyPath());

        property = nestedBean.getProperty();
        Assert.assertEquals("nestedBean.property", beanInvocationTracker.lastInvokedPropertyPath());

        Set<NestedBean> nestedBeans = rootBean.getNestedBeans();
        Assert.assertNotNull(nestedBeans);
        Assert.assertNotNull(beanInvocationTracker.lastInvokedPropertyPath());
        Assert.assertEquals("nestedBeans", beanInvocationTracker.lastInvokedPropertyPath());
    }
}