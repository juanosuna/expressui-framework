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

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;


/**
 * Asserts that the annotated method must return true, or else validation error will be thrown
 * for the bean containing the annotated method.
 * <P/>
 * This annotation should be used in cases where the assertion method validates multiple properties
 * in a bean.
 * <P/>
 * IMPORTANT: the annotated method must conform to the bean standard convention for read methods!
 * This is so that the Validator will automatically invoke the assertion method as if it were just another bean
 * property being validated.
 */
@Target({METHOD})
@Retention(RUNTIME)
@Constraint(validatedBy = AssertTrueForPropertiesValidator.class)
@Documented
public @interface AssertTrueForProperties {
    String message() default "{javax.validation.constraints.AssertTrue.message}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    /**
     * Specifies which property should be associated with the error message, from a UI perspective.
     * For example, if zipCode is not consistent with selected country, then setting errorProperty
     * to zipCode would cause the validation error to be displayed with the zipCode field rather
     * than country field.
     *
     * @return property associated with error, from UI perspective
     */
    String errorProperty();
}
