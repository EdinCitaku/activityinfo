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
package org.activityinfo.ui.client.widget.coord;

import com.extjs.gxt.ui.client.widget.form.Field;
import com.extjs.gxt.ui.client.widget.form.PropertyEditor;
import com.extjs.gxt.ui.client.widget.form.Validator;
import com.google.common.annotations.VisibleForTesting;
import org.activityinfo.io.match.coord.CoordinateAxis;
import org.activityinfo.io.match.coord.CoordinateFormatException;
import org.activityinfo.io.match.coord.CoordinateParser;
import org.activityinfo.io.match.coord.JsCoordinateNumberFormatter;
import org.activityinfo.legacy.shared.Log;

public class CoordinateEditor implements PropertyEditor<Double>, Validator {

    private final CoordinateParser parser;

    private String outOfBoundsMessage;

    private double minValue;
    private double maxValue;

    public CoordinateEditor(CoordinateAxis axis) {
        this(axis, JsCoordinateNumberFormatter.INSTANCE);
    }

    public CoordinateEditor(CoordinateAxis axis, CoordinateParser.NumberFormatter instance) {
        minValue = axis.getMinimumValue();
        maxValue = axis.getMaximumValue();

        parser = new CoordinateParser(axis, instance);

        // the parser does not enforce the bounds, but it can use them to infer the
        // hemisphere.
        parser.setMinValue(minValue);
        parser.setMaxValue(maxValue);
    }

    @Override
    public String getStringValue(Double value) {
        String s = parser.format(parser.getNotation(), value);
        Log.debug("CoordinateEditor: " + value + " -> " + s);
        return s;
    }

    @Override
    public Double convertStringValue(String value) {
        if (value == null) {
            return null;
        }

        try {
            double d = parser.parse(value);
            Log.debug("CoordinateEditor: '" + value + "' -> " + d);
            return d;
        } catch (CoordinateFormatException e) {
            return null;
        }
    }

    @Override
    public String validate(Field<?> field, String value) {
        return validate(value);
    }

    @VisibleForTesting
    String validate(String value) {
        if (value == null) {
            return null;
        }

        try {
            double coord = parser.parse(value);

            if (coord < minValue || coord > maxValue) {
                return outOfBoundsMessage;
            }

            return null;
        } catch (CoordinateFormatException ex) {
            Log.debug("CoordinateFormatException parsing [" + value + "]", ex);
            return ex.getMessage();
        } catch (NumberFormatException ex) {
            Log.debug("NumberFormatException parsing [" + value + "]", ex);
            return ex.getMessage();
        }
    }


    public double getMinValue() {
        return minValue;
    }

    public void setMinValue(double minValue) {
        this.minValue = minValue;
        this.parser.setMinValue(minValue);
    }

    public double getMaxValue() {
        return maxValue;
    }

    public void setMaxValue(double maxValue) {
        this.maxValue = maxValue;
        this.parser.setMaxValue(maxValue);
    }

    public String getOutOfBoundsMessage() {
        return outOfBoundsMessage;
    }

    public void setOutOfBoundsMessage(String outOfBoundsMessage) {
        this.outOfBoundsMessage = outOfBoundsMessage;
    }
}
