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
package org.activityinfo.model.type.time;

import org.activityinfo.json.Json;
import org.activityinfo.json.JsonValue;
import org.activityinfo.model.type.FieldTypeClass;
import org.activityinfo.model.type.FieldValue;

/**
 * Represents a specific calendar year in the ISO-8601 calendar.
 */
public class YearValue implements FieldValue, PeriodValue {

    private final int year;

    public YearValue(int year) {
        this.year = year;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        YearValue yearValue = (YearValue) o;

        if (year != yearValue.year) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return year;
    }

    @Override
    public FieldTypeClass getTypeClass() {
        return YearType.TYPE_CLASS;
    }

    @Override
    public JsonValue toJson() {
        return Json.create(year);
    }

    @Override
    public LocalDateInterval asInterval() {
        return new LocalDateInterval(new LocalDate(year, 1, 1), new LocalDate(year, 12, 31));
    }

    @Override
    public PeriodValue next() {
        return new YearValue(year + 1);
    }

    @Override
    public PeriodValue previous() {
        return new YearValue(year - 1);
    }

    public int getYear() {
        return year;
    }

    public static boolean isLeapYear(int year) {
        return !((year % 4 != 0) || ((year % 100 == 0) && (year % 400 != 0)));
    }
}
