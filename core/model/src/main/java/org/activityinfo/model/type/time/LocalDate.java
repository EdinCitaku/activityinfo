package org.activityinfo.model.type.time;

import com.bedatadriven.rebar.time.CalendricalException;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.user.datepicker.client.CalendarUtil;
import org.activityinfo.model.type.FieldTypeClass;
import org.activityinfo.model.type.FieldValue;

import java.util.Calendar;
import java.util.Date;

/**
 * {@code FieldValue} of type {@code LocalDateType}
 */
public class LocalDate implements FieldValue, TemporalValue {

    private int year;
    private int monthOfYear;
    private int dayOfMonth;

    public LocalDate() {
        this(new Date());
    }

    public LocalDate(int year, int monthOfYear, int dayOfMonth) {
        this.year = year;
        this.monthOfYear = monthOfYear;
        this.dayOfMonth = dayOfMonth;
    }

    @SuppressWarnings("deprecation")
    public LocalDate(Date date) {
        this.year = date.getYear()+1900;
        this.monthOfYear = date.getMonth()+1;
        this.dayOfMonth = date.getDate();
    }

    /**
     *
     * Io ISO-8601
     */
    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    /**
     * Gets the month-of-year field
     *
     * @return the month-of-year field, 1-12
     */
    public int getMonthOfYear() {
        return monthOfYear;
    }

    public void setMonthOfYear(int monthOfYear) {
        this.monthOfYear = monthOfYear;
    }

    /**
     *
     * @return the day-of-month, from 1 to 31
     */
    public int getDayOfMonth() {
        return dayOfMonth;
    }

    public void setDayOfMonth(int dayOfMonth) {
        this.dayOfMonth = dayOfMonth;
    }

    
    public int getQuarter() {
        int quarter0 = (monthOfYear - 1) / 3;
        return quarter0 + 1;
    }
    
    /**
     *
     * @return a java.util.Date instance representing the instant at midnight on this date
     * in the browser's timezone or the JRE's default timezone.
     */
    public Date atMidnightInMyTimezone() {
        return new Date(year-1900, monthOfYear-1, dayOfMonth);
    }

    /**
     * Returns this data as an ISO-8601 string
     */
    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        s.append(year);
        s.append("-");
        if(monthOfYear < 10) {
            s.append("0");
        }
        s.append(monthOfYear);
        s.append("-");
        if(dayOfMonth < 10) {
            s.append("0");
        }
        s.append(dayOfMonth);
        return s.toString();
    }

    @Override
    public JsonElement toJsonElement() {
        return new JsonPrimitive(toString());
    }

    /**
     * Obtains an instance of LocalDate from a text string such as 2007-12-03.
     *
     * <p>The following format is accepted in ASCII:
     *
     * <p>{Year}-{MonthOfYear}-{DayOfMonth}
     *
     * <p>The year has between 4 and 10 digits with values from MIN_YEAR to MAX_YEAR. If there are more than 4 digits then the year must be prefixed with the plus symbol. Negative years are allowed, but not negative zero.
     *
     * <p>The month-of-year has 2 digits with values from 1 to 12.
     *
     * <p>The day-of-month has 2 digits with values from 1 to 31 appropriate to the month.
     *
     * @param text the text to parse such as '2007-12-03', not null
     * @return the parsed local date, never null
     */
    public static LocalDate parse(String text) {
        int dash1 = text.indexOf('-', 1);
        if(dash1 == -1) {
            throw new CalendricalException("Cannot parse '" + text + "'");
        }
        int dash2 = text.indexOf('-', dash1+1);
        if(dash2 == -1) {
            throw new CalendricalException("Cannot parse '" + text + "'");
        }
        int year = Integer.parseInt(text.substring(0, dash1));
        int month = Integer.parseInt(text.substring(dash1+1, dash2));
        int day = Integer.parseInt(text.substring(dash2+1));

        return new LocalDate(year, month, day);
    }

    public boolean before(LocalDate toDate) {
        return compareTo(toDate) < 0;
    }

    public boolean after(LocalDate toDate) {
        return compareTo(toDate) > 0;
    }

    public int compareTo(LocalDate otherDate) {
        if(year != otherDate.year) {
            return year - otherDate.year;
        }
        if(monthOfYear != otherDate.monthOfYear) {
            return monthOfYear - otherDate.monthOfYear;
        }

        return dayOfMonth - otherDate.dayOfMonth;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + dayOfMonth;
        result = prime * result + monthOfYear;
        result = prime * result + year;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        LocalDate other = (LocalDate) obj;
        if (dayOfMonth != other.dayOfMonth)
            return false;
        if (monthOfYear != other.monthOfYear)
            return false;
        if (year != other.year)
            return false;
        return true;
    }


    @Override
    public FieldTypeClass getTypeClass() {
        return LocalDateType.TYPE_CLASS;
    }

    public static LocalDate valueOf(com.bedatadriven.rebar.time.calendar.LocalDate rebarDate) {
        return new LocalDate(rebarDate.getYear(), rebarDate.getMonthOfYear(), rebarDate.getDayOfMonth());
    }

    @Override
    public LocalDateInterval asInterval() {
        return new LocalDateInterval(this, this);
    }

    public LocalDate plusDays(int count) {
        Date date = atMidnightInMyTimezone();
        if (GWT.isClient()) {
            CalendarUtil.addDaysToDate(date, count);
            return new LocalDate(date);
        } else {
            Calendar c = Calendar.getInstance();
            c.setTime(date);
            c.add(Calendar.DAY_OF_YEAR, count);
            return new LocalDate(c.getTime());
        }
    }

    public LocalDate nextDay() {
        return plusDays(+1);
    }

    public LocalDate previousDay() {
        return plusDays(-1);
    }

}
