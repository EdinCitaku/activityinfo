/*
 * ActivityInfo
 * Copyright (C) 2009-2013 UNICEF
 * Copyright (C) 2014-2018 BeDataDriven Groep B.V.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.activityinfo.model.type;

import org.activityinfo.model.type.barcode.BarcodeType;
import org.activityinfo.model.type.geo.GeoPointType;
import org.activityinfo.model.type.number.QuantityType;
import org.activityinfo.model.type.primitive.BooleanType;
import org.activityinfo.model.type.primitive.TextType;
import org.activityinfo.model.type.time.LocalDateType;

/**
 * Defines a class of Field Types.
 *
 * FieldTypeClass are meant to provide a very specific types of fields
 * at a high level of abstraction. For example, beyond simply a "number" type,
 * we will also have a QuantityType, a RatioType, a CurrencyType, etc, that carry
 * logic with them about how they should be aggregated, indexed, etc.
 *
 * FieldTypeClasses can be further specialized with parameters: for example, the
 * QuantityType takes a "units" parameter.
 *
 */
public interface FieldTypeClass {

    /**
     *
     * @return a string uniquely identifying this {@code FieldTypeClass}. This
     * identifier will be stored with all values of types in this class.
     */
    String getId();


    /**
     *
     * @return an instance of this {@code FieldTypeClass} using default parameters
     */
    FieldType createType();


    // intermediate step to support refactoring


    public static final ParametrizedFieldTypeClass QUANTITY = QuantityType.TYPE_CLASS;

    public static final FieldTypeClass NARRATIVE = NarrativeType.TYPE_CLASS;

    public static final FieldTypeClass FREE_TEXT = TextType.TYPE_CLASS;

    public static final FieldTypeClass LOCAL_DATE = LocalDateType.TYPE_CLASS;

    public static final FieldTypeClass GEOGRAPHIC_POINT = GeoPointType.TYPE_CLASS;

    public static final FieldTypeClass BOOLEAN = BooleanType.TYPE_CLASS;

    public static final FieldTypeClass BARCODE = BarcodeType.TYPE_CLASS;

    public static final ParametrizedFieldTypeClass REFERENCE = ReferenceType.TYPE_CLASS;

}
