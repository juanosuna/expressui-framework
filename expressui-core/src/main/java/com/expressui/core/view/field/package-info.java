/**
 * Classes for configuring fields in both forms and results, wrapping Vaadin field components
 * while also providing many extra features:
 * <ul>
 * <li>Automatically and intelligently generates Vaadin field component based on data type of property this field
 * is bound to</li>
 * <li>Automatically and intelligently configures each Vaadin field with default settings</li>
 * <li>Automatically sets fields as required (with *) if bound property is @NotNull or @NotEmpty</li>
 * <li>Automatically adjusts width of fields based on property values/data</li>
 * <li>Keeps track of row and column positions in the form grid layout. A field can span multiple rows and columns.</li>
 * </ul>
 */
package com.expressui.core.view.field;