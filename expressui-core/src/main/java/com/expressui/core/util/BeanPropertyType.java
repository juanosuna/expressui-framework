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

package com.expressui.core.util;

import com.expressui.core.util.assertion.Assert;
import org.springframework.beans.BeanUtils;

import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.Valid;
import javax.validation.constraints.*;
import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.*;

public class BeanPropertyType {
    private static Map<String, BeanPropertyType> cache = new HashMap<String, BeanPropertyType>();

    private BeanPropertyType parent;
    private String id;
    private Class type;
    private Class containerType;
    private Class collectionValueType;
    private BusinessType businessType;
    private Set<Annotation> annotations = new HashSet<Annotation>();

    private BeanPropertyType(BeanPropertyType parent, String id, Class type, Class containerType, Class collectionValueType) {
        this.parent = parent;
        this.id = id;
        this.type = type;
        this.containerType = containerType;
        this.collectionValueType = collectionValueType;

        initAnnotations();
        businessType = createBusinessType();
    }

    private void initAnnotations() {
        PropertyDescriptor descriptor = BeanUtils.getPropertyDescriptor(containerType, id);
        Method method = descriptor.getReadMethod();
        Annotation[] readMethodAnnotations = method.getAnnotations();
        Collections.addAll(annotations, readMethodAnnotations);

        Field field;
        try {
            field = containerType.getDeclaredField(id);
            Annotation[] fieldAnnotations = field.getAnnotations();
            Collections.addAll(annotations, fieldAnnotations);
        } catch (NoSuchFieldException e) {
            // no need to get annotations if field doesn't exist
        }
    }

    public boolean hasAnnotation(Class annotationClass) {
        for (Annotation annotation : annotations) {
            if (annotationClass.isAssignableFrom(annotation.getClass())) {
                return true;
            }
        }

        return false;
    }

    public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
        for (Annotation annotation : annotations) {
            if (annotationClass.isAssignableFrom(annotation.getClass())) {
                return (T) annotation;
            }
        }

        return null;
    }

    public Integer getMinimumLength() {
        return MathUtil.minDisallowNull(getMinimumLengthImpl(), getMaximumLengthImpl());
    }

    public Integer getMaximumLength() {
        return MathUtil.maxDisallowNull(getMinimumLengthImpl(), getMaximumLengthImpl());
    }

    private Integer getMinimumLengthImpl() {
        Integer min = null;

        for (Annotation annotation : annotations) {
            if (Size.class.isAssignableFrom(annotation.getClass())) {
                Size size = (Size) annotation;
                min = MathUtil.minIgnoreNull(min, size.min());
            } else if (Min.class.isAssignableFrom(annotation.getClass())) {
                Min m = (Min) annotation;
                min = MathUtil.minIgnoreNull(min, String.valueOf(m.value()).length());
            } else if (Digits.class.isAssignableFrom(annotation.getClass())) {
                Digits digits = (Digits) annotation;
                min = MathUtil.minIgnoreNull(min, digits.integer() + digits.fraction() + 1);
            } else if (DecimalMin.class.isAssignableFrom(annotation.getClass())) {
                DecimalMin m = (DecimalMin) annotation;
                min = MathUtil.minIgnoreNull(min, String.valueOf(m.value()).length());
            } else if (DecimalMax.class.isAssignableFrom(annotation.getClass())) {
                DecimalMax m = (DecimalMax) annotation;
                min = MathUtil.minIgnoreNull(min, String.valueOf(m.value()).length());
            }
        }

        return min;
    }

    private Integer getMaximumLengthImpl() {
        Integer max = null;

        for (Annotation annotation : annotations) {
            if (Size.class.isAssignableFrom(annotation.getClass())) {
                Size size = (Size) annotation;
                max = MathUtil.maxIgnoreNull(max, size.max());
            } else if (Max.class.isAssignableFrom(annotation.getClass())) {
                Max m = (Max) annotation;
                max = MathUtil.maxIgnoreNull(max, String.valueOf(m.value()).length());
            } else if (Digits.class.isAssignableFrom(annotation.getClass())) {
                Digits digits = (Digits) annotation;
                max = MathUtil.maxIgnoreNull(max, digits.integer() + digits.fraction() + 1);
            } else if (DecimalMin.class.isAssignableFrom(annotation.getClass())) {
                DecimalMin m = (DecimalMin) annotation;
                max = MathUtil.maxIgnoreNull(max, String.valueOf(m.value()).length());
            } else if (DecimalMax.class.isAssignableFrom(annotation.getClass())) {
                DecimalMax m = (DecimalMax) annotation;
                max = MathUtil.maxIgnoreNull(max, String.valueOf(m.value()).length());
            }
        }

        return max;
    }

    public static BeanPropertyType getBeanPropertyType(Class clazz, String propertyPath) {
        String key = clazz.getName() + "." + propertyPath;
        if (!cache.containsKey(key)) {
            cache.put(key, getBeanPropertyTypeImpl(clazz, propertyPath));
        }

        return cache.get(key);
    }

    public static BeanPropertyType getBeanPropertyTypeImpl(Class clazz, String propertyPath) {
        String[] properties = propertyPath.split("\\.");
        Class containingType;
        Class currentPropertyType = clazz;
        BeanPropertyType beanPropertyType = null;
        for (String property : properties) {
            Class propertyType = BeanUtils.findPropertyType(property, new Class[]{currentPropertyType});
            Assert.PROGRAMMING.assertTrue(propertyType != null && !propertyType.equals(Object.class),
                    "Invalid property path: " + clazz + "." + property);

            Class propertyPathType;
            Class collectionValueType = null;
            if (Collection.class.isAssignableFrom(propertyType)) {
                collectionValueType = ReflectionUtil.getCollectionValueType(currentPropertyType, property);
                propertyPathType = collectionValueType;
            } else {
                propertyPathType = propertyType;
            }

            containingType = currentPropertyType;
            currentPropertyType = propertyPathType;

            beanPropertyType = new BeanPropertyType(beanPropertyType, property, propertyType, containingType,
                    collectionValueType);
        }

        return beanPropertyType;
    }

    public BeanPropertyType getParent() {
        return parent;
    }

    public String getId() {
        return id;
    }

    public String getLeafId() {
        return StringUtil.extractAfterPeriod(id);
    }

    public Class getType() {
        return type;
    }

    public BusinessType getBusinessType() {
        return businessType;
    }

    private BusinessType createBusinessType() {
        if (getType() == Date.class) {
            Temporal temporal = getAnnotation(Temporal.class);
            if (temporal != null && temporal.value().equals(TemporalType.DATE)) {
                return BusinessType.DATE;
            } else {
                return BusinessType.DATE_TIME;
            }
        }
        if (ReflectionUtil.isNumberType(getType())) {
            return BusinessType.NUMBER;
        }
        if (String.class.isAssignableFrom(getType())) {
            return BusinessType.TEXT;
        }

        if (BigDecimal.class.isAssignableFrom(getType())) {
            return BusinessType.MONEY;
        }

        return null;
    }

    public Class getContainerType() {
        return containerType;
    }

    public Class getCollectionValueType() {
        return collectionValueType;
    }

    public boolean isCollectionType() {
        return Collection.class.isAssignableFrom(type);
    }

    public boolean isValidatable() {
        BeanPropertyType beanPropertyType = parent;
        while (beanPropertyType != null) {
            Class containingType = beanPropertyType.getContainerType();
            String id = beanPropertyType.getId();

            PropertyDescriptor descriptor = BeanUtils.getPropertyDescriptor(containingType, id);
            Method readMethod = descriptor.getReadMethod();
            Valid validAnnotation = null;
            if (readMethod != null) {
                validAnnotation = readMethod.getAnnotation(Valid.class);
            }
            if (validAnnotation == null) {
                Field field = ReflectionUtil.getField(containingType, id);
                Assert.PROGRAMMING.assertTrue(field != null, "Cannot find field: "
                        + containingType.getName() + "." + id);

                validAnnotation = field.getAnnotation(Valid.class);
            }

            if (validAnnotation == null) {
                return false;
            } else {
                beanPropertyType = beanPropertyType.getParent();
            }
        }

        return true;
    }

    public BeanPropertyType getRoot() {
        if (parent == null) {
            return this;
        } else {
            return getParent().getRoot();
        }
    }

    public static enum BusinessType {
        NUMBER,
        DATE,
        DATE_TIME,
        MONEY,
        TEXT
    }
}