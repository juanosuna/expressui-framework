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

import com.expressui.core.util.assertion.Assert;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;

/**
 * This class may be best described as a "pragmatic hack" to the Java language. It's purpose is to overcome
 * a language weakness, where it is not possible to treat accessor methods or bean properties as objects.
 * This weaknesses often leads to Java applications that are littered with String references to bean properties
 * that are not type safe. To some extent, IDEs have compensated by validating bean properties
 * referenced in XML, JSPs, etc. However, this class provides a robust way of introspectively referencing bean
 * properties within Java code, relying on the compiler rather the IDE for validation. Usage example:
 * <pre>
 *      Person person = BeanInvocationInterceptor.newBeanRoot(Person.class);
 *      person.getAddress().getStreet();
 *      String lastInvokedPropertyPath = ((BeanRoot) person).lastInvokedPropertyPath();
 *      assert lastInvokedPropertyPath.equals("address.street");
 * </pre>
 * Here's how it works. The generated person instance is similar to a mock object. It does not serve any domain
 * purpose other than to track invocations across the underlying object graph. As each accessor method is invoked,
 * the last-invoked property path is recorded in the person root, which can be extracted by casting to
 * BeanInvocationTracker.
 *
 * It's purpose is to track invocations
 * of bean property accessor methods within the context of an object graph. Keeping tracking of these
 * invocations allows you to known what the property path is
 * An instance of this class is created and paired with each node, as accessor methods are invoked.
 */
public class BeanInvocationInterceptor implements MethodInterceptor, BeanRoot {

    private String propertyName = "";
    private String lastInvokedPropertyPath;
    private Object proxiedObject;
    private BeanInvocationInterceptor parent;

    private BeanInvocationInterceptor() {
    }

    @Override
    public String lastInvokedPropertyPath() {
        return lastInvokedPropertyPath;
    }

    private BeanInvocationInterceptor getRoot() {
        return parent == null ? this : parent.getRoot();
    }

    private String getPropertyPath() {
        return parent == null ? propertyName : parent.getPropertyPath() + "." + propertyName;
    }

    public static <T> T newBeanRoot(Class<T> clazz) {
        Assert.PROGRAMMING.notNull(clazz);
        return newInstance(clazz, null, null);
    }

    private static <T> T newInstance(Class<T> clazz, BeanInvocationInterceptor parent, Object proxiedObject) {
        Assert.PROGRAMMING.notNull(clazz);
        // Cannot proxy final classes. All final classes must be leaves in the graph
        if (Modifier.isFinal(clazz.getModifiers()) || Number.class.isAssignableFrom(clazz)) {
            return (T) newInstance(clazz);
        } else {
            try {
                Enhancer enhancer = new Enhancer();
                // Add BeanInvocationTracker only to root of the graph
                if (parent == null) {
                    enhancer.setInterfaces(new Class[]{BeanNode.class, BeanRoot.class});

                } else {
                    enhancer.setInterfaces(new Class[]{BeanNode.class});
                }
                enhancer.setSuperclass(clazz);

                // Create and set interceptor so a distinct instance is paired with every proxy in the graph
                BeanInvocationInterceptor interceptor = new BeanInvocationInterceptor();
                interceptor.parent = parent;

                // Set any object that should be proxied, maybe null, in which case empty objects can be auto-generated
                interceptor.proxiedObject = proxiedObject;
                enhancer.setCallback(interceptor);

                return (T) enhancer.create();
            } catch (Throwable e) {
                throw new Error(e);
            }
        }
    }

    @Override
    public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {

        if (BeanRoot.class.isAssignableFrom(method.getDeclaringClass())) {
            return proxy.invoke(this, args);
        }

        Object returnValue = null;
        if (proxiedObject == null) {
            try {
                returnValue = proxy.invokeSuper(obj, args);
            } catch (NoSuchMethodError error) { // happens if proxy is for interface and no super exists
            }
        } else {
            returnValue = proxy.invoke(proxiedObject, args);
        }

        if (isProperty(method)) {
            propertyName = getPropertyName(method);
            getRoot().lastInvokedPropertyPath = getPropertyPath();
        }

        if (returnValue != null && returnValue instanceof BeanNode) {
            return returnValue;
        } else {
            if (isProperty(method)) {
                return newInstance(getPropertyType(method), this, returnValue);
            } else {
                return null;
            }
        }
    }

    private static boolean isProperty(Method method) {
        return BeanUtils.findPropertyForMethod(method) != null;
    }

    private static String getPropertyName(Method method) throws BeansException {
        PropertyDescriptor propertyDescriptor = BeanUtils.findPropertyForMethod(method);
        if (propertyDescriptor == null) {
            return null;
        } else {
            return propertyDescriptor.getName();
        }
    }

    private static Class getPropertyType(Method method) throws BeansException {
        PropertyDescriptor propertyDescriptor = BeanUtils.findPropertyForMethod(method);
        if (propertyDescriptor == null) {
            return null;
        } else {
            return propertyDescriptor.getPropertyType();
        }
    }

    private static Class toClass(String className) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private static Object newInstance(Class clazz) {
        if (BigDecimal.class.isAssignableFrom(clazz)) {
            return new BigDecimal(0);
        } else if (Number.class.isAssignableFrom(clazz)
                || byte.class.equals(clazz)
                || short.class.equals(clazz)
                || int.class.equals(clazz)
                || long.class.equals(clazz)
                || float.class.equals(clazz)
                || double.class.equals(clazz)
                ) {
            return 0;
        } else if (Boolean.class.isAssignableFrom(clazz) || boolean.class.equals(clazz)) {
            return false;
        } else if (Character.class.isAssignableFrom(clazz) || char.class.equals(clazz)) {
            return '\0';
        } else if (Enum.class.isAssignableFrom(clazz)) {
            Object[] enumConstants = clazz.getEnumConstants();
            return enumConstants.length > 0 ? enumConstants[0] : null;
        } else {
            try {
                return clazz.newInstance();
            } catch (InstantiationException e) {
                return null;
            } catch (IllegalAccessException e) {
                return null;
            }
        }
    }
}

