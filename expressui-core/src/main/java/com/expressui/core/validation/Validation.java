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

import org.hibernate.validator.messageinterpolation.ValueFormatterMessageInterpolator;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.validation.Configuration;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import javax.validation.constraints.NotNull;
import javax.validation.metadata.BeanDescriptor;
import javax.validation.metadata.ConstraintDescriptor;
import javax.validation.metadata.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.Set;

import static org.springframework.web.context.WebApplicationContext.SCOPE_SESSION;

/**
 * Service for validating beans against JSR-303 validator implementation.
 */
@Component
public class Validation {
    private Validator validator;

    @PostConstruct
    public void postConstruct() {
        Configuration<?> configuration = javax.validation.Validation.byDefaultProvider().configure();
        ValidatorFactory factory = configuration
                .messageInterpolator(new ClientLocaleMessageInterpolator(configuration.getDefaultMessageInterpolator()))
                .buildValidatorFactory();

        validator = factory.getValidator();
    }

    /**
     * Validates all constraints on <code>object</code>.
     *
     * @param object object to validate
     * @param groups group or list of groups targeted for validation
     *               (default to {@link javax.validation.groups.Default})
     * @return constraint violations or an empty Set if none
     * @throws IllegalArgumentException if object is null
     *                                  or if null is passed to the varargs groups
     * @throws javax.validation.ValidationException
     *                                  if a non recoverable error happens
     *                                  during the validation process
     */
    public <T> Set<ConstraintViolation<T>> validate(T object, Class<?>... groups) {
        return validator.validate(object, groups);
    }

    /**
     * Iterate through constraint violations and find ones that are not-null violations.
     */
    private <T> ConstraintViolation findNotNullViolation(Set<ConstraintViolation<T>> violations) {
        for (ConstraintViolation violation : violations) {
            if (violation.getConstraintDescriptor().getAnnotation().annotationType().equals(NotNull.class)) {
                return violation;
            }
        }

        return null;
    }

    /**
     * Validates all constraints placed on the property of <code>object</code>
     * named <code>propertyName</code>.
     *
     * @param object       object to validate
     * @param propertyPath property path to validate (ie field and getter constraints)
     * @param groups       group or list of groups targeted for validation
     *                     (default to {@link javax.validation.groups.Default})
     * @return constraint violations or an empty Set if none
     * @throws IllegalArgumentException if <code>object</code> is null,
     *                                  if <code>propertyName</code> null, empty or not a valid object property
     *                                  or if null is passed to the varargs groups
     * @throws javax.validation.ValidationException
     *                                  if a non recoverable error happens
     *                                  during the validation process
     */
    public <T> Set<ConstraintViolation<T>> validateProperty(T object, String propertyPath, Class<?>... groups) {

        Set<ConstraintViolation<T>> violations = new HashSet<ConstraintViolation<T>>();

        try {
            int currentIndex = -1;
            String currentPropertyPath;

            do {
                currentIndex = propertyPath.indexOf(".", ++currentIndex);
                if (currentIndex >= 0) {
                    currentPropertyPath = propertyPath.substring(0, currentIndex);
                } else {
                    currentPropertyPath = propertyPath;
                }

                Set<ConstraintViolation<T>> currentViolations = validator.validateProperty(object, currentPropertyPath, groups);

                ConstraintViolation<T> notNullViolation = findNotNullViolation(currentViolations);
                if (notNullViolation == null) {
                    violations = currentViolations;
                } else {
                    violations.add(notNullViolation);
                    break;
                }
            } while (currentIndex >= 0);
        } catch (IllegalArgumentException e) {
            // ignore null property path
        }

        return violations;
    }

    /**
     * Validates all constraints placed on the property named <code>propertyName</code>
     * of the class <code>beanType</code> would the property value be <code>value</code>
     * <p/>
     * <code>ConstraintViolation</code> objects return null for
     * {@link ConstraintViolation#getRootBean()} and {@link ConstraintViolation#getLeafBean()}
     *
     * @param beanType     the bean type
     * @param propertyName property to validate
     * @param value        property value to validate
     * @param groups       group or list of groups targeted for validation
     *                     (default to {@link javax.validation.groups.Default})
     * @return constraint violations or an empty Set if none
     * @throws IllegalArgumentException if <code>beanType</code> is null,
     *                                  if <code>propertyName</code> null, empty or not a valid object property
     *                                  or if null is passed to the varargs groups
     * @throws javax.validation.ValidationException
     *                                  if a non recoverable error happens
     *                                  during the validation process
     */
    public <T> Set<ConstraintViolation<T>> validateValue(Class<T> beanType, String propertyName, Object value, Class<?>... groups) {
        return validator.validateValue(beanType, propertyName, value, groups);
    }

    /**
     * Return the descriptor object describing bean constraints.
     * The returned object (and associated objects including
     * <code>ConstraintDescriptor<code>s) are immutable.
     *
     * @param clazz class or interface type evaluated
     * @return the bean descriptor for the specified class.
     * @throws IllegalArgumentException if clazz is null
     * @throws javax.validation.ValidationException
     *                                  if a non recoverable error happens
     *                                  during the metadata discovery or if some
     *                                  constraints are invalid.
     */
    public BeanDescriptor getConstraintsForClass(Class<?> clazz) {
        return validator.getConstraintsForClass(clazz);
    }

    /**
     * Return an instance of the specified type allowing access to
     * provider-specific APIs.  If the Bean Validation provider
     * implementation does not support the specified class,
     * <code>ValidationException</code> is thrown.
     *
     * @param type the class of the object to be returned.
     * @return an instance of the specified class
     * @throws javax.validation.ValidationException
     *          if the provider does not support the call.
     */
    public <T> T unwrap(Class<T> type) {
        return validator.unwrap(type);
    }

    public boolean isCascaded(Class beanClass, String propertyName) {
        PropertyDescriptor descriptor = validator.getConstraintsForClass(beanClass).getConstraintsForProperty(propertyName);
        return descriptor != null && descriptor.isCascaded();
    }

    /**
     * Ask if a property in a bean class has NotNull annotation
     *
     * @param beanClass    bean class to check
     * @param propertyName name of property to check
     * @return true if property has NotNull annotation
     */
    public boolean isRequired(Class beanClass, String propertyName) {
        return hasAnnotation(beanClass, propertyName, NotNull.class);
    }

    /**
     * Ask if property in a bean class has an annotation
     *
     * @param beanClass       bean class to check
     * @param propertyName    name of property to check
     * @param annotationClass annotation to check
     * @return true if property has the annotation
     */
    public boolean hasAnnotation(Class beanClass, String propertyName, Class annotationClass) {
        PropertyDescriptor descriptor = validator.getConstraintsForClass(beanClass).getConstraintsForProperty(propertyName);
        if (descriptor != null) {
            for (ConstraintDescriptor<?> d : descriptor.getConstraintDescriptors()) {
                Annotation annotation = d.getAnnotation();
                if (annotationClass.isAssignableFrom(annotation.getClass())) {
                    return true;
                }
            }
        }

        return false;
    }
}
