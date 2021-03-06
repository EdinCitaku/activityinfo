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
package org.activityinfo.server.endpoint.odk;

import com.google.common.base.Strings;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.number.Quantity;
import org.activityinfo.model.type.number.QuantityType;

class QuantityFieldValueParser implements FieldValueParser {
    final private String units;

    QuantityFieldValueParser(QuantityType quantityType) {
        this.units = quantityType.getUnits();
    }

    @Override
    public FieldValue parse(String text) {

        if (Strings.isNullOrEmpty(text)) {
            return null;
        }

        double value;
        try {
            value = Double.parseDouble(text);
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to parse quantity field value: " + text, e);
        }

        return new Quantity(value);
    }
}
