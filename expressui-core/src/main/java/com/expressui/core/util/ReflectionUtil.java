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
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.DynaProperty;
import org.apache.commons.beanutils.WrapDynaBean;
import org.apache.commons.lang.ClassUtils;
import org.springframework.beans.BeanUtils;

import java.beans.PropertyDescriptor;
import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Reflection utility class
 */
public class ReflectionUtil {

    /**
     * Create new instance from the given type.
     *
     * @param type class to reflectively instantiate
     * @param <T>  type of the class
     * @return new instance
     */
    public static <T> T newInstance(Class<T> type) {
        try {
            return type.newInstance();
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Create new instance from the given type, based on given parameter types and args.
     *
     * @param type           class to reflectively instantiate
     * @param <T>            type of the class
     * @param parameterTypes used to find the right constructor
     * @param args           passed to constructor
     * @return new instance
     */
    public static <T> T newInstance(Class<T> type, Class[] parameterTypes, Object[] args) {
        try {
            Constructor constructor = type.getDeclaredConstructor(parameterTypes);
            constructor.setAccessible(true);
            return (T) constructor.newInstance(args);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Get the first generic argument of the given class
     *
     * @param clazz class from which to extract generic argument
     * @return type of generic argument
     */
    public static Class getGenericArgumentType(Class clazz) {
        return getGenericArgumentType(clazz, 0);
    }

    /**
     * Get a generic argument of the given class, based on arg index
     *
     * @param clazz    class from which to extract generic argument
     * @param argIndex index of the declared argument type
     * @return type of generic argument
     */
    public static Class getGenericArgumentType(Class clazz, int argIndex) {
        Type type = clazz.getGenericSuperclass();

        if (type != null && type instanceof ParameterizedType) {
            if (((ParameterizedType) type).getActualTypeArguments()[argIndex] instanceof Class) {
                return (Class) ((ParameterizedType) type).getActualTypeArguments()[argIndex];
            } else {
                return null;
            }
        } else {
            if (!(type instanceof Class) || type.equals(Object.class)) {
                return null;
            } else {
                return getGenericArgumentType((Class) type, argIndex);
            }
        }
    }

    /**
     * Get the value type of the given collection, as declared by the collection property type
     *
     * @param beanType     bean class
     * @param beanProperty name of property, which must be a collection
     * @return generic type declared for collection members
     */
    public static Class getCollectionValueType(Class beanType, String beanProperty) {
        PropertyDescriptor descriptor = BeanUtils.getPropertyDescriptor(beanType, beanProperty);
        Class propertyType = descriptor.getPropertyType();
        Assert.PROGRAMMING.isTrue(Collection.class.isAssignableFrom(propertyType),
                "Bean property not a collection type: " + beanType + "." + beanProperty);

        Type genericPropertyType = descriptor.getReadMethod().getGenericReturnType();
        Class collectionValueType = null;
        if (genericPropertyType != null && genericPropertyType instanceof ParameterizedType) {
            Type[] typeArgs = ((ParameterizedType) genericPropertyType).getActualTypeArguments();
            if (typeArgs != null && typeArgs.length > 0) {
                if (typeArgs.length == 1) {
                    collectionValueType = (Class) typeArgs[0];
                } else if (typeArgs.length == 2) {
                    collectionValueType = (Class) typeArgs[1];
                } else {
                    Assert.PROGRAMMING.fail("Collection type has more than two generic arguments");
                }
            }
        }

        return collectionValueType;
    }

    /**
     * Ask if the bean's properties are empty. boolean properties that are false and numbers
     * that are zero are considered empty. String values that are zero-length are considered empty.
     * All other property types must be null to be considered empty.
     *
     * @param bean bean to check
     * @return true if bean has no values
     */
    public static boolean isBeanEmpty(Object bean) {
        if (bean == null) {
            return true;
        }

        WrapDynaBean wrapDynaBean = new WrapDynaBean(bean);
        DynaProperty[] properties = wrapDynaBean.getDynaClass().getDynaProperties();
        for (DynaProperty property : properties) {
            String propertyName = property.getName();
            Class propertyType = property.getType();

            Object value = wrapDynaBean.get(propertyName);
            if (propertyType.isPrimitive()) {
                if (value instanceof Number && !value.toString().equals("0")) {
                    return false;
                } else if (value instanceof Boolean && !value.toString().equals("false")) {
                    return false;
                } else if (!value.toString().isEmpty()) {
                    return false;
                }
            } else if (value != null) {
                if (!(value instanceof Collection)) {
                    String convertedStringValue = ConvertUtils.convert(value);
                    if (!StringUtil.isEmpty(convertedStringValue)) {
                        return false;
                    }
                }
            }
        }

        return true;
    }

    /**
     * Find all properties in the bean that are complex, i.e. not BeanUtils.isSimpleValueType
     *
     * @param bean bean to reflectively analyze
     * @return collection of properties on the bean
     */
    public static Collection<String> findComplexProperties(Object bean) {
        Collection<String> complexProperties = new ArrayList<String>();

        WrapDynaBean wrapDynaBean = new WrapDynaBean(bean);
        DynaProperty[] properties = wrapDynaBean.getDynaClass().getDynaProperties();
        for (DynaProperty property : properties) {
            String propertyName = property.getName();
            Class propertyType = property.getType();
            if (!BeanUtils.isSimpleValueType(propertyType)) {
                complexProperties.add(propertyName);
            }
        }

        return complexProperties;
    }

    /**
     * Convert object value to given type. Converts primitives to their wrappers.
     * Converts strings to numbers.
     *
     * @param value value to convert
     * @param type  type to convert to
     * @param <T>   type
     * @return converted value
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     * @throws InstantiationException
     * @throws NoSuchMethodException
     */
    public static <T> T convertValue(Object value, Class<T> type) throws InvocationTargetException, IllegalAccessException, InstantiationException, NoSuchMethodException {
        Class clazz;
        if (type.isPrimitive()) {
            clazz = ClassUtils.primitiveToWrapper(type);
        } else {
            clazz = type;
        }

        if (null == value || clazz.isAssignableFrom(value.getClass())) {
            return (T) value;
        }

        Constructor<T> constructor = clazz.getConstructor(new Class[]{String.class});

        return constructor.newInstance(value.toString());
    }

    /**
     * Ask if given type is a number type.
     *
     * @param type type to check
     * @return true if a number
     */
    public static boolean isNumberType(Class type) {
        Class clazz;
        if (type.isPrimitive()) {
            clazz = ClassUtils.primitiveToWrapper(type);
        } else {
            clazz = type;
        }

        return Number.class.isAssignableFrom(clazz);
    }

    /**
     * Find a method on a class.
     *
     * @param type           class containing the method
     * @param methodName     name of the method to search for
     * @param parameterTypes parameter types declared in the method signature
     * @return found method
     */
    public static Method getMethod(Class type, String methodName, Class<?>... parameterTypes) {
        Method method = null;
        Class currentType = type;
        while (method == null && !currentType.equals(Object.class)) {
            try {
                method = currentType.getDeclaredMethod(methodName, parameterTypes);
            } catch (SecurityException e) {
                throw new RuntimeException(e);
            } catch (NoSuchMethodException e) {
                currentType = currentType.getSuperclass();
            }
        }

        return method;
    }

    /**
     * Find field on a given type
     *
     * @param type      class containing the field
     * @param fieldName name of field to search for
     * @return found field
     */
    public static Field getField(Class type, String fieldName) {
        Field field = null;
        Class currentType = type;
        while (field == null && !currentType.equals(Object.class)) {
            try {
                field = currentType.getDeclaredField(fieldName);
            } catch (SecurityException e) {
                throw new RuntimeException(e);
            } catch (NoSuchFieldException e) {
                currentType = currentType.getSuperclass();
            }
        }

        return field;
    }
}
