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
package org.activityinfo.server.util.date;

import org.activityinfo.legacy.shared.reports.model.DateRange;
import org.activityinfo.server.report.util.DateUtilCalendarImpl;
import org.junit.Test;

import java.util.Calendar;
import java.util.Date;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class DateUtilCalendarImplTest {

    private DateUtilCalendarImpl util = new DateUtilCalendarImpl();

    @Test
    public void floorMonth() {
        Calendar fifth = Calendar.getInstance();
        fifth.set(2011, Calendar.MAY, 5);

        Date date = util.floorMonth(fifth.getTime());

        Calendar floored = Calendar.getInstance();
        floored.setTime(date);

        assertThat("year", floored.get(Calendar.YEAR), equalTo(2011));
        assertThat("month", floored.get(Calendar.MONTH), equalTo(Calendar.MAY));
        assertThat("day", floored.get(Calendar.DATE), equalTo(1));
    }

    @Test
    public void lastCompleteMonth() {
        Calendar fifth = Calendar.getInstance();
        fifth.set(2011, Calendar.MAY, 5);

        DateRange range = util.lastCompleteMonthRange(fifth.getTime());
        assertThat(range.getMinDate().getDate(), equalTo(1));
        assertThat(range.getMaxDate().getDate(), equalTo(30));
    }

}
