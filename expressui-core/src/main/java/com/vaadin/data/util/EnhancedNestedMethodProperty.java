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

package com.vaadin.data.util;

import com.expressui.core.util.ReflectionUtil;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

public class EnhancedNestedMethodProperty extends NestedMethodProperty {

    private String propertyName;
    private Object instance;

    public EnhancedNestedMethodProperty(Object instance, String propertyName) {
        super(instance, propertyName);
        this.instance = instance;
        this.propertyName = propertyName;
    }

    public EnhancedNestedMethodProperty(Class<?> instanceClass, String propertyName) {
        super(instanceClass, propertyName);
    }

    @Override
    public Object getValue() {
        if (hasNullInPropertyPath()) {
            return null;
        } else {
            return super.getValue();
        }
    }

    public String getPropertyName() {
        return propertyName;
    }

    public boolean hasNullInPropertyPath() {
        try {
            List<Method> getMethods = getGetMethods();
            Object object = instance;
            for (Method getMethod : getMethods) {
                if (object == null) {
                    return true;
                } else {
                    object = getMethod.invoke(object);
                }
            }
            return false;
        } catch (final InvocationTargetException e) {
            throw new MethodProperty.MethodException(this, e.getTargetException());
        } catch (final Exception e) {
            throw new MethodProperty.MethodException(this, e);
        }
    }

    @Override
    protected void invokeSetMethod(Object value) {
        if (hasNullInPropertyPath()) {
            if (value != null) {
                fillNullsInPropertyPath();
                super.invokeSetMethod(value);
            }
        } else {
            super.invokeSetMethod(value);
        }
    }

    @Override
    public void setValue(Object newValue) throws ReadOnlyException, ConversionException {
        if (newValue == null) {
            List<Method> getMethods = getGetMethods();
            Method lastGetMethod = getMethods.get(getMethods.size() - 1);
            Class returnType = lastGetMethod.getReturnType();
            if (returnType.isPrimitive()) {
                super.setValue(ReflectionUtil.createDefaultPrimitiveValue(returnType));
                return;
            }
        }

        super.setValue(newValue);
    }

    private void fillNullsInPropertyPath() {
        try {
            List<Method> getMethods = getGetMethods();
            Object parent = null;
            Method parentGetMethod = null;
            Object object = instance;
            for (Method getMethod : getMethods) {
                if (object == null && parent != null) {
                    Class returnType = parentGetMethod.getReturnType();
                    String getMethodName = parentGetMethod.getName();
                    Class declaringClass = parentGetMethod.getDeclaringClass();
                    Method setMethod = getSetMethod(declaringClass, returnType, getMethodName);
                    Object fillerInstance = returnType.newInstance();
                    setMethod.invoke(parent, fillerInstance);
                } else {
                    parent = object;
                    parentGetMethod = getMethod;
                    object = getMethod.invoke(parent);
                }
            }
        } catch (final InvocationTargetException e) {
            throw new MethodProperty.MethodException(this, e.getTargetException());
        } catch (final Exception e) {
            throw new MethodProperty.MethodException(this, e);
        }
    }

    private Method getSetMethod(Class containerType, Class propertyType, String getMethodName) {
        try {
            String setMethodName = "set" + getMethodName.substring(3);
            return containerType.getMethod(setMethodName, new Class[]{propertyType});
        } catch (NoSuchMethodException e) {
            throw new MethodProperty.MethodException(this, e);
        }
    }
}
